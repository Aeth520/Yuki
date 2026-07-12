package cn.aetheris.yuki.check.impl.player.pingspoof;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;

@CheckData(name = "PingSpoofA (Duplicate)",
        configName = "PingSpoofA",
        description = "Invalid KeepAlive",
        type = CheckType.PINGSPOOF,
        decay = 0.26,
        setback = 1)
public final class PingSpoofA extends Check implements PacketCheck {

    public PingSpoofA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event);

            if (keepAlive.getId() == 10000) {
                if (flagAndAlert()) {
                    setbackIfAboveSetbackVL();
                    player.onPacketCancel();
                    event.setCancelled(true);
                    kickPlayer();
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}
