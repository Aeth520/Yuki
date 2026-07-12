package cn.aetheris.yuki.util.lists;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.inventory.Inventory;
import cn.aetheris.yuki.util.inventory.InventoryStorage;
import cn.aetheris.yuki.protocol.nms.ReflectionUtils;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CorrectingPlayerInventoryStorage extends InventoryStorage {
    private static final Set<String> SUPPORTED_INVENTORIES = Set.of(
            "CHEST", "DISPENSER", "DROPPER", "PLAYER",
            "ENDER_CHEST", "SHULKER_BOX", "BARREL", "CRAFTING", "CREATIVE"
    );

    private final PlayerData player;
    private final Map<Integer, Integer> serverPredictions = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> pendingSlots = new ConcurrentHashMap<>();
    private Method getOpenInventoryMethod;
    private Method getTypeMethod;
    private Method typeToStringMethod;

    
    public CorrectingPlayerInventoryStorage(PlayerData player, int size) {
        super(size);
        this.player = player;
    }

    
    public void handleClientClaimedSlotSet(int slot) {
        if (isValidSlot(slot)) {
            pendingSlots.put(slot, PluginLoader.INSTANCE.getTickManager().currentTick + 5);
        }
    }

    
    public void handleServerCorrectSlot(int slot) {
        if (isValidSlot(slot)) {
            serverPredictions.put(slot, player.lastTransactionSent.get());
        }
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        if (HookInit.getPayPluginHook().isEnabled()) {
            return;
        }
        int transaction = serverPredictions.getOrDefault(slot, -1);
        if (transaction == -1 || player.lastTransactionReceived.get() >= transaction) {
            pendingSlots.put(slot, PluginLoader.INSTANCE.getTickManager().currentTick + 5);
            serverPredictions.remove(slot);
        }
        super.setItem(slot, item);
    }

    
    public void tickWithBukkit() {
        if (player.bukkitPlayer == null) return;

        int currentTick = PluginLoader.INSTANCE.getTickManager().currentTick;
        processPendingSlots(currentTick);
        handleInventoryRefresh();
        checkRandomSlot(currentTick);
    }

    private void processPendingSlots(int tick) {
        Iterator<Map.Entry<Integer, Integer>> it = pendingSlots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> entry = it.next();
            if (entry.getValue() <= tick) {
                verifySlotSync(entry.getKey());
                it.remove();
            }
        }
    }

    private void handleInventoryRefresh() {
        if (player.getInventory().requiresRefresh) {
            MHDFScheduler.getEntityScheduler().runTask(Yuki.getInstance(), player.bukkitPlayer, () -> {
                if (!player.getInventory().requiresRefresh) return;
                if (isSupportedInventory()) {
                    player.getInventory().requiresRefresh = false;
                    player.bukkitPlayer.updateInventory();
                }
            }, null);
        }
    }

    private boolean isSupportedInventory() {
        try {
            ensureReflectionMethods();
            Object view = getOpenInventoryMethod.invoke(player.bukkitPlayer);
            Object type = getTypeMethod.invoke(view);
            String typeName = ((String) typeToStringMethod.invoke(type)).toUpperCase(Locale.ROOT);
            return SUPPORTED_INVENTORIES.contains(typeName);
        } catch (Exception e) {
            return false;
        }
    }

    private void checkRandomSlot(int tick) {
        if (tick % 5 == 0) {
            int slot = (tick / 5) % getSize();
            if (!pendingSlots.containsKey(slot) && !serverPredictions.containsKey(slot)) {
                verifySlotSync(slot);
            }
        }
    }

    private void verifySlotSync(int slot) {
        if (player.bukkitPlayer == null || !player.getInventory().isPacketInventoryActive) return;

        int convertedSlot = player.getInventory().getBukkitSlot(slot);
        if (convertedSlot != -1) {
            MHDFScheduler.getEntityScheduler().runTask(Yuki.getInstance(), player.bukkitPlayer, () -> {
                org.bukkit.inventory.ItemStack bukkitItem = player.bukkitPlayer.getInventory().getItem(convertedSlot);
                ItemStack current = getItem(slot);
                ItemStack converted = SpigotConversionUtil.fromBukkitItemStack(bukkitItem);

                if (!isItemEqual(current, converted)) {
                    setItem(slot, converted);
                    player.bukkitPlayer.updateInventory();
                }
            }, null);
        }
    }

    @SneakyThrows
    private void ensureReflectionMethods() {
        if (getOpenInventoryMethod == null) {
            Class<?> playerClass = ReflectionUtils.getClass("org.bukkit.entity.Player");
            if (playerClass != null) {
                getOpenInventoryMethod = playerClass.getMethod("getOpenInventory");
            }

            Class<?> inventoryViewClass = ReflectionUtils.getClass("org.bukkit.inventory.InventoryView");
            if (inventoryViewClass != null) {
                getTypeMethod = inventoryViewClass.getMethod("getType");
            }

            Class<?> inventoryTypeClass = ReflectionUtils.getClass("org.bukkit.event.inventory.InventoryType");
            if (inventoryTypeClass != null) {
                typeToStringMethod = inventoryTypeClass.getMethod("toString");
            }
        }
    }

    private boolean isItemEqual(ItemStack a, ItemStack b) {
        return a != null && b != null &&
                a.getType() == b.getType() &&
                a.getAmount() == b.getAmount();
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot <= Inventory.ITEMS_END;
    }
}
