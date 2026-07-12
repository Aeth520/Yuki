package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "AutoClickerD (Deviation)", type = CheckType.AUTOCLICKER, configName = "AutoClickerD", decay = 0.8, description = "Too low average deviation.", experimental = true)
public final class AutoClickerD extends Check implements PacketCheck {

    final EvictingList<Long> samples = new EvictingList(40);
    double lastDeviation;

    public AutoClickerD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.INTERACT, ExemptType.CLIENT_VERSION)) {
            long delay = player.clickProcessor.getDelay();
            if (delay > 5000L) {
                samples.clear();
                return;
            }
            samples.add(delay);
            if (samples.isFull()) {
                final double deviation = MathUtil.getSDeviation(this.samples);
                final double difference = Math.abs(deviation - this.lastDeviation);
                final double average = Math.abs(deviation + this.lastDeviation) / 2.0;
                if (difference < 3.0 && average < 150.0) {
                    if (++buffer < 8.0) {
                        if (flagAndAlert("d= " + difference
                                + "\na= " + average)) {
                            player.mitigateDamage();
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
                this.lastDeviation = deviation;
            }

        }
    }

}