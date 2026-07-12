package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;

import java.util.ArrayDeque;

@CheckData(name = "BadPacketsE", type = CheckType.BADPACKETS, configName = "BadPacketsE", decay = 0.75)
public final class BadPacketsE extends Check implements PostPredictionCheck {

    private final ArrayDeque<String> flags = new ArrayDeque<>();


    public BadPacketsE(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            if (new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT) {
                if (player.packetActionProcessor.isClickingInInventory()
                        || player.packetActionProcessor.isClosingInventory()) {
                    String verbose = "(Opening)\nclicking=" + player.packetActionProcessor.isClickingInInventory() + "\nclosing=" + player.packetActionProcessor.isClosingInventory();
                    if (!player.canSkipTicksPreVia()) {
                        flagAndAlert(verbose);
                    } else {
                        flags.add(verbose);
                    }
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW
                || event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            if (player.packetActionProcessor.isOpeningInventory()) {
                String verbose = event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW ? "click" : "close";
                if (!player.canSkipTicksPreVia()) {
                    if (flagAndAlert(verbose)) {
                        event.setCancelled(true);
                        player.onPacketCancel();
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