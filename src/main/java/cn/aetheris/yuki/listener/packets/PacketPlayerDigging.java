package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.movement.noslow.NoSlowA;
import cn.aetheris.yuki.check.impl.movement.noslow.NoSlowD;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.item.ItemBehaviour;
import cn.aetheris.yuki.util.item.ItemBehaviourRegistry;
import cn.aetheris.yuki.protocol.nms.NMSUtils;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.FoodProperties;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemConsumable;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;

public final class PacketPlayerDigging extends AbstractPacketListener {

    private static final boolean RELIABLE_COMPONENT_SYSTEM = Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_4);

    public PacketPlayerDigging() {
        super(PacketListenerPriority.LOWEST);
    }

    public static void handleUseItem(PlayerData player, ItemStack item, InteractionHand hand) {
        if (item == null) {
            player.packetStateData.setSlowedByUsingItem(false);
            return;
        }

        if (player.checkManager.getCompensatedCooldown().hasItem(item)) {
            player.packetStateData.setSlowedByUsingItem(false); 
            return; 
        }

        final ItemType material = item.getType();

        
        if (RELIABLE_COMPONENT_SYSTEM && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_4)) {
            ItemBehaviour itemBehaviour = ItemBehaviourRegistry.getItemBehaviour(material);

            if (itemBehaviour.canUse(item, player.compensatedWorld, player, hand)) {
                player.packetStateData.setSlowedByUsingItem(true);
                player.packetStateData.eatingHand = hand;
            } else {
                player.packetStateData.setSlowedByUsingItem(false);
            }

            return;
        }

        
        final ItemConsumable consumable = item.getComponentOr(ComponentTypes.CONSUMABLE, null);
        final FoodProperties foodComponent = item.getComponentOr(ComponentTypes.FOOD, null);

        
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2) && consumable != null && foodComponent == null) {
            player.packetStateData.setSlowedByUsingItem(true);
            player.packetStateData.eatingHand = hand;
        }

        
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20_5) && foodComponent != null) {
            if (foodComponent.isCanAlwaysEat() || player.food < 20 || player.gamemode == GameMode.CREATIVE) {
                player.packetStateData.setSlowedByUsingItem(true);
                player.packetStateData.eatingHand = hand;
                return;
            } else {
                player.packetStateData.setSlowedByUsingItem(false);
            }
        }

        
        if (material.hasAttribute(ItemTypes.ItemAttribute.EDIBLE) &&
                (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_15) || player.gamemode != GameMode.CREATIVE)
                || material == ItemTypes.POTION || material == ItemTypes.MILK_BUCKET) {

            
            if (item.getType() == ItemTypes.SPLASH_POTION)
                return;
            
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_9) && item.getLegacyData() > 16384) {
                return;
            }

            
            if (material == ItemTypes.POTION || material == ItemTypes.MILK_BUCKET
                    || material == ItemTypes.GOLDEN_APPLE || material == ItemTypes.ENCHANTED_GOLDEN_APPLE
                    || material == ItemTypes.HONEY_BOTTLE || material == ItemTypes.SUSPICIOUS_STEW ||
                    material == ItemTypes.CHORUS_FRUIT) {
                player.packetStateData.setSlowedByUsingItem(true);
                player.packetStateData.eatingHand = hand;
                return;
            }

            
            if (item.getType().hasAttribute(ItemTypes.ItemAttribute.EDIBLE) && ((player.bukkitPlayer != null && player.food < 20) || player.gamemode == GameMode.CREATIVE)) {
                player.packetStateData.setSlowedByUsingItem(true);
                player.packetStateData.eatingHand = hand;
                return;
            }

            final ItemStack mainHand = player.getInventory().getItemInHand(InteractionHand.MAIN_HAND);
            final ItemStack offHand = player.getInventory().getItemInHand(InteractionHand.OFF_HAND);
            final org.bukkit.inventory.ItemStack bukkitMainHand = SpigotConversionUtil.toBukkitItemStack(mainHand);
            final org.bukkit.inventory.ItemStack bukkitOffHand = SpigotConversionUtil.toBukkitItemStack(offHand);
            if ((HookInit.getMythicMobsHook().mythicMobItem(bukkitMainHand) && player.packetStateData.eatingHand == hand) ||
                    ((player.packetStateData.eatingHand == hand
                            && HookInit.getMythicMobsHook().mythicMobItem(bukkitOffHand)
                            && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_8)))) {
                player.packetStateData.setSlowedByUsingItem(false);
                return;
            }

            
            player.packetStateData.setSlowedByUsingItem(false);
        }

        if (material == ItemTypes.SHIELD && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            player.packetStateData.setSlowedByUsingItem(true);
            player.packetStateData.eatingHand = hand;
            return;
        }

        
        final NBTCompound nbt = item.getNBT(); 
        if (material == ItemTypes.CROSSBOW && nbt != null && nbt.getBoolean("Charged")) {
            player.packetStateData.setSlowedByUsingItem(false); 
            return;
        }

        
        if (material == ItemTypes.TRIDENT
                && item.getDamageValue() < item.getMaxDamage() - 1 
                && (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13_2)
                || player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8))) {
            player.packetStateData.setSlowedByUsingItem(item.getEnchantmentLevel(EnchantmentTypes.RIPTIDE, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion()) <= 0);
            player.packetStateData.eatingHand = hand;
        }

        
        
        if (material == ItemTypes.BOW || material == ItemTypes.CROSSBOW) {
            boolean isSlowedByUsingItem = player.gamemode == GameMode.CREATIVE ||
                    player.getInventory().hasAnyOfItemType(ItemTypes.ARROW, ItemTypes.TIPPED_ARROW, ItemTypes.SPECTRAL_ARROW);
            player.packetStateData.eatingHand = hand;
                
            
            
            
            
            
            player.packetStateData.setSlowedByUsingItem(isSlowedByUsingItem);
        }

        if (material == ItemTypes.SPYGLASS && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) {
            player.packetStateData.setSlowedByUsingItem(true);
            player.packetStateData.eatingHand = hand;
        }

        if (material == ItemTypes.GOAT_HORN && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19)) {
            player.packetStateData.setSlowedByUsingItem(true);
            player.packetStateData.eatingHand = hand;
        }

        
        if (material.hasAttribute(ItemTypes.ItemAttribute.SWORD)) {
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8))
                player.packetStateData.setSlowedByUsingItem(true);
            else if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_9)) 
                player.packetStateData.setSlowedByUsingItem(false);
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            final WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(event);

            final PlayerData player = getData(event.getUser());
            if (player == null) return;

            switch (dig.getAction()) {
                case START_DIGGING -> {
                    player.finishDigging = false;
                    player.digging = true;
                    player.basicDigging = true;
                    player.dropItem = false;
                    player.lastBlockDig = System.currentTimeMillis();
                }
                case RELEASE_USE_ITEM -> {
                    player.digging = false;
                    player.basicDigging = false;
                    player.finishDigging = false;
                    player.dropItem = false;
                }
                case SWAP_ITEM_WITH_OFFHAND -> {
                    player.digging = false;
                    player.basicDigging = false;
                    player.finishDigging = false;
                    player.dropItem = false;
                    if (player.packetStateData.isSlowedByUsingItem()) {
                        player.packetStateData.setSlowedByUsingItem(false);
                        player.packetStateData.slowedByUsingItemTransaction = player.lastTransactionReceived.get();
                    }
                }
                case CANCELLED_DIGGING -> {
                    player.basicDigging = false;
                    player.finishDigging = false;
                    player.dropItem = false;
                }
                case FINISHED_DIGGING -> {
                    player.basicDigging = false;
                    player.digging = false;
                    player.finishDigging = true;
                    player.dropItem = false;
                }
                case DROP_ITEM, DROP_ITEM_STACK -> {
                    player.digging = false;
                    player.basicDigging = false;
                    player.finishDigging = false;
                    if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_15)) {
                        player.dropItem = true;
                    }
                }
            }
            if (dig.getAction() == DiggingAction.RELEASE_USE_ITEM) {
                player.packetStateData.setSlowedByUsingItem(false);
                player.packetStateData.slowedByUsingItemTransaction = player.lastTransactionReceived.get();

                if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
                    ItemStack hand = player.packetStateData.eatingHand == InteractionHand.OFF_HAND ? player.getInventory().getOffHand() : player.getInventory().getHeldItem();

                    if (hand.getType() == ItemTypes.TRIDENT
                            && hand.getEnchantmentLevel(EnchantmentTypes.RIPTIDE, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion()) > 0) {
                        player.packetStateData.tryingToRiptide = true;
                    }
                }
            }
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) || event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END) {
            final PlayerData player = getData(event.getUser());
            if (player != null && player.packetStateData.isSlowedByUsingItem()
                    && !player.packetStateData.lastPacketWasTeleport
                    && !player.packetStateData.lastPacketWasOnePointSeventeenDuplicate) {
                if (player.packetStateData.eatingHand != InteractionHand.OFF_HAND
                        && player.packetStateData.getSlowedByUsingItemSlot() != player.packetStateData.lastSlotSelected
                        || player.getInventory().getItemInHand(player.packetStateData.eatingHand).isEmpty()) {
                    player.packetStateData.setSlowedByUsingItem(false);
                    player.checkManager.getCheck(NoSlowA.class).didSlotChangeLastTick = true;
                    player.checkManager.getCheck(NoSlowD.class).didSlotChangeLastTick = true;
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            final int slot = new WrapperPlayClientHeldItemChange(event).getSlot();

            
            if (slot > 8 || slot < 0) return;

            final PlayerData player = getData(event.getUser());
            if (player == null) return;

            
            
            CheckManagerListener.handleQueuedPlaces(player, false, 0, 0, System.currentTimeMillis());

            if (player.packetStateData.lastSlotSelected != slot) {
                if (PluginLoader.INSTANCE.getConfigManager().isMitigateNoSlowChangeSlot()) {
                    NMSUtils.resetItemUsage(player.bukkitPlayer);
                }

                
                if (player.canSkipTicks() && !player.isTickingReliablyFor(3) && player.packetStateData.eatingHand != InteractionHand.OFF_HAND) {
                    player.packetStateData.setSlowedByUsingItem(false);
                }
            }
            player.packetStateData.lastSlotSelected = slot;
        }

        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM
                || (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                && new WrapperPlayClientPlayerBlockPlacement(event).getFace() == BlockFace.OTHER)) {
            final PlayerData player = getData(event.getUser());
            if (player == null) return;

            final InteractionHand hand = event.getPacketType() == PacketType.Play.Client.USE_ITEM
                    ? new WrapperPlayClientUseItem(event).getHand()
                    : InteractionHand.MAIN_HAND;

            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_8)
                    && player.gamemode == GameMode.SPECTATOR)
                return;

            player.packetStateData.slowedByUsingItemTransaction = player.lastTransactionReceived.get();

            final ItemStack item = hand == InteractionHand.MAIN_HAND ?
                    player.getInventory().getHeldItem() : player.getInventory().getOffHand();

            handleUseItem(player, item, hand);
        }
    }
}