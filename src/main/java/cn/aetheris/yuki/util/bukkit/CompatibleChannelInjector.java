package cn.aetheris.yuki.util.bukkit;

import cn.aetheris.yuki.PluginLoader;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.injector.SpigotChannelInjector;
import org.bukkit.entity.Player;

public class CompatibleChannelInjector extends SpigotChannelInjector {
    @Override
    public void updatePlayer(User user, Object player) {
        try {
            super.updatePlayer(user, player);
        } catch (NullPointerException e) {
            if (player instanceof Player) {
                ((Player) player).kickPlayer(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("kick.loading"));
            } else {
                throw e;
            }
        }
    }
}
