package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "KillAuraF (Sprint)", type = CheckType.KILLAURA, configName = "KillAuraF", decay = 0.5, description = "Check for keepsprint", experimental = true)
public final class KillAuraF extends Check implements PacketCheck {

    private int ticksSinceVelocity;
    private int attacks;
    private boolean onGround;
    private boolean lastOnGround;
    private boolean lastLastOnGround;
    private double lastFriction;
    private double lastLastFriction;

    public KillAuraF(PlayerData player) {
        super(player);
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

            final boolean shouldExempt = isExempt(
                    ExemptType.NEXT_FENCE,
                    ExemptType.NEXT_SLIME,
                    ExemptType.NEXT_ICE,
                    ExemptType.NEXT_HONEY,
                    ExemptType.VEHICLE,
                    ExemptType.JOIN,
                    ExemptType.TELEPORT,
                    ExemptType.BED,
                    ExemptType.BREWERRY_PUSH,
                    ExemptType.GSIT_ACTION,
                    ExemptType.MYTHIC_ITEM_ATTACK,
                    ExemptType.RESPAWN,
                    ExemptType.LIQUID,
                    ExemptType.INVALID_GAMEMODE,
                    ExemptType.FLYING,
                    ExemptType.ELYTRA_FLYING,
                    ExemptType.CLIENT_VERSION);

            if (shouldExempt) {
                rewardBufferAndVL();
                return;
            }

            if (ticksSinceVelocity > 1 && player.getDeltaXZ() > 0.1 && attacks > 0) {
                final boolean sprinting = player.isSprinting();
                double attackMotion = getAttackMotion();
                double acceleration = player.getAcceleration();
                double walkSpeed = getSpeed() * 1.3F;
                double extra = sprinting ? 1 : 0;
                if (attackMotion > walkSpeed && acceleration < 0.005) {
                    if ((buffer += (0.5 + extra)) >= 6) {
                        if (flagAndAlert(String.format("a= %.5f\nac= %.5f\nms= %.5f\ngs= %.5f", attackMotion, acceleration, walkSpeed, player.getSpeed()))) {
                            player.mitigateDamage();
                            buffer = 6;
                        }
                    } else {
                        rewardBufferAndVL();
                    }
                }
            }
            ticksSinceVelocity++;
            lastLastFriction = lastFriction;
            lastFriction = player.getFriction();
            lastLastOnGround = lastOnGround;
            lastOnGround = onGround;
            onGround = flying.isOnGround();
            attacks = 0;
        } else if (isTransaction(event.getPacketType())) {
            ticksSinceVelocity = 0;
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (player.getTarget() != null && player.getTarget().getType() == EntityTypes.PLAYER) {
                    if (player.isLastSprinting()) {
                        attacks++;
                    }
                }
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () ->
                    ticksSinceVelocity = 0
            );
        }
    }

    private double getAttackMotion() {
        double deltaX = player.getLastDeltaX() * (lastLastOnGround ? player.getFriction() : 0.91F);
        double deltaZ = player.getLastDeltaZ() * (lastLastOnGround ? player.getFriction() : 0.91F);

        deltaX *= 0.6;
        deltaZ *= 0.6;

        double deltaXZ = Math.hypot(deltaX, deltaZ);
        return Math.abs(player.getDeltaXZ() - deltaXZ);
    }

    private float getSpeed() {
        double attributeSpeed = player.getCompensatedEntities().getSelf().getAttributeValue(Attributes.MOVEMENT_SPEED);
        if (player.getCompensatedEntities().getSelf().getPotionEffectLevel(PotionTypes.SPEED).isPresent()
                && player.getCompensatedEntities().getSelf().getPotionEffectLevel(PotionTypes.SPEED).getAsInt() > 0) {
            attributeSpeed *= 1 + (player.getCompensatedEntities().getSelf().getPotionEffectLevel(PotionTypes.SPEED).getAsInt() * 0.2);
        }
        if (player.getCompensatedEntities().getSelf().getPotionEffectLevel(PotionTypes.SLOWNESS).isPresent()
                && player.getCompensatedEntities().getSelf().getPotionEffectLevel(PotionTypes.SLOWNESS).getAsInt() > 0) {
            attributeSpeed *= 1 - (player.getCompensatedEntities().getSelf().getPotionEffectLevel(PotionTypes.SLOWNESS).getAsInt() * 0.15);
        }
        return (float) attributeSpeed;
    }
}