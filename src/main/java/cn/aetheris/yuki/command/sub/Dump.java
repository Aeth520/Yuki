package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.util.dump.DiagnosticDump;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class Dump extends AbstractCommand {

    public Dump() {
        super("Export plugin diagnostic dump", "yuki.commands.dump", false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("§b✦ Generating diagnostic dump...");
        File dumpFile = DiagnosticDump.dump();
        if (dumpFile != null) {
            sender.sendMessage("§a✔ Dump saved to: §f" + dumpFile.getAbsolutePath());
        } else {
            sender.sendMessage("§c✘ Failed to generate dump! Check console for details.");
        }
    }
}
