package cn.aetheris.yuki.check.util.exempts;

/**
 * Three-tier exempt routine, inspired by NekoAntiCheat.
 * <ul>
 *   <li>NONE — check runs normally</li>
 *   <li>FULL — skip all processing for this check</li>
 *   <li>PACKET_FILTER — skip only for specific packet types</li>
 * </ul>
 */
public enum ExemptRoutine {
    NONE,
    FULL,
    PACKET_FILTER
}