package cn.aetheris.yuki.predictionengine.movementtick;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public final class MovementTickerNautilus extends MovementTickerLivingVehicle {

    private static final float WATER_BUOYANCY = 0.05F;
    private static final float WATER_DRAG = 0.5F;

    public MovementTickerNautilus(PlayerData player) {
        super(player);

        player.speed = (float) player.compensatedEntities.getSelf().getRiding().getAttributeValue(Attributes.MOVEMENT_SPEED);

        float horizInput = player.vehicleData.vehicleHorizontal * 0.5F;
        float forwardsInput = player.vehicleData.vehicleForward;

        if (forwardsInput <= 0.0F) {
            forwardsInput *= 0.25F;
        }

        this.movementInput = new Vector3dm(horizInput, 0, forwardsInput);
        if (movementInput.lengthSquared() > 1) movementInput.normalize();
    }

    public static void floatNautilus(PlayerData player) {
        if (player.wasTouchingWater) {
            if (isAboveWaterSurface(player)) {
                player.onGround = true;
            } else {
                player.clientVelocity.multiply(WATER_DRAG).add(new Vector3dm(0, WATER_BUOYANCY, 0));
            }
        }
    }

    public static boolean isAboveWaterSurface(PlayerData player) {
        return player.y > Math.floor(player.y) + 0.5 - 1.0E-5F;
    }

    @Override
    public void livingEntityAIStep() {
        super.livingEntityAIStep();
        floatNautilus(player);
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) Collisions.handleInsideBlocks(player);
    }
}
