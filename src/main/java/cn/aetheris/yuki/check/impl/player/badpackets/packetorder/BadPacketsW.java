package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

import java.util.ArrayDeque;

@CheckData(name = "BadPacketsW", type = CheckType.BADPACKETS, configName = "BadPacketsW", decay = 0.75)
public final class BadPacketsW extends Check implements PostPredictionCheck {

    private final ArrayDeque<String> flags = new ArrayDeque<>();

    public BadPacketsW(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING || (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS
                && new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT)) {
            DiggingAction action = null;
            if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                action = new WrapperPlayClientPlayerDigging(event).getAction();
                if (action == DiggingAction.RELEASE_USE_ITEM
                        || action == DiggingAction.START_DIGGING
                        || action == DiggingAction.CANCELLED_DIGGING
                        || action == DiggingAction.FINISHED_DIGGING
                ) return;
            }

            if (player.packetActionProcessor.isAttacking()
                    || player.packetActionProcessor.isReleasing()
                    || player.packetActionProcessor.isRightClicking()
                    || player.packetActionProcessor.isPicking()
                    || player.packetActionProcessor.isDigging()
            ) {
                String verbose = "action= " + (action == null ? "openInventory" : action == DiggingAction.SWAP_ITEM_WITH_OFFHAND ? "swap" : "drop")
                        + "\nattacking= " + player.packetActionProcessor.isAttacking()
                        + "\nreleasing= " + player.packetActionProcessor.isReleasing()
                        + "\nrightClicking= " + player.packetActionProcessor.isRightClicking()
                        + "\npicking= " + player.packetActionProcessor.isPicking()
                        + "\ndigging= " + player.packetActionProcessor.isDigging();
                if (!player.canSkipTicksPreVia()) {
                    if (flagAndAlert(verbose)) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                        player.getUser().closeInventory();
                        player.mitigateDamage();
                    }
                } else {
                    flags.add(verbose);
                }
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicksPreVia()) return;

        if (player.isTickingReliablyFor(3)) {
            for (String verbose : flags) {
                flagAndAlert(verbose);
            }
        }

        flags.clear();
    }
}
