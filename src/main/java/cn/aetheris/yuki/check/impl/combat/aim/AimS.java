package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.util.Pair2;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CheckData(
        name = "AimS",
        configName = "AimS",
        type = CheckType.AIM,
        decay = 0.65,
        description = "Detect abnormal horizontal rotation patterns during attacks"
)
public final class AimS extends Check implements RotationCheck {

    private final EvictingList<Pair2<Double, Double>> rotations = new EvictingList<>(10);
    private final EvictingList<Pair2<Integer, Integer>> rotationsG = new EvictingList<>(10);

    public AimS(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        if (player.hasAttackedSince(1200L)) {
            double deltaYaw = update.getProcessor().getDeltaYaw();
            double deltaPitch = update.getProcessor().getDeltaPitch();
            double gcdValue = MathUtil.getGCDValueStatistic(0.5) * 3;
            rotations.add(new Pair2<>(deltaYaw, deltaPitch));
            rotationsG.add(new Pair2<>((int) (deltaYaw / gcdValue), (int) (deltaYaw / gcdValue)));
            if (rotations.isFull()) {
                List<Double> x = new ArrayList<>(), y = new ArrayList<>();
                List<Integer> xG = new ArrayList<>(), yG = new ArrayList<>();
                for (Pair2<Double, Double> vec2 : rotations) {
                    x.add(vec2.getX());
                    y.add(vec2.getY());
                }
                for (Pair2<Integer, Integer> vec2 : rotationsG) {
                    xG.add(vec2.getX());
                    yG.add(vec2.getY());
                }

                double devX = MathUtil.getVariance(xG);
                double devY = MathUtil.getVariance(yG);
                double min = Math.min(devX, devY);
                double max = Math.max(devX, devY);
                if ((min < 0.09 && max > 35 && Collections.min(yG) != 0.0)) {
                    if (buffer++ > 4) {
                        if (flagAndAlert("low= " + min + "\nmax= " + max)) {
                            if (getViolations() > 5) player.mitigateDamage();
                        }
                    } else {
                        rewardBufferAndVL();
                    }
                }
            }
        }
    }
}