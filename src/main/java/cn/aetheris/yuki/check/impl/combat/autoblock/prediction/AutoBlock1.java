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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "AutoBlock1 (Placement)",
        configName = "AutoBlock1",
        type = CheckType.AUTOBLOCK,
        description = "Invalid Placement Order",
        decay = 0.35,
        setback = 7,
        experimental = true)
public final class AutoBlock1 extends Check implements PostPredictionCheck {

    boolean sentPlacement;

    public AutoBlock1(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (predictionComplete.isChecked()) {
            if (!player.isCouldSkipTick()) {
                sentPlacement = false;
            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (isTickPacket(packetType)) {
            sentPlacement = false;
            return;
        }

        if (isExempt(ExemptType.CLIENT_ANTICHEAT)) {
            sentPlacement = false;
            return;
        }

        if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            sentPlacement = true;
        } else if (packetType == PacketType.Play.Client.INTERACT_ENTITY) {
            if (isExempt(ExemptType.CLIENT_ANTICHEAT, ExemptType.INVALID_GAMEMODE)) return;
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (sentPlacement) {
                    if (buffer++ > 3.5) {
                        if (flagAndAlert()) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                            buffer = 0.0;
                        }
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}