package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;

@CheckData(name = "ImpossibleK (TabComplete)", configName = "ImpossibleK", description = "Invalid Impossible Text", type = CheckType.IMPOSSIBLE)
public final class ImpossibleK extends Check implements PacketCheck {
    public ImpossibleK(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete tabComplete = new WrapperPlayClientTabComplete(event);
            if (tabComplete.getText() == null) {
                if (flagAndAlert()) {
                    event.setCancelled(true);
                    player.getSetbackTeleportUtil().executeNonSimulatingSetback();
                    kickPlayer();
                }
            }
        }
    }
}
