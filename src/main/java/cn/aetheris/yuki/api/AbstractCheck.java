package cn.aetheris.yuki.api;

import cn.aetheris.yuki.api.enums.CheckType;

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

}