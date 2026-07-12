package cn.aetheris.yuki.check.impl.misc.visual;


import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.impl.misc.visual.manager.MetaDataManager;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;

import java.util.List;


public final class MetaDataHider extends Check implements PacketCheck {

    public boolean healthHider;
    private boolean enable;
    private boolean absorptionHider;
    private boolean onlyForPlayers;

    public MetaDataHider(PlayerData player) {
        super(player);
    }


    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA) {
            return;
        }

        if (!enable) {
            return;
        }

        if (player.bypass || player.noModifyPacketPermission) {
            return;
        }

        final WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
        final int entityId = wrapper.getEntityId();
        if (event.getUser().getEntityId() == entityId) {
            return;
        }


        if (isExempt(ExemptType.INVALID_GAMEMODE)) {
            return;
        }

        final PacketEntity packetEntity = player.compensatedEntities.getEntity(entityId);

        if (packetEntity == null || !packetEntity.isLivingEntity()) {
            return;
        }

        EntityType entityType = packetEntity.getType();

        if (entityType == EntityTypes.ENDER_DRAGON || entityType == EntityTypes.WITHER) return;

        if (entityType != EntityTypes.PLAYER && onlyForPlayers) return;

        List<EntityData<?>> entityMetaData = wrapper.getEntityMetadata();


        for (EntityData data : entityMetaData) {
            if (healthHider && data.getIndex() == MetaDataManager.HEALTH) {
                float health = Float.parseFloat(String.valueOf(data.getValue()));
                if (health > 0) {
                    data.setValue(3.0F);
                    push(event, wrapper.getEntityId(), entityMetaData);
                }
            } else if (absorptionHider && entityType == EntityTypes.PLAYER && data.getIndex() == MetaDataManager.ABSORPTION) {
                setDynamicValue(data);
                push(event, wrapper.getEntityId(), entityMetaData);
            }
        }
    }

    private void setDynamicValue(EntityData obj) {
        Object value = obj.getValue();

        if (value instanceof Integer) {
            obj.setValue(5);
        } else if (value instanceof Short) {
            obj.setValue((short) 5);
        } else if (value instanceof Byte) {
            obj.setValue((byte) 5);
        } else if (value instanceof Long) {
            obj.setValue(5);
        } else if (value instanceof Float) {
            obj.setValue((float) 5);
        } else if (value instanceof Double) {
            obj.setValue(5);
        }
    }

    void push(PacketSendEvent event, int entityId, List<EntityData<?>> dataList) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            event.setCancelled(true);
            WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId, dataList);
            ChannelHelper.runInEventLoop(player.user.getChannel(), () -> HookInit.getPacketEventsHook().sendPacketSilently(player.getUser(), metadata));
        }
    }

    @Override
    public void reload() {
        super.reload();
        onlyForPlayers = getConfig().getBooleanElse("mitigates.visual.meta.only-player", true);

        healthHider = getConfig().getBooleanElse("mitigates.visual.meta.health", false);
        absorptionHider = getConfig().getBooleanElse("mitigates.visual.meta.absorption", false);

        enable = (healthHider || absorptionHider) && PluginLoader.INSTANCE.getConfigManager().isMitigateVisualMetaData();
    }
}