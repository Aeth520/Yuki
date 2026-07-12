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

@CheckData(name = "BadPacketsQ", type = CheckType.BADPACKETS, configName = "BadPacketsQ", decay = 1.0)
public final class BadPacketsQ extends Check implements PostPredictionCheck {

    private final ArrayDeque<String> flags = new ArrayDeque<>();

    public BadPacketsQ(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY
                || event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                || event.getPacketType() == PacketType.Play.Client.USE_ITEM
                || event.getPacketType() == PacketType.Play.Client.PICK_ITEM
                || event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING
                || (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS
                && new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT)
        ) if (player.packetActionProcessor.isSprinting()) {
            String verbose = "action= " + (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY ? "interact"
                    : event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT ? "place"
                    : event.getPacketType() == PacketType.Play.Client.USE_ITEM ? "use"
                    : event.getPacketType() == PacketType.Play.Client.PICK_ITEM ? "pick"
                    : event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING ? "dig"
                    : ",openInventory")
                    + "\nisSprinting= " + player.packetActionProcessor.isSprinting()
                    + "\nsneaking= " + player.packetActionProcessor.isSneaking();
            if (!player.canSkipTicksPreVia() && !player.isClientACUser()) {
                if (buffer++ > 5) {
                    if (flagAndAlert(verbose)) {
                        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING
                                && new WrapperPlayClientPlayerDigging(event).getAction() == DiggingAction.RELEASE_USE_ITEM
                        ) return; 

                        event.setCancelled(true);
                        player.mitigateDamage();
                        player.onPacketCancel();
                    }
                }
            } else {
                flags.add(verbose);
                rewardBufferAndVL();
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicksPreVia()) return;

        if (player.isTickingReliablyFor(3)) {
            for (String verbose : flags) {
                if (buffer++ > 5) {
                    if (flagAndAlert(verbose)) {
                        player.mitigateDamage();
                    }
                }
            }
        }

        flags.clear();
    }
}