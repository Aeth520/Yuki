package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "ImpossibleJ (GCD)", configName = "ImpossibleJ", description = "Checks for small GCD in y coordinates.", type = CheckType.IMPOSSIBLE, experimental = true)
public final class ImpossibleJ extends Check implements PacketCheck {
    public ImpossibleJ(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (isExempt(ExemptType.TELEPORT, ExemptType.RESPAWN, ExemptType.SERVER_VERSION)) {
                return;
            }

            final double gcd = MathUtil.gcd_eac(player.y, player.lastY);

            if (String.valueOf(gcd).contains("E")) {
                if (flagAndAlert("gcd= " + gcd)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                    player.getSetbackTeleportUtil().executeViolationSetback();
                }
            }
        }
    }
}