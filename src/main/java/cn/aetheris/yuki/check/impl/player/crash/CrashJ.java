package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;

import java.util.Set;

@CheckData(name = "CrashJ", type = CheckType.CRASH, configName = "CrashJ", description = "sent invalid payload")
public final class CrashJ extends Check implements PacketCheck {

    private final Set<String> CHANNEL_NAMES = Set.of("MC|BOpen", "MC|BEdit", "MC|BSign");

    public CrashJ(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            WrapperPlayClientPluginMessage packet = new WrapperPlayClientPluginMessage(event);
            String channelName = packet.getChannelName();

            if (CHANNEL_NAMES.contains(channelName)) {
                buffer += 2;
                if (buffer > 4) {
                    if (flagAndAlert("c= " + channelName) && shouldModifyPackets()) {
                        kickPlayer();
                        player.onPacketCancel();
                        event.setCancelled(true);
                    }
                }
            } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
                buffer -= Math.min(buffer, 1);
            }
        }
    }
}