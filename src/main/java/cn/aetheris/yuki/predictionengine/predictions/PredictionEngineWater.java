package cn.aetheris.yuki.predictionengine.predictions;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.movement.VectorData;
import cn.aetheris.yuki.util.enums.FluidTag;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.protocol.nms.FluidFallingAdjustedMovement;
import cn.aetheris.yuki.protocol.nms.ReachUtils;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.util.HashSet;
import java.util.Set;

public class PredictionEngineWater extends PredictionEngine {
    private boolean isFalling;
    private double playerGravity;
    private float swimmingFriction;

    public static void staticVectorEndOfTick(PlayerData player, Vector3dm vector, float swimmingFriction, double playerGravity, boolean isFalling) {
        vector.multiply(new Vector3dm(swimmingFriction, 0.8F, swimmingFriction));
        Vector3dm fluidVector = FluidFallingAdjustedMovement.getFluidFallingAdjustedMovement(player, playerGravity, isFalling, vector);
        vector.setX(fluidVector.getX());
        vector.setY(fluidVector.getY());
        vector.setZ(fluidVector.getZ());
    }

    public static Set<VectorData> transformSwimmingVectors(PlayerData player, Set<VectorData> base) {
        Set<VectorData> swimmingVelocities = new HashSet<>();

        
        
        
        
        
        
        
        
        
        if ((player.wasEyeInWater || player.fluidOnEyes == FluidTag.WATER || player.isSwimming || player.wasSwimming) && !player.inVehicle()) {
            for (VectorData vector : base) {
                double lookYAmount = ReachUtils.getLook(player, player.yaw, player.pitch).getY();
                double scalar = lookYAmount < -0.2 ? 0.085 : 0.06;

                
                swimmingVelocities.add(vector.returnNewModified(new Vector3dm(vector.vector.getX(), vector.vector.getY() + ((lookYAmount - vector.vector.getY()) * scalar), vector.vector.getZ()), VectorData.VectorType.SwimmingSpace));

                
                
                
                
                

                
                swimmingVelocities.add(vector.returnNewModified(vector.vector, VectorData.VectorType.SurfaceSwimming));

            }
            return swimmingVelocities;
        }
        return base;
    }

    public void guessBestMovement(float swimmingSpeed, PlayerData player, boolean isFalling, double playerGravity, float swimmingFriction) {
        this.isFalling = isFalling;
        this.playerGravity = playerGravity;
        this.swimmingFriction = swimmingFriction;
        super.guessBestMovement(swimmingSpeed, player);
    }

    @Override
    public void addJumpsToPossibilities(PlayerData player, Set<VectorData> existingVelocities) {
        for (VectorData vector : new HashSet<>(existingVelocities)) {
            if (player.couldSkipTick && vector.isZeroPointZeroThree()) {
                double extraVelFromVertTickSkipUpwards = MathUtil.clamp(player.actualMovement.getY(), vector.vector.clone().getY(), vector.vector.clone().getY() + 0.05f);
                existingVelocities.add(new VectorData(vector.vector.clone().setY(extraVelFromVertTickSkipUpwards), vector, VectorData.VectorType.Jump));
            } else {
                existingVelocities.add(new VectorData(vector.vector.clone().add(new Vector3dm(0, 0.04f, 0)), vector, VectorData.VectorType.Jump));
            }

            if (player.slightlyTouchingWater && player.lastOnGround && !player.onGround) {
                Vector3dm withJump = vector.vector.clone();
                super.doJump(player, withJump);
                existingVelocities.add(new VectorData(withJump, vector, VectorData.VectorType.Jump));
            }
        }
    }

    @Override
    public void endOfTick(PlayerData player, double playerGravity) {
        super.endOfTick(player, playerGravity);

        for (VectorData vector : player.getPossibleVelocitiesMinusKnockback()) {
            staticVectorEndOfTick(player, vector.vector, swimmingFriction, playerGravity, isFalling);
        }
    }

    @Override
    public Set<VectorData> fetchPossibleStartTickVectors(PlayerData player) {
        
        if (player.lastWasClimbing == 0 && player.pointThreeEstimator.isNearClimbable() && (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14) || !Collisions.isEmpty(player, player.boundingBox.copy().expand(
                player.clientVelocity.getX(), 0, player.clientVelocity.getZ()).expand(0.5, -SimpleCollisionBox.COLLISION_EPSILON, 0.5)))) {
            player.lastWasClimbing = FluidFallingAdjustedMovement.getFluidFallingAdjustedMovement(player, playerGravity, isFalling, player.clientVelocity.clone().setY(0.2D * 0.8F)).getY();
        }

        Set<VectorData> baseVelocities = super.fetchPossibleStartTickVectors(player);

        return transformSwimmingVectors(player, baseVelocities);
    }
}