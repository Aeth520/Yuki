package cn.aetheris.yuki.check.impl.player.multiactions;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "MultiActionsC", type = CheckType.MULTIACTIONS, configName = "MultiActionsC", description = "Use + Place same tick")
public final class MultiActionsC extends Check implements PacketCheck {

    private long tickCount = 0;
    private long lastUseTick = -1;
    private long lastPlaceTick = -1;

    public MultiActionsC(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            tickCount++;
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            if (lastPlaceTick == tickCount) {
                flagAndAlert("use+place");
            }
            lastUseTick = tickCount;
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            if (lastUseTick == tickCount) {
                flagAndAlert("use+place");
            }
            lastPlaceTick = tickCount;
        }
    }
}
