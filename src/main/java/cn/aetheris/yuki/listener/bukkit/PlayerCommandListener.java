package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.util.develop.DevelopUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class PlayerCommandListener extends AbstractListener {

    @EventHandler()
    public void onCommand(PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().toLowerCase().replace("/", "");
        final Player player = event.getPlayer();

        boolean enabled = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.command-blocker.command.enable", true);
        String message = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("command-blocker.message", "Unknown command. Type \"/help\" for help.");
        String anticheatName = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("function.command-blocker.plugin-name", "KarhuAC");
        List<String> fakePluginNames = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringListElse("function.command-blocker.command.fake-plugins-add", new LinkedList<>());
        List<String> containsName = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringListElse("function.command-blocker.command.blocks.contains", new LinkedList<>());
        List<String> equalsName = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringListElse("function.command-blocker.command.blocks.equals", new LinkedList<>());

        if (!enabled || event.isCancelled()) {
            return;
        }

        if (DevelopUtils.isDeveloper(player) || player.hasPermission("yuki.antiplugin")) {
            return;
        }

        if (!player.hasPermission("bukkit.command.plugins")) {
            return;
        }

        if ((command.equalsIgnoreCase("plugins")
                || command.equalsIgnoreCase("pl")
                || command.equalsIgnoreCase("bukkit:plugins")
                || command.equalsIgnoreCase("bukkit:pl"))) {

            PluginManager pluginManager = Bukkit.getServer().getPluginManager();
            Set<String> pluginNames = new HashSet<>();
            for (Plugin plugin : pluginManager.getPlugins()) {
                String pluginName = null;

                switch (plugin.getName().toLowerCase()) {
                    case "yuki":
                        pluginName = anticheatName;
                        break;
                    case "spiterloader":
                        pluginName = "GrimAC";
                        break;
                    case "vulcan":
                        pluginName = "MX";
                        break;
                    case "matrix":
                        pluginName = "LightAntiCheat";
                        break;
                    default:
                        if (!fakePluginNames.isEmpty()) {
                            pluginName = fakePluginNames.get(0);
                        }
                        if (pluginName == null) {
                            pluginName = plugin.getName();
                        }
                        break;
                }

                pluginNames.add(pluginName);
            }

            StringBuilder pluginList = new StringBuilder();
            pluginList.append("§f").append("Plugins (").append(pluginNames.size()).append("): ");

            int i = 0;
            for (String pluginName : pluginNames) {
                if (i > 0) {
                    pluginList.append("§f").append(", ");
                }
                pluginList.append("§a").append(pluginName);
                i++;
            }

            player.sendMessage(pluginList.toString());
            event.setCancelled(true);
            return;
        }

        if (shouldBlock(command, containsName, equalsName)) {
            if (command.contains("plugman")) {
                return;
            }
            player.sendMessage(message);
            event.setCancelled(true);
        }
    }

    private boolean shouldBlock(String command, List<String> list1, List<String> list2) {
        return list1.stream().anyMatch(command::contains) || list2.stream().anyMatch(command::equalsIgnoreCase);
    }
}
