package cn.aetheris.yuki.check.util.handler;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingQueue;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.update.RotationUpdate;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RotationDebugHandler extends Check implements RotationCheck {

    private static final String GOLD = "§6";
    private static final String GRAY = "§7";

    final Set<Player> listeners = new CopyOnWriteArraySet<>(new HashSet<>());
    boolean outputToConsole = false;

    boolean enabledFlags = false;
    boolean lastRotationIsFlag = false;

    EvictingQueue<String> yawHistory = new EvictingQueue<>(3);
    EvictingQueue<String> pitchHistory = new EvictingQueue<>(3);
    EvictingQueue<String> divisorHistory = new EvictingQueue<>(3);

    public RotationDebugHandler(PlayerData player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        final float deltaYaw = rotationUpdate.getProcessor().getDeltaYaw();
        final float deltaPitch = rotationUpdate.getProcessor().getDeltaPitch();
        final float accelYaw = Math.abs(rotationUpdate.getProcessor().getYawAccel());
        final float accelPitch = Math.abs(rotationUpdate.getProcessor().getPitchAccel());
        final float currentYaw = rotationUpdate.getProcessor().wrapDegrees(rotationUpdate.getProcessor().getYaw());
        final float currentPitch = rotationUpdate.getProcessor().getPitch();
        final double divisorX = rotationUpdate.getProcessor().getDivisorX();
        final double divisorY = rotationUpdate.getProcessor().getDivisorY();

        boolean isSuspiciousYaw = deltaYaw > 1.5f && deltaPitch > 2.5F && accelYaw > 3.0f;
        boolean isSuspiciousPitch = deltaPitch > 1.2f && accelPitch > 2.5f;

        if (listeners.isEmpty() && !outputToConsole) return;

        String yawColor = pickColor(deltaYaw, 10.0f, 10.0f);
        String pitchColor = pickColor(deltaPitch, 10.0f, 10.5f);
        String accelYawColor = pickColor(accelYaw, 12.0f, 7.0f);
        String accelPitchColor = pickColor(accelPitch, 9.0f, 9.0f);

        String header = GOLD + "----- Rotation Debug -----";
        String yawInfo = String.format("%sYaw: %.2f Δ: %s (%s) A: %.2f (%s)",
                yawColor, currentYaw, deltaYaw, getDirection(rotationUpdate.getDeltaYaw()), accelYaw, accelYawColor + "▲");
        String pitchInfo = String.format("%sPitch: %.2f Δ: %s (%s) A: %.2f (%s)",
                pitchColor, currentPitch, deltaPitch, getDirection(rotationUpdate.getDeltaPitch()), accelPitch, accelPitchColor + "▲");
        String divisorInfo = String.format("%sDivisor: X=%.3f Y=%.3f",
                GRAY, divisorX, divisorY);

        String prefix = player.bukkitPlayer == null ? "null" : player.bukkitPlayer.getName() + " ";

        boolean thisFlag = isSuspiciousYaw || isSuspiciousPitch;

        manageHistory(yawInfo, pitchInfo, divisorInfo, thisFlag);

        if (shouldSendMessages(thisFlag)) {
            sendDebugMessages(header, yawInfo, pitchInfo, divisorInfo, prefix);
        }

        listeners.removeIf(player -> !player.isOnline());
    }

    private void manageHistory(String yaw, String pitch, String divisor, boolean isFlag) {
        if (enabledFlags) {
            if (isFlag) {
                yawHistory.add(yaw);
                pitchHistory.add(pitch);
                divisorHistory.add(divisor);
            } else if (lastRotationIsFlag) {
                yawHistory.clear();
                pitchHistory.clear();
                divisorHistory.clear();
            }
            lastRotationIsFlag = isFlag;
        }
    }

    private boolean shouldSendMessages(boolean currentFlag) {
        return (enabledFlags && currentFlag) || (!enabledFlags && (!listeners.isEmpty() || outputToConsole));
    }

    private void sendDebugMessages(String header, String yaw, String pitch, String divisor, String prefix) {
        for (Player listener : listeners) {
            boolean isSelf = listener == getPlayer().bukkitPlayer;


            if (enabledFlags) {
                listener.sendMessage(header);
                for (int i = 0; i < yawHistory.size(); i++) {
                    listener.sendMessage((isSelf ? "" : prefix) + yawHistory.get(i));
                    listener.sendMessage((isSelf ? "" : prefix) + pitchHistory.get(i));
                    listener.sendMessage((isSelf ? "" : prefix) + divisorHistory.get(i));
                }
            } else {
                listener.sendMessage((isSelf ? "" : prefix) + yaw);
                listener.sendMessage((isSelf ? "" : prefix) + pitch);
                listener.sendMessage((isSelf ? "" : prefix) + divisor);
            }
        }

        if (outputToConsole) {
            LogUtils.console(prefix + yaw);
            LogUtils.console(prefix + pitch);
            LogUtils.console(prefix + divisor);
        }
    }

    private String getDirection(float delta) {
        if (delta == 0) return "■";
        return delta > 0 ? "▶" : "◀";
    }

    private String pickColor(float value, float yellowThreshold, float redThreshold) {




        return GRAY;
    }

    public void toggleListener(Player player) {
        if (!listeners.remove(player)) {
            listeners.add(player);
            sendToggleMessage(player, true);
        } else {
            sendToggleMessage(player, false);
        }
    }

    private void sendToggleMessage(Player player, boolean enabled) {
        String message = PluginLoader.INSTANCE.getLangManger().i18n(player,
                        enabled ? "commands.debug.enable" : "commands.debug.disable")
                .replace("%type%", "rotation");
        player.sendMessage(message);
    }

    public boolean toggleConsoleOutput() {
        this.outputToConsole = !outputToConsole;
        return this.outputToConsole;
    }
}