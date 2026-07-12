package cn.aetheris.yuki.predictionengine.fluid;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.util.enums.FluidTag;

public final class DefaultEntityFluidInteraction implements EntityFluidInteraction {

    @Override
    public boolean canStandOnFluid(PlayerData player, FluidTag tag) {
        return false;
    }

    @Override
    public boolean isAffectedByFluid(PlayerData player, FluidTag tag) {
        if (player.isFlying) return false;
        PacketEntity riding = player.compensatedEntities.self.getRiding();
        if (riding != null && riding.isBoat) return false;
        return true;
    }

    @Override
    public double getFluidPushMultiplier(PlayerData player, FluidTag tag) {
        return tag == FluidTag.WATER ? 0.014 : 0.007;
    }
}
