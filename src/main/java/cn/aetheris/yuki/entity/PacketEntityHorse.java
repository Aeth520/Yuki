package cn.aetheris.yuki.entity;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.attribute.ValuedAttribute;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;

import java.util.UUID;

public class PacketEntityHorse extends PacketEntityTrackYaw {

    private static final boolean HAS_SADDLE_SENT_BY_SERVER = Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_21_4);
    public boolean isRearing = false;
    public boolean hasSaddle = false;
    public boolean isTame = false;

    public PacketEntityHorse(PlayerData player, UUID uuid, EntityType type, double x, double y, double z, float xRot) {
        super(player, uuid, type, x, y, z, xRot);
        this.trackEntityEquipment = true;
        setAttribute(Attributes.STEP_HEIGHT, 1.0f);

        final boolean preAttribute = player.getClientVersion().isOlderThan(ClientVersion.V_1_20_5);
        
        trackAttribute(ValuedAttribute.ranged(Attributes.JUMP_STRENGTH, 0.7, 0, preAttribute ? 2 : 32)
                .withSetRewriter((oldValue, newValue) -> {
                    
                    if (preAttribute && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
                        return oldValue;
                    }
                    
                    return newValue;
                }));
        trackAttribute(ValuedAttribute.ranged(Attributes.MOVEMENT_SPEED, 0.225f, 0, 1024));

        if (EntityTypes.isTypeInstanceOf(type, EntityTypes.CHESTED_HORSE)) {
            setAttribute(Attributes.JUMP_STRENGTH, 0.5);
            setAttribute(Attributes.MOVEMENT_SPEED, 0.175f);
        }

        if (type == EntityTypes.ZOMBIE_HORSE || type == EntityTypes.SKELETON_HORSE) {
            setAttribute(Attributes.MOVEMENT_SPEED, 0.2f);
        }
    }

    public boolean hasSaddle() {
        if (HAS_SADDLE_SENT_BY_SERVER) {
            return this.hasSaddle;
        }

        return hasItemInSlot(EquipmentSlot.SADDLE);
    }

}