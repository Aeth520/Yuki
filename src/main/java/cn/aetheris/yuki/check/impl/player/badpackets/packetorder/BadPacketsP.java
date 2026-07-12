package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTickingState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "BadPacketsP (Zero)", type = CheckType.BADPACKETS, configName = "BadPacketsP", decay = 0.65, description = "Check for blink", experimental = true)
public final class BadPacketsP extends Check implements PostPredictionCheck {
    private final Deque<Short> transactionsToConsider = new ArrayDeque<>();
    private long nanosPerTick = (long) 50e6;
    private long lastTransSent = 0;
    private long lastTransReceived = 0;
    private int ticksSinceMovement = 0;

    public BadPacketsP(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        long currentTime = System.nanoTime();
        short id = getTransactionID(event);

        if (id <= 0 && currentTime - lastTransSent >= nanosPerTick) {
            lastTransSent = currentTime;
            transactionsToConsider.add(id);
            if (transactionsToConsider.size() > 20) transactionsToConsider.removeFirst();
        }
        if (event.getPacketType() == PacketType.Play.Server.TICKING_STATE) {
            WrapperPlayServerTickingState tickingState = new WrapperPlayServerTickingState(event);
            nanosPerTick = (long) (1e9 / Math.min(20, tickingState.getTickRate()));
        }
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        long currentTime = System.nanoTime();
        short id = getTransactionID(event);

        if (transactionsToConsider.contains(id)) {
            transactionsToConsider.remove(id);
            
            
            if (currentTime - lastTransReceived >= nanosPerTick) {
                ticksSinceMovement++;
                lastTransReceived = currentTime;
            }
        }
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {

        if (player.skippedTickInActualMovement || player.likelyKB != null) {
            ticksSinceMovement -= 19;
        }

        if (isExempt(ExemptType.INVALID_GAMEMODE, ExemptType.INVALID_MOVEMENT)) return;

        if (ticksSinceMovement > 1) {
            if (buffer++ > 4) {
                if (flagAndAlert("moveShit= " + (ticksSinceMovement - 1))) {
                    player.mitigateDamage();
                }
            }
        } else {
            rewardBufferAndVL();
        }

        rewardBufferAndVL();
        ticksSinceMovement = 0;
    }

    public short getTransactionID(PacketReceiveEvent event) {
        short id = 1; 

        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation transaction = new WrapperPlayClientWindowConfirmation(event);
            id = transaction.getActionId();
        }

        if (event.getPacketType() == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong pong = new WrapperPlayClientPong(event);

            int longID = pong.getId();
            if (longID == (short) longID) {
                id = (short) longID;
            }
        }

        return id;
    }

    public short getTransactionID(PacketSendEvent event) {
        short id = 1;

        if (event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            WrapperPlayServerWindowConfirmation transaction = new WrapperPlayServerWindowConfirmation(event);
            id = transaction.getActionId();
        }

        if (event.getPacketType() == PacketType.Play.Server.PING) {
            WrapperPlayServerPing pong = new WrapperPlayServerPing(event);

            int longID = pong.getId();
            if (longID == (short) longID) {
                id = (short) longID;
            }
        }

        return id;
    }
}