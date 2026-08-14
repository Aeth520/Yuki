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
import de.jpx3.intave.access.IntaveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public final class IntaveCommandExecutionEvent
extends IntaveEvent
implements Cancellable {
    private Player punished;
    private String command;
    private String check;
    private String violationMessage;
    private String violationDetails;
    private double activationVL;
    private boolean delayedExecution;
    private boolean cancelled;

    private IntaveCommandExecutionEvent() {
    }

    public void copy(Player punished, String command, String check, String violationMessage, String violationDetails, double activationVL, boolean delayedExecute) {
        this.punished = punished;
        this.command = command;
        this.check = check;
        this.violationMessage = violationMessage;
        this.violationDetails = violationDetails;
        this.activationVL = activationVL;
        this.delayedExecution = delayedExecute;
        this.setCancelled(false);
    }

    public Player player() {
        return this.punished;
    }

    public String command() {
        return this.command;
    }

    public void setCommand(String command) {
        Preconditions.checkNotNull((Object)command);
        this.command = command;
    }

    public String violationCheck() {
        return this.check;
    }

    public String violationMessage() {
        return this.violationMessage;
    }

    public String violationDetails() {
        return this.violationDetails;
    }

    public double activationVL() {
        return this.activationVL;
    }

    public boolean delayedExecute() {
        return this.delayedExecution;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public void referenceInvalidate() {
        this.punished = null;
    }

    public static IntaveCommandExecutionEvent empty() {
        return new IntaveCommandExecutionEvent();
    }
}

