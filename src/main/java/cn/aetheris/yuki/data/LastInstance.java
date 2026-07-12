package cn.aetheris.yuki.data;

import cn.aetheris.yuki.player.PlayerData;

public final class LastInstance {
    int lastInstance = 100;

    public LastInstance(PlayerData player) {
        player.lastInstanceManager.addInstance(this);
    }

    public boolean hasOccurredSince(int time) {
        return lastInstance <= time;
    }

    public void reset() {
        lastInstance = 0;
    }

    public void tick() {
        
        
        if (lastInstance == Integer.MAX_VALUE) lastInstance = 100;
        lastInstance++;
    }
}
