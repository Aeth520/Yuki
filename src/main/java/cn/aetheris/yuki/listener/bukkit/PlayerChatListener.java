package cn.aetheris.yuki.listener.bukkit;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.misc.spam.SpamB;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class PlayerChatListener extends AbstractListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        long now = System.currentTimeMillis();

        PlayerData data = getData(player);
        if (data == null) return;

        SpamB check = data.getCheckManager().getCheck(SpamB.class);
        if (check == null || !check.shouldModifyPackets()) return;

        data.runSafely(() -> {
            handleFrequencyCheck(event, now, check);

            if (checkRepeatedMessage(message, check) && check.buffer++ > 2) {
                flagAndCancel(event, check, "repeat_message m= " + message);
                return;
            }

            if (checkSimilarMessages(message, check) && check.buffer++ > 3.5) {
                flagAndCancel(event, check, "similar_messages m= " + message);
                return;
            }

            updateMessageHistory(message, check);
        });
    }

    private void handleFrequencyCheck(AsyncPlayerChatEvent event, long now, SpamB check) {
        check.messageTimestamps.add(now);
        while (check.messageTimestamps.size() > check.fastThreshold) {
            check.messageTimestamps.poll();
        }

        if (check.messageTimestamps.size() >= check.fastThreshold) {
            long timeWindow = 0;
            if (check.messageTimestamps.peek() != null) {
                timeWindow = now - check.messageTimestamps.peek();
            }
            if (timeWindow < 1000L) {
                flagAndCancel(event, check, "fast_messages");
            }
        }
    }

    private boolean checkRepeatedMessage(String message, SpamB check) {
        return !check.messageHistory.isEmpty() &&
                message.equalsIgnoreCase(check.messageHistory.peek());
    }

    private boolean checkSimilarMessages(String message, SpamB check) {
        if (check.messageHistory.size() < check.messageHistorySize - 1) return false;

        int totalDiff = 0;
        int totalLength = message.length();

        for (String prev : check.messageHistory) {
            totalDiff += calculateLevenshtein(message, prev);
            totalLength += prev.length();
        }

        double similarity = 1 - (double) totalDiff / totalLength;
        return similarity > check.similarityThreshold;
    }

    private void updateMessageHistory(String message, SpamB check) {
        check.messageHistory.add(message);
        while (check.messageHistory.size() > check.messageHistorySize) {
            check.messageHistory.poll();
        }
    }

    private int calculateLevenshtein(String s, String t) {
        if (s.equalsIgnoreCase(t)) return 0;

        int n = s.length();
        int m = t.length();
        if (n == 0) return m;
        if (m == 0) return n;

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        for (int j = 0; j <= m; j++) prev[j] = j;

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = (s.charAt(i - 1) == t.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[m];
    }

    private void flagAndCancel(AsyncPlayerChatEvent event, SpamB check, String type) {
        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            if (check.flagAndAlert("type= " + type)) {
                if (check.getViolations() >= check.cancelVL && check.cancelEnabled) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(
                            PluginLoader.INSTANCE.getLangManager().i18n("stop-chat")
                    );
                }
            }
        });
    }
}
