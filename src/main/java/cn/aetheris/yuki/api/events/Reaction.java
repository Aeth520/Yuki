package cn.aetheris.yuki.api.events;

/**
 * Reaction to a violation event, inspired by Intave's Reaction system.
 * Provides more granular control than a simple cancel/not-cancel binary.
 */
public enum Reaction {

    /** Completely ignore the violation — don't increment VL, don't alert, don't punish. */
    IGNORE,

    /** Record the violation (increment VL) but don't execute alerts or punishment commands. */
    INTERRUPT,

    /** Normal behavior — record the violation, send alerts, execute punishment commands. */
    INTERRUPT_AND_REPORT;

    /** @return true if this reaction should prevent VL increment */
    public boolean shouldSkipVL() {
        return this == IGNORE;
    }

    /** @return true if this reaction should prevent alert/verbose output */
    public boolean shouldSkipAlert() {
        return this != INTERRUPT_AND_REPORT;
    }

    /** @return true if this reaction should prevent punishment command execution */
    public boolean shouldSkipPunish() {
        return this != INTERRUPT_AND_REPORT;
    }
}