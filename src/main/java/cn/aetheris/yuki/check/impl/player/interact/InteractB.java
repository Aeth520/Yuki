package cn.aetheris.yuki.check.impl.player.interact;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@CheckData(name = "InteractB (Fucker)",
        configName = "InteractB",
        decay = 0.8,
        type = CheckType.INTERACT,
        experimental = true)
public final class InteractB extends Check implements BlockBreakCheck {

    public InteractB(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.isCancelled()) {
            return;
        }

        final Vector3i position = blockBreak.position;
        final double x = position.getX(), y = position.getY(), z = position.getZ();
        final StateType type = player.getCompensatedWorld().getBlockType(x, y, z);

        if (type.getMaterialType().name().contains("BED") && !type.getMaterialType().name().equalsIgnoreCase("BEDROCK")) {
            if (player.getY() < y) return;

            final Player bukkitPlayer = player.getBukkitPlayer();
            if (bukkitPlayer == null) return;

            final Location blockLoc = new Location(bukkitPlayer.getWorld(), x, y, z);

            Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
                BlockFace direction = getDirection(blockLoc);
                if (direction == null) return;

                List<Material> blocks = getAdjacentBlocks(blockLoc, direction);
                boolean invalid = false;
                if (blocks != null) {
                    invalid = checkBlocks(blocks);
                }
                if (invalid && flagAndAlert("(#1)\nd= " + direction + "\nb=" + blocks)) {
                    blockBreak.cancel();
                }
            });
        }
    }

    private BlockFace getDirection(Location blockLoc) {
        if (blockLoc.add(0.0, 0.0, 1.0).getBlock().getType().toString().contains("BED")) return BlockFace.SOUTH;
        if (blockLoc.add(-1.0, 0.0, 0.0).getBlock().getType().toString().contains("BED")) return BlockFace.WEST;
        if (blockLoc.add(0.0, 0.0, -1.0).getBlock().getType().toString().contains("BED")) return BlockFace.NORTH;
        if (blockLoc.add(1.0, 0.0, 0.0).getBlock().getType().toString().contains("BED")) return BlockFace.EAST;

        return null;
    }


    private List<Material> getAdjacentBlocks(Location blockLoc, BlockFace direction) {
        return switch (direction) {
            case SOUTH -> Arrays.asList(
                    blockLoc.add(0.0, -1.0, 0.0).getBlock().getType(),
                    blockLoc.add(0.0, -1.0, 1.0).getBlock().getType(),
                    blockLoc.add(0.0, 0.0, -1.0).getBlock().getType(),
                    blockLoc.add(-1.0, 0.0, 0.0).getBlock().getType(),
                    blockLoc.add(1.0, 0.0, 0.0).getBlock().getType(),
                    blockLoc.add(-1.0, 0.0, 1.0).getBlock().getType(),
                    blockLoc.add(1.0, 0.0, 1.0).getBlock().getType(),
                    blockLoc.add(0.0, 0.0, 2.0).getBlock().getType(),
                    blockLoc.add(0.0, 1.0, 0.0).getBlock().getType(),
                    blockLoc.add(0.0, 1.0, 1.0).getBlock().getType()
            );
            case NORTH -> Arrays.asList(
                    blockLoc.add(0.0, -1.0, 0.0).getBlock().getType(),
                    blockLoc.add(0.0, -1.0, -1.0).getBlock().getType(),
                    blockLoc.add(0.0, 0.0, 1.0).getBlock().getType(),
                    blockLoc.add(-1.0, 0.0, 0.0).getBlock().getType(),
                    blockLoc.add(1.0, 0.0, 0.0).getBlock().getType(),
                    blockLoc.add(-1.0, 0.0, -1.0).getBlock().getType(),
                    blockLoc.add(1.0, 0.0, -1.0).getBlock().getType(),
                    blockLoc.add(0.0, 0.0, -2.0).getBlock().getType(),
                    blockLoc.add(0.0, 1.0, 0.0).getBlock().getType(),
                    blockLoc.add(0.0, 1.0, -1.0).getBlock().getType()
            );
            case EAST -> Arrays.asList(
                    blockLoc.add(0.0, -1.0, 0.0).getBlock().getType(),
                    blockLoc.add(1.0, -1.0, 0.0).getBlock().getType(),
                    blockLoc.add(-1.0, 0.0, 0.0).getBlock().getType(),
                    blockLoc.add(0.0, 0.0, 1.0).getBlock().getType(),
                    blockLoc.add(0.0, 0.0, -1.0).getBlock().getType(),
                    blockLoc.add(1.0, 0.0, 1.0).getBlock().getType(),
                    blockLoc.add(1.0, 0.0, -1.0).getBlock().getType(),
                    blockLoc.add(2.0, 0.0, 0.0).getBlock().getType(),
                    blockLoc.add(0.0, 1.0, 0.0).getBlock().getType(),
                    blockLoc.add(1.0, 1.0, 0.0).getBlock().getType()
            );
            case WEST -> Arrays.asList(
                    blockLoc.add(0.0, -1.0, 0.0).getBlock().getType(),
                    blockLoc.add(-1.0, -1.0, 0.0).getBlock().getType(),
                    blockLoc.add(1.0, 0.0, 0.0).getBlock().getType(),
                    blockLoc.add(0.0, 0.0, 1.0).getBlock().getType(),
                    blockLoc.add(0.0, 0.0, -1.0).getBlock().getType(),
                    blockLoc.add(-1.0, 0.0, 1.0).getBlock().getType(),
                    blockLoc.add(-1.0, 0.0, -1.0).getBlock().getType(),
                    blockLoc.add(-2.0, 0.0, 0.0).getBlock().getType(),
                    blockLoc.add(0.0, 1.0, 0.0).getBlock().getType(),
                    blockLoc.add(-1.0, 1.0, 0.0).getBlock().getType()
            );
            default -> null;
        };
    }

    private boolean checkBlocks(List<Material> blocks) {
        boolean allSolid = blocks.stream().allMatch(Material::isSolid);
        boolean allOccluding = blocks.stream().allMatch(Material::isOccluding);
        boolean allGlassOrPane = blocks.stream().allMatch(block -> block.name().contains("GLASS") || block.name().contains("PANE"));

        return !(allSolid && allOccluding || allGlassOrPane);
    }
}
