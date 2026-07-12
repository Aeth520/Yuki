package cn.aetheris.yuki.entity;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.impl.movement.noslow.NoSlowE;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.entity.attribute.ValuedAttribute;
import cn.aetheris.yuki.util.inventory.EnchantmentHelper;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public final class PacketEntitySelf extends PacketEntity {

    private final PlayerData player;
    @Getter
    @Setter
    public int opLevel;

    public PacketEntitySelf(PlayerData player) {
        super(player, EntityTypes.PLAYER);
        this.player = player;
    }

    public PacketEntitySelf(PlayerData player, PacketEntitySelf old) {
        super(player, EntityTypes.PLAYER);
        this.player = player;
        this.opLevel = old.opLevel;
        this.attributeMap.putAll(old.attributeMap);
    }

    @Override
    protected void initAttributes(PlayerData player) {
        super.initAttributes(player);
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
            setAttribute(Attributes.STEP_HEIGHT, 0.5f);
        }

        getAttribute(Attributes.SCALE).orElseThrow().withSetRewriter((oldValue, newValue) -> {
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_20_5) || (newValue).equals(oldValue)) {
                return oldValue;
            } else {
                
                player.possibleEyeHeights[2][0] = 0.4 * newValue;
                player.possibleEyeHeights[2][1] = 1.62 * newValue;
                player.possibleEyeHeights[2][2] = 1.27 * newValue;

                
                player.possibleEyeHeights[1][0] = 1.27 * newValue;
                player.possibleEyeHeights[1][1] = 1.62 * newValue;
                player.possibleEyeHeights[1][2] = 0.4 * newValue;

                
                player.possibleEyeHeights[0][0] = 1.62 * newValue;
                player.possibleEyeHeights[0][1] = 1.27 * newValue;
                player.possibleEyeHeights[0][2] = 0.4 * newValue;
                return newValue;
            }
        });

        final ValuedAttribute movementSpeed = ValuedAttribute.ranged(Attributes.MOVEMENT_SPEED, 0.1f, 0, 1024);
        movementSpeed.with(new WrapperPlayServerUpdateAttributes.Property(Attributes.MOVEMENT_SPEED, 0.1f, new ArrayList<>()));
        trackAttribute(movementSpeed);
        trackAttribute(ValuedAttribute.ranged(Attributes.ATTACK_DAMAGE, 2, 0, 2048)); 
        trackAttribute(ValuedAttribute.ranged(Attributes.ATTACK_SPEED, 4, 0, 1024)
                .requiredVersion(player, ClientVersion.V_1_9));
        trackAttribute(ValuedAttribute.ranged(Attributes.JUMP_STRENGTH, 0.42f, 0, 32)
                .requiredVersion(player, ClientVersion.V_1_20_5));
        trackAttribute(ValuedAttribute.ranged(Attributes.BLOCK_BREAK_SPEED, 1.0, 0, 1024)
                .requiredVersion(player, ClientVersion.V_1_20_5));
        trackAttribute(ValuedAttribute.ranged(Attributes.MINING_EFFICIENCY, 0, 0, 1024)
                .requiredVersion(player, ClientVersion.V_1_21));
        trackAttribute(ValuedAttribute.ranged(Attributes.SUBMERGED_MINING_SPEED, 0.2, 0, 20)
                .requiredVersion(player, ClientVersion.V_1_21));
        trackAttribute(ValuedAttribute.ranged(Attributes.ENTITY_INTERACTION_RANGE, 3, 0, 64)
                .requiredVersion(player, ClientVersion.V_1_20_5));
        trackAttribute(ValuedAttribute.ranged(Attributes.BLOCK_INTERACTION_RANGE, 4.5, 0, 64)
                .withGetRewriter(value -> {
                    
                    if (player.gamemode == GameMode.CREATIVE
                            && Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_20_5)) {
                        return 5.0;
                    }
                    
                    return value;
                })
                .requiredVersion(player, ClientVersion.V_1_20_5));
        trackAttribute(ValuedAttribute.ranged(Attributes.WATER_MOVEMENT_EFFICIENCY, 0, 0, 1)
                .withGetRewriter(value -> {
                    
                    if (player.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
                        return 0d;
                    }

                    
                    final double depthStrider = EnchantmentHelper.getMaximumEnchantLevel(player.getInventory(), EnchantmentTypes.DEPTH_STRIDER, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion());
                    if (player.getClientVersion().isOlderThan(ClientVersion.V_1_21)) {
                        return depthStrider;
                    }

                    
                    
                    if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_21)) {
                        return depthStrider / 3;
                    }

                    
                    return value;
                })
                .requiredVersion(player, ClientVersion.V_1_21));
        trackAttribute(ValuedAttribute.ranged(Attributes.MOVEMENT_EFFICIENCY, 0, 0, 1)
                .requiredVersion(player, ClientVersion.V_1_21));
        trackAttribute(ValuedAttribute.ranged(Attributes.SNEAKING_SPEED, 0.3, 0, 1)
                .withGetRewriter(value -> {
                    if (player.getClientVersion().isOlderThan(ClientVersion.V_1_19)) {
                        return (double) 0.3f;
                    }

                    final int swiftSneak = player.getInventory().getLeggings().getEnchantmentLevel(EnchantmentTypes.SWIFT_SNEAK);
                    final double clamped = MathUtil.clamp(0.3f + swiftSneak * 0.15f, 0f, 1f);
                    if (player.getClientVersion().isOlderThan(ClientVersion.V_1_21)) {
                        return clamped;
                    }

                    
                    if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_21)) {
                        return clamped;
                    }

                    
                    return value;
                })
                .requiredVersion(player, ClientVersion.V_1_21));
    }

    public boolean inVehicle() {
        return getRiding() != null;
    }

    @Override
    public void addPotionEffect(PotionType effect, int amplifier) {
        if (effect == PotionTypes.BLINDNESS && !hasPotionEffect(PotionTypes.BLINDNESS)) {
            player.checkManager.getCheck(NoSlowE.class).startedSprintingBeforeBlind = player.isSprinting;
        }

        player.pointThreeEstimator.updatePlayerPotions(effect, amplifier);
        super.addPotionEffect(effect, amplifier);
    }

    @Override
    public void removePotionEffect(PotionType effect) {
        player.pointThreeEstimator.updatePlayerPotions(effect, null);
        super.removePotionEffect(effect);
    }

    @Override
    public void onFirstTransaction(boolean relative, boolean hasPos, double relX, double relY, double relZ, PlayerData player) {
        
    }

    @Override
    public void onSecondTransaction() {
        
    }

    @Override
    public SimpleCollisionBox getPossibleCollisionBoxes() {
        return player.boundingBox.copy(); 
    }
}