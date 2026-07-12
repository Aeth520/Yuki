package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSelectBundleItem;

@CheckData(name = "CrashL (Bundle)", type = CheckType.CRASH, configName = "CrashL", description = "Bundle crash")
public class CrashL extends Check implements PacketCheck {

    public CrashL(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.SELECT_BUNDLE_ITEM) {
            return;
        }

        if (Yuki.getInstance().getPacketEventsManager()
                .getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_17)) {
            return;
        }

        final WrapperPlayClientSelectBundleItem selectBundleItem = new WrapperPlayClientSelectBundleItem(event);
        final int selIndex = selectBundleItem.getSelectedItemIndex();

        if (selIndex >= -1) {
            return;
        }

        if (flagAndAlert("s= " + selectBundleItem.getSelectedItemIndex())) {
            event.setCancelled(true);
            player.getSetbackTeleportUtil().executeNonSimulatingSetback();
            kickPlayer();
        }
    }
}
