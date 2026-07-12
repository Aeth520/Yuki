package cn.aetheris.yuki.check.impl.misc.visual;


import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.Enchantment;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;

import java.util.Collections;
import java.util.List;


public final class EquipmentHider extends Check implements PacketCheck {

    private final List<Enchantment> enchantmentList = Collections.singletonList(
            Enchantment.builder().type(EnchantmentTypes.UNBREAKING).level(3).build());
    boolean spoofEnchantments;

    public EquipmentHider(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (player.bypass || player.noModifyPacketPermission || !PluginLoader.INSTANCE.getConfigManager().isMitigateVisualEquipment()) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);

            List<Equipment> equipmentList = wrapper.getEquipment();

            if (equipmentList.isEmpty()) {
                return;
            }

            equipmentList.forEach(equipment -> this.handleEquipment(equipment, wrapper.getClientVersion()));

            event.setCancelled(true);
            WrapperPlayServerEntityEquipment metadata = new WrapperPlayServerEntityEquipment(wrapper.getEntityId(),
                    equipmentList);
            ChannelHelper.runInEventLoop(player.user.getChannel(), () -> HookInit.getPacketEventsHook().sendPacketSilently(player.getUser(), metadata));
        }
    }

    private void handleEquipment(Equipment equipment, ClientVersion clientVersion) {
        ItemStack itemStack = equipment.getItem();

        if (itemStack == null) {
            return;
        }

        if (this.spoofEnchantments && itemStack.isEnchanted(clientVersion)) {
            itemStack.setEnchantments(this.enchantmentList, clientVersion);
            equipment.setItem(itemStack);
        }
    }

    @Override
    public void reload() {
        super.reload();
        spoofEnchantments = getConfig().getBooleanElse("mitigates.visual.equipment.enchantments", false);
    }
}