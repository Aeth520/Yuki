package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.listener.packets.*;
import cn.aetheris.yuki.listener.packets.worldreader.BasePacketWorldReader;
import cn.aetheris.yuki.listener.packets.worldreader.PacketWorldReaderEight;
import cn.aetheris.yuki.listener.packets.worldreader.PacketWorldReaderEighteen;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

import java.util.Arrays;
import java.util.List;

public final class PacketListenerInit implements Init {

    private final Yuki plugin = Yuki.getInstance();

    @Override
    public void init() {
        final ServerVersion serverVersion = plugin.getPacketEventsManager().getServerVersion();

        final List<PacketListenerCommon> listeners = Arrays.asList(
                new PreViaCheckManagerListener(),
                new PacketConfigurationListener(),
                new PacketPlayerJoinQuit(),
                new PacketPingListener(),
                new PacketPlayerDigging(),
                new PacketPlayerAttack(),
                new PacketEntityAction(),
                new PacketBlockAction(),
                new PacketSelfMetadataListener(),
                new PacketServerTeleport(),
                new PacketPlayerCooldown(),
                new PacketPlayerRespawn(),
                new CheckManagerListener(),
                new PacketPlayerSteer(),
                new PacketPlayerWindow(),
                new PacketPlayerFlying(),
                new PacketPlayerVehicleMovement(),
                new PacketPlayerTabComplete(),
                new PacketHidePlayerInfo()
        );

        listeners.forEach(listener -> PacketEvents.getAPI().getEventManager().registerListener(listener));

        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
            PacketEvents.getAPI().getEventManager().registerListener(new PacketServerTags());
        }
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            PacketEvents.getAPI().getEventManager().registerListener(new PacketPlayerUseTotemListener());
        }
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            PacketEvents.getAPI().getEventManager().registerListener(new PacketWorldReaderEighteen());
        } else if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
            PacketEvents.getAPI().getEventManager().registerListener(new PacketWorldReaderEight());
        } else {
            PacketEvents.getAPI().getEventManager().registerListener(new BasePacketWorldReader());
        }

        LogUtils.console("&3Yuki &8» &aPacketManager Initialized!");
    }
}
