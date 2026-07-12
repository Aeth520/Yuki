package cn.aetheris.yuki.check.impl.player.pingspoof;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;

@CheckData(name = "PingSpoofB (Dupe C00ID)",
        configName = "PingSpoofB",
        type = CheckType.PINGSPOOF,
        description = "Invalid KeepAlive")
public final class PingSpoofB extends Check implements PacketCheck {

    long lastId = -1;

    public PingSpoofB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event);

            long id = keepAlive.getId();

            if (id == lastId || id == 0L) {
                if (flagAndAlert("ID= " + keepAlive.getId()) && shouldModifyPackets()) {
                    setbackIfAboveSetbackVL();
                    player.onPacketCancel();
                    event.setCancelled(true);
                    kickPlayer();
                }
            }
        }
    }
}

