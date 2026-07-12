package cn.aetheris.yuki.check.impl.combat.analysis;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisF.BasicModuleConfig;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.vec.Vec2d;
import cn.aetheris.yuki.util.ray.RayUtils;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

import java.util.*;


@CheckData(
        name = "AnalysisF",
        configName = "AnalysisF",
        type = CheckType.ANALYSIS,
        experimental = true,
        decay = 0.75
)
public final class AnalysisF extends Check implements RotationCheck {
    private final List<Vec2d> rawRotations = new LinkedList<>();
    private final Map<CheckType, BasicModuleConfig> configs = new HashMap<>();
    private final Map<CheckType, Double> buffers = new HashMap<>();

    public AnalysisF(PlayerData player) {
        super(player);
        loadModuleConfig(CheckType.GCD, "gcd", 3.0, 1.0);
        loadModuleConfig(CheckType.SYNC, "sync", 5.0, 1.0);
        loadModuleConfig(CheckType.MACHINE, "machine", 5.0, 1.0);
        loadModuleConfig(CheckType.INTERPOLATION, "interpolation", 4.0, 0.85);
        loadModuleConfig(CheckType.OUTSENS_INTERPOLATION, "interpolation_out", 6.0, 1.0);
        loadModuleConfig(CheckType.OUTSENS_MACHINE, "machine_out", 6.0, 1.0);
    }

    @Override
    public void reload() {
        super.reload();
        loadModuleConfig(CheckType.GCD, "gcd", 3.0, 1.0);
        loadModuleConfig(CheckType.SYNC, "sync", 5.0, 1.0);
        loadModuleConfig(CheckType.MACHINE, "machine", 5.0, 1.0);
        loadModuleConfig(CheckType.INTERPOLATION, "interpolation", 4.0, 0.85);
        loadModuleConfig(CheckType.OUTSENS_INTERPOLATION, "interpolation_out", 6.0, 1.0);
        loadModuleConfig(CheckType.OUTSENS_MACHINE, "machine_out", 6.0, 1.0);
    }


    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (!player.hasAttackedSince(500L)) {
            buffer *= 0.95;
            return;
        }

        if (player.getTarget() == null) {
            return;
        }

        if (hasExemptions()) {
            buffer *= 0.85;
        }

        if (rotationUpdate.isCinematic2()) {
            return;
        }

        if (rotationUpdate.getProcessor().getPitch() >= 90F) {
            return;
        }

        int machineKnownMovement = 0,
                robotizedAmount = 0, infinitives = 0, gcd = 0;


        this.rawRotations.add(new Vec2d(player.getYaw(), player.getPitch()));
        final List<Vec2d> rotations = this.rawRotations;
        final Set<Double> yaws = new HashSet<>();

        if (!(this.rawRotations.size() >= 20)) {
            return;
        }

        final List<Double> robotizedStack = new LinkedList<>();

        {
            double oldYaw = rotations.get(0).getX();
            for (Vec2d r : rotations) {
                yaws.add(Math.abs(r.getX() - oldYaw));
                oldYaw = r.getX();
            }
        }

        double oldYawResult = rotations.get(0).getX();
        double oldPitchResult = rotations.get(0).getY();
        double yawChangeFirst = Math.abs(rotations.get(0).getX() - rotations.get(1).getX());

        for (Vec2d rotation : rotations) {
            double yawChange = Math.abs(rotation.getX() - oldYawResult);
            double pitchChange = Math.abs(rotation.getY() - oldPitchResult);
            double robotized = Math.abs(yawChange - yawChangeFirst);
            double interpolation;
            float yaw = (float) rotation.getX();
            if (RayUtils.scaleVal(yawChange, 2.0) == 0.1
                    || RayUtils.scaleVal(pitchChange, 2.0) == 0.1) ++gcd;
            if (RayUtils.scaleVal(yawChange, 2.0) == 0.01
                    || RayUtils.scaleVal(pitchChange, 2.0) == 0.01) ++gcd;
            if (robotized < 2 && yawChange > 2.5) robotizedAmount += 1;
            if (robotized < 0.99 && yawChange > 4) machineKnownMovement++;
            interpolation = RotateProcessor.scaleVal(yawChange / robotized, 2);
            if (Double.isInfinite(interpolation) && yawChange > 0) {
                infinitives++;
                if (infinitives > 1 && yawChange < 0.4) {
                    infinitives--;
                }
            }
            if (robotized != 0) robotizedStack.add(robotized);
            oldYawResult = yaw;

        }


        final int sens = player.calculateSensitivity();
        if (sens > 65 && sens < 90) {
            if (gcd > 2) {
                handleCheck(CheckType.GCD, "(Patten)\ng= " + gcd, player::mitigateDamage);
            }
            if (robotizedAmount >= 10 && Math.abs(MathUtil.getAverage(yaws)) > 2.5) {
                handleCheck(CheckType.SYNC, "(Sync)\nr= " + robotizedAmount + "\ns= " + sens, player::mitigateDamage);
            }
            if (machineKnownMovement > 9 && Math.abs(MathUtil.getAverage(yaws)) > 3.0) {
                handleCheck(CheckType.MACHINE, "(Normal)\nm= " + machineKnownMovement + "\ns= " + sens, player::mitigateDamage);
            }
        } else if (sens < 65) {
            if (machineKnownMovement > 8) {
                handleCheck(CheckType.OUTSENS_MACHINE, "(Machine)\nm= " + machineKnownMovement + "\ns= " + sens, player::mitigateDamage);
            }
            if (infinitives > 1 && Math.abs(MathUtil.getAverage(yaws)) > 3.2) {
                handleCheck(CheckType.OUTSENS_INTERPOLATION, "(interpolation)\ni= " + infinitives + "\ns= " + sens, player::mitigateDamage);
            }
            rewardBufferAndVL();
        }
        this.rawRotations.clear();
    }

    private boolean hasExemptions() {
        return isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE
        ) || player.packetStateData.horseInteractCausedForcedRotation ||
                player.getTarget().getType() != EntityTypes.PLAYER
                || (player.getLastTarget() != null && !player.getTarget().getUuid().equals(player.getLastTarget().getUuid()));
    }

    private void handleCheck(CheckType type, String alertMessage, Runnable callback) {
        BasicModuleConfig config = configs.get(type);
        if (!config.enabled) return;

        double currentBuffer = buffers.get(type) + config.failIncrease;
        buffers.put(type, currentBuffer);

        if (currentBuffer > config.maxBuffer) {
            if (flagAndAlert(alertMessage)) {
                callback.run();
            }
            buffers.put(type, 0.0);
        }
    }

    private void loadModuleConfig(CheckType type, String path, double defaultMaxBuffer, double defaultFailIncrease) {
        String basePath = getConfigName() + "." + path + ".";
        if (configs == null) {
            return;
        }
        configs.put(type, new BasicModuleConfig(
                getConfig().getBooleanElse(basePath + "enabled", true),
                getConfig().getDoubleElse(basePath + "max-buffer", defaultMaxBuffer),
                getConfig().getDoubleElse(basePath + "fail-increase", defaultFailIncrease)
        ));
        buffers.put(type, 0.0);
    }


    public enum CheckType {
        GCD, SYNC, MACHINE, INTERPOLATION,
        OUTSENS_MACHINE, OUTSENS_INTERPOLATION
    }
}