package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.functionality.FeatureManager;
import cn.aetheris.yuki.util.message.ColorUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Features extends AbstractCommand {

    private static final String COLOR_ACCENT = "&3";
    private static final String COLOR_LABEL = "&7";
    private static final String COLOR_TEXT = "&f";
    private static final String COLOR_ON = "&a";
    private static final String COLOR_OFF = "&c";

    public Features() {
        super("Manage feature flags", "yuki.commands.features", false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        FeatureManager fm = FeatureManager.getInstance();

        if (args.length == 0) {
            listFlags(sender, fm);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> listFlags(sender, fm);
            case "enable", "on" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "Yuki &8» " + COLOR_LABEL + "用法: /yuki features enable <flag>"));
                    return;
                }
                toggleFlag(sender, fm, args[1], true);
            }
            case "disable", "off" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "Yuki &8» " + COLOR_LABEL + "用法: /yuki features disable <flag>"));
                    return;
                }
                toggleFlag(sender, fm, args[1], false);
            }
            case "reset" -> {
                fm.resetAll();
                sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "Yuki &8» " + COLOR_ON + "所有功能开关已重置为默认值"));
            }
            case "reload" -> {
                fm.loadFromConfig();
                sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "Yuki &8» " + COLOR_ON + "功能开关已从配置重新加载"));
            }
            default -> sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "Yuki &8» " + COLOR_LABEL + "未知子命令: " + COLOR_TEXT + sub));
        }
    }

    private void listFlags(CommandSender sender, FeatureManager fm) {
        sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "=== Yuki 功能开关 ==="));
        for (Map.Entry<String, Boolean> entry : fm.getAllFlags().entrySet()) {
            String key = entry.getKey();
            boolean enabled = entry.getValue();
            boolean isDefault = fm.getDefaults().get(key).equals(enabled);
            String status = enabled ? COLOR_ON + "启用" : COLOR_OFF + "禁用";
            String marker = isDefault ? "" : COLOR_LABEL + " *";
            sender.sendMessage(ColorUtils.color(
                    COLOR_LABEL + "- " + COLOR_TEXT + key + " " + COLOR_LABEL + "-> " + status + marker));
        }
        sender.sendMessage(ColorUtils.color(COLOR_LABEL + "* 表示与默认值不同"));
    }

    private void toggleFlag(CommandSender sender, FeatureManager fm, String key, boolean enable) {
        if (!fm.isRegistered(key)) {
            sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "Yuki &8» " + COLOR_OFF + "未知功能开关: " + key));
            return;
        }
        fm.setEnabled(key, enable);
        String status = enable ? COLOR_ON + "启用" : COLOR_OFF + "禁用";
        sender.sendMessage(ColorUtils.color(
                COLOR_ACCENT + "Yuki &8» " + COLOR_TEXT + key + COLOR_LABEL + " 已" + status));
    }

    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("list", "enable", "disable", "reset", "reload")) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    result.add(s);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")
                || args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
            for (String key : FeatureManager.getInstance().getAllFlags().keySet()) {
                if (key.startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    result.add(key);
                }
            }
        }
        return result;
    }
}
