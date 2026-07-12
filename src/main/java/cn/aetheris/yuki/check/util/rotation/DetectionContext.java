package cn.aetheris.yuki.check.util.rotation;

import cn.aetheris.yuki.check.impl.combat.analysis.AnalysisG;
import cn.aetheris.yuki.check.util.rotation.entity.Rotation;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

@Getter
@Setter
public class DetectionContext {
    
    private final Queue<Rotation> rotationHistory = new ArrayDeque<>(100);
    private final Queue<Rotation> rotationDeltaHistory = new ArrayDeque<>(100);
    private final Queue<Double> reactionTimes = new ArrayDeque<>(50);
    PlayerData playerData;
    
    private ArrayList<Rotation> rotations;
    
    private double distanceToTarget;
    private Rotation currentRotation;
    private Rotation lastRotation;
    
    private float deltaYaw;
    private float deltaPitch;
    private float optimalYaw;

    private float avgYaw;
    private float avgPitch;
    private float maxYaw;
    private float maxPitch;

    
    private float lastDeltaYaw;
    private float lastDeltaPitch;
    private float lastOptimalYaw;
    private long lastMovementUpdateTime;

    private double previousYaw;
    private double previousPitch;
    private double previousYawVelocity;
    private double previousPitchVelocity;
    private double yawAcceleration;
    private double pitchAcceleration;

    public DetectionContext(PlayerData playerData) {
        this.playerData = playerData;
    }

    
    public void updateMovementData(double currentYaw, double currentPitch, long currentTime) {
        if (lastMovementUpdateTime == 0) {
            lastMovementUpdateTime = currentTime;
            previousYaw = currentYaw;
            previousPitch = currentPitch;
            return;
        }

        
        double deltaTime = (currentTime - lastMovementUpdateTime) / 1000.0;
        if (deltaTime <= 0) {
            return;
        }

        
        double currentYawVelocity = calculateAngularVelocity(previousYaw, currentYaw, deltaTime);
        double currentPitchVelocity = calculateAngularVelocity(previousPitch, currentPitch, deltaTime);

        
        yawAcceleration = (currentYawVelocity - previousYawVelocity) / deltaTime;
        pitchAcceleration = (currentPitchVelocity - previousPitchVelocity) / deltaTime;

        
        previousYawVelocity = currentYawVelocity;
        previousYaw = currentYaw;

        previousPitchVelocity = currentPitchVelocity;
        previousPitch = currentPitch;
        lastMovementUpdateTime = currentTime;
    }

    private double calculateAngularVelocity(double previousAngle, double currentAngle, double deltaTime) {
        
        double delta = currentAngle - previousAngle;
        delta = (delta + 180) % 360 - 180; 
        return delta / deltaTime;
    }

    
    public void flagDetection(AimDetectionStrategy strategy, String reason) {
        if (playerData.getCheckManager().getCheck(AnalysisG.class).flagAndAlert(strategy.getCheckName() + " | " + reason)) {
            playerData.mitigateDamage();
        }
    }

    
    public float getAngleDifference(float yaw1, float yaw2) {
        return Rotation.getAngleDifference(yaw1, yaw2);
    }

    
    public long getGcd(long a, long b) {
        return MathUtil.getGcd(a, b);
    }

    
    public void updateTargetInfo(double distance) {
        this.distanceToTarget = distance;
    }

    public void updateRotation(Rotation rotation) {
        this.lastRotation = this.currentRotation;
        this.currentRotation = rotation;

        if (rotationHistory.size() >= 100) {
            rotationHistory.poll();
        }
        rotationHistory.add(rotation);
    }

    public void updateRotationDelta(Rotation rotation) {
        if (rotationDeltaHistory.size() >= 100) {
            rotationDeltaHistory.poll();
        }
        rotationDeltaHistory.add(rotation);

        avgYaw = avgPitch = maxYaw = maxPitch = 0;
        for (Rotation rotation1 : rotationDeltaHistory) {
            avgYaw += Math.abs(rotation1.getYaw());
            avgPitch += Math.abs(rotation1.getPitch());
            if (Math.abs(rotation1.getYaw()) > maxYaw) {
                maxYaw = Math.abs(rotation1.getYaw());
            }
            if (Math.abs(rotation1.getPitch()) > maxPitch) {
                maxPitch = Math.abs(rotation1.getPitch());
            }
        }
        avgYaw /= rotationDeltaHistory.size();
        avgPitch /= rotationDeltaHistory.size();
    }


    public boolean isAimingAtTarget() {
        return distanceToTarget < 4;
    }
}