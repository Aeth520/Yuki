package cn.aetheris.yuki.check.impl.player.timer;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "TimerAA (Exploit)",
        configName = "TimerAA",
        type = CheckType.TIMER,
        decay = 0.65,
        experimental = true)
public final class TimerAA extends Check implements PacketCheck {

    long lastFlag;
    long lastFlag2;
    private boolean receivedTickEnd = true;
    private int flyingPackets = 0;

    public TimerAA(PlayerData player) {
        super(player);
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!player.supportsEndTickPreVia()) return;
        if (isFlying(event.getPacketType()) && !player.packetStateData.lastPacketWasTeleport) {
            if (!receivedTickEnd && flyingPackets >= 5) {
                if (time() - lastFlag < 800L) {
                    return;
                }
                if (flagAndAlert("(IgnoreTick)\np= " + flyingPackets)) {
                    handleViolation();
                }
                lastFlag = time();
            }
            receivedTickEnd = false;
            flyingPackets++;
        } else if (event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END) {
            receivedTickEnd = true;
            if (flyingPackets > 4) {
                if (time() - lastFlag2 < 800L) {
                    return;
                }
                if (flagAndAlert("(End)\np= " + flyingPackets)) {
                    handleViolation();
                }
                lastFlag2 = time();
            }
            flyingPackets = 0;
        }
    }

    private void handleViolation() {
        
        setbackIfAboveSetbackVL();
        player.onPacketCancel();
    }
}
