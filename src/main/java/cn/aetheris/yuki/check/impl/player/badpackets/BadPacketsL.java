package cn.aetheris.yuki.check.impl.player.badpackets;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "BadPacketsL", configName = "BadPacketsL", type = CheckType.BADPACKETS)
public final class BadPacketsL extends Check implements PacketCheck {

    final double legacyExpansion = player.getClientVersion().isOlderThan(ClientVersion.V_1_9) ? 0.1 : 0;
    final double maxXZ = 0.3001 + legacyExpansion;
    final double minY = -0.0001 - legacyExpansion;
    final double maxY = 1.8001 + legacyExpansion;

    public BadPacketsL(final PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Client.INTERACT_ENTITY)) return;

        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

        if (wrapper.getTarget().isEmpty()) return;

        Vector3f targetVector = wrapper.getTarget().get();

        PacketEntity packetEntity = player.compensatedEntities.getEntity(wrapper.getEntityId());

        if (packetEntity == null) return;

        if (!EntityTypes.PLAYER.equals(packetEntity.getType())) {
            return;
        }

        if (isExempt(ExemptType.CLIENT_ANTICHEAT)) return;

        final float scale = (float) packetEntity.getAttributeValue(Attributes.SCALE);

        if (targetVector.y > (minY * scale) && targetVector.y < (maxY * scale)
                && Math.abs(targetVector.x) < (maxXZ * scale)
                && Math.abs(targetVector.z) < (maxXZ * scale)) {
            return;
        }

        String verbose = String.format("x= %.5f\ny= %.5f\nz= %.5f", targetVector.x, targetVector.y, targetVector.z);

        if (flagAndAlert(verbose)) {
            event.setCancelled(true);
            player.onPacketCancel();
            player.mitigateDamage();
        }
    }
}