package cn.aetheris.yuki.check.impl.movement.noslow;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckData(name = "NoSlowB (Sneak)", type = CheckType.NOSLOW, configName = "NoSlowB", setback = 6)
public final class NoSlowB extends Check implements PacketCheck {

    public NoSlowB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isTickPacket(event.getPacketType())) {

            if (isExempt(ExemptType.FLYING, ExemptType.HIGH_C0F, ExemptType.CLIENT_VERSION)) return;

            if (player.gamemode == GameMode.CREATIVE
                    || player.gamemode == GameMode.SPECTATOR) return;

            if (player.food < 6.0F) {

                player.latencyUtils.addRealTimeTaskAsync(player.lastTransactionSent.get(), () -> {
                    if (player.isSprinting) {
                        if (buffer++ > 4) {
                            if (flagAndAlert("f= " + player.food)) {
                                event.setCancelled(true);
                                player.onPacketCancel();
                                setbackIfAboveSetbackVL();
                            }
                        } else {
                            rewardBufferAndVL();
                        }
                    }
                });
            }
        }
    }
}