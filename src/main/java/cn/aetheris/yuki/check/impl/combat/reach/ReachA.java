package cn.aetheris.yuki.check.impl.combat.reach;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.listener.packets.dragon.PacketEntityEnderDragonPart;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.protocol.nms.ReachUtils;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@CheckData(name = "ReachA (Distance)", type = CheckType.REACH, configName = "ReachA", description = "The player is trying to hit the entity outside of the hitBox", setback = 8)
public final class ReachA extends Check implements PacketCheck {

    private static final CheckResult NONE = new CheckResult(ResultType.NONE, "");
    private final Int2ObjectMap<Vector3d> playerAttackQueue;
    @Getter
    public double reachThreshold;
    private List<String> blacklisted = new ArrayList<>();
    private boolean ignoreNonPlayerTargets;
    private boolean cancelImpossibleHits;
    private boolean tolerantBoxMoving;
    private double flagBufferMax;
    private double flagBufferIncrement;
    @Getter
    @Setter
    private double flagBufferDecay;
    private boolean ignoreEnderDragon;

    public ReachA(PlayerData player) {
        super(player);
        playerAttackQueue = new Int2ObjectOpenHashMap<>();
    }


    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (!player.bypass && event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity action = new WrapperPlayClientInteractEntity(event);

            if (isExempt(ExemptType.INVALID_GAMEMODE, ExemptType.GSIT_ACTION)) {
                return;
            }

            if (player.getSetbackTeleportUtil().shouldBlockMovement() && cancelImpossibleHits) {
                event.setCancelled(true);
                player.onPacketCancel();
                return;
            }

            PacketEntity entity = player.compensatedEntities.entityMap.get(action.getEntityId());

            if (entity == null || entity instanceof PacketEntityEnderDragonPart) {
                if (shouldModifyPackets() && cancelImpossibleHits && player.compensatedEntities.serverPositionsMap.containsKey(action.getEntityId())) {
                    if (alert("entity is " + ((entity == null) ? "Nulled" : "EnderDragonPart"))) {
                        event.setCancelled(true);
                        player.mitigateDamage();
                        player.onPacketCancel();
                    }
                }
                return;
            }


            if (ignoreNonPlayerTargets && !entity.getType().equals(EntityTypes.PLAYER)) {
                return;
            }

            if (ignoreEnderDragon && entity.getType().equals(EntityTypes.ENDER_DRAGON)) {
                return;
            }

            if (entity.isDead) return;

            if (entity.getType() == EntityTypes.ARMOR_STAND && player.getClientVersion().isOlderThan(ClientVersion.V_1_8))
                return;

            if (player.inVehicle()) return;

            if (entity.riding != null) return;

            boolean tooManyAttacks = playerAttackQueue.size() > 10;
            if (!tooManyAttacks) {
                playerAttackQueue.put(action.getEntityId(), new Vector3d(player.x, player.y, player.z)); 
            }

            boolean knownInvalid = isKnownInvalid(entity);

            if ((shouldModifyPackets() && cancelImpossibleHits && knownInvalid) || tooManyAttacks) {
                event.setCancelled(true);
                player.mitigateDamage();
                player.onPacketCancel();
            }
        }

        
        if (isUpdate(event.getPacketType())) {
            tickBetterReachCheckWithAngle();
        }
    }

    
    private boolean isKnownInvalid(PacketEntity reachEntity) {
        if ((blacklisted.contains(reachEntity.getType().getName().toString()) || !reachEntity.isLivingEntity()) && reachEntity.getType() != EntityTypes.END_CRYSTAL)
            return false;
        if (isExempt(ExemptType.MYTHIC_ITEM_ATTACK)) return false;
        if (player.gamemode == GameMode.CREATIVE || player.gamemode == GameMode.SPECTATOR) return false;
        if (player.inVehicle()) return false;

        if (buffer > 0) {
            return checkReach(reachEntity, new Vector3d(player.x, player.y, player.z), true) != NONE;
        } else {
            SimpleCollisionBox targetBox = reachEntity.getPossibleCollisionBoxes();
            if (reachEntity.getType() == EntityTypes.END_CRYSTAL) {
                targetBox = new SimpleCollisionBox(reachEntity.trackedServerPosition.getPos().subtract(1, 0, 1), reachEntity.trackedServerPosition.getPos().add(1, 2.5, 1));
            }
            return ReachUtils.getMinReachToBox(player, targetBox) > player.compensatedEntities.getSelf().getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        }
    }


    private void tickBetterReachCheckWithAngle() {
        for (Int2ObjectMap.Entry<Vector3d> attack : playerAttackQueue.int2ObjectEntrySet()) {
            final PacketEntity reachEntity = player.compensatedEntities.entityMap.get(attack.getIntKey());
            if (reachEntity == null) continue;

            final CheckResult result = checkReach(reachEntity, attack.getValue(), false);
            switch (result.type()) {
                case REACH_PLAYER -> {
                    if (flagAndAlert(result.verbose())) {
                        player.mitigateDamage();
                    }
                }
                case REACH_ENTITY -> {
                    if (player.checkManager.getCheck(ReachB.class).flagAndAlert(result.verbose())) {
                        player.mitigateDamage();
                    }
                }
                case HITBOX_PLAYER -> {
                    if (player.checkManager.getCheck(ReachC.class).flagAndAlert(result.verbose())) {
                        player.mitigateDamage();
                    }
                }
                case HITBOX_ENTITY -> {
                    if (player.checkManager.getCheck(ReachD.class).flagAndAlert(result.verbose())) {
                        player.mitigateDamage();
                    }
                }
            }
        }
        playerAttackQueue.clear();
    }

    @NotNull
    private CheckResult checkReach(PacketEntity reachEntity, Vector3d from, boolean isPrediction) {
        SimpleCollisionBox targetBox = reachEntity.getPossibleCollisionBoxes();

        if (reachEntity.getType() == EntityTypes.END_CRYSTAL) { 
            targetBox = new SimpleCollisionBox(reachEntity.trackedServerPosition.getPos().subtract(1, 0, 1), reachEntity.trackedServerPosition.getPos().add(1, 2, 1));
        }

        
        
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_9)) {
            targetBox.expand(0.1f);
        } else if (player.getMoveTick() > 2 && tolerantBoxMoving) {
            targetBox.expand(0.18f);
        }

        targetBox.expand(reachThreshold);

        
        
        
        
        
        if (!player.packetStateData.didLastLastMovementIncludePosition || player.canSkipTicks())
            targetBox.expand(player.getMovementThreshold());

        double minDistance = Double.MAX_VALUE;

        
        List<Vector3dm> possibleLookDirs = new ArrayList<>(Collections.singletonList(ReachUtils.getLook(player, player.yaw, player.pitch)));
        
        if (!isPrediction) {
            possibleLookDirs.add(ReachUtils.getLook(player, player.lastYaw, player.pitch));

            
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                possibleLookDirs.add(ReachUtils.getLook(player, player.lastYaw, player.lastPitch));
            }

            
            if (player.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
                possibleLookDirs = Collections.singletonList(ReachUtils.getLook(player, player.yaw, player.pitch));
            }
        }

        final double maxReach = player.compensatedEntities.getSelf().getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        
        final double distance = maxReach + 3;
        final double[] possibleEyeHeights = player.getPossibleEyeHeights();
        final Vector3dm eyePos = new Vector3dm(from.getX(), 0, from.getZ());
        for (Vector3dm lookVec : possibleLookDirs) {
            for (double eye : possibleEyeHeights) {
                eyePos.setY(from.getY() + eye);
                Vector3dm endReachPos = eyePos.clone().add(new Vector3dm(lookVec.getX() * distance, lookVec.getY() * distance, lookVec.getZ() * distance));

                Vector3dm intercept = ReachUtils.calculateIntercept(targetBox, eyePos, endReachPos).first();

                if (ReachUtils.isVecInside(targetBox, eyePos)) {
                    minDistance = 0;
                    break;
                }

                if (intercept != null) {
                    minDistance = Math.min(eyePos.distance(intercept), minDistance);
                }
            }
        }

        
        if ((reachEntity.isLivingEntity()) || reachEntity.getType() == EntityTypes.END_CRYSTAL) {
            if (minDistance == Double.MAX_VALUE) {
                if (reachEntity.getType() != EntityTypes.PLAYER && blacklisted.contains(reachEntity.getType().getName().toString())) {
                    buffer = Math.min(buffer + flagBufferIncrement, flagBufferMax);
                    return new CheckResult(ResultType.HITBOX_ENTITY, "(Entity)\nentity= " + reachEntity.getType().getName());
                } else {
                    buffer = Math.min(buffer + flagBufferIncrement, flagBufferMax);
                    return new CheckResult(ResultType.HITBOX_PLAYER, "(Player)");
                }
            } else if (minDistance > maxReach) {
                if (reachEntity.getType() != EntityTypes.PLAYER && !blacklisted.contains(reachEntity.getType().getName().toString())) {
                    buffer = Math.min(buffer + flagBufferIncrement, flagBufferMax);
                    return new CheckResult(ResultType.REACH_ENTITY, String.format("(Entity)\nreach= %.5f", minDistance) + " blocks\ne= " + reachEntity.getType().getName());
                } else {
                    buffer = Math.min(buffer + flagBufferIncrement, flagBufferMax);
                    return new CheckResult(ResultType.REACH_PLAYER, String.format("(Player)\nreach= %.5f", minDistance) + " blocks");
                }
            } else {
                rewardBufferAndVL();
            }
        }

        return NONE;
    }

    
    @Override
    public void reload() {
        tolerantBoxMoving = getConfig().getBooleanElse("Reach.tolerant-box-moving", false);
        ignoreEnderDragon = getConfig().getBooleanElse("Reach.ignore-ender-dragon", false);
        ignoreNonPlayerTargets = getConfig().getBooleanElse("Reach.ignore-non-player-targets", false);
        cancelImpossibleHits = getConfig().getBooleanElse("Reach.block-impossible-hits", true);
        reachThreshold = getConfig().getDoubleElse("Reach.threshold", 0.0005);
        flagBufferMax = getConfig().getDoubleElse("Reach.buffer", 1);
        flagBufferIncrement = getConfig().getDoubleElse("Reach.increment", 1);
        flagBufferDecay = getConfig().getDoubleElse("Reach.decay", 0.25);
        blacklisted = getConfig().getList("Reach.entity-blacklist");
        if (flagBufferMax == -1) {
            flagBufferMax = Double.MAX_VALUE;
        }
    }


    private enum ResultType {
        REACH_ENTITY, REACH_PLAYER, HITBOX_ENTITY, HITBOX_PLAYER, NONE
    }

    private static final class CheckResult {
        private final ResultType type;
        private final String verbose;

        private CheckResult(ResultType type, String verbose) {
            this.type = type;
            this.verbose = verbose;
        }

        public ResultType type() {
            return type;
        }

        public String verbose() {
            return verbose;
        }
    }
}
