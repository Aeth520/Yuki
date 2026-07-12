package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.entity.PacketEntity;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;

public final class GetBoundingBox {
    public static SimpleCollisionBox getCollisionBoxForPlayer(PlayerData player, double centerX, double centerY, double centerZ) {
        if (player.inVehicle()) {
            return getPacketEntityBoundingBox(player, centerX, centerY, centerZ, player.compensatedEntities.self.getRiding());
        }

        return getPlayerBoundingBox(player, centerX, centerY, centerZ);
    }

    public static SimpleCollisionBox getPacketEntityBoundingBox(PlayerData player, double centerX, double minY, double centerZ, PacketEntity entity) {
        float width = BoundingBoxSize.getWidth(player, entity);
        float height = BoundingBoxSize.getHeight(player, entity);
        return getBoundingBoxFromPosAndSize(entity, centerX, minY, centerZ, width, height);
    }

    
    
    
    
    public static SimpleCollisionBox getPlayerBoundingBox(PlayerData player, double centerX, double minY, double centerZ) {
        float width = player.pose.width;
        float height = player.pose.height;
        return getBoundingBoxFromPosAndSize(player, centerX, minY, centerZ, width, height);
    }

    public static SimpleCollisionBox getBoundingBoxFromPosAndSize(PlayerData player, double centerX, double minY, double centerZ, float width, float height) {
        return getBoundingBoxFromPosAndSize(player.compensatedEntities.self, centerX, minY, centerZ, width, height);
    }

    public static SimpleCollisionBox getBoundingBoxFromPosAndSize(PacketEntity entity, double centerX, double minY, double centerZ, float width, float height) {
        final float scale = (float) entity.getAttributeValue(Attributes.SCALE);
        return getBoundingBoxFromPosAndSizeRaw(centerX, minY, centerZ, width * scale, height * scale);
    }

    public static SimpleCollisionBox getBoundingBoxFromPosAndSizeRaw(double centerX, double minY, double centerZ, float width, float height) {
        double minX = centerX - (width / 2f);
        double maxX = centerX + (width / 2f);
        double maxY = minY + height;
        double minZ = centerZ - (width / 2f);
        double maxZ = centerZ + (width / 2f);

        return new SimpleCollisionBox(minX, minY, minZ, maxX, maxY, maxZ, false);
    }

    public static double[] getEntityDimensions(PlayerData player, PacketEntity entity) {
        final float scale = (float) entity.getAttributeValue(Attributes.SCALE);
        final float width = BoundingBoxSize.getWidth(player, entity) * scale;
        final float height = BoundingBoxSize.getHeight(player, entity) * scale;
        return new double[]{width, height, width};
    }

    public static void expandBoundingBoxByEntityDimensions(SimpleCollisionBox box, PlayerData player, PacketEntity entity) {
        double[] dimensions = getEntityDimensions(player, entity);
        double halfWidth = dimensions[0] / 2.0;
        double height = dimensions[1];
        double halfDepth = dimensions[2] / 2.0;

        box.minX -= halfWidth;
        box.minY -= 0; 
        box.minZ -= halfDepth;
        box.maxX += halfWidth;
        box.maxY += height;
        box.maxZ += halfDepth;
    }
}