package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.check.Check;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player violation tracking with time-based expiry.
 */
public final class ViolationTracker {

    @Getter
    private final List<PunishGroup> groups = new ArrayList<>();

    void setGroups(List<PunishGroup> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
    }

    public void handleViolation(Check check) {
        long currentTime = System.currentTimeMillis();
        for (PunishGroup group : groups) {
            if (group.checks.contains(check)) {
                group.violations.put(currentTime, check);

                List<Long> keysToRemove = new ArrayList<>();
                for (Map.Entry<Long, Check> entry : group.violations.entrySet()) {
                    if (currentTime - entry.getKey() > group.removeViolationsAfter) {
                        keysToRemove.add(entry.getKey());
                    }
                }
                for (Long key : keysToRemove) {
                    group.violations.remove(key);
                }
            }
        }
    }

    public int getViolations(PunishGroup group, Check check) {
        for (Check value : group.violations.values()) {
            if (value == check) {
                return (int) value.getViolations();
            }
        }
        return 0;
    }

    @Getter
    public static class PunishGroup {
        public final Map<Long, Check> violations = new ConcurrentHashMap<>();
        final List<AbstractCheck> checks;
        final List<ParsedCommand> commands;
        final long removeViolationsAfter; // milliseconds

        public PunishGroup(List<AbstractCheck> checks, List<ParsedCommand> commands, int removeViolationsAfterSeconds) {
            this.checks = checks;
            this.commands = commands;
            this.removeViolationsAfter = removeViolationsAfterSeconds * 1000L;
        }
    }

    @Getter
    public static class ParsedCommand {
        final int threshold;
        final int interval;
        int executeCount;
        final String command;

        public ParsedCommand(int threshold, int interval, String command) {
            this.threshold = threshold;
            this.interval = interval;
            this.command = command;
        }

        void incrementExecuteCount() {
            executeCount++;
        }
    }
}