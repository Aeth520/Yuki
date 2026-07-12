package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class DiscordTest extends AbstractCommand {

    public DiscordTest() {
        super("Test Discord webhook", "yuki.commands.discordtest", false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        boolean success = PluginLoader.INSTANCE.getDiscordWebhookManager().test();
        if (success) {
            sender.sendMessage("§a✔ Discord webhook test message sent!");
        } else {
            sender.sendMessage("§c✘ Discord webhook is not configured or disabled!");
        }
    }
}
