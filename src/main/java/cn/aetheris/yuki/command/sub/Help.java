package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class Help extends AbstractCommand {

    public Help() {
        super(
                "Show help",
                "yuki.commands.help",
                false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("commands.help.message"));
    }
}