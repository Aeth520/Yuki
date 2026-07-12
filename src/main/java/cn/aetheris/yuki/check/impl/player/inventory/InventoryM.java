package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(
        name = "InventoryM (ChestStealer)",
        configName = "InventoryM",
        type = CheckType.INVENTORY,
        description = "Chest interaction anomaly detection",
        decay = 0.95,
        setback = 8)
public final class InventoryM extends InventoryCheck {

    private final Deque<Long> pickUPTimestamps = new ArrayDeque<>();

    private final Deque<Long> swapTimestamps = new ArrayDeque<>();
    private final Deque<Long> quickMoveTimestamps = new ArrayDeque<>();
    private final Deque<Long> throwTimestamps = new ArrayDeque<>();
    private long lastQuickMove;
    private double stdThreshold;
    private int quickMoveLimit;
    private int pickUPLimit;
    private int throwLimit;
    private double throwStdThreshold;
    private int lastSlot = -1;
    private int nowPickUPSlot = -1;
    private int sequentialOperations;
    private long lastOperationTime;
    private boolean mitigate;
    private boolean shouldMitigate;

    public InventoryM(PlayerData player) {
        super(player);
        mitigate = false;
        lastQuickMove = 1L;
    }

    @Override
    public void reload() {
        super.reload();
        this.stdThreshold = getConfig().getDoubleElse(getConfigName() + ".std.main", 2.8);
        this.pickUPLimit = getConfig().getIntElse(getConfigName() + ".limit.pick-up", 12);
        this.quickMoveLimit = getConfig().getIntElse(getConfigName() + ".limit.quick-move", 12);
        this.throwLimit = getConfig().getIntElse(getConfigName() + ".limit.throw", 10);
        this.throwStdThreshold = getConfig().getDoubleElse(getConfigName() + ".std.throw", 1.5);
        this.shouldMitigate = getConfig().getBooleanElse(getConfigName() + ".mitigate", false);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (mitigate && shouldMitigate) {
            event.setCancelled(true);
            event.markForReEncode(true);
            player.getInventory().requiresRefresh = true;
            player.getUser().closeInventory();
            mitigate = false;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CLICK_WINDOW) {
            return;
        }

        if (mitigate && shouldMitigate) {
            event.setCancelled(true);
            player.getInventory().requiresRefresh = true;
            player.getUser().closeInventory();
            mitigate = false;
            return;
        }

        WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
        long now = time();

        processSwapOperation(packet, now);
        processPickUPOperation(packet, now);
        processThrowOperation(packet, now);
        processQuickMoveOperation(packet, now);
        processSequentialOperation(packet, now);
    }

    private void cleanupDeque(Deque<Long> deque, long now) {
        while (!deque.isEmpty() && now - deque.peekFirst() > 500) {
            deque.pollFirst();
        }
    }

    private void processSwapOperation(WrapperPlayClientClickWindow packet, long now) {
        if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.SWAP) {
            swapTimestamps.addLast(now);
            cleanupDeque(swapTimestamps, now);
            if (swapTimestamps.size() >= 8) {
                double std = MathUtil.calculateStandardDeviation(swapTimestamps);
                if (std < stdThreshold) {
                    if (flagAndAlert(String.format("std= %.2f", std))) {
                        swapTimestamps.clear();
                        mitigate = true;
                    }
                }
            }
        }
    }

    private void processQuickMoveOperation(WrapperPlayClientClickWindow packet, long now) {
        if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
            final double diff = Math.abs(now - lastQuickMove);
            lastQuickMove = now;
            quickMoveTimestamps.addLast(now);
            cleanupDeque(quickMoveTimestamps, now);
            if (quickMoveTimestamps.size() > quickMoveLimit) {
                double acceleration = Math.abs(MathUtil.calculateAcceleration(quickMoveTimestamps));
                if (Double.isNaN(acceleration) || Double.isInfinite(acceleration)) {
                    quickMoveTimestamps.clear();
                    rewardBufferAndVL();
                    return;
                }

                if (acceleration >= 0.00 && acceleration <= 0.445 && diff <= 2) {
                    buffer *= 0.65;
                    return;
                }

                buffer += 1.25;
                if (buffer > 3.5) {
                    if (flagAndAlert("q= " + quickMoveTimestamps.size() + "\na= " + acceleration)) {
                        mitigate = true;
                        quickMoveTimestamps.clear();
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }

    private void processPickUPOperation(WrapperPlayClientClickWindow packet, long now) {
        if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.PICKUP) {
            int lastPickUPSlot = nowPickUPSlot;
            nowPickUPSlot = packet.getSlot();

            if (nowPickUPSlot == lastPickUPSlot) {
                pickUPTimestamps.clear();
                return;
            }

            pickUPTimestamps.addLast(now);
            cleanupDeque(pickUPTimestamps, now);
            if (pickUPTimestamps.size() > pickUPLimit) {
                if (flagAndAlert("p= " + pickUPTimestamps.size())) {
                    player.getInventory().requiresRefresh = true;
                    pickUPTimestamps.clear();
                    mitigate = true;
                }
            }
        }
    }


    private void processThrowOperation(WrapperPlayClientClickWindow packet, long now) {
        if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.THROW) {
            throwTimestamps.addLast(now);
            cleanupDeque(throwTimestamps, now);
            if (throwTimestamps.size() > throwLimit) {
                if (flagAndAlert("t= " + throwTimestamps.size())) {
                    throwTimestamps.clear();
                    mitigate = true;
                }
            }
            if (throwTimestamps.size() >= 5) {
                double mean = MathUtil.calculateMeanInterval(throwTimestamps);
                double std = MathUtil.calculateStandardDeviation(throwTimestamps);
                if (mean < 100 && std < throwStdThreshold) {
                    if (flagAndAlert(String.format("std= %.2f (%.1fms)", std, mean))) {
                        throwTimestamps.clear();
                        mitigate = true;
                    }
                }
            }
        }
    }

    
    private void processSequentialOperation(WrapperPlayClientClickWindow packet, long now) {
        if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
            return;
        }
        if (packet.getSlot() >= 0) {
            if (lastSlot != -1) {
                
                if (packet.getSlot() == lastSlot + 1 && now - lastOperationTime < 50) {
                    sequentialOperations++;
                    if (sequentialOperations > 5) {
                        if (flagAndAlert("seq= " + sequentialOperations)) {
                            sequentialOperations = 0;
                            mitigate = true;
                        }
                    }
                } else {
                    sequentialOperations = 0;
                }
            }
            lastSlot = packet.getSlot();
            lastOperationTime = now;
        }
    }
}
