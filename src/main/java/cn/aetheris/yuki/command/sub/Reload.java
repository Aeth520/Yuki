package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class Reload extends AbstractCommand {

    public Reload() {
        super("Reload the plugin", "yuki.commands.reload", false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        try {
            PluginLoader.INSTANCE.getExternalAPI().reload();
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.reload.success"));
        } catch (RuntimeException e) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.reload.fail")
                    .replace("%reason%", e.getMessage()));
        }
    }
}