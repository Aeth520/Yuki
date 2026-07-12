package cn.aetheris.yuki.check.impl.combat.autoblock;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "AutoBlockD (UseItem)",
        configName = "AutoBlockD",
        description = "When players attack entities while using item",
        type = CheckType.AUTOBLOCK,
        decay = 0.25
)
public final class AutoBlockD extends Check implements PacketCheck {

    long lastFlag;
    int tick;

    public AutoBlockD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.packetStateData.isSlowedByUsingItem()
                && (player.packetStateData.eatingHand == InteractionHand.OFF_HAND)
                && event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            if (new WrapperPlayClientInteractEntity(event).getAction()
                    == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                if (tick++ > 6) {

                    if (player.packetStateData.lastSlotSelected != player.packetStateData.getSlowedByUsingItemSlot()) {
                        tick = 0;
                        return;
                    }

                    if (time() - lastFlag < 500L || buffer++ < 10) {
                        return;
                    }

                    if (flagAndAlert("t= " + tick)) {
                        event.setCancelled(true);
                        resetPlayerUseItem(player.bukkitPlayer);
                        player.onPacketCancel();
                        lastFlag = time();
                    }
                }
            } else {
                rewardBufferAndVL();
                tick = 0;
            }
        }
    }
}