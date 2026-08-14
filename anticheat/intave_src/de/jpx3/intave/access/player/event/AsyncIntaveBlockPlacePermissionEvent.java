/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Cancellable
 */
package de.jpx3.intave.access.player.event;

import de.jpx3.intave.access.IntaveEvent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public final class AsyncIntaveBlockPlacePermissionEvent
extends IntaveEvent
implements Cancellable {
    private Player player;
    private World world;
    private boolean mainHand;
    private int blockX;
    private int blockY;
    private int blockZ;
    private int enumDirection;
    private Material type;
    private int variant;
    private boolean cancelled;

    protected AsyncIntaveBlockPlacePermissionEvent() {
    }

    public void copy(Player player, World world, boolean mainHand, int blockX, int blockY, int blockZ, int enumDirection, Material type, int variant) {
        this.player = player;
        this.world = world;
        this.mainHand = mainHand;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.enumDirection = enumDirection;
        this.type = type;
        this.variant = variant;
        this.cancelled = false;
    }

    public Player player() {
        return this.player;
    }

    public World world() {
        return this.world;
    }

    public boolean isMainHand() {
        return this.mainHand;
    }

    public int blockX() {
        return this.blockX;
    }

    public int blockY() {
        return this.blockY;
    }

    public int blockZ() {
        return this.blockZ;
    }

    public int enumDirection() {
        return this.enumDirection;
    }

    public Material type() {
        return this.type;
    }

    public int variant() {
        return this.variant;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public void referenceInvalidate() {
        this.player = null;
        this.world = null;
    }

    public static AsyncIntaveBlockPlacePermissionEvent empty() {
        return new AsyncIntaveBlockPlacePermissionEvent();
    }
}

