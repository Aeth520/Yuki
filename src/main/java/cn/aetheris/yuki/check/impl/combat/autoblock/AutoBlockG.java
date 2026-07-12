package cn.aetheris.yuki.check.impl.combat.autoblock;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AutoBlockG (Tick)",
        configName = "AutoBlockG",
        type = CheckType.AUTOBLOCK,
        decay = 0.75,
        experimental = true
)
public final class AutoBlockG extends Check implements PacketCheck {

    private final EvictingList<Double> sample = new EvictingList<>(5);
    private final List<Long> attackIntervals = new ArrayList<>();
    private double uses;
    private long lastAttackTime = 0L;
    private long lastUseItemTime = 0L;

    public AutoBlockG(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {

            if (isExempt(ExemptType.CLIENT_ANTICHEAT, ExemptType.CLIENT_VERSION)
                    || player.canSkipTicksPreVia()) return;

            if (player.hasAttackedSince(50L)) {

                if (player.isAttacking()) {
                    if (lastAttackTime != 0 && lastUseItemTime != 0) {
                        long timeDifference = lastAttackTime - lastUseItemTime;
                        if (timeDifference <= 50L) {
                            if (flagAndAlert("(Change)\ndiff= " + timeDifference)) {
                                player.mitigateDamage();
                            }
                        }
                    }
                    lastAttackTime = time();
                }

                if (player.packetStateData.isSlowedByUsingItem()) {
                    sample.add(uses++);
                    if (sample.isFull()) {
                        double std = MathUtil.stdDev(sample);
                        if (std > 0.5) {
                            if (flagAndAlert("(Change#2)\nstd= " + std)) {
                                player.mitigateDamage();
                            }
                        }
                        lastUseItemTime = time();
                    }

                    if (lastAttackTime != 0L) {
                        long currentTime = time();
                        if (currentTime - lastAttackTime >= 50L) {
                            long interval = currentTime - lastAttackTime;
                            attackIntervals.add(interval);
                        }
                    }

                    if (attackIntervals.size() > 3) {
                        long diff = attackIntervals.get(attackIntervals.size() - 1) - attackIntervals.get(attackIntervals.size() - 2);
                        if (Math.abs(diff) > 50L) {
                            if (flagAndAlert("(Change#3)\ndif= " + Math.abs(diff))) {
                                player.mitigateDamage();
                            }
                        }

                    } else {
                        sample.clear();
                    }
                }
            }
        }
    }
}
