package cn.aetheris.yuki.check.impl.player.baritone;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "BaritoneA", decay = 0.5, description = "The player is using baritone (rotation)", type = CheckType.BARITONE)
public final class BaritoneA extends Check implements RotationCheck, PacketCheck {

    double lastDivisor;
    boolean checking;
    boolean waitForBreak;
    boolean canCheck;

    public BaritoneA(PlayerData player) {
        super(player);
        canCheck = false;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (isExempt(ExemptType.TELEPORT)) {
                return;
            }
            canCheck = true;
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING && canCheck) {
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
            if (player.gamemode == GameMode.CREATIVE
                    || player.gamemode == GameMode.SPECTATOR) return;

            if (digging.getAction() == DiggingAction.START_DIGGING) {
                waitForBreak = false;
                if (checking) {
                    if ((lastDivisor > 0 && lastDivisor < 0.009) || lastDivisor > 10) {
                        waitForBreak = true;
                    } else buffer = Math.max(0, buffer - getDecay());
                }

                checking = false;
            } else if (digging.getAction() == DiggingAction.CANCELLED_DIGGING
                    || digging.getAction() == DiggingAction.FINISHED_DIGGING) {
                if (waitForBreak) {
                    if (buffer++ > 10) {
                        flagAndAlert("divisor= " + lastDivisor);
                        buffer = 0.0;
                    }
                }
                waitForBreak = false;
            } else {
                rewardBufferAndVL();
            }
        }
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaPitch = rotationUpdate.getProcessor().getDeltaPitch();
        float lastDeltaPitch = rotationUpdate.getProcessor().getLastDeltaPitch();
        if (deltaPitch > 0.1 && lastDeltaPitch > 0.1) {
            checking = true;
            long expanded = (long) (deltaPitch * MathUtil.EXPANDER);
            long lastExpanded = (long) (lastDeltaPitch * MathUtil.EXPANDER);

            long gcd = MathUtil.gcd_eac(expanded, lastExpanded);

            lastDivisor = gcd / MathUtil.EXPANDER;
        }
    }
}