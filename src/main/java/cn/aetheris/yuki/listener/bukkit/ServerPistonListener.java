package cn.aetheris.yuki.listener.bukkit;


import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.helper.BlockFaceHelper;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.PistonData;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("ALL")
public final class ServerPistonListener extends AbstractListener {
    private static final double MAX_HORIZONTAL_DISTANCE = 24.0;
    private static final double MAX_VERTICAL_DISTANCE = 64.0;
    private final Material SLIME_BLOCK;
    private final Material HONEY_BLOCK;
    public ServerPistonListener() {
        SLIME_BLOCK = Material.getMaterial("SLIME_BLOCK");
        HONEY_BLOCK = Material.getMaterial("HONEY_BLOCK");
    }

    
    private static boolean isCloseEnough(Vector3i vectorA, Vector3d vectorB) {
        return Math.abs(vectorA.getX() - vectorB.getX()) <= MAX_HORIZONTAL_DISTANCE
                && Math.abs(vectorA.getY() - vectorB.getY()) <= MAX_VERTICAL_DISTANCE
                && Math.abs(vectorA.getZ() - vectorB.getZ()) <= MAX_HORIZONTAL_DISTANCE;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonPushEvent(BlockPistonExtendEvent event) {
        boolean hasSlimeBlock = false;
        boolean hasHoneyBlock = false;

        final List<SimpleCollisionBox> boxes = new LinkedList<>();
        for (Block block : event.getBlocks()) {
            boxes.add(new SimpleCollisionBox(0, 0, 0, 1, 1, 1, true)
                    .offset(block.getX(),
                            block.getY(),
                            block.getZ()));
            boxes.add(new SimpleCollisionBox(0, 0, 0, 1, 1, 1, true)
                    .offset(block.getX() + event.getDirection().getModX(),
                            block.getY() + event.getDirection().getModY(),
                            block.getZ() + event.getDirection().getModZ()));

            
            if (block.getType() == SLIME_BLOCK) {
                hasSlimeBlock = true;
            }

            if (block.getType() == HONEY_BLOCK) {
                hasHoneyBlock = true;
            }
        }

        Block piston = event.getBlock();

        
        boxes.add(new SimpleCollisionBox(0, 0, 0, 1, 1, 1, true)
                .offset(piston.getX() + event.getDirection().getModX(),
                        piston.getY() + event.getDirection().getModY(),
                        piston.getZ() + event.getDirection().getModZ()));

        final int chunkX = event.getBlock().getX() >> 4;
        final int chunkZ = event.getBlock().getZ() >> 4;
        final BlockFace blockFace = BlockFaceHelper.fromBukkitFace(event.getDirection());
        final Vector3i sourcePos = new Vector3i(piston.getX(), piston.getY(), piston.getZ());

        for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
            if (player.compensatedWorld.isChunkLoaded(chunkX, chunkZ) && isCloseEnough(sourcePos, player.compensatedEntities.self.trackedServerPosition.getPos())) {
                final int lastTrans = player.lastTransactionSent.get();
                PistonData data = new PistonData(blockFace, boxes, lastTrans, true, hasSlimeBlock, hasHoneyBlock);
                player.latencyUtils.addRealTimeTaskAsync(lastTrans, () -> player.compensatedWorld.activePistons.add(data));
            }
        }
    }

    
    
    
    
    
    
    
    
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetractEvent(BlockPistonRetractEvent event) {
        boolean hasSlimeBlock = false;
        boolean hasHoneyBlock = false;

        List<SimpleCollisionBox> boxes = new ArrayList<>();
        BlockFace face = BlockFaceHelper.fromBukkitFace(event.getDirection());

        
        if (event.getBlocks().isEmpty()) {
            Block piston = event.getBlock();

            
            boxes.add(new SimpleCollisionBox(0, 0, 0, 1, 1, 1, true)
                    .offset(piston.getX() + face.getModX(),
                            piston.getY() + face.getModY(),
                            piston.getZ() + face.getModZ()));
        }

        for (Block block : event.getBlocks()) {
            boxes.add(new SimpleCollisionBox(0, 0, 0, 1, 1, 1, true)
                    .offset(block.getX(), block.getY(), block.getZ()));
            boxes.add(new SimpleCollisionBox(0, 0, 0, 1, 1, 1, true)
                    .offset(block.getX() + face.getModX(), block.getY() + face.getModY(), block.getZ() + face.getModZ()));

            
            if (block.getType() == SLIME_BLOCK) {
                hasSlimeBlock = true;
            }

            if (block.getType() == HONEY_BLOCK) {
                hasHoneyBlock = true;
            }
        }

        final int chunkX = event.getBlock().getX() >> 4;
        final int chunkZ = event.getBlock().getZ() >> 4;
        Vector3i sourcePos = new Vector3i(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());

        for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
            if (player.compensatedWorld.isChunkLoaded(chunkX, chunkZ) && isCloseEnough(sourcePos, player.compensatedEntities.self.trackedServerPosition.getPos())) {
                int lastTrans = player.lastTransactionSent.get();
                PistonData data = new PistonData(face, boxes, lastTrans, false, hasSlimeBlock, hasHoneyBlock);
                player.latencyUtils.addRealTimeTaskAsync(lastTrans, () -> player.compensatedWorld.activePistons.add(data));
            }
        }
    }
}