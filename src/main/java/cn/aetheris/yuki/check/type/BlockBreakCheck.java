package cn.aetheris.yuki.check.type;

import cn.aetheris.yuki.util.update.BlockBreak;

public interface BlockBreakCheck extends PostPredictionCheck {
    default void onBlockBreak(final BlockBreak blockBreak) {
    }

    default void onPostFlyingBlockBreak(final BlockBreak blockBreak) {
    }
}