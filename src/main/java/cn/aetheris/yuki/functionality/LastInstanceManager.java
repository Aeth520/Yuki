package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.util.LastInstance;
import cn.aetheris.yuki.util.update.PredictionComplete;

import java.util.ArrayList;
import java.util.List;

public final class LastInstanceManager extends Check implements PostPredictionCheck {
    private final List<LastInstance> instances = new ArrayList<>();

    public LastInstanceManager(PlayerData player) {
        super(player);
    }

    public void addInstance(LastInstance instance) {
        instances.add(instance);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        for (LastInstance instance : instances) {
            instance.tick();
        }
    }
}
