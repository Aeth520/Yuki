package cn.aetheris.yuki.util.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.injector.ChannelInjector;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.netty.NettyManager;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import com.github.retrooper.packetevents.util.LogManager;
import io.github.retrooper.packetevents.bukkit.InternalBukkitListener;
import io.github.retrooper.packetevents.injector.SpigotChannelInjector;
import io.github.retrooper.packetevents.injector.connection.ServerConnectionInitializer;
import io.github.retrooper.packetevents.manager.InternalBukkitPacketListener;
import io.github.retrooper.packetevents.manager.player.PlayerManagerImpl;
import io.github.retrooper.packetevents.manager.protocol.ProtocolManagerImpl;
import io.github.retrooper.packetevents.manager.server.ServerManagerImpl;
import io.github.retrooper.packetevents.netty.NettyManagerImpl;
import io.github.retrooper.packetevents.util.BukkitLogManager;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.protocolsupport.ProtocolSupportUtil;
import io.github.retrooper.packetevents.util.viaversion.CustomPipelineUtil;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ForkedSpigotPacketEventsAPI extends PacketEventsAPI {
    @Getter
    private final PacketEventsSettings settings;
    @Getter
    private final Plugin plugin;
    @Getter
    private final ProtocolManager protocolManager = new ProtocolManagerImpl();
    @Getter
    private final ServerManager serverManager = new ServerManagerImpl();
    @Getter
    private final PlayerManager playerManager = new PlayerManagerImpl();
    @Getter
    private final NettyManager nettyManager = new NettyManagerImpl();
    private final SpigotChannelInjector injector = new CompatibleChannelInjector();
    @Getter
    private final LogManager logManager = new BukkitLogManager(this);
    @Getter
    private boolean loaded;
    @Getter
    private boolean initialized;
    private boolean lateBind = false;
    @Getter
    private boolean terminated = false;

    public ForkedSpigotPacketEventsAPI(PacketEventsSettings settings, JavaPlugin plugin) {
        this.settings = settings;
        this.plugin = plugin;
    }

    public void load() {
        if (!this.loaded) {
            String id = plugin.getName().toLowerCase();
            PacketEvents.IDENTIFIER = "pe-" + id;
            PacketEvents.ENCODER_NAME = "pe-encoder-" + id;
            PacketEvents.DECODER_NAME = "pe-decoder-" + id;
            PacketEvents.CONNECTION_HANDLER_NAME = "pe-connection-handler-" + id;
            PacketEvents.SERVER_CHANNEL_HANDLER_NAME = "pe-connection-initializer-" + id;
            PacketEvents.TIMEOUT_HANDLER_NAME = "pe-timeout-handler-" + id;

            try {
                SpigotReflectionUtil.init();
                CustomPipelineUtil.init();
                WrappedBlockState.ensureLoad();
            } catch (Exception var3) {
                throw new IllegalStateException(var3);
            }

            if (!PacketType.isPrepared()) {
                PacketType.prepare();
            }

            this.lateBind = !this.injector.isServerBound();
            if (!this.lateBind) {
                this.injector.inject();
            }

            this.loaded = true;
            getEventManager().registerListener(new InternalBukkitPacketListener());
        }

    }

    public void init() {
        this.load();
        if (!this.initialized) {
            Bukkit.getPluginManager().registerEvents(new InternalBukkitListener(plugin), plugin);
            if (this.lateBind) {
                Runnable lateBindTask = () -> {
                    if (this.injector.isServerBound()) {
                        this.injector.inject();
                    }

                };
                FoliaScheduler.runTaskOnInit(plugin, lateBindTask);
            }

            if (!"true".equalsIgnoreCase(System.getenv("PE_IGNORE_INCOMPATIBILITY"))) {
                this.checkCompatibility();
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                CompatibleChannelInjector injector = (CompatibleChannelInjector) PacketEvents.getAPI().getInjector();
                injector.updatePlayer(user, player);
            }

            this.initialized = true;
        }

    }

    private void checkCompatibility() {
        ViaVersionUtil.checkIfViaIsPresent();
        ProtocolSupportUtil.checkIfProtocolSupportIsPresent();
        Plugin viaPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
        int majorVersion;
        if (viaPlugin != null) {
            String[] ver = viaPlugin.getDescription().getVersion().split("\\.", 3);
            majorVersion = Integer.parseInt(ver[0]);
            int minor = Integer.parseInt(ver[1]);
            if (majorVersion < 4 || majorVersion == 4 && minor < 5) {
                PacketEvents.getAPI().getLogManager().severe("You are attempting to combine 2.0 PacketEvents with a ViaVersion older than 4.5.0, please update your ViaVersion!");
                Plugin ourPluginx = this.getPlugin();
                Bukkit.getPluginManager().disablePlugin(ourPluginx);
                throw new IllegalStateException("ViaVersion incompatibility! Update to v4.5.0 or newer!");
            }
        }

        Plugin protocolLibPlugin = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (protocolLibPlugin != null) {
            majorVersion = Integer.parseInt(protocolLibPlugin.getDescription().getVersion().split("\\.", 2)[0]);
            if (majorVersion < 5) {
                PacketEvents.getAPI().getLogManager().severe("You are attempting to combine 2.0 PacketEvents with a ProtocolLib version older than v5.0.0. This is no longer works, please update to their dev builds. https://ci.dmulloy2.net/job/ProtocolLib/lastBuild/");
                Plugin ourPlugin = this.getPlugin();
                Bukkit.getPluginManager().disablePlugin(ourPlugin);
                throw new IllegalStateException("ProtocolLib incompatibility! Update to v5.0.0 or newer!");
            }
        }

    }

    public void terminate() {
        if (this.initialized) {
            this.injector.uninject();

            for (User user : ProtocolManager.USERS.values()) {
                ServerConnectionInitializer.destroyHandlers(user.getChannel());
            }

            this.getEventManager().unregisterAllListeners();
            this.initialized = false;
            this.terminated = true;
        }

    }

    public ChannelInjector getInjector() {
        return this.injector;
    }

}
