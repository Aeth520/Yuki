package cn.aetheris.yuki.check.impl.combat.aim;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

@CheckData(
        name = "AimV",
        description = "Simplified rotation analysis",
        configName = "AimV",
        type = CheckType.AIM,
        decay = 0.65
)
public final class AimV extends Check implements RotationCheck {

    private static final float MIN_DELTA = 0.085F;
    private final ArrayDeque<Float> yawSamples = new ArrayDeque<>();
    private final ArrayDeque<Float> pitchSamples = new ArrayDeque<>();
    private int time;

    public AimV(PlayerData player) {
        super(player);
    }

    @Override
    public void process(@NotNull RotationUpdate update) {
        if (player.isAttacking()) time = 0;
        if (!player.hasAttackedSince(500L) ||
                player.getTarget().getType() != EntityTypes.PLAYER ||
                Math.abs(update.getTo().getPitch()) >= 89.9F ||
                isExempt(ExemptType.TELEPORT, ExemptType.SERVER_SENT_PULLBACK, ExemptType.ELYTRA_FLYING)) {
            return;
        }

        if (time < 5) {
            time++;
            if (time >= 2) {

                final float dy = update.getProcessor().getDeltaYaw() % 360F;
                final float dp = update.getProcessor().getDeltaPitch();

                yawSamples.addLast(dy);
                pitchSamples.addLast(dp);

                while (yawSamples.size() > 16) {
                    yawSamples.removeFirst();
                    pitchSamples.removeFirst();
                }

                if (yawSamples.size() == 16) {
                    double yawSum = 0, yawSq = 0;
                    int yawValid = 0;

                    for (float y : yawSamples) {
                        if (Math.abs(y) < MIN_DELTA) continue;
                        yawSum += y;
                        yawSq += y * y;
                        yawValid++;
                    }

                    double pitchSum = 0, pitchSq = 0;
                    for (float p : pitchSamples) {
                        if (Math.abs(p) < MIN_DELTA) continue;
                        pitchSum += p;
                        pitchSq += p * p;
                    }


                    double yawStd = yawValid > 0 ? MathUtil.stdDev(yawSum, yawSq, yawValid) : 0;
                    double pitchStd = yawValid > 0 ? MathUtil.stdDev(pitchSum, pitchSq, yawValid) : 0;

                    if (yawValid > 8) {
                        if ((yawStd < 0.25F && pitchStd > 2.85F) || (pitchStd < 0.05F && yawStd > 2.45F)) {
                            if (buffer++ > 10) {
                                if (flagAndAlert("ps= " + pitchStd + "\nys= " + yawStd)) {
                                    yawSamples.clear();
                                    pitchSamples.clear();
                                    player.mitigateDamage();
                                    buffer -= 5;
                                    Bukkit.getScheduler().runTaskLaterAsynchronously(Yuki.getInstance(), (Runnable) player::mitigateDamage, 40L);
                                }
                            } else {
                                rewardBufferAndVL();
                            }
                        }
                    }
                }
            }
        }
    }
}
