package cn.aetheris.yuki.check.impl.combat.autoblock.prediction;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

import java.util.ArrayList;
import java.util.List;

import static cn.aetheris.yuki.listener.packets.patch.ResyncWorldUtil.resyncPosition;

@CheckData(name = "AutoBlock3", configName = "AutoBlock3", type = CheckType.AUTOBLOCK, decay = 0.865, experimental = true)
public final class AutoBlock3 extends Check implements PostPredictionCheck {

    private final List<String> flags = new ArrayList<>();
    boolean entity, block;

    public AutoBlock3(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            if (packet.getAction() == DiggingAction.START_DIGGING || packet.getAction() == DiggingAction.FINISHED_DIGGING) {
                block = true;
                if (entity) {
                    if (!player.canSkipTicks()) {
                        if (buffer++ > 5) {
                            if (flagAndAlert("dig= true")) {
                                player.mitigateDamage();
                                player.onPacketCancel();
                                resyncPosition(player, packet.getBlockPosition());
                            }
                        }
                    } else {
                        flags.add("dig");
                        rewardBufferAndVL();
                    }
                }
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicks()) {

            if (!shouldModifyPackets()) return;

            if (player.isTickingReliablyFor(3)) {
                for (String verbose : flags) {
                    flagAndAlert(verbose);
                }
            }

            flags.clear();
        }
    }
}