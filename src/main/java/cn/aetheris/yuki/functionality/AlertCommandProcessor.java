package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import github.scarsz.configuralize.DynamicConfig;

/**
 * Handles alert message rendering and command execution for punishment groups.
 */
public final class AlertCommandProcessor {

    private final PlayerData player;
    private final ViolationTracker violationTracker;
    private String experimentalSymbol;
    private String hoverAction;
    private String hoverMessage;

    public AlertCommandProcessor(PlayerData player, ViolationTracker violationTracker) {
        this.player = player;
        this.violationTracker = violationTracker;
    }

    public void reload() {
        DynamicConfig config = PluginLoader.INSTANCE.getConfigManager().getConfig();
        experimentalSymbol = config.getStringElse("output.alerts.expsymbol", "*");
        hoverAction = config.getString("commands.action");
        hoverMessage = config.getString("output.hover");
    }

    public String replaceAlertPlaceholders(String original, int vl, Check check, String alertString, String verbose) {
        String result = PluginLoader.INSTANCE.getLangManager().format(original
                .replace("[alert]", alertString)
                .replace("[proxy]", alertString)
                .replace("%check_name%", check.getCheckName())
                .replace("%max_vl%", Integer.toString(check.getMaxVL()))
                .replace("%vl%", Integer.toString(vl))
                .replace("%add%", String.format("%.1f", Math.abs(check.lastViolations - check.violations)))
                .replace("%verbose%", verbose)
                .replace("%description%", check.getDescription())
                .replace("%exp%", check.getExperimental() ? experimentalSymbol : ""));
        return PluginLoader.INSTANCE.getExternalAPI().replaceVariables(player, result, true);
    }

    public String replaceHoverPlaceholders(Check check, String hoverString, String verbose, String... info) {
        String line = String.join("\n", hoverString);
        String details = String.join("\n", info);
        line = line.replace("%description%", check.getDescription())
                .replace("%check_name%", check.getCheckName())
                .replace("%info%", details.isEmpty() ? "None provided" : details)
                .replace("%verbose%", verbose);
        return PluginLoader.INSTANCE.getExternalAPI().replaceVariables(player, line, true);
    }

    public String getExperimentalSymbol() {
        return experimentalSymbol;
    }

    public String getHoverAction() {
        return hoverAction;
    }

    public String getHoverMessage() {
        return hoverMessage;
    }

    public ViolationTracker getViolationTracker() {
        return violationTracker;
    }
}