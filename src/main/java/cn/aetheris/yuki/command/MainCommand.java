package cn.aetheris.yuki.command;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.sub.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class MainCommand implements TabExecutor {

    private final Map<String, AbstractCommand> subCommands = new HashMap<>();

    public MainCommand() {
        register();
    }

    private void registerSubCommand(String name, AbstractCommand command) {
        subCommands.put(name.toLowerCase(), command);
    }

    private void register() {
        registerSubCommand("delay", new Delay());
        registerSubCommand("help", new Help());
        registerSubCommand("develop", new Develop());
        registerSubCommand("setback", new Setback());
        registerSubCommand("control", new Control());
        registerSubCommand("controlcheck", new ControlCheck());
        registerSubCommand("freeze", new Freeze());
        registerSubCommand("punish", new Punish());
        registerSubCommand("decrypt", new Decrypt());
        registerSubCommand("unfreeze", new UnFreeze());
        registerSubCommand("version", new Info());
        registerSubCommand("info", new Info());
        registerSubCommand("stopspectating", new StopSpectate());
        registerSubCommand("stopspec", new StopSpectate());
        registerSubCommand("spectate", new Spectate());
        registerSubCommand("spec", new Spectate());
        registerSubCommand("verbose", new Verbose());
        registerSubCommand("reload", new Reload());
        registerSubCommand("profile", new Profile());
        registerSubCommand("log", new Log());
        registerSubCommand("logs", new Log());
        registerSubCommand("debug", new Debug());
        registerSubCommand("perf", new Perf());
        registerSubCommand("benchmark", new Perf());
        registerSubCommand("menu", new Menu());
        registerSubCommand("consoledebug", new ConsoleDebug());
        registerSubCommand("sendalert", new SendAlert());
        registerSubCommand("alerts", new Alerts());
        registerSubCommand("crash", new Crash());
        registerSubCommand("history", new History());
        registerSubCommand("hist", new History());
        registerSubCommand("mitigate", new Mitigate());
        registerSubCommand("discordtest", new DiscordTest());
        registerSubCommand("dump", new Dump());
        registerSubCommand("features", new Features());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("commands.help.message"));
            return true;
        }

        AbstractCommand command = subCommands.get(args[0].toLowerCase());
        if (command == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("unknown-command"));
            return true;
        }

        return command.onCommand(sender, cmd, label, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String name : subCommands.keySet()) {
                AbstractCommand sub = subCommands.get(name);
                if (sub != null && name.startsWith(input) &&
                        (sub.getPermission() == null || sender.hasPermission(sub.getPermission()))) {
                    completions.add(name);
                }
            }
            return completions;
        }

        AbstractCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.onTabComplete(sender, cmd, alias, Arrays.copyOfRange(args, 1, args.length));
        }
        return Collections.emptyList();
    }
}
