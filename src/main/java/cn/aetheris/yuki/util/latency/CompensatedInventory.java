package cn.aetheris.yuki.util.latency;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.inventory.EquipmentType;
import cn.aetheris.yuki.util.inventory.Inventory;
import cn.aetheris.yuki.util.inventory.menu.AbstractContainerMenu;
import cn.aetheris.yuki.util.inventory.menu.MenuType;
import cn.aetheris.yuki.util.inventory.menu.NotImplementedMenu;
import cn.aetheris.yuki.util.lists.CorrectingPlayerInventoryStorage;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenHorseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


public final class CompensatedInventory extends Check implements PacketCheck {
    private static final int PLAYER_INVENTORY_CASE = -1;
    private static final int UNSUPPORTED_INVENTORY_CASE = -2;
    
    public Inventory inventory;
    
    public AbstractContainerMenu menu;
    
    public boolean isPacketInventoryActive = true;
    public boolean requiresRefresh = false;
    public int stateID = 0; 
    public int openWindowID = 0;
    
    
    
    private int packetSendingInventorySize = PLAYER_INVENTORY_CASE;

    public CompensatedInventory(PlayerData playerData) {
        super(playerData);

        CorrectingPlayerInventoryStorage storage = new CorrectingPlayerInventoryStorage(player, 46);
        inventory = new Inventory(playerData, storage);

        menu = inventory;
    }

    
    public int getBukkitSlot(int packetSlot) {
        
        if (packetSlot <= 4) {
            return -1;
        }
        
        if (packetSlot <= 8) {
            
            return (7 - packetSlot) + 36;
        }
        
        if (packetSlot <= 35) {
            return packetSlot;
        }
        
        if (packetSlot <= 44) {
            
            return packetSlot - 36;
        }
        
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9) && packetSlot == 45) {
            return 40;
        }
        return -1;
    }

    
    private void markPlayerSlotAsChanged(int clicked) {
        
        if (openWindowID == 0) {
            inventory.getInventoryStorage().handleClientClaimedSlotSet(clicked);
            return;
        }

        
        
        if (menu instanceof NotImplementedMenu) return;

        
        
        int nonPlayerInvSize = menu.getSlots().size() - 36 + 9;
        int playerInvSlotclicked = clicked - nonPlayerInvSize;
        
        inventory.getInventoryStorage().handleClientClaimedSlotSet(playerInvSlotclicked);
    }

    public ItemStack getItemInHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? getHeldItem() : getOffHand();
    }

    
    private void markServerForChangingSlot(int clicked, int windowID) {
        
        if (packetSendingInventorySize == -2) return;
        
        if (packetSendingInventorySize == PLAYER_INVENTORY_CASE || windowID == 0) {
            
            inventory.getInventoryStorage().handleServerCorrectSlot(clicked);
            return;
        }
        
        int nonPlayerInvSize = menu.getSlots().size() - 36 + 9;
        int playerInvSlotclicked = clicked - nonPlayerInvSize;

        inventory.getInventoryStorage().handleServerCorrectSlot(playerInvSlotclicked);
    }

    public ItemStack getHeldItem() {
        ItemStack item = isPacketInventoryActive || player.bukkitPlayer == null ? inventory.getHeldItem() :
                SpigotConversionUtil.fromBukkitItemStack(player.bukkitPlayer.getInventory().getItemInHand());
        return item == null ? ItemStack.EMPTY : item;
    }

    public ItemStack getOffHand() {
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_9))
            return ItemStack.EMPTY;
        ItemStack item = isPacketInventoryActive || player.bukkitPlayer == null ? inventory.getOffhand() :
                SpigotConversionUtil.fromBukkitItemStack(player.bukkitPlayer.getInventory().getItemInOffHand());
        return item == null ? ItemStack.EMPTY : item;
    }

    public ItemStack getHelmet() {
        ItemStack item = isPacketInventoryActive || player.bukkitPlayer == null ? inventory.getHelmet() :
                SpigotConversionUtil.fromBukkitItemStack(player.bukkitPlayer.getInventory().getHelmet());
        return item == null ? ItemStack.EMPTY : item;
    }

    public ItemStack getChestplate() {
        ItemStack item = isPacketInventoryActive || player.bukkitPlayer == null ? inventory.getChestplate() :
                SpigotConversionUtil.fromBukkitItemStack(player.bukkitPlayer.getInventory().getChestplate());
        return item == null ? ItemStack.EMPTY : item;
    }

    public ItemStack getLeggings() {
        ItemStack item = isPacketInventoryActive || player.bukkitPlayer == null ? inventory.getLeggings() :
                SpigotConversionUtil.fromBukkitItemStack(player.bukkitPlayer.getInventory().getLeggings());
        return item == null ? ItemStack.EMPTY : item;
    }

    public ItemStack getBoots() {
        ItemStack item = isPacketInventoryActive || player.bukkitPlayer == null ? inventory.getBoots() :
                SpigotConversionUtil.fromBukkitItemStack(player.bukkitPlayer.getInventory().getBoots());
        return item == null ? ItemStack.EMPTY : item;
    }

    private ItemStack getByEquipmentType(EquipmentType type) {
        return switch (type) {
            case HEAD -> getHelmet();
            case CHEST -> getChestplate();
            case LEGS -> getLeggings();
            case FEET -> getBoots();
            case OFFHAND -> getOffHand();
            case MAINHAND -> getHeldItem();
        };
    }

    public boolean hasItemType(ItemType type) {
        if (isPacketInventoryActive || player.bukkitPlayer == null)
            return inventory.hasItemType(type);

        
        for (org.bukkit.inventory.ItemStack item : player.bukkitPlayer.getInventory().getContents()) {
            ItemStack itemStack = SpigotConversionUtil.fromBukkitItemStack(item);
            if (itemStack != null && itemStack.getType() == type) return true;
        }
        return false;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            WrapperPlayClientUseItem item = new WrapperPlayClientUseItem(event);

            ItemStack use = item.getHand() == InteractionHand.MAIN_HAND ? player.getInventory().getHeldItem() : player.getInventory().getOffHand();

            EquipmentType equipmentType = EquipmentType.getEquipmentSlotForItem(use);
            if (equipmentType != null) {
                int slot;
                switch (equipmentType) {
                    case HEAD:
                        slot = Inventory.SLOT_HELMET;
                        break;
                    case CHEST:
                        slot = Inventory.SLOT_CHESTPLATE;
                        break;
                    case LEGS:
                        slot = Inventory.SLOT_LEGGINGS;
                        break;
                    case FEET:
                        slot = Inventory.SLOT_BOOTS;
                        break;
                    default: 
                        return;
                }

                ItemStack currentEquippedItem = getByEquipmentType(equipmentType);
                
                if (player.getClientVersion().isOlderThan(ClientVersion.V_1_19_4) && !currentEquippedItem.isEmpty())
                    return;

                
                int swapItemSlot = item.getHand() == InteractionHand.MAIN_HAND ? inventory.selected + Inventory.HOTBAR_OFFSET : Inventory.SLOT_OFFHAND;

                
                
                inventory.getInventoryStorage().handleClientClaimedSlotSet(swapItemSlot);
                inventory.getInventoryStorage().setItem(swapItemSlot, currentEquippedItem);

                
                inventory.getInventoryStorage().handleClientClaimedSlotSet(slot);
                inventory.getInventoryStorage().setItem(slot, use);
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(event);

            
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) return;

            if (dig.getAction() == DiggingAction.DROP_ITEM) {
                ItemStack heldItem = getHeldItem();
                if (heldItem != null) {
                    heldItem.setAmount(heldItem.getAmount() - 1);
                    if (heldItem.getAmount() <= 0) {
                        heldItem = null;
                    }
                }
                inventory.setHeldItem(heldItem);
                inventory.getInventoryStorage().handleClientClaimedSlotSet(Inventory.HOTBAR_OFFSET + player.packetStateData.lastSlotSelected);
            }

            if (dig.getAction() == DiggingAction.DROP_ITEM_STACK) {
                inventory.setHeldItem(null);
                inventory.getInventoryStorage().handleClientClaimedSlotSet(Inventory.HOTBAR_OFFSET + player.packetStateData.lastSlotSelected);
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            final int slot = new WrapperPlayClientHeldItemChange(event).getSlot();

            
            if (slot > 8 || slot < 0) return;

            inventory.selected = slot;
        }

        if (event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            WrapperPlayClientCreativeInventoryAction action = new WrapperPlayClientCreativeInventoryAction(event);
            if (player.gamemode != GameMode.CREATIVE) return;

            boolean valid = action.getSlot() >= 1 &&
                    (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_8) ?
                            action.getSlot() <= 45 : action.getSlot() < 45);

            if (valid) {
                player.getInventory().inventory.getSlot(action.getSlot()).set(action.getItemStack());
                inventory.getInventoryStorage().handleClientClaimedSlotSet(action.getSlot());
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW && !event.isCancelled()) {
            WrapperPlayClientClickWindow click = new WrapperPlayClientClickWindow(event);

            
            if (click.getWindowId() != openWindowID) {
                return;
            }

            
            if (menu instanceof NotImplementedMenu) {
                return;
            }

            
            Optional<Map<Integer, ItemStack>> slots = click.getSlots();
            slots.ifPresent(integerItemStackMap -> integerItemStackMap.keySet().forEach(this::markPlayerSlotAsChanged));

            
            
            int button = click.getButton();
            
            
            int slot = click.getSlot();
            
            WrapperPlayClientClickWindow.WindowClickType clickType = click.getWindowClickType();

            if (slot == -1 || slot == -999 || slot < menu.getSlots().size()) {
                menu.doClick(button, slot, clickType);
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            this.closeActiveInventory();
        }
    }

    public void markSlotAsResyncing(BlockPlace place) {
        
        if (place.hand == InteractionHand.MAIN_HAND) {
            inventory.getInventoryStorage().handleClientClaimedSlotSet(Inventory.HOTBAR_OFFSET + player.packetStateData.lastSlotSelected);
        } else {
            inventory.getInventoryStorage().handleServerCorrectSlot(Inventory.SLOT_OFFHAND);
        }
    }

    public void onBlockPlace(BlockPlace place) {
        if (player.gamemode != GameMode.CREATIVE && place.itemStack.getType() != ItemTypes.POWDER_SNOW_BUCKET) {
            markSlotAsResyncing(place);
            place.itemStack.setAmount(place.itemStack.getAmount() - 1);
        }
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        
        
        
        if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow open = new WrapperPlayServerOpenWindow(event);

            MenuType menuType = MenuType.getMenuType(open.getType());

            AbstractContainerMenu newMenu;
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_14)) {
                newMenu = MenuType.getMenuFromID(player, inventory, menuType);
            } else {
                newMenu = MenuType.getMenuFromString(player, inventory, open.getLegacyType(), open.getLegacySlots(), open.getHorseId());
            }

            packetSendingInventorySize = newMenu instanceof NotImplementedMenu ? UNSUPPORTED_INVENTORY_CASE : newMenu.getSlots().size();

            
            
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                openWindowID = open.getContainerId();
                menu = newMenu;
                isPacketInventoryActive = !(newMenu instanceof NotImplementedMenu);
                requiresRefresh = newMenu instanceof NotImplementedMenu;
            });
        }

        
        if (event.getPacketType() == PacketType.Play.Server.OPEN_HORSE_WINDOW) {
            WrapperPlayServerOpenHorseWindow open = new WrapperPlayServerOpenHorseWindow(event);

            packetSendingInventorySize = UNSUPPORTED_INVENTORY_CASE;
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                isPacketInventoryActive = false;
                requiresRefresh = true;
                openWindowID = open.getWindowId();
            });
        }

        
        if (event.getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
            packetSendingInventorySize = PLAYER_INVENTORY_CASE;

            
            
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), this::closeActiveInventory);
        }

        
        if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
            WrapperPlayServerWindowItems items = new WrapperPlayServerWindowItems(event);
            stateID = items.getStateId();

            List<ItemStack> slots = items.getItems();
            for (int i = 0; i < slots.size(); i++) {
                markServerForChangingSlot(i, items.getWindowId());
            }

            final int cachedPacketInvSize = packetSendingInventorySize;
            final AtomicBoolean updatedValue = new AtomicBoolean();
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                
                
                
                
                
                if (slots.size() == cachedPacketInvSize || items.getWindowId() == 0) {
                    isPacketInventoryActive = true;
                    updatedValue.set(true);
                }
            });

            if (items.getWindowId() == 0) { 
                player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                    if (!isPacketInventoryActive) return;
                    for (int i = 0; i < slots.size(); i++) {
                        inventory.getSlot(i).set(slots.get(i));
                    }
                    if (items.getCarriedItem().isPresent()) {
                        inventory.setCarried(items.getCarriedItem().get());
                    }
                });
            } else {
                player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                    if (!isPacketInventoryActive) return;
                    if (items.getWindowId() == openWindowID) {
                        for (int i = 0; i < slots.size(); i++) {
                            menu.getSlot(i).set(slots.get(i));
                        }
                    }
                    if (items.getCarriedItem().isPresent()) {
                        inventory.setCarried(items.getCarriedItem().get());
                    }
                });
            }

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                
                if (updatedValue.get() && !menu.equals(inventory)) {
                    isPacketInventoryActive = false;
                }
            });
        }

        
        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
            
            
            
            WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(event);

            if (slot.getWindowId() == -2) { 
                inventory.getInventoryStorage().handleServerCorrectSlot(slot.getSlot());
            } else if (slot.getWindowId() == 0) { 
                inventory.getInventoryStorage().handleServerCorrectSlot(slot.getSlot());
            } else {
                markServerForChangingSlot(slot.getSlot(), slot.getWindowId());
            }

            stateID = slot.getStateId();

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                if (!isPacketInventoryActive) return;
                if (slot.getWindowId() == -1) { 
                    inventory.setCarried(slot.getItem());
                } else if (slot.getWindowId() == -2) { 
                    if (inventory.getInventoryStorage().getSize() > slot.getSlot() && slot.getSlot() >= 0) {
                        inventory.getInventoryStorage().setItem(slot.getSlot(), slot.getItem());
                    }
                } else if (slot.getWindowId() == 0) { 
                    
                    
                    
                    if (slot.getSlot() >= 0 && slot.getSlot() <= 45) {
                        inventory.getSlot(slot.getSlot()).set(slot.getItem());
                    }
                } else if (slot.getWindowId() == openWindowID) { 
                    menu.getSlot(slot.getSlot()).set(slot.getItem());
                }
            });
        }
    }

    
    private void closeActiveInventory() {
        openWindowID = 0;
        menu = inventory;
        menu.setCarried(ItemStack.EMPTY); 
    }

    public boolean hasAnyOfItemType(ItemType... items) {
        if (isPacketInventoryActive || player.bukkitPlayer == null) return inventory.hasAnyOfItemType(items);

        
        for (org.bukkit.inventory.ItemStack item : player.bukkitPlayer.getInventory().getContents()) {
            ItemStack itemStack = SpigotConversionUtil.fromBukkitItemStack(item);
            if (itemStack != null) {
                for (ItemType itemType : items)
                    if (itemStack.getType() == itemType) return true;
            }
        }
        return false;
    }
}
