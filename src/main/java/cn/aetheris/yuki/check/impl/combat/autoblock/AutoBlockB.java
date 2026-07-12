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

@CheckData(name = "AutoBlockB (MultiActions)", type = CheckType.AUTOBLOCK, description = "MultiAction when UseItem", configName = "AutoBlockB", setback = 5)
public final class AutoBlockB extends Check implements PacketCheck {

    long lastUseItem;
    long useItem;

    public AutoBlockB(PlayerData player) {
        super(player);
        useItem = -1L;
        lastUseItem = -1L;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Client.INTERACT_ENTITY)) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (isExempt(ExemptType.CLIENT_VERSION)) { 
                return;
            }

            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (player.packetStateData.isSlowedByUsingItem()
                        && player.packetStateData.lastSlotSelected
                        == player.packetStateData.getSlowedByUsingItemSlot()) {
                    if (lastUseItem == -1L && useItem == -1L) { 
                        useItem = player.lastBlockPlaceUseItem;
                        lastUseItem = useItem;
                        return;
                    }

                    if (useItem == lastUseItem) {
                        if (buffer++ > 3) {
                            if (flagAndAlert("now= " + useItem + "\nlast= " + lastUseItem)) {
                                setbackIfAboveSetbackVL();
                                resetPlayerUseItem(player.bukkitPlayer);
                                player.onPacketCancel();
                                player.mitigateDamage();
                                buffer = 0;
                            }
                        }
                    } else {
                        rewardBufferAndVL();
                    }
                }

                lastUseItem = useItem;
                useItem = player.lastBlockPlaceUseItem;
            }
        }
    }
}