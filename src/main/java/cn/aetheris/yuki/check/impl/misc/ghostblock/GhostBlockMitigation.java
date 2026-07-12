package cn.aetheris.yuki.check.impl.misc.ghostblock;

import cn.aetheris.mhdfscheduler.runnable.MHDFRunnable;
import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.protocol.nms.BlockUtils;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class GhostBlockMitigation extends BlockPlaceCheck {
    private boolean enable, alertEnable, shouldCancel, shouldSync;
    private double maxBuffer;
    private int distance;

    public GhostBlockMitigation(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (!enable
                || player.isFlying
                || player.packetStateData.lastPacketWasTeleport
                || player.bypass
                || player.noModifyPacketPermission
                || player.bukkitPlayer == null
                || isExempt(ExemptType.CLIENT_ANTICHEAT)) return;

        final StateType blockType = place.getMaterial();
        if (blockType == StateTypes.NETHER_PORTAL
                || blockType == StateTypes.BELL
                || blockType == StateTypes.FIRE) return;

        final World world = player.bukkitPlayer.getWorld();
        final Vector3i pos = place.getPlacedBlockPos();
        final Vector3i againstPos = place.position;

        final Vector3i playerFeet = new Vector3i(
                (int) Math.floor(player.bukkitPlayer.getLocation().getX()),
                (int) Math.floor(player.bukkitPlayer.getLocation().getY() - 0.1),
                (int) Math.floor(player.bukkitPlayer.getLocation().getZ())
        );

        new MHDFRunnable() {
            @Override
            public void run() {
                MHDFScheduler.getRegionScheduler().runTask(Yuki.getInstance(), world, pos.getX(), pos.getZ(), () -> {
                    if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                        return;
                    }

                    boolean foundNonAir = false;
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();

                    if (world.isChunkLoaded(againstPos.getX() >> 4, againstPos.getZ() >> 4)) {
                        Block againstBlock = world.getBlockAt(againstPos.getX(), againstPos.getY(), againstPos.getZ());
                        if (againstBlock.getType() != Material.AIR) {
                            foundNonAir = true;
                        }
                    }

                    if (!foundNonAir && world.isChunkLoaded(playerFeet.getX() >> 4, playerFeet.getZ() >> 4)) {
                        Block feetBlock = world.getBlockAt(playerFeet.getX(), playerFeet.getY(), playerFeet.getZ());
                        if (feetBlock.getType() != Material.AIR) {
                            foundNonAir = true;
                        }
                    }

                    if (!foundNonAir) {
                        outer:
                        for (int i = x - distance; i <= x + distance; i++) {
                            for (int k = z - distance; k <= z + distance; k++) {
                                if (!world.isChunkLoaded(i >> 4, k >> 4)) continue;

                                for (int j = y - distance; j <= y + distance; j++) {
                                    if (i == x && j == y && k == z) continue;

                                    Block block = world.getBlockAt(i, j, k);
                                    if (block.getType() != Material.AIR) {
                                        foundNonAir = true;
                                        break outer;
                                    }
                                }
                            }
                        }
                    }

                    if (!foundNonAir) {
                        if (alertEnable) {
                            String msg = PluginLoader.INSTANCE.getLangManager()
                                    .i18n(player, "mitigates.ghost-block.alert.message")
                                    .replace("%distance%", String.valueOf(distance))
                                    .replace("%player%", player.getName())
                                    .replace("%type%", place.getMaterial().getName())
                                    .replace("%x%", String.valueOf(x))
                                    .replace("%y%", String.valueOf(y))
                                    .replace("%z%", String.valueOf(z));

                            for (UUID uuid : PluginLoader.INSTANCE.getAlertManager().getEnabledAlerts()) {
                                final Player bukkitPlayer = Bukkit.getPlayer(uuid);
                                if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
                                    bukkitPlayer.sendMessage(msg);
                                }
                            }

                            if (buffer++ > maxBuffer) {
                                if (shouldCancel) place.resync();
                                if (shouldSync)
                                    BlockUtils.refreshBlocksAroundPlayer(player, place.getPlacedBlockPos(), world);
                            }
                        } else {
                            rewardBufferAndVL();
                        }
                    }
                });
            }
        }.runTask(Yuki.getInstance());
    }

    @Override
    public void reload() {
        super.reload();
        enable = getConfig().getBooleanElse("mitigates.ghost-block.place", true);
        alertEnable = getConfig().getBooleanElse("mitigates.ghost-block.alert.enable", true);
        shouldCancel = getConfig().getBooleanElse("mitigates.ghost-block.cancel", false);
        shouldSync = getConfig().getBooleanElse("mitigates.ghost-block.sync", false);
        distance = Math.max(2, Math.min(4, getConfig().getIntElse("mitigates.ghost-block.distance", 2)));
        maxBuffer = Math.max(1, getConfig().getDoubleElse("mitigates.ghost-block.buffer", 3));
    }
}
