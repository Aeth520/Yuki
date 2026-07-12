package cn.aetheris.yuki.predictionengine.movementtick;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntityRideable;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;

public final class MovementTickerPig extends MovementTickerRideable {
    public MovementTickerPig(PlayerData player) {
        super(player);
        movementInput = new Vector3dm(0, 0, 1);
    }

    @Override
    public float getSteeringSpeed() { 
        PacketEntityRideable pig = (PacketEntityRideable) player.compensatedEntities.getSelf().getRiding();
        return (float) pig.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225f;
    }
}
