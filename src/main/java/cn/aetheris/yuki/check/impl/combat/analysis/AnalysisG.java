package cn.aetheris.yuki.check.impl.combat.analysis;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisG.*;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.rotation.AimDetectionStrategy;
import cn.aetheris.yuki.check.util.rotation.DetectionContext;
import cn.aetheris.yuki.check.util.rotation.entity.Rotation;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

import java.util.ArrayList;
import java.util.List;

@CheckData(
        name = "AnalysisG",
        configName = "AnalysisG",
        type = CheckType.ANALYSIS,
        decay = 0.85,
        experimental = true
)
public class AnalysisG extends Check implements PacketCheck {
    private final DetectionContext context;
    private final List<AimDetectionStrategy> detectionStrategies;
    public ArrayList<Rotation> rotations = new ArrayList<>();

    public AnalysisG(PlayerData player) {
        super(player);
        detectionStrategies = initializeDetectionStrategies();
        context = new DetectionContext(player);
    }

    
    private List<AimDetectionStrategy> initializeDetectionStrategies() {
        return List.of(
                new AccelerationDetection(),
                new AimPathAnalysis(),
                new CorrelationAnalysis(),
                new CoordinationDetection(),
                new FrictionDetection()
        );
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isFlying(event.getPacketType())) return;
        if (player.isTeleporting()) {
            resetDetectionStrategies();
            return;
        }

        if (!shouldModifyPackets()) {
            return;
        }

        final float movedYaw = player.getYaw() - player.getLastYaw();
        final float movedPitch = player.getPitch() - player.getLastPitch();
        rotations.add(new Rotation(movedYaw, movedPitch));
        if (rotations.size() > 100) {
            rotations.remove(0);
        }
        if (!player.hasAttackedSince(2000)) {
            resetDetectionStrategies();
            return;
        }

        if (player.target == null) {
            resetDetectionStrategiesChangeTarget();
            return;
        }

        if (player.lastTarget != player.target) {
            resetDetectionStrategiesChangeTarget();
            return;
        }

        if (player.target.getType() != EntityTypes.PLAYER) {
            resetDetectionStrategiesChangeTarget();
            return;
        }

        updateDetectionContext(player);
        executeDetectionStrategies(player);
        saveLastRotationValues();
    }


    private void updateDetectionContext(PlayerData profile) {
        context.setRotations(rotations);
        context.setDeltaYaw(profile.getRotateProcessor().deltaYaw % 360F);
        context.setDeltaPitch(profile.getRotateProcessor().deltaPitch);

        if (profile.getTarget() != null) {
            context.setOptimalYaw(player.getRotateProcessor().optimalYaw);
        }
        final float movedYaw = player.getYaw() - player.getLastYaw();
        final float movedPitch = player.getPitch() - player.getLastPitch();

        context.updateMovementData(
                profile.getYaw(),
                profile.getPitch(),
                time()
        );
        context.updateRotation(new Rotation(
                profile.getYaw(),
                profile.getPitch()
        ));
        context.updateRotationDelta(new Rotation(
                movedYaw,
                movedPitch
        ));
    }

    private void executeDetectionStrategies(PlayerData profile) {
        for (AimDetectionStrategy strategy : detectionStrategies) {
            strategy.detect(profile, context);
        }
    }

    private void resetDetectionStrategies() {
        for (AimDetectionStrategy strategy : detectionStrategies) {
            strategy.reset();
        }
    }

    private void resetDetectionStrategiesChangeTarget() {
        for (AimDetectionStrategy strategy : detectionStrategies) {
            strategy.changeTarget();
        }
    }

    private void saveLastRotationValues() {
        context.setLastDeltaYaw(context.getDeltaYaw());
        context.setLastDeltaPitch(context.getDeltaPitch());
        context.setLastOptimalYaw(context.getOptimalYaw());
    }
}