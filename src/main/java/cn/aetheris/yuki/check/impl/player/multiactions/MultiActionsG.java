package cn.aetheris.yuki.check.impl.player.multiactions;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "MultiActionsG", type = CheckType.MULTIACTIONS, configName = "MultiActionsG", description = "Switch + Use same tick")
public final class MultiActionsG extends Check implements PacketCheck {

    private long tickCount = 0;
    private long lastSwitchTick = -1;
    private long lastUseTick = -1;

    public MultiActionsG(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            tickCount++;
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            if (lastUseTick == tickCount) {
                flagAndAlert("switch+use");
            }
            lastSwitchTick = tickCount;
        } else if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            if (lastSwitchTick == tickCount) {
                flagAndAlert("switch+use");
            }
            lastUseTick = tickCount;
        }
    }
}
