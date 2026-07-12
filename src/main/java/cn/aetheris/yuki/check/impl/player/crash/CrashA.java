package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "CrashA", type = CheckType.CRASH, configName = "CrashA", description = "Invalid Location")
public final class CrashA extends Check implements PacketCheck {

    final double value = 2.9999999E7D;

    public CrashA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);

            if (isExempt(ExemptType.TELEPORT)) return;

            if (packet.hasPositionChanged()) {

                if (Math.abs(packet.getLocation().getX()) > value
                        || Math.abs(packet.getLocation().getZ()) > value
                        || Math.abs(packet.getLocation().getY()) > Integer.MAX_VALUE) {
                    if (flagAndAlert()) {
                        kickPlayer();
                        event.setCancelled(true);
                        player.getSetbackTeleportUtil().executeViolationSetback();
                        player.onPacketCancel();
                    }
                }
            }
        }
    }
}