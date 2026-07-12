package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;

@CheckData(name = "InventoryC", description = "Open inventory while dig", setback = 8, decay = 0.15)
public final class InventoryC extends InventoryCheck {

    public InventoryC(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action != DiggingAction.START_DIGGING) return;

        if (player.hasInventoryOpen) {
            if (buffer++ > 3) {
                if (flagAndAlert()) {
                    player.onPacketCancel();
                    blockBreak.cancel();
                }
                closeInventory();
            }
        } else {
            rewardBufferAndVL();
        }
    }
}