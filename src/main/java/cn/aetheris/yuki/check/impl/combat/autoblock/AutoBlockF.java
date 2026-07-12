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

@CheckData(name = "AutoBlockF (DIG)",
        configName = "AutoBlockE",
        description = "Attacked while sending BlockDig packet",
        type = CheckType.AUTOBLOCK,
        decay = 0.75
)
public final class AutoBlockF extends Check implements PacketCheck {
    public AutoBlockF(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Client.INTERACT_ENTITY)) {

            if (isExempt(ExemptType.CLIENT_ANTICHEAT, ExemptType.CLIENT_VERSION) || player.isCouldSkipTick()) return;

            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (player.packetStateData.isSlowedByUsingItem()
                        && player.packetStateData.lastSlotSelected
                        == player.packetStateData.getSlowedByUsingItemSlot()) {
                    if (buffer++ > 4) {
                        if (flagAndAlert("")) {
                            player.mitigateDamage();
                            rewardBufferAndVL();
                            resetPlayerUseItem(player.bukkitPlayer);
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        }
    }
}