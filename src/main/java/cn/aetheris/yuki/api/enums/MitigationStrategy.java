package cn.aetheris.yuki.api.enums;

/**
 * Per-check mitigation strategy, inspired by Intave's MitigationStrategy.
 * Controls how aggressively a check responds to violations.
 */
public enum MitigationStrategy {

    /** Full mitigation: setback, shuffle hotbar, and reset use item. */
    AGGRESSIVE,

    /** Normal mitigation: setback and shuffle hotbar. */
    CAREFUL,

    /** Light mitigation: setback only. */
    LENIENT,

    /** No mitigation: flag and alert only, no player-side actions. */
    SILENT;

    public boolean allowsSetback() {
        return this != SILENT;
    }

    public boolean allowsShuffle() {
        return this == AGGRESSIVE || this == CAREFUL;
    }

    public boolean allowsResetUseItem() {
        return this == AGGRESSIVE;
    }
}