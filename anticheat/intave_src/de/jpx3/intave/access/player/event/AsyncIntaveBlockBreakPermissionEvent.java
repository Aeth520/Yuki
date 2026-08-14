/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Cancellable
 */
package de.jpx3.intave.access.player.event;

import de.jpx3.intave.access.IntaveEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public final class AsyncIntaveBlockBreakPermissionEvent
extends IntaveEvent
implements Cancellable {
    private Player player;
    private Block block;
    private boolean cancelled;

    private AsyncIntaveBlockBreakPermissionEvent() {
    }

    public Player player() {
        return this.player;
    }

    public Block block() {
        return this.block;
    }

    public void copy(Player player, Block block) {
        this.player = player;
        this.block = block;
        this.cancelled = false;
    }

    @Override
    public void referenceInvalidate() {
        this.player = null;
        this.block = null;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static AsyncIntaveBlockBreakPermissionEvent empty() {
        return new AsyncIntaveBlockBreakPermissionEvent();
    }
}

