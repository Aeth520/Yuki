package cn.aetheris.yuki.check.impl.movement.movementvalidation;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.api.events.PredictionEvent;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.movement.groundspoof.GroundSpoofA;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.ghostblock.GhostBlockUtil;
import cn.aetheris.yuki.util.location.PacketLocation;
import cn.aetheris.yuki.util.time.TimeUtils;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@CheckData(
        name = "MovementValidation",
        configName = "MovementValidation",
        description = "Move not according to vanilla",
        type = CheckType.MOVEMENT_VALIDATION,
        decay = 3.0
)
public final class MovementValidation extends Check implements PostPredictionCheck {

    private static final AtomicInteger FLAGS = new AtomicInteger(0);
    private final Set<String> addedTags = new HashSet<>();  
    public boolean refreshBlocks;
    @Getter
    private double offset;
    private double setbackDecayMultiplier;
    private double threshold;
    private double immediateSetbackThreshold;
    private double maxAdvantage;
    private double maxCeiling;
    private int joinWaitTime;
    private boolean silent;
    private boolean ignoreGhostBlock;
    private double advantageGained;
    private long lastReduce;

    public MovementValidation(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked() || !shouldModifyPackets()) {
            return;
        }

        if (!TimeUtils.hasExpired(player.joinTime, joinWaitTime)) {
            return;
        }

        if (player.isGliding && PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.ignore-elytra", false)) {
            rewardVL();
            return;
        }

        if (isExempt(ExemptType.VEHICLE_SWITCH,
                ExemptType.MYTHIC_MOB,
                ExemptType.GSIT_ACTION,
                ExemptType.BREWERRY_PUSH,
                ExemptType.WEAPON_SHOOT)) {
            rewardVL();
            return;
        }

        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_8)
                && isExempt(ExemptType.INVALID_GAMEMODE)) {
            return;
        }

        
        if ((player.isWorldChange() && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20))
                || player.getSetbackTeleportUtil().insideUnloadedChunk()) {
            rewardVL();
            return;
        }

        final double currentOffset = predictionComplete.getOffset();
        this.offset = currentOffset;

        PredictionEvent predictionEvent = new PredictionEvent(getPlayer(), this, currentOffset);
        Bukkit.getPluginManager().callEvent(predictionEvent);
        if (predictionEvent.isCancelled()) {
            return;
        }

        if (!ignoreGhostBlock && GhostBlockUtil.isGhostBlock(player)) {
            rewardVL();
            return;
        }

        
        final PacketLocation loc = player.getLocationData().clone().add(0, 0.65, 0);
        final boolean serverAboveAir = player.getCompensatedWorld().getBlock(loc.toVector()).getType().isAir();
        if (player.cancelledBlockTicks < 6 && player.isOnGround() != player.isClientClaimsLastOnGround() && player.predictedVelocity.isJump() && serverAboveAir
                && player.getDeltaXZ() < 3.85F) {
            player.onGround = false;  
            player.clientClaimsLastOnGround = false;
            return;
        }

        if (player.bukkitPlayer != null && player.bukkitPlayer.getAllowFlight() &&
                !PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.flying-check.bukkit", false)) {
            return;
        }

        addStatsTag();

        boolean isImmediateSetback = currentOffset >= immediateSetbackThreshold;
        boolean isThresholdExceeded = currentOffset >= threshold;
        if (isThresholdExceeded || isImmediateSetback) {
            lastReduce = time();
            if (!flag()) {
                return;
            }

            advantageGained += currentOffset;
            boolean isSetback = advantageGained >= maxAdvantage || isImmediateSetback;
            giveOffsetLenienceNextTick(currentOffset);
            if (isSetback) {
                player.getSetbackTeleportUtil().executeViolationSetback();
            }

            synchronized (FLAGS) {
                int flagId = (FLAGS.getAndIncrement() & 255) + 1;
                String humanFormattedOffset = formatOffset(currentOffset);
                alert("offset= " + humanFormattedOffset +
                        "\nax= " + String.format("%.3f", player.actualMovement.getX()) +
                        "\nay= " + String.format("%.3f", player.actualMovement.getY()) +
                        "\naz= " + String.format("%.3f", player.actualMovement.getZ()) +
                        "\npx= " + String.format("%.3f", player.getPredictedVelocity().vector.getX()) +
                        "\npy= " + String.format("%.3f", player.getPredictedVelocity().vector.getY()) +
                        "\npz= " + String.format("%.3f", player.getPredictedVelocity().vector.getZ()) +
                        "\ng= " + player.clientClaimsLastOnGround + " | " + player.isServerOnGround() +
                        "\ncg= " + player.isOnGround() + " | " + player.isLastOnGround() +
                        "\nm= " + isSetback +
                        "\nfm= " + !player.isVanillaMath() +
                        "\nd= " + player.worldChange +
                        "\nt= " + (silent ? -1 : String.format("%.3f", immediateSetbackThreshold).substring(0, 5)) +
                        "\ntags= " + String.join(", ", addedTags));  
                resetTags();
                predictionComplete.setIdentifier(flagId);

                final String strictness = getConfig().getStringElse("MovementValidation.strictness", "balanced");
                final boolean special = getConfig().getBooleanElse("MovementValidation.special", false);
                updateThresholds(strictness, special);

                if (!silent && currentOffset > 0.045 && player.isSneaking() && !strictness.contains("chaos") && !strictness.contains("lenient")) {
                    player.resyncPose();
                }
                if (!silent && currentOffset > 0.1535 && player.packetStateData.isSlowedByUsingItem() && !strictness.contains("chaos") && !strictness.contains("lenient")) {
                    resetPlayerUseItem(player.bukkitPlayer);
                }
                if (!silent && currentOffset > 0.0845 && !player.isClientClaimsLastOnGround() && !player.isOnGround() && !strictness.contains("chaos") && !strictness.contains("lenient")) {
                    final GroundSpoofA groundSpoof = player.getCheckManager().getCheck(GroundSpoofA.class);
                    groundSpoof.flipPlayerGroundStatus = true;
                    player.getSetbackTeleportUtil().executeTeleport(player.getLocationData().clone().add(0, 0.5, 0));
                }
                if (!silent && currentOffset > 0.35 && isExempt(ExemptType.VOID) && !strictness.contains("chaos") && !strictness.contains("lenient")) {
                    player.getSetbackTeleportUtil().executeViolationSetback();
                }
                if (!silent && currentOffset > 0.0295 && player.hasAttackedSince(800L) && !strictness.contains("chaos") && !strictness.contains("lenient")) {
                    player.mitigateDamage();
                }
            }
            advantageGained = Math.min(advantageGained, maxCeiling);
        } else {
            advantageGained *= setbackDecayMultiplier;
            if (time() - lastReduce < 500L) {
                return;
            }
            rewardVL();
            lastReduce = time();
        }

        removeOffsetLenience();
        resetTags();
    }

    private void addStatsTag() {
        
        if (player.clientClaimsLastOnGround || player.onGround) {
            addedTags.add("Ground");
        }

        if (player.wasTouchingWater) {
            addedTags.add("Water");
        }

        if (player.wasTouchingLava) {
            addedTags.add("Lava");
        }

        if (player.isClimbing) {
            addedTags.add("Climbing");
        }

        if (player.packetStateData.tryingToRiptide) {
            addedTags.add("Riptide");
        }

        if (player.isSwimming) {
            addedTags.add("Swimming");
        }

        if (player.uncertaintyHandler.isSteppingOnSlime) {
            addedTags.add("onSlime");
        }

        if (player.uncertaintyHandler.isSteppingOnIce) {
            addedTags.add("onIce");
        }

        if (player.uncertaintyHandler.isSteppingOnHoney) {
            addedTags.add("onHoney");
        }

        if (player.uncertaintyHandler.isSteppingOnFence) {
            addedTags.add("onFence");
        }

        if (player.uncertaintyHandler.isSteppingOnCarpet) {
            addedTags.add("onCarpet");
        }

        if (player.isSprinting) {
            addedTags.add("Sprinting");
        }
        if (player.isSneaking) {
            addedTags.add("Sneaking");
        }

        if (player.isInBed || player.lastInBed) {
            addedTags.add("Bed");
        }

        if (player.predictedVelocity.isKnockback()) {
            addedTags.add("Velocity");
        }

        if (player.predictedVelocity.isExplosion()) {
            addedTags.add("Explosion");
        }

        if (player.predictedVelocity.isJump()) {
            addedTags.add("Jump");
        }

        if (player.predictedVelocity.isTrident()) {
            addedTags.add("Trident");
        }

        if (player.predictedVelocity.isSwimHop()) {
            addedTags.add("SwimHop");
        }

        if (player.predictedVelocity.isAttackSlow()) {
            addedTags.add("AttackSlow");
        }

        if (player.getSetbackTeleportUtil().insideUnloadedChunk()) {
            addedTags.add("InsideUnloadedChunk");
        }

        if (GhostBlockUtil.isGhostBlock(player) && !ignoreGhostBlock) {
            addedTags.add("GhostBlock");
        }
        if (player.packetStateData.isSlowedByUsingItem()) {
            addedTags.add("SlowedByUsingItem");
        }

        if (player.worldChange) {
            addedTags.add("WorldChangeDsync");
        }

        if (player.inVehicle()) {
            addedTags.add("Riding");
        }

        if (!player.onGround && !player.clientClaimsLastOnGround) {
            addedTags.add("inAir");
        }

        if (player.hasAttackedSince(300L)) {
            addedTags.add("Attacking");
        }

        if (player.wasEyeInWater) {
            addedTags.add("EyeInWater");
        }

        if (isExempt(ExemptType.VOID)) {
            addedTags.add("Void");
        }

        if (isExempt(ExemptType.INTERACT)) {
            addedTags.add("Interact");
        }

        if (player.likelyKB != null) {
            addedTags.add("isKockBack");
        }
    }

    private void resetTags() {
        addedTags.clear();
    }

    private void updateThresholds(String strictness, boolean special) {
        immediateSetbackThreshold = silent ? Double.MAX_VALUE : special ? 1.05 : switch (strictness) {
            case "strict" -> 0.055;
            case "lenient" -> 0.155;
            case "chaos" -> 0.553;
            default -> 0.06;
        };
        maxAdvantage = silent ? Double.MAX_VALUE : special ? Double.MAX_VALUE : switch (strictness) {
            case "strict" -> 1.35;
            case "lenient" -> 7.0;
            case "chaos" -> 8.6;
            default -> 4.25;
        };
        maxCeiling = special ? 8.0 : switch (strictness) {
            case "strict" -> 2.5;
            case "lenient" -> 6.75;
            case "chaos" -> 10.5;
            default -> 4.0;
        };
        threshold = special ? 1.001 : switch (strictness) {
            case "strict" -> 0.012;
            case "lenient" -> 0.0395;
            case "chaos" -> 0.355;
            default -> 0.08;
        };
    }

    private void giveOffsetLenienceNextTick(double offset) {
        double minimizedOffset = Math.min(offset, 1);
        player.uncertaintyHandler.lastHorizontalOffset = minimizedOffset;
        player.uncertaintyHandler.lastVerticalOffset = minimizedOffset;
    }

    private void removeOffsetLenience() {
        player.uncertaintyHandler.lastHorizontalOffset = 0;
        player.uncertaintyHandler.lastVerticalOffset = 0;
    }

    @Override
    public void reload() {
        super.reload();
        final String strictness = getConfig().getStringElse("MovementValidation.strictness", "balanced");
        final boolean special = getConfig().getBooleanElse("MovementValidation.special", false);
        silent = getConfig().getBooleanElse("MovementValidation.silent", false);
        setbackDecayMultiplier = special ? 0.99999 : switch (strictness) {
            case "strict" -> 0.9985;
            case "lenient" -> 0.9995;
            case "chaos" -> 0.99995;
            default -> 0.999;
        };
        threshold = special ? 1.001 : switch (strictness) {
            case "strict" -> 0.012;
            case "lenient" -> 0.0395;
            case "chaos" -> 0.355;
            default -> 0.08;
        };
        immediateSetbackThreshold = silent ? Double.MAX_VALUE : special ? 1.05 : switch (strictness) {
            case "strict" -> 0.055;
            case "lenient" -> 0.155;
            case "chaos" -> 0.553;
            default -> 0.06;
        };
        maxAdvantage = silent ? Double.MAX_VALUE : special ? Double.MAX_VALUE : switch (strictness) {
            case "strict" -> 1.35;
            case "lenient" -> 7.0;
            case "chaos" -> 8.6;
            default -> 4.25;
        };
        maxCeiling = special ? 8.0 : switch (strictness) {
            case "strict" -> 2.5;
            case "lenient" -> 6.75;
            case "chaos" -> 10.5;
            default -> 4.0;
        };
        joinWaitTime = getConfig().getIntElse("MovementValidation.join-wait-time", 5);
        refreshBlocks = getConfig().getBooleanElse("MovementValidation.refresh-blocks", false);
        ignoreGhostBlock = getConfig().getBooleanElse("MovementValidation.ignore-ghost-block", false);
    }
}
