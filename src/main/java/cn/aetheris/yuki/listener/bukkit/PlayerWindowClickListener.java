package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.util.message.LogUtils;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public final class PlayerWindowClickListener extends AbstractListener {

    private long lastPickup;

    @EventHandler()
    private void onPlayerWindowClick(InventoryClickEvent event) {
        if (!PluginLoader.INSTANCE.getConfigManager().isMitigateAutoTotem()) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
            return;
        }

        
        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            if (event.getRawSlot() != 45 && safeGetType(event.getCurrentItem()).contains("TOTEM")) {
                this.lastPickup = System.currentTimeMillis();
            }
            if (event.getRawSlot() == 45 && event.getHotbarButton() >= 0 && safeGetType(event.getClickedInventory().getItem(event.getHotbarButton())).contains("TOTEM")) {
                this.useTotem(event);
            }
        }

        if (event.getAction() == InventoryAction.PLACE_ALL && event.getRawSlot() == 45 && event.getCursor().getType().name().contains("TOTEM")) {
            LogUtils.debug("&b" + event.getWhoClicked().getName() + "&7 sent PICK_ALL contains the totem");
            useTotem(event);
        }
        if (event.getAction() == InventoryAction.PICKUP_ALL && event.getCurrentItem() != null && event.getCurrentItem().getType().name().contains("TOTEM")) {
            LogUtils.debug("&b" + event.getWhoClicked().getName() + "&7 sent PICK_ALL contains the totem but currentItem is null");
            this.lastPickup = System.currentTimeMillis();
        }
    }

    private void useTotem(InventoryClickEvent event) {
        long delay = System.currentTimeMillis() - lastPickup;
        if (delay < 150L) {
            event.setResult(Event.Result.DENY);
            LogUtils.mitigate("&b" + event.getWhoClicked().getName() + "&7 Mitigate for using auto totem? (&b" + lastPickup + "&7)");
        }
    }

    private String safeGetType(ItemStack itemStack) {
        if (itemStack == null) {
            return "AIR";
        } else {
            return itemStack.getType().name();
        }
    }
}

