/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package de.jpx3.intave.access.check;

import de.jpx3.intave.access.check.Check;
import de.jpx3.intave.access.check.CheckStatisticsAccess;
import de.jpx3.intave.access.check.MitigationStrategy;
import de.jpx3.intave.access.player.UnknownPlayerException;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public interface CheckAccess {
    public String name();

    public Check enumCheck();

    public boolean enabled();

    default public double violationLevelOf(Player player) throws UnknownPlayerException {
        return this.violationLevelOf(player, "thresholds");
    }

    public double violationLevelOf(Player var1, String var2) throws UnknownPlayerException;

    default public void addViolationPoints(Player player, double amount) throws UnknownPlayerException {
        this.addViolationPoints(player, "thresholds", amount);
    }

    public void addViolationPoints(Player var1, String var2, double var3) throws UnknownPlayerException;

    default public void resetViolationLevel(Player player) throws UnknownPlayerException {
        this.resetViolationLevel(player, "thresholds");
    }

    public void resetViolationLevel(Player var1, String var2) throws UnknownPlayerException;

    default public MitigationStrategy mitigationStrategy() {
        return MitigationStrategy.NOT_SUPPORTED;
    }

    default public void setMitigationStrategy(MitigationStrategy mitigationStrategy) {
        throw new UnsupportedOperationException("Check " + this.name() + " does not support a mitigation strategy");
    }

    public Map<Integer, List<String>> commandsOf(String var1);

    public CheckStatisticsAccess statistics();
}

