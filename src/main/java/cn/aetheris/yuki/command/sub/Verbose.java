package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class Verbose extends AbstractCommand {

    public Verbose() {
        super(
                "Toggle verbose alerts",
                "yuki.commands.verbose",
                true);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String label, @NotNull String[] args) {
        PluginLoader.INSTANCE.getAlertManager().toggleVerbose(player.getUniqueId());
        if (PluginLoader.INSTANCE.getAlertManager().hasAlertsEnabled(player.getUniqueId())) {
            PluginLoader.INSTANCE.getAlertManager().toggleAlerts(player.getUniqueId());
        }
    }
}