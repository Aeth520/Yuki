/*
 * Decompiled with CFR 0.152.
 */
package cn.dg32z.neko.api;

import cn.dg32z.libs.com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import cn.dg32z.neko.api.enums.Category;
import java.util.Set;

public interface IHandler {
    public String getCheckName();

    public String getDescription();

    public String getConfigName();

    public Category getCheckType();

    public double getViolations();

    public void setViolations(double var1);

    public int getMaxVL();

    public double getDecay();

    public double getSetbackVL();

    public double getBuffer();

    public double getMaxTps();

    public double getMaxMspt();

    public void setMaxTps(double var1);

    public void setMaxMspt(double var1);

    public void setBuffer(double var1);

    public boolean isEnabled();

    public void setEnabled(boolean var1);

    public boolean isUtilityClass();

    public void reload();

    public boolean getExperimental();

    public Exempt exemptData(int var1);

    default public boolean shouldSkipForExempt(int channelId) {
        return false;
    }

    default public boolean hasExempt() {
        return false;
    }

    default public void refreshExemptPermissions() {
    }

    public record Exempt(ExemptRoutine routine, ExternalCondition condition, Set<PacketTypeCommon> exemptPackets) {
    }

    @FunctionalInterface
    public static interface ExternalCondition {
        public boolean assume(Object var1);
    }

    public static enum ExemptRoutine {
        NONE,
        FULL,
        PACKET_FILTER;

    }
}
