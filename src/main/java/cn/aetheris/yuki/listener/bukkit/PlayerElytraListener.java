package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.functionality.ConfigManager;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerElytraListener extends AbstractListener {

    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        PlayerData data = getData(player);
        UUID uuid = player.getUniqueId();

        if (data == null) return;

        final ConfigManager config = PluginLoader.INSTANCE.getConfigManager();
        boolean enabled = config.getConfig().getBooleanElse("mitigates.elytra.enable", true);
        boolean speedLimitEnabled = config.getConfig().getBooleanElse("mitigates.elytra.limit-speed.enable", true);
        double maxSpeed = config.getConfig().getDoubleElse("mitigates.elytra.limit-speed.max-speed", 10.0);
        double pitchFactor = config.getConfig().getDoubleElse("mitigates.elytra.limit-speed.pitch-factor", 0.5);
        double fireworkBoost = config.getConfig().getDoubleElse("mitigates.elytra.limit-speed.firework-boost", 3.0);
        double pitchThreshold = config.getConfig().getDoubleElse("mitigates.elytra.limit-speed.pitch-threshold", 10.0);

        if (!enabled || !speedLimitEnabled) return;

        if (!data.isGliding() ||
                !player.isGliding() ||
                data.isBypass() ||
                data.sinceRiptideSpinTick < 50L ||
                data.getSetbackTeleportUtil().insideUnloadedChunk() ||
                from.distanceSquared(to) < 0.005F) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - cooldownMap.getOrDefault(uuid, 0L) < 2000L) {
            return;
        }

        if (data.getExemptProcessor().isExempt(ExemptType.LIQUID)) {
            cooldownMap.put(uuid, currentTime);
            return;
        }

        Vector velocity = to.toVector().subtract(from.toVector());
        double speed = velocity.length() * 20;

        float pitch = player.getLocation().getPitch();
        double pitchAdjustedMaxSpeed = maxSpeed;

        if (pitch > pitchThreshold) {
            pitchAdjustedMaxSpeed += pitch * pitchFactor / 90.0;
        }

        boolean isUsingFirework = false;
        if (player.isHandRaised()) {
            boolean mainHandFirework = data.getInventory().getItemInHand(InteractionHand.MAIN_HAND).getType() == ItemTypes.FIREWORK_ROCKET;
            boolean offHandFirework = data.getInventory().getItemInHand(InteractionHand.OFF_HAND).getType() == ItemTypes.FIREWORK_ROCKET;
            isUsingFirework = mainHandFirework || offHandFirework;
        }

        if (isUsingFirework || data.isFireworkBoost() || data.getFireworkBoostTicks() < 160) {
            pitchAdjustedMaxSpeed += fireworkBoost;
        }

        if (speed > pitchAdjustedMaxSpeed) {
            final Vector newVelocity = velocity.normalize().multiply(pitchAdjustedMaxSpeed / 20);
            if (!data.isNoSetbackPermission()) player.setVelocity(newVelocity);

            LogUtils.debug(String.format(
                    "Limited elytra speed for %s | Original: %.2f m/s | Max allowed: %.2f m/s | Pitch: %.1f° | Firework: %s",
                    player.getName(), speed, pitchAdjustedMaxSpeed, pitch, isUsingFirework
            ));

            cooldownMap.put(uuid, currentTime);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldownMap.remove(event.getPlayer().getUniqueId());
    }
}
