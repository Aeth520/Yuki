/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.bukkit.event.Cancellable
 */
package cn.dg32z.neko.api.events;

import cn.dg32z.neko.api.events.NekoEvent;
import lombok.Generated;
import org.bukkit.event.Cancellable;

public abstract class NekoCancellableEvent
extends NekoEvent
implements Cancellable {
    private boolean cancelled;

    @Generated
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Generated
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
