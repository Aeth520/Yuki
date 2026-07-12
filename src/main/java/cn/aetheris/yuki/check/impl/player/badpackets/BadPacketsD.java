package cn.aetheris.yuki.check.impl.player.badpackets;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;

@CheckData(name = "BadPacketsD (TAB)", type = CheckType.BADPACKETS, configName = "BadPacketsD", description = "HackClient TabComplete", decay = 0.5)
public final class BadPacketsD extends Check implements PacketCheck {
    public BadPacketsD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete tabComplete = new WrapperPlayClientTabComplete(event);
            if (tabComplete.getText().startsWith(".") && tabComplete.getText().contains(" ")) {
                if (flagAndAlert("T= " + tabComplete.getText())) {
                    player.mitigateDamage();
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        } else {
            rewardVL();
        }
    }
}