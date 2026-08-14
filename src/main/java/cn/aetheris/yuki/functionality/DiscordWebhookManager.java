package cn.aetheris.yuki.functionality;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public final class DiscordWebhookManager {

    private boolean enabled;
    private String webhookUrl;
    private String username;
    private String avatarUrl;

    public boolean isEnabled() { return enabled; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }

    public DiscordWebhookManager() {
        reload();
    }

    public void reload() {
        var config = PluginLoader.INSTANCE.getConfigManager().getConfig();
        this.enabled = config.getBooleanElse("discord.enabled", false);
        this.webhookUrl = config.getStringElse("discord.webhook-url", "");
        this.username = config.getStringElse("discord.username", "Yuki");
        this.avatarUrl = config.getStringElse("discord.avatar-url", "");
    }

    public void sendMessage(String content) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty()) return;

        JsonObject payload = new JsonObject();
        payload.addProperty("content", content);
        if (username != null && !username.isEmpty()) {
            payload.addProperty("username", username);
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            payload.addProperty("avatar_url", avatarUrl);
        }

        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(webhookUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    Yuki.getInstance().getLogger().warning("Discord webhook returned response code: " + responseCode);
                }
            } catch (Exception e) {
                Yuki.getInstance().getLogger().log(Level.WARNING, "Failed to send Discord webhook message", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public boolean test() {
        if (!enabled) return false;
        if (webhookUrl == null || webhookUrl.isEmpty()) return false;
        sendMessage("**Yuki** Discord webhook test - connection successful!");
        return true;
    }
}
