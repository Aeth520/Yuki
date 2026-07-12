package cn.aetheris.yuki.check.impl.player.multiactions;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "MultiActionsD", type = CheckType.MULTIACTIONS, configName = "MultiActionsD", description = "Attack + Drop same tick")
public final class MultiActionsD extends Check implements PacketCheck {

    private long tickCount = 0;
    private long lastAttackTick = -1;
    private long lastDropTick = -1;

    public MultiActionsD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            tickCount++;
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (lastDropTick == tickCount) {
                flagAndAlert("attack+drop");
            }
            lastAttackTick = tickCount;
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            DiggingAction action = new WrapperPlayClientPlayerDigging(event).getAction();
            if (action == DiggingAction.DROP_ITEM || action == DiggingAction.DROP_ITEM_STACK) {
                if (lastAttackTick == tickCount) {
                    flagAndAlert("attack+drop");
                }
                lastDropTick = tickCount;
            }
        }
    }
}
