package cn.aetheris.yuki.check.impl.combat.velocity;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "VelocityE (Spoof)",
        alternativeName = "VelocityE",
        configName = "VelocityE",
        type = CheckType.VELOCITY,
        setback = 8,
        decay = 0.65,
        experimental = true)
public class VelocityE extends Check implements PostPredictionCheck {


    public VelocityE(@NotNull PlayerData player) {
        super(player);
    }


    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) {
            return;
        }

        if (predictionComplete.getData().isTeleport()) {
            return;
        }

        if (!Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
            return;
        }

        if (isExempt(ExemptType.VOID,
                ExemptType.VEHICLE_SWITCH,
                ExemptType.VEHICLE,
                ExemptType.FLYING,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.RIPTIDE,
                ExemptType.RESPAWN,
                ExemptType.TELEPORT,
                ExemptType.ELYTRA_FLYING,
                ExemptType.SERVER_SENT_ROTATE)
                || player.getPredictedVelocity().isExplosion()
                || player.getPredictedVelocity().isKnockback()
                || player.getSetbackTeleportUtil().shouldBlockMovement()) {
            return;
        }

        
        final boolean onGround = player.isOnGround();
        final boolean lastOnGround = player.isLastOnGround();
        final double deltaY = player.getDeltaY();
        final boolean invalid = deltaY <= 1e-3;
        final boolean isVelocitySetBack = player.getLikelyKB() != null && player.getLikelyKB().isSetback;
        final boolean touchingWater = player.isWasTouchingWater();

        if (isVelocitySetBack) {
            return;
        }

        if (player.getPredictedVelocity().isKnockback()) {
            if (invalid
                    && touchingWater
                    && !player.isSwimming()) {
                if (flagAndAlertWithSetback("(TypeA)")) {
                    player.mitigateDamage();
                }
            }
            if (onGround
                    && lastOnGround
                    && player.hasAttackedSince(5000)
                    && !touchingWater
                    && isUnderAir()
                    && deltaY > 1E-10) {
                if (flagAndAlertWithSetback("(TypeB)")) {
                    player.mitigateDamage();
                }
            }
        }
    }

    public boolean isUnderAir() {
        final double margin = 0.1;
        final SimpleCollisionBox boundingBox = player.getBoundingBox().copy();
        final double minX = boundingBox.getMinX();
        final double minZ = boundingBox.getMinZ();
        final double maxY = boundingBox.getMaxY();

        final double expandedMaxY = maxY + margin;

        boolean isUnderAir = true;
        for (double yOffset = expandedMaxY; yOffset < expandedMaxY + 1.0; yOffset += 0.25) {
            WrappedBlockState blockAbove = player.getCompensatedWorld()
                    .getBlock(NumberConversions.floor(minX), NumberConversions.floor(yOffset), NumberConversions.floor(minZ));
            if (!blockAbove.getType().isAir()) {
                isUnderAir = false;
                break;
            }
        }

        return isUnderAir || player.getY() % 1 / 8 == 0;
    }

}
