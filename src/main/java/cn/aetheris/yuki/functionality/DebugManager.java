package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.VectorData;
import cn.aetheris.yuki.util.lists.EvictingQueue;
import cn.aetheris.yuki.util.location.PacketLocation;
import cn.aetheris.yuki.util.update.PredictionComplete;
import cn.aetheris.yuki.math.vector.Vector3dm;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DebugManager extends Check implements PostPredictionCheck {
    public static final Map<Integer, StringBuilder> flags = new ConcurrentHashMap<>();
    private static final String TITLE_COLOR = "§6§l";
    private static final String LABEL_COLOR = "§a";
    private static final String VALUE_COLOR = "§7";
    private static final String WARN_COLOR = "§c";
    @Getter
    @Setter
    private static boolean enable;
    private final List<VectorData> predicted = new EvictingQueue<>(60);
    private final List<Vector3dm> actually = new EvictingQueue<>(60);
    private final List<PacketLocation> locations = new EvictingQueue<>(60);
    private final List<Vector3dm> startTickClientVel = new EvictingQueue<>(60);
    private final List<Vector3dm> baseTickAddition = new EvictingQueue<>(60);
    private final List<Vector3dm> baseTickWater = new EvictingQueue<>(60);
    private final Object2IntMap<StringBuilder> continuedDebug = new Object2IntOpenHashMap<>();

    public DebugManager(PlayerData player) {
        super(player);
    }

    
    public static StringBuilder getFlag(int identifier) {
        return flags.get(identifier);
    }

    
    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked() || !enable) return;

        PacketLocation location = new PacketLocation(
                player.x, player.y, player.z,
                player.yaw, player.pitch,
                player.getLocationData().getTimeStamp(),
                player.bukkitPlayer != null ? player.bukkitPlayer.getWorld().getName() : "null",
                player.isOnGround()
        );

        cleanContinuedDebug(location);
        collectPlayerData(location);

        int id = predictionComplete.getIdentifier() == 0 ? 1 : predictionComplete.getIdentifier();
        generateNewDebugInfo(id);
    }

    
    private void cleanContinuedDebug(PacketLocation location) {
        Iterator<Map.Entry<StringBuilder, Integer>> iterator = continuedDebug.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<StringBuilder, Integer> entry = iterator.next();
            appendDebug(entry.getKey(), location);
            if (entry.getValue() - 1 <= 0) {
                iterator.remove();
            } else {
                entry.setValue(entry.getValue() - 1);
            }
        }
    }

    
    private void collectPlayerData(PacketLocation location) {
        predicted.add(player.predictedVelocity);
        actually.add(player.actualMovement);
        locations.add(location);
        startTickClientVel.add(player.startTickClientVel);
        baseTickAddition.add(player.baseTickAddition);
        baseTickWater.add(player.baseTickWaterPushing);
    }

    
    private void generateNewDebugInfo(int identifier) {
        StringBuilder sb = buildBaseInfo();
        appendHistoricalData(sb);
        appendEnvironmentInfo(sb);
        flags.put(identifier, sb);
        continuedDebug.put(sb, 40);
    }

    
    private StringBuilder buildBaseInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(TITLE_COLOR).append("---- AntiCheat Debug Info ----\n")
                .append(LABEL_COLOR).append("Info: ").append(VALUE_COLOR).append(PluginLoader.INSTANCE.getVersion()).append("\n")
                .append(LABEL_COLOR).append("Player: ").append(VALUE_COLOR).append(player.user.getName()).append("\n")
                .append(LABEL_COLOR).append("Client: ").append(VALUE_COLOR).append(player.getClientVersion().getReleaseName()).append("\n");
        return sb;
    }

    
    private void appendHistoricalData(StringBuilder sb) {
        sb.append("\n").append(TITLE_COLOR).append("-- Historical Movement --\n");
        for (int i = 0; i < predicted.size(); i++) {
            appendMovementDiff(sb, predicted.get(i), actually.get(i), locations.get(i));
        }
    }

    
    private void appendEnvironmentInfo(StringBuilder sb) {
        sb.append("\n").append(TITLE_COLOR).append("-- Environment Info --\n");
        appendBoundingBoxInfo(sb);
        appendFluidState(sb);
    }

    
    private void appendBoundingBoxInfo(StringBuilder sb) {
        sb.append(LABEL_COLOR).append("Bounding Box:\n")
                .append(VALUE_COLOR).append(String.format(
                        "[X: %.2f-%.2f, Y: %.2f-%.2f, Z: %.2f-%.2f]\n",
                        player.boundingBox.minX, player.boundingBox.maxX,
                        player.boundingBox.minY, player.boundingBox.maxY,
                        player.boundingBox.minZ, player.boundingBox.maxZ
                ));
    }

    
    private void appendFluidState(StringBuilder sb) {
        sb.append(LABEL_COLOR).append("Fluid State: ")
                .append(VALUE_COLOR).append(player.wasTouchingWater ? "In Water" : "Dry")
                .append(WARN_COLOR).append(player.wasTouchingLava ? " (IN LAVA!)" : "")
                .append("\n");
    }

    
    private void appendMovementDiff(StringBuilder sb, VectorData predict, Vector3dm actual, PacketLocation location) {
        Vector3dm offset = actual.clone().subtract(predict.vector);
        sb.append(LABEL_COLOR).append("Tick: ").append(VALUE_COLOR)
                .append(String.format("Predicted: %s | Actual: %s\nOffset: %s (%.2f blocks)\nLocation: %s\n",
                        vectorToString(predict.vector), vectorToString(actual), vectorToString(offset), offset.length(), location));
    }

    
    private void appendDebug(StringBuilder sb, PacketLocation location) {
        
        sb.append(TITLE_COLOR).append("---- Movement Debug ----\n");

        
        appendMovementDiff(sb, player.predictedVelocity, player.actualMovement, location);

        
        Vector3dm offset = player.actualMovement.clone().subtract(player.predictedVelocity.vector);
        sb.append(LABEL_COLOR).append("Offset: ").append(VALUE_COLOR).append(vectorToString(offset))
                .append(" (Distance: ").append(String.format("%.3f", offset.length())).append(")\n");

        
        sb.append(LABEL_COLOR).append("Location: ").append(VALUE_COLOR).append(location).append("\n");

        
        sb.append(LABEL_COLOR).append("Start Velocity: ").append(VALUE_COLOR).append(vectorToString(player.startTickClientVel)).append("\n");

        
        if (player.baseTickAddition.lengthSquared() > 0) {
            sb.append(LABEL_COLOR).append("Additional Velocity: ").append(VALUE_COLOR).append(vectorToString(player.baseTickAddition)).append("\n");
        }

        
        if (player.baseTickWaterPushing.lengthSquared() > 0) {
            sb.append(LABEL_COLOR).append("Water Push: ").append(VALUE_COLOR).append(vectorToString(player.baseTickWaterPushing)).append("\n");
        }

        
        checkMovementState(sb);

        
        sb.append(TITLE_COLOR).append("-----------------------\n\n");
    }

    
    private void checkMovementState(StringBuilder sb) {
        if (player.predictedVelocity.isZeroPointZeroThree()) {
            sb.append(WARN_COLOR).append("Movement threshold/tick skipping detected!\n");
        }
        if (player.predictedVelocity.isAttackSlow()) {
            sb.append(WARN_COLOR).append("* 0.6 horizontal attack slowdown\n");
        }
        if (player.predictedVelocity.isKnockback()) {
            appendKnockbackInfo(sb);
        }
        if (player.predictedVelocity.isExplosion()) {
            appendExplosionInfo(sb);
        }
        if (player.predictedVelocity.isTrident()) {
            sb.append(WARN_COLOR).append("Trident movement detected\n");
        }
        if (player.predictedVelocity.isSwimHop()) {
            sb.append(WARN_COLOR).append("Swim hop detected\n");
        }
        if (player.predictedVelocity.isJump()) {
            sb.append(WARN_COLOR).append("Jump detected\n");
        }
        if (player.isServerOnGround() && !player.isOnGround()) {
            sb.append(WARN_COLOR).append("Ground dsync detected\n");
        }
    }

    
    private void appendKnockbackInfo(StringBuilder sb) {
        if (player.firstBreadKB != null) {
            sb.append(WARN_COLOR).append("First knockback: ").append(vectorToString(player.firstBreadKB.vector)).append("\n");
        }
        if (player.likelyKB != null) {
            sb.append(WARN_COLOR).append("Second knockback: ").append(vectorToString(player.likelyKB.vector)).append("\n");
        }
    }

    
    private void appendExplosionInfo(StringBuilder sb) {
        if (player.firstBreadExplosion != null) {
            sb.append(WARN_COLOR).append("First explosion: ").append(vectorToString(player.firstBreadExplosion.vector)).append("\n");
        }
        if (player.likelyExplosions != null) {
            sb.append(WARN_COLOR).append("Second explosion: ").append(vectorToString(player.likelyExplosions.vector)).append("\n");
        }
    }

    
    private String vectorToString(Vector3dm vec) {
        return String.format("(%.5f, %.5f, %.5f)", vec.getX(), vec.getY(), vec.getZ());
    }
}
