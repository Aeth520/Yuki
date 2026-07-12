package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "CrashC", type = CheckType.CRASH, configName = "CrashC", setback = 1)
public final class CrashC extends Check implements PacketCheck {
    public CrashC(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            final WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

            if (flying.hasPositionChanged()) {
                Location pos = flying.getLocation();
                if (Double.isNaN(pos.getX())
                        || Double.isNaN(pos.getY())
                        || Double.isNaN(pos.getZ())
                        || Double.isInfinite(pos.getX())
                        || Double.isInfinite(pos.getY())
                        || Double.isInfinite(pos.getZ())
                        || Float.isNaN(pos.getYaw())
                        || Float.isNaN(pos.getPitch())
                        || Float.isInfinite(pos.getYaw())
                        || Float.isInfinite(pos.getPitch())) {

                    if (flagAndAlert("x= " + pos.getX()
                            + "\ny= " + pos.getY()
                            + "\nz= " + pos.getZ()
                            + "\nyaw= " + pos.getYaw()
                            + "\npitch= " + pos.getPitch()) && shouldModifyPackets()) {
                        player.getSetbackTeleportUtil().executeViolationSetback();
                        player.onPacketCancel();
                        event.setCancelled(true);
                        kickPlayer();
                    }
                }
            }
        }
    }
}
