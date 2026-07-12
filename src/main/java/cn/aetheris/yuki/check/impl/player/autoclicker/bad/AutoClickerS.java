package cn.aetheris.yuki.check.impl.player.autoclicker.bad;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "AutoClickerS (BadPackets)", type = CheckType.AUTOCLICKER, configName = "AutoClickerS", decay = 0.05, description = "Invalid totaltick", experimental = true)
public final class AutoClickerS extends Check implements PacketCheck {

    int ticks;
    int lastTicks;
    int totalTicks;
    int buffer;
    int streak;

    public AutoClickerS(PlayerData player) {
        super(player);
        ticks = 0;
        lastTicks = 0;
        totalTicks = 0;
        buffer = 0;
        streak = 0;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);

            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                boolean proper = ticks < 4 && lastTicks < 4;

                if (proper) {
                    boolean invalid = ticks == lastTicks;

                    if (invalid) {
                        ++buffer;
                    }

                    if (++totalTicks == 25) {
                        if (buffer > 22) {
                            flagAndAlert("b=" + buffer);
                        }

                        if (++buffer > 15) {
                            if (++streak > 2) {
                                flagAndAlert("s=" + streak);
                            }
                        } else {
                            streak = 0;
                            rewardVL();
                        }

                        totalTicks = 0;
                    }
                }
                this.lastTicks = ticks;
            }
        } else if (isFlying(event.getPacketType())) {
            ++ticks;
        }
    }
}
