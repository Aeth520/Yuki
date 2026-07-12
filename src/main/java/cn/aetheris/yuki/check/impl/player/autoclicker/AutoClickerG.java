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

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "AutoClickerG (Bad)", type = CheckType.AUTOCLICKER, configName = "AutoClickerG", description = "No randomization cps", decay = 0.85)
public final class AutoClickerG extends Check implements PacketCheck {
    private final Deque<Integer> delays = new ArrayDeque<>();
    private int delay = 0;

    public AutoClickerG(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (!isExempt(ExemptType.INTERACT, ExemptType.INVALID_GAMEMODE)) {
                    if (delay <= 5 && delay > 0) {
                        delays.add(delay);
                    }

                    if (delays.size() == 40) {
                        double average = MathUtil.getAverage(delays);
                        double std = MathUtil.stdDev(average, delays);
                        if (!(average <= 2.0) || !(std < 0.15) || !(player.getCps() > 8.0)) {
                            rewardBufferAndVL();
                        } else if (buffer++ > 10.0) {
                            if (flagAndAlert("std= " + std + "\navg= " + average + "\nc= " + player.getCps())) {
                                player.mitigateDamage();
                                rewardBufferAndVL();
                            }
                        }
                        delays.removeFirst();
                    }

                    delay = 0;
                }
            } else if (isFlying(event.getPacketType())) {
                ++delay;
            }
        }
    }
}