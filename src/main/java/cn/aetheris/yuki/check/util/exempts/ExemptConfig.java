package cn.aetheris.yuki.check.util.exempts;

import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

import java.util.Collections;
import java.util.Set;

/**
 * Immutable exempt configuration for a single check, inspired by NekoAntiCheat's IHandler.Exempt.
 */
public record ExemptConfig(
    ExemptRoutine routine,
    ExemptCondition condition,
    Set<PacketTypeCommon> exemptPackets
) {
    public static final ExemptConfig NONE = new ExemptConfig(ExemptRoutine.NONE, ctx -> false, Collections.emptySet());

    public static ExemptConfig full(ExemptCondition condition) {
        return new ExemptConfig(ExemptRoutine.FULL, condition, Collections.emptySet());
    }

    public static ExemptConfig packetFilter(ExemptCondition condition, Set<PacketTypeCommon> packets) {
        return new ExemptConfig(ExemptRoutine.PACKET_FILTER, condition, packets);
    }

    public boolean shouldSkip(PacketTypeCommon packetType) {
        if (routine == ExemptRoutine.NONE) return false;
        if (routine == ExemptRoutine.FULL) return true;
        return exemptPackets != null && exemptPackets.contains(packetType);
    }
}