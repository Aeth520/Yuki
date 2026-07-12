package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.check.util.handler.PayloadHandler;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientPluginMessage;

public final class PacketConfigurationListener extends AbstractPacketListener {

    public PacketConfigurationListener() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Configuration.Client.PLUGIN_MESSAGE) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            WrapperConfigClientPluginMessage wrapper = new WrapperConfigClientPluginMessage(event);
            String channelName = wrapper.getChannelName();
            byte[] data = wrapper.getData();
            if (channelName.equalsIgnoreCase("minecraft:brand") || channelName.equals("MC|Brand")) {
                player.checkManager.getCheck(PayloadHandler.class).handle(channelName, data);
            }
        }
    }
}