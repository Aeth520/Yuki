package cn.aetheris.yuki.api;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.api.enums.MitigationStrategy;

public interface AbstractCheck {

    String getCheckName();

    String getDescription();

    String getConfigName();

    CheckType getCheckType();

    double getViolations();

    void setViolations(double violations);

    int getMaxVL();

    double getDecay();

    double getSetbackVL();

    double getBuffer();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void reload();

    boolean getExperimental();

    /** Per-check minimum TPS before the check stops flagging. Default = global config value. */
    double getMaxTps();

    void setMaxTps(double maxTps);

    /** Per-check maximum MSPT before the check stops flagging. 0 = disabled. */
    double getMaxMspt();

    void setMaxMspt(double maxMspt);

    /**
     * Utility classes are infrastructure checks (e.g. ghost block detector, cooldown compensator)
     * that should NOT be treated as real detections — they are excluded from permission registration,
     * violation tracking, and GUI check listings.
     */
    boolean isUtilityClass();

    /** Per-check mitigation strategy controlling how aggressively the check responds. */
    MitigationStrategy getMitigationStrategy();

    void setMitigationStrategy(MitigationStrategy strategy);
}