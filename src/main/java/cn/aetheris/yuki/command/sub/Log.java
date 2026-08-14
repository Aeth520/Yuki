package cn.aetheris.yuki.command.sub;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.functionality.DebugManager;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class Log extends AbstractCommand {

    public Log() {
        super("Upload a log by flag ID", "yuki.commands.log", false);
    }


    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.prediciton-logs.usage"));
            return;
        }

        int flagId;
        try {
            flagId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.prediciton-logs.invalid"));
            return;
        }

        final StringBuilder flagData = DebugManager.getFlag(flagId);
        if (flagData == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.prediciton-logs.not-found")
                    .replace("%flagId%", String.valueOf(flagId)));
            return;
        }

        String logContent = flagData.toString();
        sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.prediciton-logs.uploading"));

        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            String url = uploadPaste(logContent);
            if (url != null) {
                sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.prediciton-logs.uploadded")
                        .replace("%url%", url));
            } else {
                sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.prediciton-logs.uploadfail"));
            }
        });
    }

    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (Integer id : DebugManager.flags.keySet()) {
                String flag = id.toString();
                if (StringUtil.startsWithIgnoreCase(flag, args[0])) {
                    completions.add(flag);
                }
            }
        }
        return completions;
    }

    public String uploadPaste(String contents) {
        contents = contents.replace("§c", "")
                .replace("§7", "")
                .replace("§6§l", "")
                .replace("§a", "");

        HttpURLConnection connection = null;
        try {
            String PASTE_UPLOAD_URL = "https://paste.md-5.net/documents";
            connection = (HttpURLConnection) new URL(PASTE_UPLOAD_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) " +
                    "AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(contents.getBytes(Charsets.UTF_8));
            }

            Gson gson = new Gson();
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), Charsets.UTF_8)) {
                JsonObject object = gson.fromJson(reader, JsonObject.class);
                String PASTE_URL = "https://paste.md-5.net/";
                return PASTE_URL + object.get("key").getAsString();
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
