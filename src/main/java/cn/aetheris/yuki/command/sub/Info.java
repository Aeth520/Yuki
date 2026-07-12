package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public final class Info extends AbstractCommand {

    private static final DecimalFormat MEM_FORMAT = new DecimalFormat("#,##0.0");

    public Info() {
        super(
                "Show yuki info",
                "yuki.commands.info",
                false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(HookInit.getPlaceholderAPIHook().setPlaceholders(null, PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("commands.info.message")
                .replace("%version%", PluginLoader.INSTANCE.getVersion())
                .replace("%server%", PluginLoader.INSTANCE.getExternalAPI().getServerName())
                .replace("%packetevents%", PacketEvents.getAPI().getVersion().toString())
                .replace("%memory%", getMemoryUsage())
                .replace("%folia%", "false")
                .replace("%support_adventure%", PaperUtils.isNativeSupportAdventureApi() + "")
                .replace("%via%", HookInit.getViaPluginHook().isEnabled() + "")
                .replace("%server_version%", Yuki.getInstance().getPacketEventsManager().getServerVersion().name())
                .replace("%system%", System.getProperty("os.name") == null ? PacketEvents.getAPI().getServerManager().getOS().name() : System.getProperty("os.name"))
                .replace("%java%", System.getProperties().getProperty("java.version") == null ? "Unknown" : System.getProperties().getProperty("java.version"))));
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long max = runtime.maxMemory() / (1024 * 1024);
        return MEM_FORMAT.format(used) + "MB/" + MEM_FORMAT.format(max) + "MB";
    }
}