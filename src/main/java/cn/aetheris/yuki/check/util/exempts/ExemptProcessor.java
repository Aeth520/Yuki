package cn.aetheris.yuki.check.util.exempts;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

import java.util.function.Function;

/**
 * Per-player unified exemption decision point.
 * Consolidates all exemption checks — bypass, permission, TPS, ExemptType, ExemptConfig —
 * into a single evaluation chain, inspired by NekoAntiCheat and Intave patterns.
 */
public final class ExemptProcessor {

    final PlayerData data;

    public ExemptProcessor(final PlayerData data) {
        this.data = data;
    }

    // ============================
    //  Unified Decision Methods
    // ============================

    /**
     * Full packet-processing check: is the check allowed to process packets at all?
     * Checks: enabled, bypass, no-modify-packet permission, exempted.
     */
    public boolean canProcess(AbstractCheck check) {
        if (!check.isEnabled()) return false;
        if (data.bypass) return false;
        if (data.noModifyPacketPermission) return false;
        if (check.isUtilityClass()) return false;
        return true;
    }

    /**
     * Full flagging check: is the check allowed to flag/alert?
     * Checks: bypass, exempted, TPS threshold.
     * Global factors (experimental mode, server lag) are handled in Check.java.
     */
    public boolean canFlag(AbstractCheck check) {
        if (data.bypass) return false;

        if (!canProcess(check)) return false;

        if (check.getMaxTps() > 0 && data.getTPS() < check.getMaxTps()) return false;

        return true;
    }

    // ============================
    //  Legacy ExemptType API
    // ============================

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

    // ============================
    //  Routine-based ExemptConfig API
    // ============================

    /**
     * Check if the given config should exempt this check.
     * Evaluates the condition first, then the routine.
     */
    public boolean shouldExempt(ExemptConfig config) {
        if (config == null || config.routine() == ExemptRoutine.NONE) return false;
        return config.condition().assume(data);
    }

    /**
     * Check if the given config should exempt this check for a specific packet type.
     */
    public boolean shouldExempt(ExemptConfig config, PacketTypeCommon packetType) {
        if (!shouldExempt(config)) return false;
        return config.shouldSkip(packetType);
    }
}