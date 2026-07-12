package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "BadPacketsU (ConsistentY)", type = CheckType.BADPACKETS, configName = "BadPacketsU", decay = 0.66, description = "Check for y onsistent")
public final class BadPacketsU extends Check implements PostPredictionCheck {

    private double yMap;
    private double yDifference;

    public BadPacketsU(final PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {
            if (player.getLocationData() == null
                    || player.getLastLocationData() == null
                    || !player.onGround
                    || !isExempt(ExemptType.INTERACT, ExemptType.FLYING)) return;
            final double dif = yMap - player.getLocationData().getY();
            final int d = (int) (dif * 100);
            if ((dif > 0 && dif < 0.2) && (d != 98 && d != 99 && d != 97)) {
                if (Math.abs(dif - yDifference) == (Math.round(Math.abs(dif - yDifference) * 100.0) / 100.0)) {
                    if (buffer++ > 2) {
                        if (flagAndAlert("dif= " + dif + "\nbuffer= " + buffer)) {
                            player.mitigateDamage();
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
            yDifference = dif;
            yMap = player.getLocationData().getY();
        }
    }
}
