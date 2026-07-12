package cn.aetheris.yuki.util.dump;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.functionality.PerformanceMonitor;
import cn.aetheris.yuki.player.PlayerData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

@UtilityClass
public final class DiagnosticDump {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public static File dump() {
        JsonObject root = new JsonObject();

        root.add("plugin", dumpPluginInfo());
        root.add("server", dumpServerInfo());
        root.add("performance", dumpPerformance());
        root.add("players", dumpPlayers());

        String timestamp = DATE_FORMAT.format(new Date());
        File dumpDir = new File(Yuki.getInstance().getDataFolder(), "dumps");
        if (!dumpDir.exists()) dumpDir.mkdirs();

        File dumpFile = new File(dumpDir, "dump_" + timestamp + ".json");
        try (FileWriter writer = new FileWriter(dumpFile)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            Yuki.getInstance().getLogger().log(Level.SEVERE, "Failed to write dump file", e);
            return null;
        }

        Yuki.getInstance().getLogger().info("Dump saved to: " + dumpFile.getAbsolutePath());
        return dumpFile;
    }

    private static JsonObject dumpPluginInfo() {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", "Yuki");
        obj.addProperty("version", Yuki.getInstance().getDescription().getVersion());
        obj.addProperty("javaVersion", System.getProperty("java.version"));
        obj.addProperty("dataFolder", Yuki.getInstance().getDataFolder().getAbsolutePath());
        return obj;
    }

    private static JsonObject dumpServerInfo() {
        JsonObject obj = new JsonObject();
        obj.addProperty("serverName", Bukkit.getName());
        obj.addProperty("version", Bukkit.getVersion());
        obj.addProperty("bukkitVersion", Bukkit.getBukkitVersion());
        obj.addProperty("onlinePlayers", Bukkit.getOnlinePlayers().size());
        obj.addProperty("maxPlayers", Bukkit.getMaxPlayers());
        Runtime runtime = Runtime.getRuntime();
        obj.addProperty("maxMemoryMB", runtime.maxMemory() / 1024 / 1024);
        obj.addProperty("totalMemoryMB", runtime.totalMemory() / 1024 / 1024);
        obj.addProperty("freeMemoryMB", runtime.freeMemory() / 1024 / 1024);
        obj.addProperty("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
        obj.addProperty("availableProcessors", runtime.availableProcessors());
        obj.addProperty("osName", System.getProperty("os.name"));
        obj.addProperty("osArch", System.getProperty("os.arch"));
        return obj;
    }

    private static JsonObject dumpPerformance() {
        JsonObject obj = new JsonObject();
        PerformanceMonitor monitor = PerformanceMonitor.getInstance();
        obj.addProperty("tps", monitor.getTPS());
        obj.addProperty("mspt", monitor.getMSPT());
        return obj;
    }

    private static JsonObject dumpPlayers() {
        JsonObject obj = new JsonObject();
        for (PlayerData data : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
            JsonObject playerObj = new JsonObject();
            playerObj.addProperty("name", data.getName());
            playerObj.addProperty("uuid", data.getUniqueId().toString());
            playerObj.addProperty("brand", data.getBrand());
            playerObj.addProperty("version", data.getVersionName());
            playerObj.addProperty("ping", data.getTransactionPing());
            obj.add(data.getName(), playerObj);
        }
        return obj;
    }
}
