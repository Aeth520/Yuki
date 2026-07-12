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

@CheckData(name = "AutoClickerF (Avg)", type = CheckType.AUTOCLICKER, configName = "AutoClickerF", decay = 0.55, experimental = true)
public final class AutoClickerF extends Check implements PacketCheck {

    long lastFlag;

    int flying;

    Deque<Integer> samples = new ArrayDeque<>();

    public AutoClickerF(PlayerData player) {
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
                if (samples.size() == 500) {
                    double kur = new Kurtosis().evaluate(MathUtil.dequeTranslator(samples));
                    double ske = new Skewness().evaluate(MathUtil.dequeTranslator(samples));
                    double std = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));
                    if (ske < 0.2 && kur < 0.0 && std < 0.7) {
                        if (time() - lastFlag < 500L) {
                            return;
                        }
                        if (buffer++ > 2) {
                            if (flagAndAlert("std= " + std + "\nkur= " + kur + "\nske= " + ske)) {
                                buffer = 0.0;
                            }
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
