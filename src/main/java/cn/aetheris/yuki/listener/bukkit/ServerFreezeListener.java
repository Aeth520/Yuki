package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.data.player.FreezeData;
import cn.aetheris.yuki.util.message.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public final class ServerFreezeListener extends AbstractListener {

    final String message;

    public ServerFreezeListener() {
        this.message = PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("commands.freeze.target-side");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (FreezeData.isFrozen(player)) {
            player.eject();
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (FreezeData.isFrozen(player)) {
                event.setCancelled(true);
                player.sendMessage(message);
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (FreezeData.isFrozen(player)) {
                event.setCancelled(true);
                player.sendMessage(message);
            }
        }
    }

    @EventHandler
    public void onPlayerCraftItem(EnchantItemEvent event) {
        final Player player = event.getEnchanter();
        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(message);
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        if (FreezeData.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        boolean enable = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("function.freeze.punish.enable");
        String command = PluginLoader.INSTANCE.getConfigManager().getConfig().getString(ColorUtils.color("function.freeze.punish.command")
        );
        if (FreezeData.isFrozen(player)) {
            FreezeData.setFrozen(player, false);
            if (enable) {
                Bukkit.getScheduler().runTask(Yuki.getInstance(), () ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName())));
            }
        }
    }
}
