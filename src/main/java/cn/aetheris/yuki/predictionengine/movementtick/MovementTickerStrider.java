package cn.aetheris.yuki.predictionengine.movementtick;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.attribute.ValuedAttribute;
import cn.aetheris.yuki.entity.PacketEntityStrider;
import cn.aetheris.yuki.protocol.nms.BlockProperties;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

import java.util.ArrayList;

public final class MovementTickerStrider extends MovementTickerRideable {

    private static final WrapperPlayServerUpdateAttributes.PropertyModifier SUFFOCATING_MODIFIER = new WrapperPlayServerUpdateAttributes.PropertyModifier(
            ResourceLocation.minecraft("suffocating"), -0.34F, WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE);

    public MovementTickerStrider(PlayerData player) {
        super(player);
        movementInput = new Vector3dm(0, 0, 1);
    }

    public static void floatStrider(PlayerData player) {
        if (player.wasTouchingLava) {
            if (isAbove(player) && player.compensatedWorld.getLavaFluidLevelAt((int) Math.floor(player.x), (int) Math.floor(player.y + 1), (int) Math.floor(player.z)) == 0) {
                player.onGround = true;
            } else {
                player.clientVelocity.multiply(0.5).add(new Vector3dm(0, 0.05, 0));
            }
        }
    }

    public static boolean isAbove(PlayerData player) {
        return player.y > Math.floor(player.y) + 0.5 - 1.0E-5F;
    }

    @Override
    public void livingEntityAIStep() {
        super.livingEntityAIStep();

        StateType posMaterial = player.compensatedWorld.getBlockType(player.x, player.y, player.z);
        StateType belowMaterial = BlockProperties.getOnPos(player, player.mainSupportingBlockData, new Vector3d(player.x, player.y, player.z));

        final PacketEntityStrider strider = (PacketEntityStrider) player.compensatedEntities.getSelf().getRiding();
        strider.isShaking = !BlockTags.STRIDER_WARM_BLOCKS.contains(posMaterial) &&
                !BlockTags.STRIDER_WARM_BLOCKS.contains(belowMaterial) &&
                !player.wasTouchingLava;
    }

    @Override
    public float getSteeringSpeed() {
        PacketEntityStrider strider = (PacketEntityStrider) player.compensatedEntities.getSelf().getRiding();
        final boolean newSpeed = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20);
        final float coldSpeed = newSpeed ? 0.35F : 0.23F;
        
        
        final ValuedAttribute movementSpeedAttr = strider.getAttribute(Attributes.MOVEMENT_SPEED).get();
        float updatedMovementSpeed = (float) movementSpeedAttr.get();
        if (newSpeed) {
            final WrapperPlayServerUpdateAttributes.Property lastProperty = movementSpeedAttr.property().orElse(null);
            if (lastProperty != null && (!strider.isShaking || lastProperty.getModifiers()
                    .stream().noneMatch(mod -> mod.getName().getKey().equals("suffocating")))) {
                WrapperPlayServerUpdateAttributes.Property newProperty =
                        new WrapperPlayServerUpdateAttributes.Property(lastProperty.getAttribute(), lastProperty.getValue()
                                , new ArrayList<>(lastProperty.getModifiers()));
                if (!strider.isShaking) {
                    newProperty.getModifiers().removeIf(modifier -> modifier.getName().getKey().equals("suffocating"));
                } else {
                    newProperty.getModifiers().add(SUFFOCATING_MODIFIER);
                }
                movementSpeedAttr.with(newProperty);
                updatedMovementSpeed = (float) movementSpeedAttr.get();
                movementSpeedAttr.with(lastProperty);
            }
        }

        return updatedMovementSpeed * (strider.isShaking ? coldSpeed : 0.55F);
    }

    @Override
    public boolean canStandOnLava() {
        return true;
    }
}
