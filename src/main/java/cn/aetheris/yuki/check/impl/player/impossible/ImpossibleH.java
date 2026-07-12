package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;

import java.util.LinkedList;

@CheckData(name = "ImpossibleH (C00)", configName = "ImpossibleH", description = "Predict ID", type = CheckType.IMPOSSIBLE)
public final class ImpossibleH extends Check implements PacketCheck {
    private final LinkedList<Long> keepalives = new LinkedList<>();

    public ImpossibleH(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
            keepalives.add(new WrapperPlayServerKeepAlive(event).getId());
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            final long id = new WrapperPlayClientKeepAlive(event).getId();

            for (long keepalive : keepalives) {
                if (keepalive == id) {
                    Long data;
                    do {
                        data = keepalives.poll();
                    } while (data != null && data != id);

                    return;
                }
            }

            if (flagAndAlert("id= " + id)) {
                event.setCancelled(true);
                player.onPacketCancel();
            }
        }
    }
}