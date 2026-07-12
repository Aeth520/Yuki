package cn.aetheris.yuki.listener.bukkit.hooks;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.api.events.FlagEvent;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldGuardListener extends AbstractListener {
    private final Map<UUID, Long> passMap = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if (event.isCancelled()) {
            final Location location = player.getLocation();
            final Block placed = event.getBlockPlaced();
            if (placed.getY() <= location.getY()) {
                if (Math.abs(placed.getX() - location.getX()) <= 1.35) {
                    if (Math.abs(placed.getZ() - location.getZ()) <= 1.35) {
                        if (placed.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                            passMap.put(player.getUniqueId(), System.currentTimeMillis());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        passMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onVL(FlagEvent event) {
        if (passMap.containsKey(event.getPlayer().getUniqueId())) {
            long delay = System.currentTimeMillis() - passMap.get(event.getPlayer().getUniqueId());
            if (event.getCheckType().equals(CheckType.GROUNDSPOOF)
                    || event.getCheckType().equals(CheckType.MOVEMENT_VALIDATION)) {
                if (delay < 900) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
