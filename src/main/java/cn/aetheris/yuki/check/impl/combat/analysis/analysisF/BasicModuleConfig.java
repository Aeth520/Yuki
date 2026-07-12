package cn.aetheris.yuki.check.impl.combat.analysis.analysisF;


public final class BasicModuleConfig {
    public boolean enabled;
    public double maxBuffer;
    public double failIncrease;

    public BasicModuleConfig(boolean enabled, double maxBuffer, double failIncrease) {
        this.enabled = enabled;
        this.maxBuffer = maxBuffer;
        this.failIncrease = failIncrease;
    }
}
