package cn.aetheris.yuki.check.impl.combat.aim;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.protocol.nms.vec.Vec2f;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CheckData(
        name = "AimU",
        configName = "AimU",
        description = "Deviated from the predicted rotation route",
        type = CheckType.AIM,
        decay = 0.75,
        setback = 6
)
public final class AimU extends Check implements RotationCheck {

    private static final int SAMPLE_SIZE = 100;
    private static final int PATTERN_LENGTH = 3;
    private final List<Vec2f> sample = new ArrayList<>(SAMPLE_SIZE);
    private double buffer2;

    public AimU(PlayerData player) {
        super(player);
    }

    @Override
    public void process(@NotNull RotationUpdate update) {
        if (!player.hasAttackedSince(600L) || isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE)) {
            sample.clear();
            return;
        }

        if (player.getTarget() != null && (player.getTarget() != EntityTypes.PLAYER || player.getTarget() != player.getLastTarget())) {
            sample.clear();
            return;
        }


        sample.add(new Vec2f(update.getProcessor().getDeltaYaw(), update.getProcessor().getDeltaPitch()));
        if (sample.size() < SAMPLE_SIZE) {
            return;
        }

        boolean flagged = false;
        int filterCount = 0;
        List<Vec2f> patterns = new ArrayList<>();
        for (int i = 1; i < SAMPLE_SIZE; i++) {
            Vec2f prev = sample.get(i - 1);
            Vec2f curr = sample.get(i);
            float absX = Math.abs(curr.getX());
            float diff = Math.abs(Math.abs(curr.getX()) - Math.abs(prev.getY()));
            if (absX > 1.0 && diff < 1e-4) {
                if (++filterCount > 3 && buffer++ >= 3) {
                    flagged = flagAndAlert("(Filter)\np= " + diff);
                    if (flagged) {
                        player.mitigateDamage();
                        rewardBufferAndVL();
                        Bukkit.getScheduler().runTaskLaterAsynchronously(Yuki.getInstance(), (Runnable) player::mitigateDamage, 40L);
                    }
                }
            }
            if (!flagged) {
                if (i <= SAMPLE_SIZE - PATTERN_LENGTH) {
                    for (int j = i + PATTERN_LENGTH; j <= SAMPLE_SIZE - PATTERN_LENGTH; j++) {
                        for (int k = 0; k < PATTERN_LENGTH; k++) {
                            Vec2f a = sample.get(i + k);
                            Vec2f b = sample.get(j + k);
                            if (Objects.equals(a, b) && !patterns.contains(a)) {
                                patterns.add(a);
                            }
                        }
                    }
                }
            }
        }

        if (!flagged) {
            for (Vec2f vec : patterns) {
                float x = Math.abs(vec.getX());
                float y = Math.abs(vec.getY());
                if ((x > 1.0 || y > 1.0) && x > 0.26 && y > 0.26) {
                    if (buffer2++ > 6) {
                        if (flagAndAlert("(Offset)\np= " + vec)) {
                            player.mitigateDamage();
                            rewardBufferAndVL();
                            
                            Bukkit.getScheduler().runTaskLaterAsynchronously(Yuki.getInstance(), (Runnable) player::mitigateDamage, 40L);
                        }
                        break;
                    }
                }
            }
        }

        if (!flagged) {
            buffer2 = Math.max(0, buffer2 - getDecay());
            if (buffer2 == 0) rewardVL();
        }
        sample.clear();
    }
}
