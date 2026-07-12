package cn.aetheris.yuki.check.impl.combat.autoblock.prediction;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "AutoBlock4 (Invalid)", description = "Block Order", configName = "AutoBlock4", type = CheckType.AUTOBLOCK, setback = 6, decay = 0.35, experimental = true)
public final class AutoBlock4 extends Check implements PostPredictionCheck {

    boolean sentPlacement;

    public AutoBlock4(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (player.getClientVersion().isNewerThan(ClientVersion.V_1_8) && (!player.skippedTickInActualMovement || !player.isTickingReliablyFor(3))) {
            sentPlacement = false;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (WrapperPlayClientPlayerFlying.isFlying(packetType)) {
            sentPlacement = false;
            return;
        }

        if (isExempt(ExemptType.CLIENT_ANTICHEAT)) {
            sentPlacement = false;
            return;
        }

        if (packetType != PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT && packetType != PacketType.Play.Client.PLAYER_DIGGING) {
            return;
        }

        if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            sentPlacement = true;
        } else if (sentPlacement) {
            DiggingAction action = new WrapperPlayClientPlayerDigging(event).getAction();
            if (action == DiggingAction.DROP_ITEM || action == DiggingAction.DROP_ITEM_STACK) {
                if (isExempt(ExemptType.CLIENT_ANTICHEAT, ExemptType.INVALID_GAMEMODE)) return;
                if (flagAndAlert()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }
}