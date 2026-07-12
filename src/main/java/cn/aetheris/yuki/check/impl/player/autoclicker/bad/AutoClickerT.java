package cn.aetheris.yuki.check.impl.player.autoclicker.bad;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "AutoClickerT (FLYING)", type = CheckType.AUTOCLICKER, configName = "AutoClickerT", decay = 0.65, experimental = true)
public final class AutoClickerT extends Check implements PacketCheck {

    int swings;
    int movements;
    int buffer;
    long lastSwing;

    public AutoClickerT(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {

            if (isExempt(ExemptType.SERVER_VERSION)) return;

            ++movements;
            if (movements == 20) {
                if (swings > 20) {
                    if (buffer++ > 1) {
                        flagAndAlert("s= " + swings + "\nm= " + movements);
                    } else {
                        rewardBufferAndVL();
                    }
                }
                if (time() - lastSwing <= 350L) {
                    movements = 0;
                    swings = 0;
                }
            } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
                if (time() - player.getLastDelayedMovePacket() > 110L
                        && time() - player.getLastDelayedMovePacket() < 110L
                        && !isExempt(ExemptType.INTERACT)) {
                    ++swings;
                    lastSwing = time();
                }
            }
        }
    }
}
