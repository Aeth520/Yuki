package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.tag.SyncedTag;
import cn.aetheris.yuki.entity.tag.SyncedTags;
import cn.aetheris.yuki.util.enums.FluidTag;
import cn.aetheris.yuki.util.inventory.EnchantmentHelper;
import cn.aetheris.yuki.util.materials.Materials;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTool;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

public final class BlockBreakSpeed {

    
    private static final Set<StateType> HARVESTABLE_TYPES_1_21_4 = Sets.newHashSet(
            StateTypes.BELL,
            StateTypes.LANTERN,
            StateTypes.SOUL_LANTERN,
            StateTypes.COPPER_DOOR,
            StateTypes.EXPOSED_COPPER_DOOR,
            StateTypes.OXIDIZED_COPPER_DOOR,
            StateTypes.WEATHERED_COPPER_DOOR,
            StateTypes.WAXED_COPPER_DOOR,
            StateTypes.WAXED_EXPOSED_COPPER_DOOR,
            StateTypes.WAXED_OXIDIZED_COPPER_DOOR,
            StateTypes.WAXED_WEATHERED_COPPER_DOOR,
            StateTypes.IRON_DOOR,
            StateTypes.HEAVY_WEIGHTED_PRESSURE_PLATE,
            StateTypes.LIGHT_WEIGHTED_PRESSURE_PLATE,
            StateTypes.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            StateTypes.STONE_PRESSURE_PLATE,
            StateTypes.BREWING_STAND,
            StateTypes.ENDER_CHEST
    );
    private static final boolean serverUsesComponentsAndRules = Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5);
    @Getter
    private static float speed = 1.0f;

    public static double getBlockDamage(PlayerData player, WrappedBlockState block) {
        
        
        ItemStack tool = player.getInventory().getHeldItem();
        ItemType toolType = tool.getType();

        if (player.gamemode == GameMode.CREATIVE) {
            if (serverUsesComponentsAndRules && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_5)) {
                return tool.getComponent(ComponentTypes.TOOL)
                        .map(ItemTool::isCanDestroyBlocksInCreative)
                        .orElse(true) ? 1 : 0;
            } else {
                if (toolType.hasAttribute(ItemTypes.ItemAttribute.SWORD) || toolType == ItemTypes.TRIDENT
                        || (toolType == ItemTypes.DEBUG_STICK && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13))
                        || (toolType == ItemTypes.MACE && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20_5))) {
                    return 0;
                }
                return 1;
            }
        }

        float blockHardness = block.getType().getHardness();

        
        if ((block.getType() == StateTypes.PISTON || block.getType() == StateTypes.PISTON_HEAD || block.getType() == StateTypes.STICKY_PISTON) && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_15_2)) {
            blockHardness = 0.5f;
        }

        if (blockHardness == -1) return 0; 

        boolean isCorrectToolForDrop = false;
        float speedMultiplier = 1.0F;

        
        
        
        if (serverUsesComponentsAndRules && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20_5)) {
            Optional<ItemTool> toolComponentOpt = tool.getComponent(ComponentTypes.TOOL);
            if (toolComponentOpt.isPresent()) {
                ItemTool itemTool = toolComponentOpt.get();

                
                
                speedMultiplier = itemTool.getDefaultMiningSpeed();

                boolean speedFound = false;
                boolean dropsFound = false;

                for (ItemTool.Rule rule : itemTool.getRules()) {
                    MappedEntitySet<StateType.Mapped> predicate = rule.getBlocks();
                    ResourceLocation tagKey = predicate.getTagKey();
                    boolean isMatch;

                    
                    if (tagKey != null) {
                        SyncedTag<StateType> playerTag = player.tagManager.block(tagKey);
                        isMatch = (playerTag != null && playerTag.contains(block.getType()))
                                || BlockTags.getByName(tagKey.getKey()).contains(block.getType());
                    } else {
                        isMatch = predicate.getEntities().contains(block.getType().getMapped());
                    }

                    
                    if (isMatch) {
                        
                        if (!speedFound && rule.getSpeed() != null) {
                            speedMultiplier = rule.getSpeed();
                            speedFound = true;
                        }

                        
                        if (!dropsFound && rule.getCorrectForDrops() != null) {
                            isCorrectToolForDrop = rule.getCorrectForDrops();
                            dropsFound = true;
                        }
                    }

                    if (speedFound && dropsFound) {
                        break;
                    }
                }
            }
        } else {
            
            if (toolType.hasAttribute(ItemTypes.ItemAttribute.AXE)) {
                isCorrectToolForDrop = player.tagManager.block(SyncedTags.MINEABLE_AXE).contains(block.getType());
            } else if (toolType.hasAttribute(ItemTypes.ItemAttribute.PICKAXE)) {
                isCorrectToolForDrop = player.tagManager.block(SyncedTags.MINEABLE_PICKAXE).contains(block.getType());
            } else if (toolType.hasAttribute(ItemTypes.ItemAttribute.SHOVEL)) {
                isCorrectToolForDrop = player.tagManager.block(SyncedTags.MINEABLE_SHOVEL).contains(block.getType());
            } else if (toolType.hasAttribute(ItemTypes.ItemAttribute.HOE)) {
                isCorrectToolForDrop = player.tagManager.block(SyncedTags.MINEABLE_HOE).contains(block.getType());
            }

            if (isCorrectToolForDrop) {
                int tier = 0;
                if (toolType.hasAttribute(ItemTypes.ItemAttribute.WOOD_TIER)) { 
                    speedMultiplier = 2.0f;
                } else if (toolType.hasAttribute(ItemTypes.ItemAttribute.STONE_TIER)) { 
                    speedMultiplier = 4.0f;
                    tier = 1;
                } else if (toolType.hasAttribute(ItemTypes.ItemAttribute.IRON_TIER)) { 
                    speedMultiplier = 6.0f;
                    tier = 2;
                } else if (toolType.hasAttribute(ItemTypes.ItemAttribute.DIAMOND_TIER)) { 
                    speedMultiplier = 8.0f;
                    tier = 3;
                } else if (toolType.hasAttribute(ItemTypes.ItemAttribute.GOLD_TIER)) { 
                    speedMultiplier = 12.0f;
                } else if (toolType.hasAttribute(ItemTypes.ItemAttribute.NETHERITE_TIER)) { 
                    speedMultiplier = 9.0f;
                    tier = 4;
                }

                if (tier < 3 && player.tagManager.block(SyncedTags.NEEDS_DIAMOND_TOOL).contains(block.getType())) {
                    isCorrectToolForDrop = false;
                } else if (tier < 2 && player.tagManager.block(SyncedTags.NEEDS_IRON_TOOL).contains(block.getType())) {
                    isCorrectToolForDrop = false;
                } else if (tier < 1 && player.tagManager.block(SyncedTags.NEEDS_STONE_TOOL).contains(block.getType())) {
                    isCorrectToolForDrop = false;
                }
            }

            
            if (toolType == ItemTypes.SHEARS) {
                isCorrectToolForDrop = true;

                if (block.getType() == StateTypes.COBWEB || Materials.isLeaves(block.getType())) {
                    speedMultiplier = 15.0f;
                } else if (BlockTags.WOOL.contains(block.getType())) {
                    speedMultiplier = 5.0f;
                } else if (block.getType() == StateTypes.VINE ||
                        block.getType() == StateTypes.GLOW_LICHEN) {
                    speedMultiplier = 2.0f;
                } else {
                    isCorrectToolForDrop = block.getType() == StateTypes.COBWEB ||
                            block.getType() == StateTypes.REDSTONE_WIRE ||
                            block.getType() == StateTypes.TRIPWIRE;
                }
            }

            
            if (toolType.hasAttribute(ItemTypes.ItemAttribute.SWORD)) {
                if (block.getType() == StateTypes.COBWEB) {
                    speedMultiplier = 15.0f;
                } else if (player.tagManager.block(SyncedTags.SWORD_EFFICIENT).contains(block.getType())) {
                    speedMultiplier = 1.5f;
                }

                isCorrectToolForDrop = block.getType() == StateTypes.COBWEB;
            }
        }

        if (speedMultiplier > 1.0f) {
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21) && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
                speedMultiplier += (float) player.compensatedEntities.self.getAttributeValue(Attributes.MINING_EFFICIENCY);
            } else {
                int digSpeed = tool.getEnchantmentLevel(EnchantmentTypes.BLOCK_EFFICIENCY, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion());
                if (digSpeed > 0) {
                    speedMultiplier += digSpeed * digSpeed + 1;
                }
            }
        }

        OptionalInt digSpeed = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.HASTE);
        OptionalInt conduit = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.CONDUIT_POWER);

        if (digSpeed.isPresent() || conduit.isPresent()) {
            int hasteLevel = Math.max(digSpeed.isEmpty() ? 0 : digSpeed.getAsInt(), conduit.isEmpty() ? 0 : conduit.getAsInt());
            speedMultiplier *= (float) (1 + (0.2 * (hasteLevel + 1)));
        }

        OptionalInt miningFatigue = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.MINING_FATIGUE);

        if (miningFatigue.isPresent()) {
            switch (miningFatigue.getAsInt()) {
                case 0:
                    speedMultiplier *= 0.3f;
                    break;
                case 1:
                    speedMultiplier *= 0.09f;
                    break;
                case 2:
                    speedMultiplier *= 0.0027f;
                    break;
                default:
                    speedMultiplier *= 0.00081f;
            }
        }

        speedMultiplier *= (float) player.compensatedEntities.self.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);

        if (player.fluidOnEyes == FluidTag.WATER) {
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21) && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
                speedMultiplier *= (float) player.compensatedEntities.self.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
            } else {
                if (EnchantmentHelper.getMaximumEnchantLevel(player.getInventory(), EnchantmentTypes.AQUA_AFFINITY, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion()) == 0) {
                    speedMultiplier /= 5;
                }
            }
        }

        if (!player.packetStateData.packetPlayerOnGround) {
            speedMultiplier /= 5;
        }

        float damage = speedMultiplier / blockHardness;

        boolean canHarvest = !block.getType().isRequiresCorrectTool() || isCorrectToolForDrop
                
                || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_4) && HARVESTABLE_TYPES_1_21_4.contains(block.getType());
        if (canHarvest) {
            damage /= 30F;
        } else {
            damage /= 100F;
        }

        speed = speedMultiplier;

        return damage;
    }
}