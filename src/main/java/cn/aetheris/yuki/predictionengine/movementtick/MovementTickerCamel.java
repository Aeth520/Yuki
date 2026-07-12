package cn.aetheris.yuki.predictionengine.movementtick;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntityCamel;

public final class MovementTickerCamel extends MovementTickerHorse {

    public MovementTickerCamel(PlayerData player) {
        super(player);
    }

    @Override
    public float getExtraSpeed() {
        PacketEntityCamel camel = (PacketEntityCamel) player.compensatedEntities.getSelf().getRiding();
        
        
        final boolean wantsToJump = player.vehicleData.horseJump > 0.0F && !player.vehicleData.horseJumping && player.lastOnGround;
        if (wantsToJump) return 0;
        return player.isSprinting && player.vehicleData.camelDashCooldown <= 0 && !camel.dashing ? 0.1f : 0.0f;
    }
}