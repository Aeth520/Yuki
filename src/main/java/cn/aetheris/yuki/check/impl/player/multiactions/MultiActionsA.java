package cn.aetheris.yuki.check.impl.player.multiactions;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "MultiActionsA", type = CheckType.MULTIACTIONS, configName = "MultiActionsA", description = "Attack + Use same tick")
public final class MultiActionsA extends Check implements PacketCheck {

    private long tickCount = 0;
    private long lastAttackTick = -1;
    private long lastUseTick = -1;

    public MultiActionsA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            tickCount++;
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (lastUseTick == tickCount) {
                flagAndAlert("attack+use");
            }
            lastAttackTick = tickCount;
        } else if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            if (lastAttackTick == tickCount) {
                flagAndAlert("attack+use");
            }
            lastUseTick = tickCount;
        }
    }
}
