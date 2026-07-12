package cn.aetheris.yuki.predictionengine.fluid;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.enums.FluidTag;

public interface EntityFluidInteraction {

    boolean canStandOnFluid(PlayerData player, FluidTag tag);

    boolean isAffectedByFluid(PlayerData player, FluidTag tag);

    double getFluidPushMultiplier(PlayerData player, FluidTag tag);
}
