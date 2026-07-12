package cn.aetheris.yuki.check.impl.player.timer;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

@CheckData(name = "TimerB (Vehicle)",
        configName = "TimerB",
        description = "Invalid Game Speed In Vehicle",
        type = CheckType.TIMER,
        setback = 10)
public final class TimerB extends TimerA {
    boolean isDummy = false;

    public TimerB(PlayerData player) {
        super(player);
    }

    @Override
    public boolean shouldCountPacketForTimer(PacketTypeCommon packetType) {
        if (isExempt(ExemptType.JOIN, ExemptType.NOT_COMBAT, ExemptType.TELEPORT, ExemptType.VEHICLE_DIED, ExemptType.LAGGING, ExemptType.FLYING))
            return false;

        if (packetType == PacketType.Play.Client.VEHICLE_MOVE) {
            isDummy = false;
            return true;
        }

        if (packetType == PacketType.Play.Client.STEER_VEHICLE) {
            if (isDummy) { 
                return true;
            }
            
            isDummy = true;
            
        }
        return false;
    }
}
