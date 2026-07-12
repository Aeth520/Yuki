package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class StopSpectate extends AbstractCommand {

    public StopSpectate() {
        super(
                "Stop spectating",
                "yuki.commands.stopspectating",
                true);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String label, @NotNull String[] args) {
        String locationArg = args.length > 0 ? args[0] : "none";
        boolean teleportBack = locationArg.equals("none") || !locationArg.equalsIgnoreCase("here") || !player.hasPermission("yuki.commands.stopspectating.here");
        PluginLoader.INSTANCE.getSpectateManager().disable(player.getUniqueId(), teleportBack);
        player.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.spectate.stop"));
    }
}