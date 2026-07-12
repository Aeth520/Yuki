package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ExemptInit implements Init {

    @Override
    public void init() {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                if (user != null) {
                    PluginLoader.INSTANCE.getPlayerDataManager().exemptUsers.add(user);
                    LogUtils.consolePrefixed("&f" + player.getName() + " exempt " + user.getName() + " &7(Online Player?)");
                }
            }
        });
    }
}
