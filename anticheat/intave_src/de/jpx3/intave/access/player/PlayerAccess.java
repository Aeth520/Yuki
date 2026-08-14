/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access.player;

import de.jpx3.intave.access.check.Check;
import de.jpx3.intave.access.player.PlayerClicks;
import de.jpx3.intave.access.player.PlayerConnection;
import de.jpx3.intave.access.player.trust.TrustFactor;

public interface PlayerAccess {
    public int protocolVersion();

    public void setProtocolVersion(int var1);

    default public double violationLevel(Check check) {
        return this.violationLevel(check, "thresholds");
    }

    default public double violationLevel(String check) {
        return this.violationLevel(check, "thresholds");
    }

    default public double violationLevel(Check check, String threshold) {
        return this.violationLevel(check.typeName(), threshold);
    }

    public double violationLevel(String var1, String var2);

    default public void addViolationPoints(Check check, double amount) {
        this.addViolationPoints(check, "thresholds", amount);
    }

    default public void addViolationPoints(String check, double amount) {
        this.addViolationPoints(check, "thresholds", amount);
    }

    default public void addViolationPoints(Check check, String threshold, double amount) {
        this.addViolationPoints(check.typeName(), threshold, amount);
    }

    public void addViolationPoints(String var1, String var2, double var3);

    default public void resetViolationLevel(Check check) {
        this.resetViolationLevel(check, "thresholds");
    }

    default public void resetViolationLevel(String check) {
        this.resetViolationLevel(check, "thresholds");
    }

    default public void resetViolationLevel(Check check, String threshold) {
        this.resetViolationLevel(check.typeName(), threshold);
    }

    public void resetViolationLevel(String var1, String var2);

    public TrustFactor trustFactor();

    @Deprecated
    public void setTrustFactor(TrustFactor var1);

    public PlayerClicks clicks();

    public PlayerConnection connection();
}

