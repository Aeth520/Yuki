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
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "AutoClickerB (Dev)", type = CheckType.AUTOCLICKER, configName = "AutoClickerB", decay = 0.85)
public final class AutoClickerB extends Check implements PacketCheck {

    int flying;
    Deque<Integer> samples = new ArrayDeque<>();
    private long lastFlag;

    public AutoClickerB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (!isExempt(ExemptType.INTERACT)) {
                if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_8)) {
                    if (flying > 10 && player.elapsedMS(event.getTimestamp(), player.lastFlying) <= 70L) {
                        samples.add(flying);
                    }
                } else if (flying < 10) {
                    samples.add(flying);
                }
                if (this.samples.size() == 300) {
                    int outliers = MathUtil.getOutliers(this.samples);
                    double cps = 20.0 / MathUtil.getAverage(this.samples);
                    double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(this.samples));
                    double kur = new Kurtosis().evaluate(MathUtil.dequeTranslator(this.samples));
                    double ske = new Skewness().evaluate(MathUtil.dequeTranslator(this.samples));
                    if (kur < 0.0 && ske < -0.5 && outliers <= 3) {
                        if (time() - lastFlag < 1000L) {
                            return;
                        }
                        if (buffer++ > 4) {
                            if (flagAndAlert("std= " + std + "\nkur= " + kur + "\nske= " + ske + "\no= " + outliers + "\nc= " + cps)) {
                                buffer = 0.0;
                            }
                            lastFlag = time();
                        } else {
                            rewardBufferAndVL();
                        }
                        samples.clear();
                    }
                    flying = 0;
                } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
                    if (!isExempt(ExemptType.TELEPORT)) {
                        ++flying;
                    }
                }
            }
        }
    }
}
