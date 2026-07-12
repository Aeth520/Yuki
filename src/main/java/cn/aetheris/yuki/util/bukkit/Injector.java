package cn.aetheris.yuki.util.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import org.bukkit.plugin.java.JavaPlugin;

public class Injector {
    public static ForkedSpigotPacketEventsAPI ensureCompatibilityForLoading(JavaPlugin plugin, PacketEventsSettings settings) {
        ForkedSpigotPacketEventsAPI api = new ForkedSpigotPacketEventsAPI(settings, plugin);
        PacketEvents.setAPI(api);
        return api;

    }
}
