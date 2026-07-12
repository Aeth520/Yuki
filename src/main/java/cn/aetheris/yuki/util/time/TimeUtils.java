package cn.aetheris.yuki.util.time;

import java.time.Duration;

public final class TimeUtils {
    
    public static boolean hasExpired(long timestamp, long seconds) {
        return System.currentTimeMillis() - timestamp > seconds * 1000L;
    }

    
    public static String formatCompactDuration(long duration) {
        if (duration < 1000) {
            return duration + "ms";
        }

        Duration d = Duration.ofMillis(duration);
        long days = d.toDays();
        if (days > 0) {
            return days + "d" + formatRemaining(d.minusDays(days));
        }

        long hours = d.toHours();
        if (hours > 0) {
            return hours + "h" + formatRemaining(d.minusHours(hours));
        }

        long minutes = d.toMinutes();
        if (minutes > 0) {
            return minutes + "m" + formatRemaining(d.minusMinutes(minutes));
        }

        long seconds = d.getSeconds();
        return seconds + "s";
    }

    
    private static String formatRemaining(Duration remaining) {
        if (remaining.toMillis() == 0) {
            return "";
        }
        return formatCompactDuration(remaining.toMillis());
    }

    
    public static boolean elapsed(long from, long required) {
        return System.currentTimeMillis() - from > required;
    }

    
    public static long elapsed(long starttime) {
        return System.currentTimeMillis() - starttime;
    }

    
    public static String formatRelativeTime(long createdAt) {
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - createdAt);

        if (duration.toDays() > 0) {
            return duration.toDays() + "天前";
        }
        if (duration.toHours() > 0) {
            return duration.toHours() + "小时前";
        }
        if (duration.toMinutes() > 0) {
            return duration.toMinutes() + "分钟前";
        }
        return duration.getSeconds() + "秒前";
    }
}