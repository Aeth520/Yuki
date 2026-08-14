package cn.aetheris.yuki.functionality.moderation;

import cn.aetheris.yuki.Yuki;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ModerationManager {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([smhdwMy])", Pattern.CASE_INSENSITIVE);

    public void kick(Player player, String reason) {
        if (player == null) return;
        Bukkit.getScheduler().runTask(Yuki.getInstance(), () ->
                player.kickPlayer(reason)
        );
    }

    public void ban(String playerName, String reason, String duration) {
        Date expiry = parseDuration(duration);
        Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, reason, expiry, "Yuki");
        Player online = Bukkit.getPlayerExact(playerName);
        if (online != null) {
            kick(online, reason);
        }
    }

    public void banIp(String ipAddress, String reason, String duration) {
        Date expiry = parseDuration(duration);
        Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
            Bukkit.getBanList(BanList.Type.IP).addBan(ipAddress, reason, expiry, "Yuki");
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getAddress() != null && ipAddress.equals(online.getAddress().getAddress().getHostAddress())) {
                    online.kickPlayer(reason);
                }
            }
        });
    }

    public void warn(Player player, String message) {
        if (player == null) return;
        player.sendMessage(message);
    }

    public void unban(String playerName) {
        Bukkit.getScheduler().runTask(Yuki.getInstance(), () ->
                Bukkit.getBanList(BanList.Type.NAME).pardon(playerName)
        );
    }

    public void unbanIp(String ipAddress) {
        Bukkit.getScheduler().runTask(Yuki.getInstance(), () ->
                Bukkit.getBanList(BanList.Type.IP).pardon(ipAddress)
        );
    }

    private Date parseDuration(String duration) {
        if (duration == null || duration.isBlank()) return null;
        String lower = duration.trim().toLowerCase();
        if (lower.equals("permanent") || lower.equals("perm") || lower.equals("-1")) {
            return null;
        }
        Matcher matcher = DURATION_PATTERN.matcher(lower);
        long totalMillis = 0;
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            totalMillis += switch (unit) {
                case "s" -> TimeUnit.SECONDS.toMillis(value);
                case "m" -> TimeUnit.MINUTES.toMillis(value);
                case "h" -> TimeUnit.HOURS.toMillis(value);
                case "d" -> TimeUnit.DAYS.toMillis(value);
                case "w" -> TimeUnit.DAYS.toMillis(value * 7);
                case "M" -> TimeUnit.DAYS.toMillis(value * 30);
                case "y" -> TimeUnit.DAYS.toMillis(value * 365);
                default -> 0;
            };
        }
        if (!matched) return null;
        return new Date(System.currentTimeMillis() + totalMillis);
    }
}
