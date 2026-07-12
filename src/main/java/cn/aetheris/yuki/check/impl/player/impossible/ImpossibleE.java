package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(name = "ImpossibleE (DigFace)", configName = "ImpossibleE", description = "Invalid Digging", experimental = true, type = CheckType.IMPOSSIBLE)
public final class ImpossibleE extends Check implements PacketCheck {
    public ImpossibleE(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(event);

            boolean invalid = dig.getAction() == DiggingAction.RELEASE_USE_ITEM && dig.getBlockFace().getFaceValue() == 255;

            if (invalid) {
                if (flagAndAlert("F= " + dig.getBlockFace().getFaceValue())) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}