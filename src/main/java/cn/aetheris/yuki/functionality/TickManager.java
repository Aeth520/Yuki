package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.core.tick.Tickable;
import cn.aetheris.yuki.core.tick.impl.BlockDataTick;
import cn.aetheris.yuki.core.tick.impl.DataTick;
import cn.aetheris.yuki.core.tick.impl.InventoryTick;
import cn.aetheris.yuki.core.tick.impl.ResetTick;
import cn.aetheris.yuki.util.maps.ClassLoadingMap;

public final class TickManager {
    private final ClassLoadingMap<Tickable> syncTick;
    private final ClassLoadingMap<Tickable> asyncTick;
    public int currentTick;

    public TickManager() {
        syncTick = new ClassLoadingMap<>(null);
        syncTick.put(ResetTick.class, new ResetTick());

        asyncTick = new ClassLoadingMap<>(null);
        asyncTick.put(BlockDataTick.class, new BlockDataTick());
        asyncTick.put(DataTick.class, new DataTick());
        asyncTick.put(InventoryTick.class, new InventoryTick());
    }

    
    public void tickSync() {
        currentTick++;
        syncTick.forEachValue(Tickable::tick);
    }

    
    public void tickAsync() {
        asyncTick.forEachValue(Tickable::tick);
    }
}
