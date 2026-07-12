package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.functionality.AbstractHook;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PacketEventsHook extends AbstractHook {

    
    private static final String[][] CONFLICT_PLUGINS = {
            {"Matrix", "检查到您使用了 Matrix 这可能会造反作弊的异常!"},
            {"Vulcan", "检查到您使用了 Vulcan 这可能会造反作弊的异常!"},
            {"LightAntiCheat", "检查到您使用了 LightAntiCheat 这可能会造反作弊的异常!"},
            {"EasyAntiCheat", "检查到您使用了 EasyAntiCheat 这可能会造反作弊的异常!"},
            {"Verus", "检查到您使用了 Verus 这可能会造反作弊的异常!"},
            {"GrimAC", "检查到您使用了 GrimAC 这可能会造反作弊的异常!"},
            {"PolarLoader", "检查到您使用了 Polar 这可能会造反作弊的异常!"},
            {"AxiomPaper", "检查到您使用了 AxiomPaper 这可能会造反作弊的异常!"},
            {"ItemAdder", "检查到您使用了 ItemAdder 这可能会造反作弊的异常!"},
            {"SpiterLoader", "检查到您使用了 Spiter 这可能会造反作弊的异常!"},
            {"Medusa", "检查到您使用了 Medusa 这可能会造反作弊的异常!"},
            {"EasyAntiCheat", "检查到您使用了 EasyAntiCheat 这可能会造反作弊的异常!"},
            {"ThePitPremium", "检查到您使用了 ThePitPremium 这可能会造反作弊的异常!"},
            {"ThePitUltimate", "检查到您使用了 ThePitUltimate 这可能会造反作弊的异常!"}
    };

    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    
    @Override
    public void hook() {
        preloadClasses();
        checkConflictPlugins();
        super.enabled = true;

        final String type = PluginLoader.INSTANCE.getConfigManager().getConfig().getString("function.click-listener.mode");
        if (type.isEmpty()) {
            return;
        }
        if (type.equalsIgnoreCase("bukkit") && Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_17)) {
            LogUtils.console("&3Yuki &8» &c检查到您的服务器低于 &b1.17 &c而您使用了 &bBukkit &c的动作收包 这很可能造成一些错误或者误判!!!");
        }
        if (type.equalsIgnoreCase("packet") && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
            LogUtils.console("&3Yuki &8» &c检查到您的服务器高于 &b1.17 &c而您使用了 &bPacket &c的动作收包 这很可能造成一些错误或者误判!!!");
        }
    }

    
    private void preloadClasses() {
        Executors.defaultThreadFactory().newThread(() -> {
            StateTypes.AIR.getName();
            ItemTypes.AIR.getName();
            EntityTypes.PLAYER.getParent();
            EntityDataTypes.BOOLEAN.getName();
            ChatTypes.CHAT.getName();
            EnchantmentTypes.ALL_DAMAGE_PROTECTION.getName();
            ParticleTypes.DUST.getName();
        }).start();
    }

    
    private void checkConflictPlugins() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (String[] pluginInfo : CONFLICT_PLUGINS) {
            Plugin plugin = pluginManager.getPlugin(pluginInfo[0]);
            if (plugin != null && plugin.isEnabled()) {
                LogUtils.console("&3Yuki &8» &c" + pluginInfo[1]);
            }
        }
    }

    
    @Override
    public void unhook() {
        LogUtils.console("&3Yuki &8» &fTerminate PacketEvent...");


        if (Bukkit.getOnlinePlayers().isEmpty()) {
            terminatePacketEvents();


            if (PacketEvents.getAPI().isTerminated()) {
                executor.shutdownNow();
                super.enabled = false;
            }
        }
    }

    
    private void kickAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(player ->
                player.kickPlayer("服务器维护中，请稍后重连")
        );
    }

    
    private void terminatePacketEvents() {
        PacketEvents.getAPI().terminate();

    }



























































































    public User getUser(Player player) {
        if (player == null) {
            return null;
        }
        if (!isEnabled()) {
            return null;
        }
        return PacketEvents.getAPI().getPlayerManager().getUser(player);
    }

    
    public void sendPacket(User user, PacketWrapper<?> packet) {
        if (isEnabled()) {
            user.sendPacket(packet);
        }
    }

    
    public void sendPacket(Player player, PacketWrapper<?> packet) {
        if (isEnabled()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
    }


    
    public void sendPacketSilently(User user, PacketWrapper<?> packet) {
        if (isEnabled()) {
            user.sendPacketSilently(packet);
        }
    }

    
    public void sendPacketSilently(Player player, PacketWrapper<?> packet) {
        if (isEnabled()) {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        }
    }
}
