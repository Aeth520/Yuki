package cn.aetheris.yuki.util.ray;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RayTraceUtil {

    public static boolean isEntityVisible(World world, Location observerEyeLoc, BoundingBox targetBox, Set<Material> passthroughMaterials) {
        BoundingBox expandedBox = targetBox.clone().expand(0.05, 0.05, 0.05);
        List<Vector> targetPoints = getOptimizedPointsOnBoundingBox(expandedBox);

        for (Vector point : targetPoints) {
            Vector direction = point.clone().subtract(observerEyeLoc.toVector());
            double distance = observerEyeLoc.toVector().distance(point);

            if (direction.lengthSquared() < 0.0001) return true;
            direction.normalize();

            RayTraceResult result = world.rayTraceBlocks(observerEyeLoc, direction, distance, FluidCollisionMode.NEVER, true);

            if (result == null || result.getHitBlock() == null) {
                return true;
            }

            Block hitBlock = result.getHitBlock();
            if (passthroughMaterials.contains(hitBlock.getType())) {
                return true;
            }
            if (hitBlock.getType().name().contains("STAIRS")) {
                return true;
            }
            if (hitBlock.getType().name().contains("WALL")) {
                return true;
            }
            if (hitBlock.getType().name().contains("TRAPDOOR")) {
                return true;
            }
            if (hitBlock.getType().name().contains("BUTTON")) {
                return true;
            }
            if (hitBlock.getType().name().contains("CHAIN")) {
                return true;
            }
            if (hitBlock.getType().name().contains("BARS")) {
                return true;
            }
            if (hitBlock.getType().name().contains("PLATE")) {
                return true;
            }
            if (hitBlock.getType().name().contains("GLASS")) {
                return true;
            }
            if (hitBlock.getType().name().contains("DOOR")) {
                return true;
            }
            if (hitBlock.getType().name().contains("CARPET")) {
                return true;
            }
            if (hitBlock.getType().name().contains("SCULK")) {
                return true;
            }
            if (hitBlock.getType().name().contains("LCTERN")) {
                return true;
            }
            if (hitBlock.getType().name().contains("BELL")) {
                return true;
            }
            if (hitBlock.getType().name().contains("REDSTONE")) {
                return true;
            }
        }
        return false;
    }

    private static List<Vector> getOptimizedPointsOnBoundingBox(BoundingBox box) {
        List<Vector> points = new ArrayList<>(5);
        double minX = box.getMinX(), minY = box.getMinY(), minZ = box.getMinZ();
        double maxX = box.getMaxX(), maxY = box.getMaxY(), maxZ = box.getMaxZ();
        Vector center = box.getCenter();

        points.add(center);
        points.add(new Vector(center.getX(), maxY, center.getZ()));
        points.add(new Vector(center.getX(), minY, center.getZ()));
        double midY1 = minY + box.getHeight() * 0.33;
        double midY2 = minY + box.getHeight() * 0.66;
        points.add(new Vector(minX, midY1, minZ));
        points.add(new Vector(maxX, midY2, maxZ));

        return points;
    }
}