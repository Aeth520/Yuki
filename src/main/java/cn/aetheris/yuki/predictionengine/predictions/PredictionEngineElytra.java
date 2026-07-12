package cn.aetheris.yuki.predictionengine.predictions;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.VectorData;
import cn.aetheris.yuki.protocol.nms.ReachUtils;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PredictionEngineElytra extends PredictionEngine {
    public static Vector3dm getElytraMovement(PlayerData player, Vector3dm vector, Vector3dm lookVector) {
        float yRotRadians = player.pitch * 0.017453292F;
        double horizontalSqrt = Math.sqrt(lookVector.getX() * lookVector.getX() + lookVector.getZ() * lookVector.getZ());
        double horizontalLength = vector.clone().setY(0).length();
        double length = lookVector.length();

        
        double vertCosRotation = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_18_2) ? Math.cos(yRotRadians) : player.trigHandler.cos(yRotRadians);
        vertCosRotation = (float) (vertCosRotation * vertCosRotation * Math.min(1.0D, length / 0.4D));

        
        
        
        double recalculatedGravity = player.compensatedEntities.self.getAttributeValue(Attributes.GRAVITY);
        if (player.clientVelocity.getY() <= 0 && player.compensatedEntities.getSlowFallingAmplifier().isPresent()) {
            recalculatedGravity = player.getClientVersion().isOlderThan(ClientVersion.V_1_20_5) ? 0.01 : Math.min(recalculatedGravity, 0.01);
        }

        vector.add(new Vector3dm(0.0D, recalculatedGravity * (-1.0D + vertCosRotation * 0.75D), 0.0D));
        double d5;

        
        if (vector.getY() < 0.0D && horizontalSqrt > 0.0D) {
            d5 = vector.getY() * -0.1D * vertCosRotation;
            vector.add(new Vector3dm(lookVector.getX() * d5 / horizontalSqrt, d5, lookVector.getZ() * d5 / horizontalSqrt));
        }

        
        if (yRotRadians < 0.0F && horizontalSqrt > 0.0D) {
            d5 = horizontalLength * (double) (-player.trigHandler.sin(yRotRadians)) * 0.04D;
            vector.add(new Vector3dm(-lookVector.getX() * d5 / horizontalSqrt, d5 * 3.2D, -lookVector.getZ() * d5 / horizontalSqrt));
        }

        
        if (horizontalSqrt > 0) {
            vector.add(new Vector3dm((lookVector.getX() / horizontalSqrt * horizontalLength - vector.getX()) * 0.1D, 0.0D, (lookVector.getZ() / horizontalSqrt * horizontalLength - vector.getZ()) * 0.1D));
        }

        return vector;
    }

    
    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(PlayerData player, Set<VectorData> possibleVectors, float speed) {
        List<VectorData> results = new ArrayList<>();

        
        for (int shitmath = 0; shitmath <= 1; shitmath++, player.trigHandler.toggleShitMath()) {
            Vector3dm currentLook = ReachUtils.getLook(player, player.yaw, player.pitch);
            for (int applyStuckSpeed = 1; applyStuckSpeed >= 0; applyStuckSpeed--) {
                if (applyStuckSpeed == 0 && player.isForceStuckSpeed()) break;
                for (VectorData data : possibleVectors) {
                    Vector3dm elytraResult = getElytraMovement(player, data.vector.clone(), currentLook);
                    if (applyStuckSpeed != 0) elytraResult.multiply(player.stuckSpeedMultiplier);
                    elytraResult.multiply(new Vector3dm(0.99F, 0.98F, 0.99F));
                    VectorData modified = data.returnNewModified(elytraResult, VectorData.VectorType.InputResult);
                    modified.input = new Vector3dm(0, 0, 0);
                    results.add(modified);
                }
            }
        }

        return results;
    }

    
    @Override
    public void addJumpsToPossibilities(PlayerData player, Set<VectorData> existingVelocities) {
        new PredictionEngineNormal().addJumpsToPossibilities(player, existingVelocities);
    }
}