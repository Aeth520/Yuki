package cn.aetheris.yuki.listener.packets.patch;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ResyncWorldUtil {

    static Map<BlockData, Integer> blockDataToId = new ConcurrentHashMap<>();

    public static void resyncPosition(PlayerData player, Vector3i pos) {
        resyncPositions(player, pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static void resyncPosition(PlayerData player, Vector3i pos, int sequence) {
        if (player.bukkitPlayer == null) return;

        Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
            if (!player.bukkitPlayer.isOnline() || !player.getSetbackTeleportUtil().hasAcceptedSpawnTeleport) return;

            final int chunkX = pos.x >> 4;
            final int chunkZ = pos.z >> 4;

            if (!player.compensatedWorld.isChunkLoaded(chunkX, chunkZ)) return;
            if (player.bukkitPlayer.getLocation().distance(new Location(player.bukkitPlayer.getWorld(), pos.x, pos.y, pos.z)) >= 64)
                return;
            if (!player.bukkitPlayer.getWorld().isChunkLoaded(chunkX, chunkZ)) return; 

            final Block block = player.bukkitPlayer.getWorld().getChunkAt(chunkX, chunkZ).getBlock(pos.x & 15, pos.y, pos.z & 15);

            final int blockId;

            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
                
                blockId = blockDataToId.computeIfAbsent(block.getBlockData(), data -> WrappedBlockState.getByString(Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion(), data.getAsString(false)).getGlobalId());
            } else {
                blockId = (block.getType().getId() << 4) | block.getData();
            }

            HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayServerBlockChange(pos, blockId));
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19)) { 
                HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayServerAcknowledgeBlockChanges(sequence)); 
            }
        });
    }

    public static void resyncPositions(PlayerData player, SimpleCollisionBox box) {
        resyncPositions(player, MathUtil.floor(box.minX), MathUtil.floor(box.minY), MathUtil.floor(box.minZ),
                MathUtil.ceil(box.maxX), MathUtil.ceil(box.maxY), MathUtil.ceil(box.maxZ));
    }

    public static void resyncPositions(PlayerData player, int minBlockX, int mY, int minBlockZ, int maxBlockX, int mxY, int maxBlockZ) {
        
        if (!player.compensatedWorld.isChunkLoaded(minBlockX >> 4, minBlockZ >> 4) || !player.compensatedWorld.isChunkLoaded(minBlockX >> 4, maxBlockZ >> 4)
                || !player.compensatedWorld.isChunkLoaded(maxBlockX >> 4, minBlockZ >> 4) || !player.compensatedWorld.isChunkLoaded(maxBlockX >> 4, maxBlockZ >> 4))
            return;

        if (player.bukkitPlayer == null) return;
        World world = player.bukkitPlayer.getWorld();

        Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
                    boolean flat = Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13);

                    if (player.bukkitPlayer == null) return;
                    
                    if (!player.getSetbackTeleportUtil().hasAcceptedSpawnTeleport) return;

                    
                    if (!world.isChunkLoaded(minBlockX >> 4, minBlockZ >> 4) || !world.isChunkLoaded(minBlockX >> 4, maxBlockZ >> 4)
                            || !world.isChunkLoaded(maxBlockX >> 4, minBlockZ >> 4) || !world.isChunkLoaded(maxBlockX >> 4, maxBlockZ >> 4))
                        return;

                    
                    
                    final int minSection = player.compensatedWorld.getMinHeight() >> 4;
                    final int minBlock = minSection << 4;
                    final int maxBlock = player.compensatedWorld.getMaxHeight() - 1;

                    int minBlockY = Math.max(minBlock, mY);
                    int maxBlockY = Math.min(maxBlock, mxY);

                    int minChunkX = minBlockX >> 4;
                    int maxChunkX = maxBlockX >> 4;

                    int minChunkY = minBlockY >> 4;
                    int maxChunkY = maxBlockY >> 4;

                    int minChunkZ = minBlockZ >> 4;
                    int maxChunkZ = maxBlockZ >> 4;

                    for (int currChunkZ = minChunkZ; currChunkZ <= maxChunkZ; ++currChunkZ) {
                        int minZ = currChunkZ == minChunkZ ? minBlockZ & 15 : 0; 
                        int maxZ = currChunkZ == maxChunkZ ? maxBlockZ & 15 : 15; 

                        for (int currChunkX = minChunkX; currChunkX <= maxChunkX; ++currChunkX) {
                            int minX = currChunkX == minChunkX ? minBlockX & 15 : 0; 
                            int maxX = currChunkX == maxChunkX ? maxBlockX & 15 : 15; 

                            Chunk chunk = world.getChunkAt(currChunkX, currChunkZ);

                            for (int currChunkY = minChunkY; currChunkY <= maxChunkY; ++currChunkY) {
                                int minY = currChunkY == minChunkY ? minBlockY & 15 : 0; 
                                int maxY = currChunkY == maxChunkY ? maxBlockY & 15 : 15; 

                                int totalBlocks = (maxX - minX + 1) * (maxZ - minZ + 1) * (maxY - minY + 1);
                                WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks = new WrapperPlayServerMultiBlockChange.EncodedBlock[totalBlocks];

                                int blockIndex = 0;
                                
                                
                                for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                                    for (int currX = minX; currX <= maxX; ++currX) {
                                        for (int currY = minY; currY <= maxY; ++currY) {
                                            Block block = chunk.getBlock(currX, currY | (currChunkY << 4), currZ);

                                            int blockId;

                                            if (flat) {
                                                
                                                blockId = blockDataToId.computeIfAbsent(block.getBlockData(), data -> WrappedBlockState.getByString(Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion(), data.getAsString(false)).getGlobalId());
                                            } else {
                                                blockId = (block.getType().getId() << 4) | block.getData();
                                            }

                                            encodedBlocks[blockIndex++] = new WrapperPlayServerMultiBlockChange.EncodedBlock(blockId, currX, currY | (currChunkY << 4), currZ);
                                        }
                                    }
                                }

                                WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(new Vector3i(currChunkX, currChunkY, currChunkZ), true, encodedBlocks);
                                ChannelHelper.runInEventLoop(player.user.getChannel(), () -> HookInit.getPacketEventsHook().sendPacket(player.getUser(), packet));
                            }
                        }
                    }
                });
    }
}