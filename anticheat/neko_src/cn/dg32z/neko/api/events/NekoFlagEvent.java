/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.bukkit.event.HandlerList
 */
package cn.dg32z.neko.api.events;

import cn.dg32z.libs.org.jetbrains.annotations.NotNull;
import cn.dg32z.neko.api.IHandler;
import cn.dg32z.neko.api.PlayerAPI;
import cn.dg32z.neko.api.enums.Category;
import cn.dg32z.neko.api.events.NekoCancellableEvent;
import lombok.Generated;
import org.bukkit.event.HandlerList;

public class NekoFlagEvent
extends NekoCancellableEvent {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerAPI playerAPI;
    private final IHandler check;

    public NekoFlagEvent(PlayerAPI playerAPI, IHandler check) {
        this.playerAPI = playerAPI;
        this.check = check;
    }

    public PlayerAPI getPlayer() {
        return this.playerAPI;
    }

    public double getViolations() {
        return this.check.getViolations();
    }

    public Category getCheckType() {
        return this.check.getCheckType();
    }

    public boolean isSetback() {
        return this.check.getViolations() > this.check.getSetbackVL();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Generated
    public PlayerAPI getPlayerAPI() {
        return this.playerAPI;
    }

    @Generated
    public IHandler getCheck() {
        return this.check;
    }
}
