package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.block.MainSupportingBlockData;
import cn.aetheris.yuki.entity.PacketEntityHorse;
import cn.aetheris.yuki.entity.PacketEntityStrider;
import cn.aetheris.yuki.util.materials.Materials;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;

public final class BlockProperties {
    public static float getFrictionInfluencedSpeed(float f, PlayerData player) {
        if (player.lastOnGround) {
            return (float) (player.speed * (0.21600002f / (f * f * f)));
        }

        
        if (player.inVehicle()) {
            if (player.compensatedEntities.self.getRiding().type == EntityTypes.PIG || player.compensatedEntities.self.getRiding() instanceof PacketEntityHorse) {
                return (float) (player.speed * 0.1f);
            }

            if (player.compensatedEntities.self.getRiding() instanceof PacketEntityStrider strider) {
                
                if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20)) {
                    return (float) player.speed * 0.1f;
                }

                
                return (float) strider.getAttributeValue(Attributes.MOVEMENT_SPEED) * (strider.isShaking ? 0.66F : 1.0F) * 0.1f;
            }
        }

        if (player.isFlying) {
            return player.flySpeed * 20 * (player.isSprinting ? 0.1f : 0.05f);
        }

        
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19_4)) {
            return player.isSprinting ? 0.025999999F : 0.02f;
        }

        return player.lastSprintingForSpeed ? (float) ((double) 0.02f + 0.005999999865889549D) : 0.02f;
    }

    
    public static StateType getOnPos(PlayerData player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)) {
            return BlockProperties.getOnBlock(player, playerPos.getX(), playerPos.getY(), playerPos.getZ());
        }

        Vector3i pos = getOnPos(player, playerPos, mainSupportingBlockData, 0.2F);
        return player.compensatedWorld.getBlockType(pos.x, pos.y, pos.z);
    }

    public static float getFriction(PlayerData player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)) {
            double searchBelowAmount = 0.5000001;

            if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15))
                searchBelowAmount = 1;

            StateType type = player.compensatedWorld.getBlockType(playerPos.getX(), playerPos.getY() - searchBelowAmount, playerPos.getZ());
            return getMaterialFriction(player, type);
        }

        StateType underPlayer = getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos);
        return getMaterialFriction(player, underPlayer);
    }

    public static float getBlockSpeedFactor(PlayerData player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
        
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) return 1.0f;
        if (player.isGliding || player.isFlying) return 1.0f;

        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)) {
            return getBlockSpeedFactorLegacy(player, playerPos);
        }

        WrappedBlockState inBlock = player.compensatedWorld.getBlock(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        float inBlockSpeedFactor = getBlockSpeedFactor(player, inBlock.getType());
        if (inBlockSpeedFactor != 1.0f || inBlock.getType() == StateTypes.WATER || inBlock.getType() == StateTypes.BUBBLE_COLUMN) {
            return getModernVelocityMultiplier(player, inBlockSpeedFactor);
        }

        StateType underPlayer = getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos);
        return getModernVelocityMultiplier(player, getBlockSpeedFactor(player, underPlayer));
    }

    public static boolean onHoneyBlock(PlayerData player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) return false;

        StateType inBlock = player.compensatedWorld.getBlockType(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        return inBlock == StateTypes.HONEY_BLOCK || getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos) == StateTypes.HONEY_BLOCK;
    }

    
    private static StateType getBlockPosBelowThatAffectsMyMovement(PlayerData player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
        Vector3i pos = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)
                ? new Vector3i(MathUtil.floor(playerPos.getX()), MathUtil.floor(playerPos.getY() - 0.5000001), MathUtil.floor(playerPos.getZ()))
                : getOnPos(player, playerPos, mainSupportingBlockData, 0.500001F);
        return player.compensatedWorld.getBlockType(pos.x, pos.y, pos.z);
    }

    private static Vector3i getOnPos(PlayerData player, Vector3d playerPos, MainSupportingBlockData mainSupportingBlockData, float searchBelowPlayer) {
        Vector3i mainBlockPos = mainSupportingBlockData.getBlockPos();
        if (mainBlockPos != null) {
            StateType blockstate = player.compensatedWorld.getBlockType(mainBlockPos.x, mainBlockPos.y, mainBlockPos.z);

            
            boolean shouldReturn = (!((double) searchBelowPlayer <= 0.5D) || !BlockTags.FENCES.contains(blockstate)) &&
                    !BlockTags.WALLS.contains(blockstate) &&
                    !BlockTags.FENCE_GATES.contains(blockstate);

            return shouldReturn ? mainBlockPos.withY(MathUtil.floor(playerPos.getY() - (double) searchBelowPlayer)) : mainBlockPos;
        } else {
            return new Vector3i(MathUtil.floor(playerPos.getX()), MathUtil.floor(playerPos.getY() - searchBelowPlayer), MathUtil.floor(playerPos.getZ()));
        }
    }

    public static float getMaterialFriction(PlayerData player, StateType material) {
        float friction = 0.6f;

        if (material == StateTypes.ICE) friction = 0.98f;
        if (material == StateTypes.SLIME_BLOCK && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8))
            friction = 0.8f;
        
        if (material == StateTypes.HONEY_BLOCK && player.getClientVersion().isOlderThan(ClientVersion.V_1_15))
            friction = 0.8f;
        if (material == StateTypes.PACKED_ICE) friction = 0.98f;
        if (material == StateTypes.FROSTED_ICE) friction = 0.98f;
        if (material == StateTypes.BLUE_ICE) {
            friction = 0.98f;
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13))
                friction = 0.989f;
        }

        return friction;
    }

    private static StateType getOnBlock(PlayerData player, double x, double y, double z) {
        StateType block1 = player.compensatedWorld.getBlockType(MathUtil.floor(x), MathUtil.floor(y - 0.2F), MathUtil.floor(z));

        if (block1.isAir()) {
            StateType block2 = player.compensatedWorld.getBlockType(MathUtil.floor(x), MathUtil.floor(y - 1.2F), MathUtil.floor(z));

            if (Materials.isFence(block2) || Materials.isWall(block2) || Materials.isGate(block2)) {
                return block2;
            }
        }

        return block1;
    }

    private static float getBlockSpeedFactorLegacy(PlayerData player, Vector3d pos) {
        StateType block = player.compensatedWorld.getBlockType(pos.getX(), pos.getY(), pos.getZ());

        
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16) && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_16_1)) {
            StateType onBlock = BlockProperties.getOnBlock(player, pos.getX(), pos.getY(), pos.getZ());
            if (onBlock == StateTypes.SOUL_SAND && player.getInventory().getBoots().getEnchantmentLevel(EnchantmentTypes.SOUL_SPEED, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion()) > 0)
                return 1.0f;
        }

        float speed = getBlockSpeedFactor(player, block);
        if (speed != 1.0f || block == StateTypes.SOUL_SAND || block == StateTypes.WATER || block == StateTypes.BUBBLE_COLUMN)
            return speed;

        StateType block2 = player.compensatedWorld.getBlockType(pos.getX(), pos.getY() - 0.5000001, pos.getZ());
        return getBlockSpeedFactor(player, block2);
    }

    private static float getBlockSpeedFactor(PlayerData player, StateType type) {
        if (type == StateTypes.HONEY_BLOCK) return 0.4f;
        if (type == StateTypes.SOUL_SAND) {
            
            
            
            if (player.getClientVersion().isOlderThan(ClientVersion.V_1_21)
                    && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16_2)
                    && player.getInventory().getBoots().getEnchantmentLevel(EnchantmentTypes.SOUL_SPEED, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion()) > 0)
                return 1.0f;
            return 0.4f;
        }
        return 1.0f;
    }

    private static float getModernVelocityMultiplier(PlayerData player, float blockSpeedFactor) {
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_21)) return blockSpeedFactor;
        return (float) MathUtil.lerp((float) player.compensatedEntities.self.getAttributeValue(Attributes.MOVEMENT_EFFICIENCY), blockSpeedFactor, 1.0F);
    }
}