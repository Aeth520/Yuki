package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.time.Watch;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "AutoClickerM (Consistency)", type = CheckType.AUTOCLICKER, configName = "AutoClickerM", description = "Impossible consistency")
public final class AutoClickerM extends Check implements PacketCheck {

    private final Watch dbc = new Watch();
    private final EvictingList<Long> samples;
    private long lastFlag;
    private int dc = 0;

    public AutoClickerM(PlayerData player) {
        super(player);
        samples = new EvictingList<>(30);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.INTERACT)) {
            long delay = player.clickProcessor.getDelay();

            
            if (!dbc.hasTimeElapsed(30)) {
                dc++;
            }
            dbc.reset();

            samples.add(delay);
            if (samples.isFull()) {
                long count = samples.stream().filter(l -> l > 150L).count();
                int outliers = (int) count;
                double cps = player.getCps();
                double average = MathUtil.getAverage(samples);
                if (outliers == 0 && cps > 7.5 && dc < 7) {
                    if (time() - lastFlag < 700L) {
                        return;
                    }
                    if (buffer++ > 15) {
                        if (flagAndAlert("avg= " + average
                                + "\nol= " + outliers)) {
                            buffer = 0.0;
                        }
                        lastFlag = time();
                    }
                } else {
                    rewardBufferAndVL();
                }
                samples.clear();
            }
        }
    }
}