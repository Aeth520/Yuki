package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.LinkedList;
import java.util.Queue;

@CheckData(name = "AutoClickerI (Poor)", type = CheckType.AUTOCLICKER, configName = "AutoClickerI", decay = 0.75, description = "Poor randomization CPS")
public final class AutoClickerI extends Check implements PacketCheck {

    private final Queue<Integer> delays = new LinkedList<>();
    private int delay = 0;
    private long lastFlag = 0;

    public AutoClickerI(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (!isExempt(ExemptType.INTERACT)) {
                if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_8)) {
                    if (delay > 10 && player.elapsedMS(event.getTimestamp(), player.lastFlying) <= 70L) {
                        delays.add(delay);
                    }
                } else {
                    if (delay >= 10) {
                        delays.add(delay);
                    }
                }
                if (delays.size() >= 150) {
                    double std = MathUtil.getStandardDeviation(delays);
                    double cps = 20.0 / MathUtil.getAverage(delays);

                    if (std < 0.445) {
                        if (time() - lastFlag < 500L) {
                            return;
                        }

                        if (buffer++ > 3) {
                            if (flagAndAlert("s= " + std + "\nc= " + cps)) {
                                buffer = 0.0;
                            }
                            lastFlag = time();
                        } else {
                            rewardBufferAndVL();
                        }
                    }

                    delays.clear();
                    rewardBufferAndVL();
                }

                delay = 0;
            }
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            delay++;
        }
    }
}