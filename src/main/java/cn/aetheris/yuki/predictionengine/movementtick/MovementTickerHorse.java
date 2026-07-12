package cn.aetheris.yuki.predictionengine.movementtick;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntityHorse;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public class MovementTickerHorse extends MovementTickerLivingVehicle {

    public MovementTickerHorse(PlayerData player) {
        super(player);

        PacketEntityHorse horsePacket = (PacketEntityHorse) player.compensatedEntities.getSelf().getRiding();

        if (!horsePacket.hasSaddle) return;

        player.speed = horsePacket.getAttributeValue(Attributes.MOVEMENT_SPEED) + getExtraSpeed();
        
        float horizInput = player.vehicleData.vehicleHorizontal * 0.5F;
        float forwardsInput = player.vehicleData.vehicleForward;

        if (forwardsInput <= 0.0F) {
            forwardsInput *= 0.25F;
        }

        this.movementInput = new Vector3dm(horizInput, 0, forwardsInput);
        if (movementInput.lengthSquared() > 1) movementInput.normalize();
    }

    public float getExtraSpeed() {
        return 0f;
    }

    @Override
    public void livingEntityAIStep() {
        super.livingEntityAIStep();
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) Collisions.handleInsideBlocks(player);
    }
}
