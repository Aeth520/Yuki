package cn.aetheris.yuki.check.impl.misc.packet;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.time.Watch;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

import java.util.concurrent.atomic.AtomicBoolean;

public final class PacketMitigation extends Check implements PacketCheck {
    private static final long TRANSACTION_TIMEOUT_MS = 500;
    private static final long SETBACK_COOLDOWN_MS = 150;
    private static final int MAX_BUFFERED_PACKETS = 3;

    private final Watch transactionTimer = new Watch();
    private final Watch lastSetBackTimer = new Watch();
    private final Watch fakeLagTimer = new Watch();
    private final Watch fakeLagDecayTimer = new Watch();

    private final AtomicBoolean isBufferingTransactions = new AtomicBoolean(false);
    private final AtomicBoolean shouldCancelActions = new AtomicBoolean(false);

    private int bufferedPacketCount = 0;
    private int fakeLagCount = 0;
    private int fakeLagDecisionBuffer = 0;

    private boolean enable;
    private boolean lagInventoryMitigate;
    private boolean fakeLagMitigate;
    private boolean delayInteractMitigate;


    public PacketMitigation(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!enable) {
            return;
        }
        if (isTransaction(event.getPacketType())) {
            handleTransactionPacket();
        } else if (isFlying(event.getPacketType())) {
            handleFlyingPacket(event);
        } else if (isActionPacket(event.getPacketType())) {
            handleActionPacket(event);
        } else if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            handleWindowClickPacket(event);
        }
    }

    private void handleWindowClickPacket(PacketReceiveEvent event) {
        if (lagInventoryMitigate && (time() - player.getTranDelay()) >= 120) {
            event.setCancelled(true);
            LogUtils.mitigate("&b" + player.getName() + "&7 has been cancel inventory action (&b" + (time() - player.getTranDelay()) + "&7)");
        }
    }

    private void handleTransactionPacket() {
        transactionTimer.reset();

        if (isBufferingTransactions.get()) {
            bufferedPacketCount++;

            if (bufferedPacketCount > MAX_BUFFERED_PACKETS) {
                shouldCancelActions.set(delayInteractMitigate);
            }
        }
    }

    private void handleFlyingPacket(PacketReceiveEvent event) {
        boolean decay = false;
        if (!fakeLagTimer.hasTimeElapsed(20)) {
            fakeLagCount++;
            if (fakeLagCount >= 2 && fakeLagMitigate) {
                fakeLagCount = 0;
                fakeLagDecisionBuffer++;
                if (fakeLagDecisionBuffer > 7) {
                    
                    executeSetback();
                    LogUtils.setback("&b " + player.getName() + "&7 has been setback for timer-range &7(c=&b" + fakeLagCount + "&7)");
                }
            } else {
                decay = true;
            }
        } else {
            decay = true;
            fakeLagCount = 0;
        }
        if (decay) {
            if (fakeLagDecayTimer.hasTimeElapsed(1000)) {
                fakeLagDecisionBuffer = Math.max(fakeLagDecisionBuffer - 1, 0);
                fakeLagDecayTimer.reset();
            }
        }
        fakeLagTimer.reset();
        if (shouldCancelActions.get()) {

            if (lastSetBackTimer.hasTimeElapsed(SETBACK_COOLDOWN_MS)) {
                LogUtils.setback("&b " + player.getName() + "&7 has been setback for delay packet &7(t=&b" + lastSetBackTimer.getTime() + "&7)");
                executeSetback();
            }
            lastSetBackTimer.reset();

        }

        if (transactionTimer.hasTimeElapsed(TRANSACTION_TIMEOUT_MS)) {
            isBufferingTransactions.set(true);
        } else {
            isBufferingTransactions.set(false);
            shouldCancelActions.set(false);
        }
    }

    private void handleActionPacket(PacketReceiveEvent event) {
        if (shouldCancelActions.get()) {
            event.setCancelled(true);
        }
    }

    private void executeSetback() {
        player.getSetbackTeleportUtil().executeForceResync();
    }

    private boolean isActionPacket(PacketTypeCommon packetType) {
        return packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT ||
                packetType == PacketType.Play.Client.USE_ITEM ||
                packetType == PacketType.Play.Client.INTERACT_ENTITY;
    }

    @Override
    public void reload() {
        super.reload();
        enable = getConfig().getBoolean("mitigates.lag-spoof.enable");
        fakeLagMitigate = getConfig().getBoolean("mitigates.lag-spoof.fake-lag");
        lagInventoryMitigate = getConfig().getBoolean("mitigates.lag-spoof.inventory");
        delayInteractMitigate = getConfig().getBoolean("mitigates.lag-spoof.delay-use-item");
    }
}