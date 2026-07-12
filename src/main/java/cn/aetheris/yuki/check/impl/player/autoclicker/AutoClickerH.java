package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AutoClickerH (Ratio)", type = CheckType.AUTOCLICKER, configName = "AutoClickerH", description = "Impossible ratio consistency", decay = 1.0, experimental = true)
public final class AutoClickerH extends Check implements PacketCheck {

    private final List<Double> samples = new ArrayList<>(100);
    private int buffer = 0;
    private long lastFlag;

    public AutoClickerH(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isFlying(event.getPacketType())) {
            return;
        }

        if (!player.hasAttackedSince(2000L)) {
            return;
        }

        double cps = player.getCps();
        if (cps <= 12) {
            return;
        }

        samples.add(cps);
        if (samples.size() > 100) {
            samples.remove(0);
        }


        double cpsAverage = MathUtil.getAverage(samples);

        double ratio = cpsAverage > 0 ? cps / cpsAverage : 0;

        if (cpsAverage >= 10) {
            if (ratio > 1.25) {
                if (time() - lastFlag < 1000L) {
                    return;
                }
                if (++buffer > 5) {
                    if (flagAndAlert("buffer= " + buffer + "\nratio= " + ratio + "\ncps= " + cps + "\nav= " + cpsAverage)) {
                        rewardBufferAndVL();
                        if (getViolations() > 8) player.mitigateDamage();
                    }
                    lastFlag = time();
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}