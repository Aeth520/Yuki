package cn.aetheris.yuki.check.impl.player.pingspoof;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "PingSpoofF (Diff)",
        configName = "PingSpoofF",
        description = "Check for invalid ping diff",
        experimental = true,
        decay = 0.84)
public class PingSpoofF extends Check implements PacketCheck {

    private final EvictingList<Long> lost = new EvictingList<>(40);
    private final EvictingList<Long> trans = new EvictingList<>(40);
    private final EvictingList<Long> realTrans = new EvictingList<>(40);

    private long lastFlying = -1;
    private long lostTime = 0;

    public PingSpoofF(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isExempt(ExemptType.JOIN)) {
            lost.clear();
            trans.clear();
            realTrans.clear();
            return;
        }
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (isExempt(ExemptType.TELEPORT,
                    ExemptType.INVALID_GAMEMODE)) {
                lost.clear();
                trans.clear();
                realTrans.clear();
            }
            long flyingDelay = time() - lastFlying;
            if (lastFlying != -1) {
                lostTime += flyingDelay - 50;
                lost.add(lostTime);
                if (lost.isFull() && trans.isFull() && realTrans.isFull()) {
                    double pingDifference = Math.abs(player.getTransactionPing() - player.getKeepAlivePing());
                    double lostStd = MathUtil.getStandardDeviation(lost);
                    double transStd = MathUtil.getStandardDeviation(trans);
                    double realTransStd = MathUtil.getStandardDeviation(realTrans);
                    double minDifference = Math.min(Math.abs(transStd - lostStd), Math.abs(transStd - realTransStd));

                    if (pingDifference > 50 && transStd > lostStd && transStd > realTransStd) {
                        if (minDifference > 15) {
                            if (buffer++ >= 5) {
                                if (flagAndAlert("ls= " + lostStd + "\nts= " + transStd + "\nrts= " + realTransStd + "\nd= " + minDifference)) {
                                    player.mitigateDamage();
                                    lost.clear();
                                    trans.clear();
                                    realTrans.clear();
                                }
                                if (buffer > 8) buffer = 8;
                            }
                        } else {
                            rewardBufferAndVL();
                        }
                    }

                    lost.clear();
                    trans.clear();
                    realTrans.clear();
                }
            }
            lastFlying = time();
        }
        if (isTransaction(event.getPacketType())) {
            if (time() - player.lastAttack <= 250) trans.add(player.transactionPing);
        }
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            realTrans.add(player.transactionPing);
        }
    }
}
