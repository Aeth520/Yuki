package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ControlCheck extends AbstractCommand {

    public ControlCheck() {
        super(
                "Control check",
                "yuki.commands.controlcheck",
                false
        );
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.control-check.usage"));
            return;
        }

        String checkName = args[0];
        final Player player = Bukkit.getPlayer(sender.getName());
        final PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(Objects.requireNonNull(player));

        if (data == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(player, "not-data-user"));
            return;
        }

        
        
        for (AbstractCheck check : data.getChecks()) {
            if (check.getConfigName() == null) {
                continue;
            }
            if (check.getConfigName().equalsIgnoreCase(checkName) || check.getCheckName().equalsIgnoreCase(checkName)) {
                if (check.isEnabled()) {
                    sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.control-check.enable").replace("%check_name%", check.getConfigName()));
                    check.setEnabled(false);
                } else {
                    sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.control-check.disable").replace("%check_name%", check.getConfigName()));
                    check.setEnabled(true);
                }
                break;



            }
        }
    }

    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        final Player player = Bukkit.getPlayer(sender.getName());
        final PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(Objects.requireNonNull(player));
        if (data == null) {
            return List.of();
        }
        List<String> list = new LinkedList<>();
        for (AbstractCheck abstractCheck : data.getChecks()) {
            String configName = abstractCheck.getConfigName();
            if (configName != null) {
                list.add(configName);
            }
        }
        return list;
    }


}