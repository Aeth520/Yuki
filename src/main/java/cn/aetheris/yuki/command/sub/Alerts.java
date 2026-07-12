package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class Alerts extends AbstractCommand {

    public Alerts() {
        super(
                "Toggle alerts",
                "yuki.commands.alerts",
                true);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String label, @NotNull String[] args) {
        PluginLoader.INSTANCE.getAlertManager().toggleAlerts(player.getUniqueId());
    }
}