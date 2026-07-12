package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.functionality.crash.CrashManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Crash extends AbstractCommand {
    public Crash() {
        super("crash player", "yuki.commands.crash", false);
    }

    
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!validateInput(sender, args)) return;

        Player target = Bukkit.getPlayer(args[0]);
        String crashType = args.length > 1 ? args[1] : "explosion";

        if (processCrash(target, crashType)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.crash.message"));
        } else {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.crash.invalid-type"));
        }
    }

    private boolean processCrash(Player target, String type) {
        switch (type.toLowerCase()) {
            case "explosion" -> CrashManager.sendExplosion(target);
            case "posandlook" -> CrashManager.sendInvalidPosition(target);
            case "invalidparticle" -> CrashManager.sendInvalidParticle(target);
            case "entity" -> CrashManager.spawnArmorStands(target, 80);
            case "nbt" -> CrashManager.sendCorruptNBT(target);
            case "teleport" -> CrashManager.sendNANTeleport(target);
            case "data" -> CrashManager.sendEntityMeta(target);
            case "stop" -> CrashManager.cleanupEntities(target);
            default -> { return false; }
        }
        return true;
    }


    
    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new LinkedList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String n = player.getName();
                if (n.startsWith(args[0].toLowerCase())) {
                    list.add(n);
                }
            }
            return list;
        }
        if (args.length == 2) {
            return Arrays.asList("explosion", "posandlook",
                    "invalidparticle", "entity", "nbt", "teleport", "data", "stop");
        }
        return Collections.emptyList();
    }

    private boolean validateInput(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.crash.usage"));
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("not-found").replace("%player%", args[0]));
            return false;
        }
        if (target == sender) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("not-my-self"));
            return false;
        }
        return true;
    }


}