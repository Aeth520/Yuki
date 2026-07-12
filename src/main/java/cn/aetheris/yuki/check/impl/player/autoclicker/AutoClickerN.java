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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "AutoClickerN (BAD)", type = CheckType.AUTOCLICKER, configName = "AutoClickerN", decay = 0.254, description = "Bad randomization cps", experimental = true)
public final class AutoClickerN extends Check implements PacketCheck {

    private final Deque<Integer> delays = new ArrayDeque<>();
    private final Deque<Double> samples = new ArrayDeque<>();
    private int delay;

    public AutoClickerN(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interactType = new WrapperPlayClientInteractEntity(event);
            if (interactType.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK && !isExempt(ExemptType.INTERACT)) {
                if (delay <= 5 && delay > 0) {
                    delays.add(delay);
                }

                if (delays.size() >= 60) {
                    double skewness = new Skewness().evaluate(MathUtil.dequeTranslator(delays));
                    if (samples.add(skewness) && samples.size() >= 30) {
                        double avgSkewness = MathUtil.getAverage(samples);
                        double stdSkewness = new StandardDeviation().evaluate(MathUtil.dequeTranslator(samples));
                        double cps = player.getCps();
                        if (!(avgSkewness < 0.0) || !(stdSkewness < 2.0) || !(cps > 8.0)) {
                            rewardBufferAndVL();
                        } else if (buffer++ > 3) {
                            if (flagAndAlert("avg= " + avgSkewness + "\nstd = " + stdSkewness + "\ncps= " + cps)) {
                                rewardBufferAndVL();
                            }

                            samples.removeFirst();
                        }

                        delays.clear();
                    }

                    delay = 0;
                }
            } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
                ++delay;
            }
        }
    }
}
                      