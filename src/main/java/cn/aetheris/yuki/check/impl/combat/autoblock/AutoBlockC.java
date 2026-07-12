package cn.aetheris.yuki.check.impl.combat.autoblock;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;

@CheckData(name = "AutoBlockC", configName = "AutoBlockC", type = CheckType.AUTOBLOCK, description = "Invalid attack order.", decay = 0.15)
public final class AutoBlockC extends Check implements BlockBreakCheck {

    public AutoBlockC(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (player.packetStateData.isSlowedByUsingItem()
                && (player.packetStateData.lastSlotSelected
                == player.packetStateData.getSlowedByUsingItemSlot()
                || player.packetStateData.eatingHand == InteractionHand.OFF_HAND)) {
            
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)) {
                return;
            }

            if (flagAndAlert("action= " + blockBreak.action)) {
                blockBreak.cancel();
            }
        }
    }
}