package cn.aetheris.yuki.check.util.exempts;

/**
 * Dynamic condition for exempt evaluation, inspired by NekoAntiCheat's ExternalCondition.
 * Evaluated per-check, per-packet to determine if the check should be skipped.
 */
@FunctionalInterface
public interface ExemptCondition {

    /**
     * @param context arbitrary context object (e.g. PlayerData, PacketReceiveEvent)
     * @return true if the check should be exempt
     */
    boolean assume(Object context);
}