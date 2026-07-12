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
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "AutoClickerO (Consistency)", type = CheckType.AUTOCLICKER, configName = "AutoClickerO", description = "Standard consistency", decay = 0.35, experimental = true)
public final class AutoClickerO extends Check implements PacketCheck {

    private final Deque<Integer> samples = new ArrayDeque<>();
    private int flying;
    private double lastSTD;
    private boolean lastSet;

    public AutoClickerO(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (flying < 10 && !isExempt(ExemptType.INTERACT)) {
                samples.add(flying);
            }

            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) {
                return;
            }

            if (samples.size() == 1000) {
                int outliers = MathUtil.getOutliers(samples);
                double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));
                double sdd = Math.abs(std - lastSTD);
                double cps = 20.0 / MathUtil.getAverage(samples);
                if (std < 0.65 && sdd < 0.1) {
                    if (buffer++ > 3) {
                        flagAndAlert("std= " + std + " | " + sdd + "\no= " + outliers + "\nc= " + cps);
                    }
                } else {
                    rewardBufferAndVL();
                }

                samples.clear();
                lastSTD = std;
            }

            if (!lastSet && samples.size() == 500) {
                lastSTD = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));
                lastSet = true;
            }

            flying = 0;
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !isExempt(ExemptType.TELEPORT)) {
            ++flying;
        }
    }
}
