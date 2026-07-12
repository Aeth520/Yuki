package cn.aetheris.yuki.check.impl.movement.groundspoof;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.util.ghostblock.GhostBlockUtil;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.time.TimeUtils;
import cn.aetheris.yuki.util.update.PredictionComplete;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


@CheckData(name = "GroundSpoofC (Math)",
        configName = "GroundSpoofC",
        description = "Check for spoof ground",
        experimental = true,
        decay = 0.75,
        setback = 8)
public class GroundSpoofC extends Check implements PostPredictionCheck {

    private double buffer2;
    private double buffer3;
    private int serverAirTick;
    private int clientAirTick;
    private int clientGroundTick;
    private int serverGroundTick;

    private boolean mitigateSpoofGround;
    private boolean ignoreGhostBlock;

    private boolean mathGround;

    public GroundSpoofC(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked() || predictionComplete.getData().isTeleport()) {
            return;
        }

        if (isExempt(ExemptType.NEXT_ICE,
                ExemptType.NEXT_SLIME,
                ExemptType.NEXT_HONEY,
                ExemptType.ELYTRA_FLYING,
                ExemptType.LIQUID,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.RIPTIDE,
                ExemptType.TELEPORT,
                ExemptType.RESPAWN,
                ExemptType.GSIT_ACTION,
                ExemptType.BREWERRY_PUSH,
                ExemptType.JOIN,
                ExemptType.WEB,
                ExemptType.VEHICLE_SWITCH,
                ExemptType.VEHICLE)
                || player.getSetbackTeleportUtil().shouldBlockMovement() 
                || player.getUncertaintyHandler().isSteppingNearScaffolding
                || player.getUncertaintyHandler().isNearGlitchyBlock
                || player.exemptOnGround()
                || TimeUtils.elapsed(player.getCancelledBlockTicks(), 10L)
        ) {
            rewardBufferAndVL();
            return;
        }

        boolean lastMathGround = mathGround;
        mathGround = player.getY() % 0.015625 == 0.0; 
        boolean clientGround = player.isOnGround();
        boolean lastClientGround = player.isLastOnGround();
        boolean clientClaimsLastOnGround = player.isClientClaimsLastOnGround();

        double offset = predictionComplete.getOffset();

        final SimpleCollisionBox box = player.getBoundingBox().copy().expand(player.getMovementThreshold());
        final int blockX = NumberConversions.floor(box.minX);
        final int blockY = NumberConversions.floor(box.minY - 0.01);
        final int blockZ = NumberConversions.floor(box.minZ);
        final WrappedBlockState block = player.getCompensatedWorld().getBlock(
                new Vector3dm(blockX,
                        blockY,
                        blockZ
                ));

        
        
        
        
        boolean serverGround = block.getType().isAir(); 

        player.setServerOnGround(serverGround);

        serverGroundTick = serverGround ? serverGroundTick + 1 : 0;
        serverAirTick = !serverGround ? serverAirTick + 1 : 0;
        clientAirTick = !clientGround && clientClaimsLastOnGround ? clientAirTick + 1 : 0;
        clientGroundTick = clientGround && clientClaimsLastOnGround ? clientGroundTick + 1 : 0;

        
        if (clientGround != clientClaimsLastOnGround) {
            return;
        }

        
        if (GhostBlockUtil.isGhostBlock(player) && ignoreGhostBlock) {
            return;
        }

        final GroundSpoofA check = player.getCheckManager().getCheck(GroundSpoofA.class);
        if (check == null) {
            return;
        }
        if (serverAirTick >= 4 && shouldModifyPackets() && mitigateSpoofGround && (clientGround || mathGround)) {
            player.getSetbackTeleportUtil().executeViolationSetback();
            player.setFallDistance(0);
            player.mitigateDamage();
            LogUtils.mitigate("&b" + player.getName() + "&7 has been reset ground stats for &bSpoofGround &7(" + clientGround + " | " + mathGround + ")");
            check.flipPlayerGroundStatus = true;
        }

        
        if (offset > 0.058) {
            return;
        }

        
        if (!clientGround && !lastClientGround && mathGround && serverGroundTick >= 2) {
            if (buffer++ > 3) {
                if (flagAndAlertWithSetback("(TowTick)\nst= " + serverAirTick + "\nct= " + clientGroundTick)) {
                    player.mitigateDamage();
                    player.setFallDistance(0);
                    check.flipPlayerGroundStatus = true;
                }
            }
        } else {
            rewardBufferAndVL();
        }

        
        if (clientGroundTick >= 10 && !mathGround && !lastMathGround && serverAirTick >= 10) {
            if (flagAndAlertWithSetback("(DSync)\nct= " + clientGroundTick + "\nst= " + serverAirTick)) {
                player.mitigateDamage();
                player.setFallDistance(0);
                check.flipPlayerGroundStatus = true;
            }
        }

        
        if (clientGround
                && !mathGround
                && !lastMathGround
                && serverAirTick > 5) {
            if (buffer3++ > 5) {
                if (flagAndAlertWithSetback("(WithOutMath)\nst= " + serverAirTick + "\nct= " + clientGroundTick + "\nmg= " + mathGround + "\nlmg= " + lastMathGround)) {
                    player.mitigateDamage();
                    player.setFallDistance(0);
                }
            }
            check.flipPlayerGroundStatus = true;
        } else {
            buffer3 = Math.min(buffer3 - getDecay(), 1.0);
        }


        
        if (!clientClaimsLastOnGround && mathGround && serverGroundTick >= 1) {
            if (buffer2++ > 3) {
                if (flagAndAlertWithSetback("(Living)\nst= " + serverGroundTick)) {
                    player.mitigateDamage();
                    player.setFallDistance(0);
                }
            }
            check.flipPlayerGroundStatus = true;
        } else {
            buffer2 = Math.min(buffer2 - getDecay(), 1.0);
        }
    }

    @Override
    public void reload() {
        super.reload();
        mitigateSpoofGround = getConfig().getBooleanElse(getConfigName() + ".mitigate-ground", false);
        ignoreGhostBlock = getConfig().getBooleanElse(getConfigName() + ".ignore-ghost", false);
    }
}
