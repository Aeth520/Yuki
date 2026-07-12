package cn.aetheris.yuki.check.util.exempts;

import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;

import java.util.function.Function;

public final class ExemptProcessor {

    final PlayerData data;

    public ExemptProcessor(final PlayerData data) {
        this.data = data;
    }

    public boolean isExempt(final ExemptType exemptType) {
        if (exemptType == null || exemptType.getException() == null) {
            return false;
        }
        
        return exemptType.getException().apply(data);
    }

    public boolean isExempt(final ExemptType... exemptTypes) {
        for (final ExemptType exemptType : exemptTypes) {
            
            if (this.isExempt(exemptType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isExempt(final Function<PlayerData, Boolean> exception) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception function cannot be null");
        }
        return exception.apply(data);
    }
}
