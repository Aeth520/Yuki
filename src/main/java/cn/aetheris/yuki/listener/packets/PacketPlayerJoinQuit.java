package cn.aetheris.yuki.listener.packets;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.combat.analysis.AnalysisA;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.functionality.PlayerDataManager;
import cn.aetheris.yuki.core.plugin.hooks.ViaPipelineEnforcer;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserConnectEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PacketPlayerJoinQuit extends AbstractPacketListener {


    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
            User user = event.getUser();
            LogUtils.console("&3Yuki &8» &aRegistering user &b" + user.getName() +
                    " &7(" + user.getClientVersion().getReleaseName() + ") &awith uuid " + user.getUUID());
            event.getTasksAfterSend().add(() -> PluginLoader.INSTANCE.getPlayerDataManager().addUser(user));
            MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {
                PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(user);
                if (data != null) {
                    final Player player = event.getPlayer();
                    if (data.gamemode == null) {
                        if (player == null) {
                            data.gamemode = GameMode.SURVIVAL;
                        } else {
                            data.gamemode = SpigotConversionUtil.fromBukkitGameMode(player.getGameMode());
                        }
                    }
                    if (player != null) {
                        ViaPipelineEnforcer.enforce(player);
                    }
                }
            }, 10L);

        }
    }


    @Override
    public void onUserConnect(UserConnectEvent event) {
        if (event.getUser().getConnectionState() == ConnectionState.PLAY
                && !PluginLoader.INSTANCE.getPlayerDataManager().exemptUsers.contains(event.getUser())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        User user = event.getUser();
        MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {

            PlayerDataManager playerDataManager = PluginLoader.INSTANCE.getPlayerDataManager();
            playerDataManager.remove(user);

            if (user.getProfile().getUUID() == null) return; 

            final Player player = Bukkit.getPlayer(user.getProfile().getUUID());
            if (player != null) {
                PluginLoader.INSTANCE.getAlertManager().handlePlayerQuit(player.getUniqueId());
                PluginLoader.INSTANCE.getSpectateManager().onQuit(player.getUniqueId());
                AnalysisA.DEBUG_PLAYERS.remove(player.getName());
                LogUtils.console("&3Yuki &8» &fHandling disconnection for " + user.getName());
            }
        }, 5L);
    }
}
