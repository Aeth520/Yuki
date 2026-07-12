package cn.aetheris.yuki.command;

import cn.aetheris.yuki.PluginLoader;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@Getter
public abstract class AbstractCommand implements TabExecutor, Command {

    private final String description;
    private final String permission;
    private final boolean onlyPlayer;

    public AbstractCommand(@NotNull String description, @Nullable String permission, boolean onlyPlayer) {
        this.description = description;
        this.permission = permission;
        this.onlyPlayer = onlyPlayer;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
    }

    @Override
    public void execute(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
    }

    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return new LinkedList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (onlyPlayer && !(sender instanceof Player)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("only-player"));
            return true;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("no-permission"));
            return true;
        }

        if (onlyPlayer) {
            execute((Player) sender, label, args);
        } else {
            execute(sender, label, args);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        List<String> result = new LinkedList<>();
        for (String s : tabCompleter(sender, label, args)) {
            if (s.toLowerCase(Locale.ROOT).startsWith(args[args.length - 1].toLowerCase(Locale.ROOT))) {
                result.add(s);
            }
        }
        return result;
    }
}
