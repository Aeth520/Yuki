package cn.aetheris.yuki.command.sub;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.functionality.PerformanceMonitor;
import cn.aetheris.yuki.functionality.PerformanceMonitor.CheckStats;
import cn.aetheris.yuki.functionality.PerformanceMonitor.TickStats;
import cn.aetheris.yuki.util.message.ColorUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Perf extends AbstractCommand {

    private static final String COLOR_GOOD = "&a";
    private static final String COLOR_WARN = "&e";
    private static final String COLOR_BAD = "&c";
    private static final String COLOR_LABEL = "&7";
    private static final String COLOR_ACCENT = "&3";
    private static final String COLOR_TEXT = "&f";

    public Perf() {
        super(
                "Show performance monitor statistics",
                "yuki.commands.perf",
                false
        );
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reset")) {
            PerformanceMonitor.getInstance().reset();
            cn.aetheris.yuki.check.CheckStatistics.resetAll();
            sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "Yuki " + "&8» " + COLOR_GOOD + "性能统计已重置"));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            PerformanceMonitor monitor = PerformanceMonitor.getInstance();
            TickStats tickStats = monitor.getTickStats();
            double tps = monitor.getTPS();
            double mspt = monitor.getMSPT();
            long totalChecks = monitor.getTotalChecks();
            int activeChecks = monitor.getCheckStats().size();
            List<Map.Entry<String, CheckStats>> slowest = monitor.getSlowestChecks(5);

            sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "=== Yuki 性能监控 ==="));
            sender.sendMessage(ColorUtils.color(
                    COLOR_LABEL + "TPS: " + colorByTps(tps) + String.format("%.1f", tps)
                            + " " + COLOR_LABEL + "MSPT: " + colorByMspt(mspt) + String.format("%.1f", mspt) + "ms"));
            sender.sendMessage(ColorUtils.color(
                    COLOR_LABEL + "每 tick 平均: " + colorByMspt(tickStats.getAverageMs()) + String.format("%.2f", tickStats.getAverageMs()) + "ms"
                            + " " + COLOR_LABEL + "最大: " + COLOR_BAD + String.format("%.2f", tickStats.getMaxMs()) + "ms"
                            + " " + COLOR_LABEL + "95分位: " + colorByMspt(tickStats.getP95Ms()) + String.format("%.2f", tickStats.getP95Ms()) + "ms"));
            sender.sendMessage(ColorUtils.color(
                    COLOR_LABEL + "采样 tick 数: " + COLOR_TEXT + tickStats.getSampleCount()
                            + " " + COLOR_LABEL + "活跃检查数: " + COLOR_TEXT + activeChecks
                            + " " + COLOR_LABEL + "总检查次数: " + COLOR_TEXT + totalChecks));

            if (slowest.isEmpty()) {
                sender.sendMessage(ColorUtils.color(COLOR_LABEL + "暂无检查统计数据"));
            } else {
                sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "--- 最慢检查 (Top 5) ---"));
                int rank = 1;
                for (Map.Entry<String, CheckStats> entry : slowest) {
                    CheckStats stats = entry.getValue();
                    String avgMs = String.format("%.3f", stats.getAverageMs());
                    String maxMs = String.format("%.3f", stats.getMaxNanos() / 1_000_000.0);
                    long count = stats.getCount();
                    sender.sendMessage(ColorUtils.color(
                            COLOR_LABEL + rank + ". " + COLOR_TEXT + entry.getKey()
                                    + " " + COLOR_LABEL + "- " + colorByMspt(stats.getAverageMs()) + avgMs + "ms"
                                    + " " + COLOR_LABEL + "(max " + COLOR_BAD + maxMs + "ms"
                                    + COLOR_LABEL + ", n=" + COLOR_TEXT + count + COLOR_LABEL + ")"));
                    rank++;
                }
            }

            sender.sendMessage(ColorUtils.color(COLOR_LABEL + "提示: 使用 " + COLOR_TEXT + "/yuki perf reset" + COLOR_LABEL + " 重置统计"));

            // --- Detection outcome statistics (Intave-style) ---
            List<Map.Entry<String, cn.aetheris.yuki.check.CheckStatistics>> topVl =
                    cn.aetheris.yuki.check.CheckStatistics.topByViolations(5);
            if (!topVl.isEmpty()) {
                sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "--- 检测统计: 违规最多 (Top 5) ---"));
                for (Map.Entry<String, cn.aetheris.yuki.check.CheckStatistics> entry : topVl) {
                    cn.aetheris.yuki.check.CheckStatistics s = entry.getValue();
                    if (s.totalViolations() == 0) break;
                    sender.sendMessage(ColorUtils.color(
                            COLOR_TEXT + entry.getKey()
                                    + COLOR_LABEL + " - 违规 " + COLOR_BAD + s.totalViolations()
                                    + COLOR_LABEL + " / 触发 " + COLOR_TEXT + s.totalProcessed()
                                    + COLOR_LABEL + " / 放行 " + COLOR_GOOD + s.totalPassed()));
                }
                List<Map.Entry<String, cn.aetheris.yuki.check.CheckStatistics>> topGated =
                        cn.aetheris.yuki.check.CheckStatistics.topByGated(3);
                boolean gatedHeaderSent = false;
                for (Map.Entry<String, cn.aetheris.yuki.check.CheckStatistics> entry : topGated) {
                    cn.aetheris.yuki.check.CheckStatistics s = entry.getValue();
                    if (s.totalFails() < 10) break;
                    if (!gatedHeaderSent) {
                        sender.sendMessage(ColorUtils.color(COLOR_ACCENT + "--- 被门控拦截最多 (Top 3, 可能配置问题) ---"));
                        gatedHeaderSent = true;
                    }
                    sender.sendMessage(ColorUtils.color(
                            COLOR_TEXT + entry.getKey()
                                    + COLOR_LABEL + " - 拦截 " + COLOR_WARN + s.totalFails()
                                    + COLOR_LABEL + " / 违规 " + COLOR_TEXT + s.totalViolations()));
                }
            }
        });
    }

    private String colorByTps(double tps) {
        if (tps >= 19.0) return COLOR_GOOD;
        if (tps >= 15.0) return COLOR_WARN;
        return COLOR_BAD;
    }

    private String colorByMspt(double mspt) {
        if (mspt < 1.0) return COLOR_GOOD;
        if (mspt < 3.0) return COLOR_WARN;
        return COLOR_BAD;
    }
}
