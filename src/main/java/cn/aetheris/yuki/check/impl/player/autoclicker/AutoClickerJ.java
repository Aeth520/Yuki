package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.time.Watch;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@CheckData(name = "AutoClickerJ (Average)",
        type = CheckType.AUTOCLICKER,
        configName = "AutoClickerJ",
        decay = 1.15)
public final class AutoClickerJ extends Check implements PacketCheck {

    private final Watch dbc = new Watch();
    private final Queue<Integer> averageTicks = new ConcurrentLinkedQueue<>();
    private int dc;
    private int lastTicks = 0;
    private int done = 0;
    private boolean swing;
    private int ticks = 0;

    public AutoClickerJ(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (swing && player.hasAttackedSince(2400L)) {
                if (ticks < 7) {
                    averageTicks.add(ticks);
                    if (averageTicks.size() > 50) {
                        averageTicks.poll();
                    }
                }
                if (averageTicks.size() > 40) {
                    final double average = MathUtil.getAverage(averageTicks);
                    if (average < 2.5) {
                        if (ticks > 3 && ticks < 20 && lastTicks < 20) {
                            rewardBufferAndVL();
                            done = 0;
                        } else if (done++ > 600.0 / (average * 1.5)) {
                            if (flagAndAlert("avg= " + average + "%")) {
                                done = 0;
                            }
                        }
                    } else {
                        rewardBufferAndVL();
                        done = 0;
                    }
                }
                lastTicks = ticks;
                ticks = 0;
            }
            swing = false;
            ++ticks;
        } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION && isExempt(ExemptType.INVALID_GAMEMODE, ExemptType.INTERACT)) {
            if (!dbc.hasTimeElapsed(30)) {
                dc++;
            }
            dbc.reset();
            swing = true;
        }
    }
}