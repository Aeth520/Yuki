/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Cancellable
 */
package de.jpx3.intave.access.check.event;

import com.google.common.base.Preconditions;
import de.jpx3.intave.IntaveAccessor;
import de.jpx3.intave.access.IntaveEvent;
import de.jpx3.intave.access.check.Check;
import de.jpx3.intave.access.player.PlayerAccess;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public final class IntaveViolationEvent
extends IntaveEvent
implements Cancellable {
    private Player punished;
    private String checkName;
    private String message;
    private String details;
    private double vlBefore;
    private double vlAfter;
    private Reaction reaction = Reaction.INTERRUPT_AND_REPORT;
    private static final double REDUCE_APPLIER = 1000.0;

    private IntaveViolationEvent() {
    }

    public void copy(Player punished, String checkName, String message, String details, double vlBefore, double vlAfter) {
        this.punished = punished;
        this.checkName = checkName;
        this.message = message;
        this.details = details;
        this.vlBefore = vlBefore;
        this.vlAfter = vlAfter;
        this.reaction = Reaction.INTERRUPT_AND_REPORT;
        this.setCancelled(false);
    }

    public Player player() {
        return this.punished;
    }

    public PlayerAccess playerAccess() {
        return IntaveAccessor.unsafeAccess().player(this.player());
    }

    @Deprecated
    public String check() {
        return this.checkName;
    }

    public String checkName() {
        return this.checkName;
    }

    public Check checkEnum() {
        return Check.fromName(this.checkName);
    }

    public String message() {
        if (this.details.isEmpty()) {
            return this.message;
        }
        return this.message + " (" + this.details.trim() + ")";
    }

    public String details() {
        return this.details;
    }

    public String compactMessage() {
        return this.message;
    }

    public double addedViolationPoints() {
        return this.reducePrecision(this.vlAfter - this.vlBefore);
    }

    private double reducePrecision(double input) {
        return (double)Math.round(input * 1000.0) / 1000.0;
    }

    public double violationLevelBeforeViolation() {
        return this.vlBefore;
    }

    public double violationLevelAfterViolation() {
        return this.vlAfter;
    }

    @Deprecated
    public boolean isCancelled() {
        return this.reaction != Reaction.INTERRUPT_AND_REPORT;
    }

    @Deprecated
    public void setCancelled(boolean cancelled) {
        this.suggestReaction(cancelled ? Reaction.IGNORE : Reaction.INTERRUPT_AND_REPORT);
    }

    public void suggestReaction(Reaction reaction) {
        Preconditions.checkNotNull((Object)((Object)reaction));
        this.reaction = reaction;
    }

    public Reaction reaction() {
        return this.reaction;
    }

    @Override
    public void referenceInvalidate() {
        this.punished = null;
    }

    public static IntaveViolationEvent empty() {
        return new IntaveViolationEvent();
    }

    public static enum Reaction {
        IGNORE,
        INTERRUPT,
        INTERRUPT_AND_REPORT;

    }
}

