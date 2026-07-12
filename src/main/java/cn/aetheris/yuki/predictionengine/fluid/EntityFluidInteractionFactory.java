package cn.aetheris.yuki.predictionengine.fluid;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.entity.PacketEntityStrider;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

public final class EntityFluidInteractionFactory {

    private static final DefaultEntityFluidInteraction DEFAULT = new DefaultEntityFluidInteraction();
    private static final StriderEntityFluidInteraction STRIDER = new StriderEntityFluidInteraction();

    public static EntityFluidInteraction create(PlayerData player) {
        if (!player.inVehicle()) return DEFAULT;

        PacketEntity riding = player.compensatedEntities.self.getRiding();
        if (riding instanceof PacketEntityStrider || riding.type == EntityTypes.STRIDER) {
            return STRIDER;
        }

        return DEFAULT;
    }
}
