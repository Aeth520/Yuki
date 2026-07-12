package cn.aetheris.yuki.util.latency;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;

import java.util.HashSet;
import java.util.Set;


public final class CompensatedFireworks extends Check implements PostPredictionCheck {

    
    private final Set<Integer> activeFireworks = new HashSet<>();
    private final Set<Integer> fireworksToRemoveNextTick = new HashSet<>();

    public CompensatedFireworks(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        
        
        activeFireworks.removeAll(fireworksToRemoveNextTick);
        fireworksToRemoveNextTick.clear();
    }

    public boolean hasFirework(int entityId) {
        return activeFireworks.contains(entityId);
    }

    public void addNewFirework(int entityID) {
        activeFireworks.add(entityID);
    }

    public void removeFirework(int entityID) {
        if (activeFireworks.contains(entityID)) {
            fireworksToRemoveNextTick.add(entityID);
        }
    }

    public int getMaxFireworksAppliedPossible() {
        return activeFireworks.size();
    }
}
