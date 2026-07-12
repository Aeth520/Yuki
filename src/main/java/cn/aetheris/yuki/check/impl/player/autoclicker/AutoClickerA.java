package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "AutoClickerA (Swing)", description = "Check for high cps", type = CheckType.AUTOCLICKER, configName = "AutoClickerA", decay = 0.54)
public final class AutoClickerA extends Check implements PacketCheck {

    private long lastFlag;
    private boolean shouldCancel;
    private int leftCpsMax;

    public AutoClickerA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ANIMATION) {
            return;
        }

        if (isExempt(ExemptType.LAGGING)) {
            return;
        }

        int cps = player.getCps();
        if (cps <= leftCpsMax) {
            rewardBufferAndVL();
            return;
        }

        if (time() - lastFlag < 1000L) {
            return;
        }
        if (flagAndAlert("cps= " + cps)) {
            lastFlag = time();
            if (shouldCancel) {
                event.setCancelled(true);
                player.onPacketCancel();
                player.mitigateDamage();
            }
        } else {
            rewardVL();
        }
    }

    @Override
    public void reload() {
        super.reload();
        leftCpsMax = getConfig().getIntElse(getConfigName() + ".max-cps", 30);
        shouldCancel = getConfig().getBooleanElse(getConfigName() + ".should-cancel", false);
        if (leftCpsMax < 0) leftCpsMax = Integer.MAX_VALUE;
    }
}
