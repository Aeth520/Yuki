package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.Deque;
import java.util.LinkedList;

@CheckData(name = "AutoClickerC (Duplicates)", type = CheckType.AUTOCLICKER, configName = "AutoClickerC", description = "Duplicate cps line", decay = 0.55, experimental = true)
public final class AutoClickerC extends Check implements PacketCheck {

    private final Deque<Long> samples;
    private long lastArmAnimation;
    private long lastDelay;
    private long lastFlag;


    public AutoClickerC(PlayerData player) {
        super(player);
        this.samples = new LinkedList<>();
        this.lastArmAnimation = time();
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ANIMATION) {
            return;
        }

        if (isExempt(ExemptType.CLIENT_VERSION)) {
            return;
        }

        long now = time();
        long delay = now - lastArmAnimation;
        double cps = player.getCps();

        if (delay > 1L && delay < 140L && !isExempt(ExemptType.INTERACT, ExemptType.TELEPORT)) {
            final long acceleration = Math.abs(delay - lastDelay);
            samples.add(acceleration);
        }

        if (samples.size() == 20) {
            long distinctCount = samples.stream().distinct().count();
            long duplicates = samples.size() - distinctCount;

            if (duplicates == 0L || duplicates == 20L) {
                if (++buffer > 3) {
                    if (time() - lastFlag < 150L) {
                        return;
                    }
                    if (flagAndAlert("c= " + cps + "\nd= " + duplicates)) {
                        lastFlag = time();
                        player.mitigateDamage();
                    }
                } else {
                    buffer = Math.max(buffer - 0.5, 0);
                    rewardVL();
                }
            }

            lastDelay = delay;
            lastArmAnimation = now;
        }
    }
}
