package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.player.exploit.ExploitE;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Set;

public class PlayerUseTridentListener extends AbstractListener {

    private final Set<String> useTridentSet = new HashSet<>();

    
    public static ItemStack getClickItem(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getClick() == ClickType.NUMBER_KEY) {
            return player.getInventory().getItem(event.getHotbarButton());
        }

        return event.getCurrentItem();
    }


    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() != Material.TRIDENT &&
                player.getInventory().getItemInOffHand().getType() != Material.TRIDENT) {
            return;
        }

        useTridentSet.add(player.getName());
    }

    
    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        if (!(projectile instanceof Trident)) {
            return;
        }

        ProjectileSource source = projectile.getShooter();

        if (!(source instanceof Player player)) {
            return;
        }

        useTridentSet.remove(player.getName());
    }

    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        useTridentSet.remove(player.getName());
    }

    
    @EventHandler()
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (event.getSlotType() != InventoryType.SlotType.CRAFTING && event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }

        boolean enable = PluginLoader.INSTANCE.getConfigManager().isMitigateTridentUse();

        if (!enable) {
            return;
        }

        if (!useTridentSet.contains(player.getName())) {
            return;
        }

        ItemStack item = getClickItem(event);

        if (item == null) {
            return;
        }

        if (item.getType() != Material.TRIDENT) {
            return;
        }

        final PlayerData data = getData(player);

        if (data == null) {
            return;
        }

        final ExploitE check = data.getCheckManager().getCheck(ExploitE.class);

        event.setCancelled(true);
        useTridentSet.remove(player.getName());
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            if (check.shouldModifyPackets()) {
                check.failed("type= " + event.getSlotType().name(), "SWAP");
            }
        });
    }
}
