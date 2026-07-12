package cn.aetheris.yuki.predictionengine.fluid;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.enums.FluidTag;

public final class StriderEntityFluidInteraction implements EntityFluidInteraction {

    @Override
    public boolean canStandOnFluid(PlayerData player, FluidTag tag) {
        return tag == FluidTag.LAVA;
    }

    @Override
    public boolean isAffectedByFluid(PlayerData player, FluidTag tag) {
        return tag == FluidTag.WATER && !player.isFlying;
    }

    @Override
    public double getFluidPushMultiplier(PlayerData player, FluidTag tag) {
        return tag == FluidTag.LAVA ? 0.0 : 0.014;
    }
}
