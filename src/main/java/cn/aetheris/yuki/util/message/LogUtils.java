package cn.aetheris.yuki.util.message;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.util.file.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.UUID;

public final class LogUtils {
    private static final String DEVELOP_ENABLE_KEY = "function.develop.enable";
    private static final String SEND_TYPE_KEY = "function.develop.send";

    private static final String DEBUG_PREFIX = "&bDebug &7> ";
    private static final String SYNC_PREFIX = "&eYuki &8» &fSync ";
    private static final String CANCEL_PREFIX = "&cYuki &8» &fPacket ";
    private static final String MITIGATE_PREFIX = "&6Yuki &8» &fMitigate ";
    private static final String SETBACK_PREFIX = "&dYuki &8» &fSetBack ";


    
    public static void console(final String info) {
        Bukkit.getConsoleSender().sendMessage(ColorUtils.color(info));
    }

    
    public static void debug(final String info) {
        if (isDevelopEnabled()) {
            processLogging(info, "debug", DEBUG_PREFIX, PluginLoader.INSTANCE.getAlertManager().getEnabledDebug());
        }
    }

    
    public static void sync(final String info) {
        if (isDevelopEnabled() && isSubFunctionEnabled("function.develop.sync")) {
            processLogging(info, "sync", SYNC_PREFIX, PluginLoader.INSTANCE.getAlertManager().getEnabledSync());
        }
    }

    
    public static void cancel(final String info) {
        if (isDevelopEnabled() && isSubFunctionEnabled("function.develop.cancel")) {
            processLogging(info, "cancel", CANCEL_PREFIX, PluginLoader.INSTANCE.getAlertManager().getEnabledPacketCancel());
        }
    }

    
    public static void mitigate(final String info) {
        if (isDevelopEnabled() && isSubFunctionEnabled("function.develop.mitigate")) {
            processLogging(info, "mitigate", MITIGATE_PREFIX, PluginLoader.INSTANCE.getAlertManager().getEnabledMitigate());
        }
    }

    
    public static void setback(final String info) {
        if (isDevelopEnabled() && isSubFunctionEnabled("function.develop.setback")) {
            processLogging(info, "setback", SETBACK_PREFIX, PluginLoader.INSTANCE.getAlertManager().getEnabledSetBack());
        }
    }

    
    public static void exception(String description, Throwable throwable) {
        Yuki.getInstance().getLogger().severe(description + ": " + getStackTrace(throwable));
    }

    
    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }


    
    private static boolean isDevelopEnabled() {
        return PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse(DEVELOP_ENABLE_KEY, true);
    }

    
    private static void processLogging(String info, String logType, String consolePrefix, Collection<UUID> targetPlayers) {
        if (shouldSendToConsole()) {
            console(consolePrefix + info);
        }

        if (shouldSendToPlayer() && !targetPlayers.isEmpty()) {
            sendToPlayers(info, targetPlayers, consolePrefix);
        }

        if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.develop.log", false)) {
            logToFile(info, logType);
        }
    }

    
    private static void sendToPlayers(String originalInfo, Collection<UUID> players, String prefix) {
        final String coloredMessage = ColorUtils.color(prefix + originalInfo);
        for (UUID uuid : players) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(coloredMessage);
                logPlayerInfoIfNeeded(player, originalInfo, prefix);
            }
        }
    }

    
    private static void logPlayerInfoIfNeeded(Player player, String info, String prefix) {
        if (info.contains(player.getName())) {
            info = ColorUtils.stripColor(info);
            String cleanInfo = info.replace("&7", "").replace("&b", "");
            String logType = parseLogTypeFromPrefix(prefix);
            LoggerUtil.log(player.getName(), cleanInfo, logType);
        }
    }

    
    private static String parseLogTypeFromPrefix(String prefix) {
        if (prefix.contains("Packet")) return "cancel";
        if (prefix.contains("Mitigate")) return "mitigate";
        if (prefix.contains("SetBack")) return "setback";
        if (prefix.contains("Sync")) return "sync";
        return "debug";
    }

    
    private static void logToFile(String info, String logType) {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (info.contains(player.getName())) {
                LoggerUtil.log(player.getName(), info.replace("&7", "").replace("&b", ""), logType);
            }
        }
    }

    
    private static boolean shouldSendToConsole() {
        return checkSendType("console");
    }

    
    private static boolean shouldSendToPlayer() {
        return checkSendType("player");
    }

    
    private static boolean checkSendType(String expectedType) {
        String sendType = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse(SEND_TYPE_KEY, "both");
        return "both".equalsIgnoreCase(sendType) || sendType.equalsIgnoreCase(expectedType);
    }

    
    private static boolean isSubFunctionEnabled(String configKey) {
        return PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse(configKey, true);
    }
}
