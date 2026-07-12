package cn.aetheris.yuki.listener.packets.dragon;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import lombok.Getter;

@Getter
public final class PacketEntityEnderDragonPart extends PacketEntity {

    private final DragonPart part;
    private final float width, height;

    public PacketEntityEnderDragonPart(PlayerData player, DragonPart part, double x, double y, double z, float width, float height) {
        super(player, null, EntityTypes.ENDER_DRAGON, x, y, z);
        this.part = part;
        this.width = width;
        this.height = height;
    }

}