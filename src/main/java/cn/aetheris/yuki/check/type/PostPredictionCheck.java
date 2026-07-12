package cn.aetheris.yuki.check.type;

import cn.aetheris.yuki.util.update.PredictionComplete;

public interface PostPredictionCheck extends PacketCheck {

    default void onPredictionComplete(final PredictionComplete predictionComplete) {
    }
}
