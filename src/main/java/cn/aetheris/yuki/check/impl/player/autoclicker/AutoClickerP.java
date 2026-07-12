package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(
        name = "AutoClickerP (Oscillation)",
        type = CheckType.AUTOCLICKER,
        configName = "AutoClickerP",
        decay = 0.75,
        experimental = true
)
public final class AutoClickerP extends Check implements PacketCheck {

    private final Deque<Integer> delays = new ArrayDeque<>();
    private final Deque<Integer> delays2 = new ArrayDeque<>();
    private long lastFlag;
    private int delay;

    public AutoClickerP(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            boolean valid = isExempt(ExemptType.INTERACT);
            if (player.getClientVersion().isNewerThan(ClientVersion.V_1_8)) {
                if (delay < 10 && valid && player.elapsedMS(event.getTimestamp(), player.getLastFlying()) <= 70L) {
                    delays.add(delay);
                }
            } else if (delay < 10 && valid) {
                delays.add(delay);
            }

            if (delays.size() == 50) {
                int osc = (int) MathUtil.getOscillation(delays);
                delays2.add(osc);
                if (delays2.size() == 8) {
                    double std = MathUtil.getStandardDeviation(delays2);
                    double cps = 20.0 / MathUtil.getAverage(delays);
                    if (cps > 6.5 && std < 0.3) {
                        if (time() - lastFlag < 500L) {
                            return;
                        }
                        if (buffer++ > 4) {
                            if (flagAndAlert("c= " + cps + "\nstd= " + std)) {
                                player.mitigateDamage();
                                rewardBufferAndVL();
                            }
                            lastFlag = time();
                        }
                    }

                    delays2.clear();
                }

                delays.clear();
            }

            delay = 0;
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            ++delay;
        }

    }
}
