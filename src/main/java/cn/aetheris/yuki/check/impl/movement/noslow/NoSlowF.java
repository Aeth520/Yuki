package cn.aetheris.yuki.check.impl.movement.noslow;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;

import java.util.Collections;
import java.util.OptionalInt;

@CheckData(name = "NoSlowF", type = CheckType.NOSLOW, configName = "NoSlowF", description = "Extra noslow exploit fixer", decay = 0.5)
public final class NoSlowF extends Check implements PostPredictionCheck {

    int velocityTick = 0;
    double desyncedTick = 0;
    int webTick;
    int startUsingTick = 0;
    double airTick = 0;
    int speedSpikeTick = 0;

    long lastFlag1;
    long lastFlag2;

    public NoSlowF(PlayerData player) {
        super(player);
    }

    public double tryPlayerMove(double strafe, double forward, double friction) {
        double f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4F) {
            f = Math.sqrt(f);
            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe *= f;
            forward *= f;
            return Math.sqrt(strafe * strafe + forward * forward);
        }
        return 0;
    }

    private float applyPotionEffects(PlayerData profile) {
        float speedBoost = 1;

        OptionalInt speed = profile.getCompensatedEntities().getPotionLevelForPlayer(PotionTypes.SPEED);
        if (speed.isPresent()) {
            speedBoost *= 1.0f + 0.2f * (speed.getAsInt() + 1);
        }

        OptionalInt slowness = profile.getCompensatedEntities().getPotionLevelForPlayer(PotionTypes.SLOWNESS);
        if (slowness.isPresent()) {
            speedBoost *= 1.0f - 0.15f * (slowness.getAsInt() + 1);
        }

        return speedBoost;
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(event);
            if (velocity.getEntityId() != player.getEntityID()) return;

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                velocityTick = 0;
            });
        }
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) return;

        velocityTick++;
        if (isExempt(
                ExemptType.TELEPORT,
                ExemptType.NEXT_SLIME,
                ExemptType.BREWERRY_PUSH,
                ExemptType.NEXT_HONEY,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.LIQUID,
                ExemptType.SWIMMING,
                ExemptType.SLIGHTLY_TOUCHING_LIQUID,
                ExemptType.FLYING,
                ExemptType.WAS_SWIMMING,
                ExemptType.SEEM_WATER,
                ExemptType.VEHICLE_DIED,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE,
                ExemptType.MOVE_LAGGING
        )) {
            return;
        }


        
        if (Collections.max(player.uncertaintyHandler.pistonX) != 0
                || Collections.max(player.uncertaintyHandler.pistonY) != 0
                || Collections.max(player.uncertaintyHandler.pistonZ) != 0) {
            return;
        }

        if (player.predictedVelocity.isKnockback()
                || player.predictedVelocity.isExplosion()) {
            return;
        }

        if (!player.isMoving()) {
            return;
        }

        if (!player.onGround) {
            airTick++;
        } else {
            airTick = 0;
        }
        
        if (player.packetStateData.isSlowedByUsingItem()) {
            startUsingTick++;
        } else {
            startUsingTick = 0;
        }


        double moveSpeed;

        
        boolean sprintStatus = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19_4);

        
        if (player.lastOnGround) {
            moveSpeed = player.getSpeed() * (0.16277136F / (player.getFriction() * player.getFriction() * player.getFriction()));
        } else {
            boolean sprinting = sprintStatus ? player.isSprinting() : player.isLastSprinting();
            moveSpeed = sprinting ? 0.026 : 0.02;
        }

        
        moveSpeed *= applyPotionEffects(player);
        if (player.isSprinting()) {
            moveSpeed *= 1.3;
        }

        
        double friction = player.getFriction();
        if (player.inWeb) {
            friction *= 0.25;
        }

        double actuallyMoveSpeed = player.getDeltaXZ();

        double move = player.packetStateData.isSlowedByUsingItem() ? 0.2 : 1;
        double expectedMoveSpeed = player.getLastDeltaXZ() * friction + tryPlayerMove(move, move, moveSpeed);

        if (player.isLastOnGround() && !player.onGround)
            expectedMoveSpeed += 0.2; 

        
        double moreSpeed = Math.max(actuallyMoveSpeed - expectedMoveSpeed, 0);

        
        webTick = player.inWeb ? webTick + 1 : 0;
        if (player.inWeb) {
            final double max = getMax();
            
            if (((moreSpeed > (webTick <= 10 ? 0.2 : 0.035) && player.getDeltaY() < 0.00001) || Math.abs(actuallyMoveSpeed - expectedMoveSpeed) < max) && velocityTick >= 10) {
                if (time() - lastFlag1 < 200L) {
                    return;
                }
                final NoSlowG check = player.getCheckManager().getCheck(NoSlowG.class);
                if (check == null) {
                    return;
                }
                if (!check.shouldModifyPackets()) {
                    return;
                }
                if (desyncedTick++ > 5) {
                    if (check.flagAndAlert(String.format("(MathLine)\ne= %.3f\na= %.3f\ndiff= %.3f\nj= %s\nms= %.3f\ndy= %.3f\ndxz= %.3f\nlimit= %.5f", expectedMoveSpeed, actuallyMoveSpeed, Math.abs(actuallyMoveSpeed - expectedMoveSpeed), player.getPredictedVelocity().isJump(), moreSpeed, player.getDeltaY(), player.getDeltaXZ(), max))) {
                        player.getSetbackTeleportUtil().executeViolationSetback();
                    }
                    lastFlag1 = time();
                }
            } else {
                desyncedTick = Math.max(desyncedTick - 0.85, 0);
            }
        } else if (startUsingTick != 0) {
            if (moreSpeed > 0.075 && velocityTick > 10) {
                speedSpikeTick++;
                if (time() - lastFlag2 < 300L) {
                    return;
                }
                if (speedSpikeTick >= 3) {
                    desyncedTick += 2.0;
                } else if (speedSpikeTick >= 2) {
                    desyncedTick += 1.0;
                } else {
                    desyncedTick += 0.5;
                }
                if (desyncedTick > 3) {
                    if (flagAndAlert(String.format("(Limitation)\ne= %.5f\na= %.5f", expectedMoveSpeed, actuallyMoveSpeed))) {
                        resetPlayerUseItem(player.getBukkitPlayer());
                    }
                    lastFlag2 = time();
                    speedSpikeTick = 0;
                    desyncedTick = 0;
                }
            } else {
                desyncedTick = Math.max(desyncedTick - 1.0, 0);
                speedSpikeTick = Math.max(0, speedSpikeTick - 1);
            }
        }
    }

    private double getMax() {
        double max = 0.0325;
        if (player.getDeltaXZ() > 0.00001 && webTick <= 10) {
            max = 0.002;
        }
        if (player.getDeltaY() > 0.00001 && player.getY() > player.getLastY()) {
            max = 0.0285;
        } else if (player.getY() < player.getLastY() || player.isLastJumping()) {
            if (webTick <= 6) {
                max = 0.0005;
            } else if (player.getDeltaXZ() > 0.00001) {
                max = 0.013;
            } else {
                max = 0.019;
            }
        }
        return max;
    }
}