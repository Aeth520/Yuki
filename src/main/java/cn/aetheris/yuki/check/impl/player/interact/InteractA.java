package cn.aetheris.yuki.check.impl.player.interact;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;

import static cn.aetheris.yuki.listener.packets.patch.ResyncWorldUtil.resyncPosition;

@CheckData(name = "InteractA (Multi)", configName = "InteractA", description = "Invalid Dig Action", decay = 0.55, type = CheckType.INTERACT)
public final class InteractA extends Check implements BlockBreakCheck {

    double maxBuffer;
    long lastFlag;

    public InteractA(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (player.packetStateData.isSlowedByUsingItem()
                && (player.packetStateData.lastSlotSelected
                == player.packetStateData.getSlowedByUsingItemSlot()
                || player.packetStateData.eatingHand == InteractionHand.OFF_HAND)) {

            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)) return;

            if (player.packetStateData.getSlowedByUsingItemSlot() != player.getLastServerChangeSlot()) {
                return;
            }

            if (blockBreak.action == DiggingAction.START_DIGGING
                    || blockBreak.action == DiggingAction.CANCELLED_DIGGING
                    || blockBreak.action == DiggingAction.FINISHED_DIGGING) {








                if (time() - lastFlag < 500L) {
                    return;
                }

                if (buffer++ > maxBuffer) {
                    if (flagAndAlert("type= " + blockBreak.action)) {
                        blockBreak.cancel();
                        player.onPacketCancel();
                        resyncPosition(player, blockBreak.position);
                        buffer = 0;
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }

    @Override
    public void reload() {
        super.reload();
        maxBuffer = getConfig().getDoubleElse("Interacts.buffer.a", 6);
    }
}