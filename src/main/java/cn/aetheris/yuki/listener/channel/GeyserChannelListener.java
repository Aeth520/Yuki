package cn.aetheris.yuki.listener.channel;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.functionality.GeyserManager;
import cn.aetheris.yuki.player.PlayerData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class GeyserChannelListener implements PluginMessageListener {

    @Getter
    private static final Set<UUID> bedRockUser = new HashSet<>();

    
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!"yuki:bedrock".equalsIgnoreCase(channel)
                || !PluginLoader.INSTANCE.getConfigManager().isHookGeyserBungee()) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String playerName = in.readUTF();
            Player bukkitPlayer = Bukkit.getPlayer(playerName);
            if (bukkitPlayer == null) {
                return;
            }
            final PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(bukkitPlayer);
            if (data == null) {
                return;
            }

            data.setBypass(true);
            PluginLoader.INSTANCE.setGeyserManager(new GeyserManager(data));
            PluginLoader.INSTANCE.getGeyserManager().addExemptUser(data.getUser());
            PluginLoader.INSTANCE.setGeyserManager(null);
            bedRockUser.add(bukkitPlayer.getUniqueId());
        } catch (IOException ignored) {
        }
    }
}
