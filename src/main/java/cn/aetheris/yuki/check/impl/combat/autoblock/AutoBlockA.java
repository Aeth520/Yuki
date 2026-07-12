package cn.aetheris.yuki.check.impl.combat.autoblock;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "AutoBlockA (Interact)", description = "Interact when Blocking", configName = "AutoBlockA", type = CheckType.AUTOBLOCK, setback = 3)
public final class AutoBlockA extends Check implements PacketCheck {

    int lastInteractEntity;

    public AutoBlockA(PlayerData player) {
        super(player);
        lastInteractEntity = -1;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Client.INTERACT_ENTITY)) {

            if (isExempt(ExemptType.CLIENT_ANTICHEAT, ExemptType.CLIENT_VERSION) || player.isCouldSkipTick()) return;

            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.INTERACT)) {
                lastInteractEntity = wrapper.getEntityId();
            }
            
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (player.packetStateData.isSlowedByUsingItem()
                        && player.packetStateData.lastSlotSelected
                        == player.packetStateData.getSlowedByUsingItemSlot()) {

                    if (wrapper.getEntityId() != lastInteractEntity) {
                        if (buffer++ > 4) {
                            if (flagAndAlert()) {
                                resetPlayerUseItem(player.bukkitPlayer);
                                player.onPacketCancel();
                                player.mitigateDamage();
                            }
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
            lastInteractEntity = -1;
        }
    }
}