package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.mhdfscheduler.runnable.MHDFRunnable;
import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockUtils {

    
    public static void refreshBlocksAroundPlayer(PlayerData data, Location location) {
        new MHDFRunnable() {
            @Override
            public void run() {
                MHDFScheduler.getRegionScheduler().runTask(Yuki.getInstance(), location.clone(), () -> {
                    final Location loc = location.clone();
                    final World world = loc.getWorld();
                    if (world == null) return;

                    final int radius = 3;
                    final int px = loc.getBlockX();
                    final int py = loc.getBlockY();
                    final int pz = loc.getBlockZ();
                    final int maxY = world.getMaxHeight() - 1;

                    final boolean isNewVersion = Yuki.getInstance().getPacketEventsManager()
                            .getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13);

                    final int minChunkX = (px - radius) >> 4;
                    final int maxChunkX = (px + radius) >> 4;
                    final int minChunkZ = (pz - radius) >> 4;
                    final int maxChunkZ = (pz + radius) >> 4;

                    for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                        for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                            if (!world.isChunkLoaded(cx, cz)) {
                                world.loadChunk(cx, cz);
                            }
                        }
                    }
                    for (int x = px - radius; x <= px + radius; x++) {
                        for (int z = pz - radius; z <= pz + radius; z++) {
                            if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

                            for (int y = Math.max(0, py - radius); y <= Math.min(maxY, py + radius); y++) {
                                final Block block = world.getBlockAt(x, y, z);
                                final Vector3i position = new Vector3i(x, y, z);

                                try {
                                    WrapperPlayServerBlockChange blockChange;

                                    if (isNewVersion) {
                                        BlockData blockData = block.getBlockData();
                                        blockChange = new WrapperPlayServerBlockChange(
                                                position,
                                                blockData.getMaterial().getId()
                                        );
                                    } else {
                                        int combined = (block.getType().getId() << 4) | (block.getData() & 0xF);
                                        blockChange = new WrapperPlayServerBlockChange(position, combined);
                                    }
                                    ChannelHelper.runInEventLoop(data.user.getChannel(), () -> HookInit.getPacketEventsHook().sendPacket(data.getUser(), blockChange));
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });
            }
        }.runTask(Yuki.getInstance());
    }

    
    public static void refreshBlocksAroundPlayer(PlayerData data, Vector3i location, World world) {
        refreshBlocksAroundPlayer(data, new Location(world, location.x, location.y, location.z));
    }
}
