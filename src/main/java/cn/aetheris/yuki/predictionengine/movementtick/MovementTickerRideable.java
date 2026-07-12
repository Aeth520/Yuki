package cn.aetheris.yuki.predictionengine.movementtick;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntityRideable;
import cn.aetheris.yuki.protocol.nms.Collisions;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public class MovementTickerRideable extends MovementTickerLivingVehicle {

    public MovementTickerRideable(PlayerData player) {
        super(player);

        
        float f = getSteeringSpeed();

        PacketEntityRideable boost = ((PacketEntityRideable) player.compensatedEntities.getSelf().getRiding());

        
        if (boost.currentBoostTime++ < boost.boostTimeMax) {
            
            f += f * 1.15F * player.trigHandler.sin((float) boost.currentBoostTime / (float) boost.boostTimeMax * (float) Math.PI);
        }

        player.speed = f;

    }

    
    public float getSteeringSpeed() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void livingEntityTravel() {
        super.livingEntityTravel();
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) Collisions.handleInsideBlocks(player);
    }
}
