package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;

public final class Riptide {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    
    public static Vector3dm getRiptideVelocity(PlayerData player) {
        ItemStack mainHandItem = player.getInventory().getHeldItem();
        ItemStack offHandItem = player.getInventory().getOffHand();

        int riptideLevel;
        if (mainHandItem.getType() == ItemTypes.TRIDENT) {
            riptideLevel = mainHandItem.getEnchantmentLevel(EnchantmentTypes.RIPTIDE, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion());
        } else if (offHandItem.getType() == ItemTypes.TRIDENT) {
            riptideLevel = offHandItem.getEnchantmentLevel(EnchantmentTypes.RIPTIDE, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion());
        } else {
            return new Vector3dm();
        }

        
        float yawRad = player.yaw * DEG_TO_RAD;
        float pitchRad = player.pitch * DEG_TO_RAD;

        
        float xComponent = -player.trigHandler.sin(yawRad) * player.trigHandler.cos(pitchRad);
        float yComponent = -player.trigHandler.sin(pitchRad);
        float zComponent = player.trigHandler.cos(yawRad) * player.trigHandler.cos(pitchRad);

        float vectorLength = (float) Math.sqrt(xComponent * xComponent + yComponent * yComponent + zComponent * zComponent);
        
        float speed = 3.0F * ((1.0F + riptideLevel) / 4.0F);

        
        xComponent = xComponent * (speed / vectorLength);
        yComponent = yComponent * (speed / vectorLength);
        zComponent = zComponent * (speed / vectorLength);

        
        return player.verticalCollision ? new Vector3dm(xComponent, 0, zComponent) : new Vector3dm(xComponent, yComponent, zComponent);
    }
}
