package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.ColorUtils;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.OfflinePlayer;

public final class LangManger {

    public String format(String string) {
        return ColorUtils.color(string.replace("%prefix%",
                PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("anticheat-prefix", "&3Yuki &8» &f")));
    }

    public String i18n(String string) {
        String config = PluginLoader.INSTANCE.getConfigManager().getConfig().getString(string);
        return config != null ? format("%prefix%" + config) : string;
    }

    public String i18nWithoutPrefix(String string) {
        String config = PluginLoader.INSTANCE.getConfigManager().getConfig().getString(string);
        return config != null ? format(config) : string;
    }

    public String i18n(OfflinePlayer player, String string) {
        String playerName = (player != null && player.getName() != null) ? player.getName() : "Nulled";
        return i18n(string).replace("%player%", playerName);
    }

    public String i18n(PlayerData data, String string) {
        PluginLoader.INSTANCE.getExternalAPI().replaceVariables(data, string, true);
        return i18n(string).replace("%data%", data.getName());
    }


    public String toUnlabledString(Vector3i vec) {
        return vec == null ? "null" : vec.x + ", " + vec.y + ", " + vec.z;
    }

    public String toUnlabledString(Vector3f vec) {
        return vec == null ? "null" : vec.x + ", " + vec.y + ", " + vec.z;
    }
}
