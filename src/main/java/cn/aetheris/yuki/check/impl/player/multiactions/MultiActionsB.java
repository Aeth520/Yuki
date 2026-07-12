package cn.aetheris.yuki.check.impl.player.multiactions;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "MultiActionsB", type = CheckType.MULTIACTIONS, configName = "MultiActionsB", description = "Attack + Switch same tick")
public final class MultiActionsB extends Check implements PacketCheck {

    private long tickCount = 0;
    private long lastAttackTick = -1;
    private long lastSwitchTick = -1;

    public MultiActionsB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            tickCount++;
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (lastSwitchTick == tickCount) {
                flagAndAlert("attack+switch");
            }
            lastAttackTick = tickCount;
        } else if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            if (lastAttackTick == tickCount) {
                flagAndAlert("attack+switch");
            }
            lastSwitchTick = tickCount;
        }
    }
}
