package cn.aetheris.yuki.check.util.handler;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.functionality.DebugManager;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingQueue;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.update.PredictionComplete;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class PredictionDebugHandler extends Check implements PostPredictionCheck {

    private static final String GRAY = "§7";
    private static final String GREEN = "§a";
    private static final String DARK_GREEN = "§2";
    private static final String YELLOW = "§e";
    private static final String RED = "§c";
    private static final String DARK_RED = "§4";

    final Set<Player> listeners = new CopyOnWriteArraySet<>(new HashSet<>());
    boolean outputToConsole = false;

    boolean enabledFlags = false;
    boolean lastMovementIsFlag = false;

    EvictingQueue<String> predicted = new EvictingQueue<>(5);
    EvictingQueue<String> actually = new EvictingQueue<>(5);
    EvictingQueue<String> offset = new EvictingQueue<>(5);

    public PredictionDebugHandler(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) return;

        double offset = predictionComplete.getOffset();

        
        if (listeners.isEmpty() && !outputToConsole) return;
        
        if (player.predictedVelocity.vector.lengthSquared() == 0 && offset == 0) return;

        String color = pickColor(offset, offset);

        Vector3dm predicted = player.predictedVelocity.vector;
        Vector3dm actually = player.actualMovement;

        String xColor = pickColor(Math.abs(predicted.getX() - actually.getX()), offset);
        String yColor = pickColor(Math.abs(predicted.getY() - actually.getY()), offset);
        String zColor = pickColor(Math.abs(predicted.getZ() - actually.getZ()), offset);

        String p = color + "Predicted XYZ: " + xColor + String.format("%.10f", predicted.getX()) + " " + yColor + String.format("%.10f", predicted.getY()) + " " + zColor + String.format("%.10f", predicted.getZ());
        String a = color + "Actually XYZ: " + xColor + String.format("%.10f", actually.getX()) + " " + yColor + String.format("%.10f", actually.getY()) + " " + zColor + String.format("%.10f", actually.getZ());
        String c = color + "Movement Speed: " + player.getCompensatedEntities().getSelf().getAttributeValue(Attributes.MOVEMENT_SPEED);
        String canSkipTick = (player.couldSkipTick + " ").substring(0, 1);
        String actualMovementSkip = (player.skippedTickInActualMovement + " ").substring(0, 1);
        String o = GRAY + canSkipTick + "→ 0.03 →" + actualMovementSkip + color + " Offset: " + String.format("%.5f", offset);

        String prefix = player.bukkitPlayer == null ? "null" : player.bukkitPlayer.getName() + " ";

        boolean thisFlag = !GRAY.equals(color) && !GREEN.equals(color);
        if (enabledFlags) {
            
            if (lastMovementIsFlag) {
                this.predicted.clear();
                this.actually.clear();
                this.offset.clear();
            }
            
            this.predicted.add(p);
            this.actually.add(a);
            this.offset.add(o);

            lastMovementIsFlag = thisFlag;
        }

        if (thisFlag) {
            for (int i = 0; i < this.predicted.size(); i++) {
                player.user.sendMessage(this.predicted.get(i));
                player.user.sendMessage(this.actually.get(i));
                player.user.sendMessage(this.offset.get(i));
            }
        }

        for (Player player : listeners) {
            
            player.sendMessage((player == getPlayer().bukkitPlayer ? "" : prefix) + p);
            player.sendMessage((player == getPlayer().bukkitPlayer ? "" : prefix) + a);
            player.sendMessage((player == getPlayer().bukkitPlayer ? "" : prefix) + o);
        }

        
        listeners.removeIf(player -> !player.isOnline());

        if (outputToConsole) {
            LogUtils.console(prefix + p);
            LogUtils.console(prefix + a);
            LogUtils.console(prefix + c);
            LogUtils.console(prefix + o);
        }
    }

    private String pickColor(double offset, double totalOffset) {
        if (player.getSetbackTeleportUtil().blockOffsets) return GRAY;
        if (offset <= 0 || totalOffset <= 0) {
            return GRAY;
        } else if (offset < 0.0001) {
            return GREEN;
        } else if (offset < 0.0035) {
            return DARK_GREEN;
        } else if (offset < 0.05) {
            return YELLOW;
        } else if (offset < 0.08) {
            return RED;
        } else {
            return DARK_RED;
        }
    }

    public void toggleListener(Player player) {
        if (!listeners.remove(player)) {
            listeners.add(player);
            DebugManager.setEnable(true);
            player.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(player, "commands.debug.enable").replace("%type%", "prediction"));
        } else {
            DebugManager.setEnable(false);
            player.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(player, "commands.debug.disable").replace("%type%", "prediction"));
        }
    }


    public boolean toggleConsoleOutput() {
        this.outputToConsole = !outputToConsole;
        return this.outputToConsole;
    }
}
