package cn.aetheris.yuki.check.impl.combat.velocity;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.math.OptifineFastMath;
import cn.aetheris.yuki.math.VanillaMath;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

@CheckData(name = "VelocityD (Hor)", configName = "VelocityD", decay = 0.85, experimental = true)
public class VelocityD extends Check implements PostPredictionCheck {

    public static final List<float[]> KEY_COMBOS = Collections.synchronizedList(
            Arrays.asList(
                    new float[]{1.0F, -1.0F},
                    new float[]{1.0F, 0.0F},
                    new float[]{1.0F, 1.0F},
                    new float[]{0.0F, -1.0F},
                    new float[]{0.0F, 0.0F},
                    new float[]{0.0F, 1.0F},
                    new float[]{-1.0F, -1.0F},
                    new float[]{-1.0F, 0.0F},
                    new float[]{-1.0F, 1.0F}
            )
    );
    private static final boolean[] BOOL_OPTIONS = {true, false};
    private boolean attack;
    private double kbZ = 0;
    private double kbX = 0;
    private int ticks;

    private boolean allowJumpReset;
    private double minVelocity;

    public VelocityD(@NotNull PlayerData player) {
        super(player);
    }

    public static float sin(float a, boolean f) {
        if (f) {
            return OptifineFastMath.sin(a);
        }
        return VanillaMath.sin(a);
    }

    public static float cos(float a, boolean f) {
        if (f) {
            return OptifineFastMath.cos(a);
        }
        return VanillaMath.cos(a);
    }

    public float getAttributeSpeed(final PlayerData data) {
        double attributeSpeed = data.getCompensatedEntities().getSelf().getAttributeValue(Attributes.MOVEMENT_SPEED);
        OptionalInt speed = data.getCompensatedEntities().getPotionLevelForPlayer(PotionTypes.SPEED);
        if (speed.isPresent()) {
            attributeSpeed *= 1.0f + 0.2f * (speed.getAsInt() + 1);
        }

        OptionalInt slowness = data.getCompensatedEntities().getPotionLevelForPlayer(PotionTypes.SLOWNESS);
        if (slowness.isPresent()) {
            attributeSpeed *= 1.0f - 0.15f * (slowness.getAsInt() + 1);
        }
        return (float) attributeSpeed;
    }

    private void resetState() {
        kbX = 0.0;
        kbZ = 0.0;
        ticks = 0;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (player.getTarget() != null && !player.getTarget().isDead && player.getTarget().getType() == EntityTypes.PLAYER) {
                    attack = true;
                }
            }
        }

        if (isFlying(event.getPacketType())) {
            player.velocitySinceTick++;
            if (player.velocitySinceTick == 1 && player.getLikelyKB() != null) {
                kbX = player.getLikelyKB().vector.getX();
                kbZ = player.getLikelyKB().vector.getZ();

            }
            if (isExempt(ExemptType.LIQUID,
                    ExemptType.WEB,
                    ExemptType.TELEPORT,
                    ExemptType.ELYTRA_FLYING,
                    ExemptType.VEHICLE,
                    ExemptType.VEHICLE_SWITCH,
                    ExemptType.FLYING) ||
                    player.getCompensatedEntities().hasPotionEffect(PotionTypes.LEVITATION)) {
                resetState();
            }
            if (!(kbX == 0 || kbZ == 0)) {
                
                double clientKB = player.getDeltaXZ();
                double deltaX = player.getDeltaX();
                double deltaZ = player.getDeltaZ();
                boolean ground = player.isLastOnGround();
                double X = kbX, Z = kbZ;
                double friction = player.getFriction();
                Double min = null;
                for (boolean sprint : BOOL_OPTIONS) {
                    for (boolean jump : BOOL_OPTIONS) {
                        for (boolean using : BOOL_OPTIONS) {
                            for (boolean sneaking : BOOL_OPTIONS) {
                                for (float[] sff : KEY_COMBOS) {
                                    float strafe = sff[0];
                                    float forward = sff[1];
                                    double predictedX = X;
                                    double predictedZ = Z;

                                    if (sprint && forward != 1.0F) {
                                        continue;
                                    }

                                    if (attack) {
                                        if (time() - player.lastAttack > 100L) continue;
                                        predictedX *= 0.6;
                                        predictedZ *= 0.6;
                                    }
                                    if (using) {
                                        strafe *= 0.2F;
                                        forward *= 0.2F;
                                    }
                                    if (sneaking) {
                                        strafe *= 0.3F;
                                        forward *= 0.3F;
                                    }


                                    if (jump && sprint && ground && allowJumpReset) {
                                        float radians = player.getYaw() * ((float) Math.PI / 180);
                                        predictedX -= sin(radians, false) * 0.2F;
                                        predictedZ += cos(radians, false) * 0.2F;
                                    }

                                    float f4;
                                    float f5 = sprint ? (getAttributeSpeed(player) * 1.3f) : getAttributeSpeed(player);
                                    if (ground) {
                                        f4 = (float) friction;
                                        float f = 0.16277136f / (f4 * f4 * f4);
                                        f5 *= f;
                                    } else {
                                        f5 = sprint ? 0.026f : 0.02f;
                                    }
                                    
                                    strafe *= 0.98f;
                                    forward *= 0.98f;
                                    double[] predicts = moveFlying(predictedX, predictedZ, strafe, forward, f5, player.getYaw(), !player.isVanillaMath());

                                    predictedX = predicts[0];
                                    predictedZ = predicts[1];

                                    double offsetX = deltaX - predictedX;
                                    double offsetZ = deltaZ - predictedZ;
                                    double offsetH = Math.hypot(offsetX, offsetZ);

                                    if (min == null || offsetH < min) {
                                        min = offsetH;
                                        kbX = predictedX;
                                        kbZ = predictedZ;
                                    }
                                }
                            }
                        }
                    }
                }

                double dKbX = deltaX / kbX;
                double dKbZ = deltaZ / kbZ;
                double kbH = Math.hypot(kbX, kbZ);
                double ptc = clientKB / kbH * 100;
                
                
                double diff = Math.abs(kbH - clientKB);
                boolean exempt = isExempt(ExemptType.TELEPORT) || player.horizontalCollision;

                
                boolean rev = dKbZ < -0.1 || dKbX < -0.1;

                double allowed = 0.002 + (player.getTotalMovePacketsSent() <= 2 ? 0.03 : 0);
                if (!exempt && ((ptc < minVelocity && diff > allowed) || (ptc >= 300)
                        || (rev && diff > allowed))) {
                    if (buffer++ > 8) {
                        if (flagAndAlertWithSetback(String.format("ptc= %.5f\ndiff= %.5f\na= %s\nr= %s", ptc, diff, attack, rev))) {
                            resetState();
                            buffer *= 0.85;
                            if (attack) player.mitigateDamage();
                            if (ptc < 40) player.getSetbackTeleportUtil().executeViolationSetback();
                            if (ptc == 0) {
                                player.getSetbackTeleportUtil().executeForceResync();
                            }
                            return;
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }

                kbX *= ground ? friction : 0.91f;
                kbZ *= ground ? friction : 0.91f;

                if (kbX == 0 || kbZ == 0 || Math.abs(kbX) < 0.005 || Math.abs(kbZ) < 0.005 || ++ticks > 6) {
                    resetState();
                    return;
                }
                if (exempt) {
                    resetState();
                }
            }
            attack = false;
        }
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(event);
            if (velocity.getEntityId() != player.getEntityID()) return;
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.velocitySinceTick = 0);
        }

    }

    public double[] moveFlying(double motionX, double motionZ, float strafe, float forward, float friction,
                               float yaw, boolean fastMath) {
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = MathUtil.sqrt(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = sin(yaw * (float) Math.PI / 180.0F, fastMath);
            float f2 = cos(yaw * (float) Math.PI / 180.0F, fastMath);
            motionX += strafe * f2 - forward * f1;
            motionZ += forward * f2 + strafe * f1;
        }

        return new double[]{motionX, motionZ};
    }

    @Override
    public void reload() {
        super.reload();
        allowJumpReset = getConfig().getBooleanElse(getConfigName() + ".allowed-jump-reset", true);
        minVelocity = getConfig().getDoubleElse(getConfigName() + ".min-velocity", 90);
    }
}
