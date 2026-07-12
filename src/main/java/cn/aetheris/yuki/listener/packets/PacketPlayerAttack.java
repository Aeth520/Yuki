package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.player.badpackets.BadPacketsN;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.entity.PacketEntityHorse;
import cn.aetheris.yuki.protocol.nms.NMSUtils;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.lumine.mythic.bukkit.MythicBukkit;

public class PacketPlayerAttack extends AbstractPacketListener {

    public PacketPlayerAttack() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            final PlayerData player = getData(event.getUser());

            if (player == null) return;

            
            if (!player.compensatedEntities.entityMap.containsKey(interact.getEntityId()) && !player.compensatedEntities.serverPositionsMap.containsKey(interact.getEntityId())
                    
                    && (!player.compensatedEntities.entitiesRemovedThisTick.contains(interact.getEntityId()) || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14))) {
                final BadPacketsN badPacketsN = player.checkManager.getCheck(BadPacketsN.class);
                if (badPacketsN.buffer++ > 2) {
                    if (badPacketsN.flagAndAlert("(DSync?)\ne= " + interact.getEntityId() + "\nserver= false")) {
                        player.mitigateDamage();
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    badPacketsN.rewardBufferAndVL();
                }
                return;
            }

            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (PluginLoader.INSTANCE.getConfigManager().isMitigateUseItem()) {
                    NMSUtils.resetItemUsage(player.bukkitPlayer);
                }
                player.totalFlyingPacketsSent = 0;
                player.isAttacking = true;
                player.lastAttack = event.getTimestamp();
                player.dropItem = false;
                PacketEntity entity = player.compensatedEntities.getEntity(interact.getEntityId());
                if (entity == null) {
                    return;
                }
                player.target = entity;
                player.lastTarget = player.target;
                org.bukkit.inventory.ItemStack main = SpigotConversionUtil.toBukkitItemStack(player.getInventory().getItemInHand(InteractionHand.MAIN_HAND));
                org.bukkit.inventory.ItemStack off = SpigotConversionUtil.toBukkitItemStack(player.getInventory().getItemInHand(InteractionHand.OFF_HAND));

                org.bukkit.inventory.ItemStack mythicItem = null;

                if (HookInit.getMythicMobsHook().isEnabled()) {
                    if (MythicBukkit.inst().getItemManager().isMythicItem(main)) {
                        mythicItem = main;
                    }

                    if (off != null && MythicBukkit.inst().getItemManager().isMythicItem(off)) {
                        mythicItem = off;
                    }

                    if (mythicItem != null && !isStackable(mythicItem)) {
                        player.setSinceMythicMobItemAttackTicks(0);
                    }
                }

                
                
                if (player.compensatedEntities.self.getAttributeValue(Attributes.ATTACK_DAMAGE) <= 0) return;

                ItemStack heldItem = player.getInventory().getHeldItem();

                if (!entity.isLivingEntity
                        || entity.type == EntityTypes.PLAYER
                        || entity.type == EntityTypes.PAINTING
                        || entity.type == EntityTypes.ENDER_DRAGON
                        && player.getClientVersion().isOlderThan(ClientVersion.V_1_21_2)) {
                    int knockBackLevel = player.getClientVersion().isOlderThan(ClientVersion.V_1_21) && heldItem != null
                            ? heldItem.getEnchantmentLevel(EnchantmentTypes.KNOCKBACK, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion())
                            : 0;
                    final boolean hasNegativeKB = knockBackLevel < 0;

                    final boolean isLegacyPlayer = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8);
                    
                    final boolean noCooldown = isLegacyPlayer || Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_9);
                    if (!isLegacyPlayer) {
                        knockBackLevel = Math.max(knockBackLevel, 0);
                    }

                    
                    
                    

                    if ((player.lastSprinting && !hasNegativeKB && noCooldown) || knockBackLevel > 0) {
                        player.minAttackSlow++;
                        player.maxAttackSlow++;

                        
                        if (knockBackLevel == 0) {
                            player.maxAttackSlow = player.minAttackSlow = 1;
                        }
                    } else if (!isLegacyPlayer && player.lastSprinting) {
                        
                        if (player.maxAttackSlow > 0
                                && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
                                && player.compensatedEntities.self.getAttributeValue(Attributes.ATTACK_SPEED) < 16) { 
                            return;
                        }

                        
                        player.maxAttackSlow++;
                    }
                }
            } else if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT) {
                
                
                if (player.compensatedEntities.getEntity(interact.getEntityId()) instanceof PacketEntityHorse
                        && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_13)) {
                    player.packetStateData.horseInteractCausedForcedRotation = true;
                }
            }
        }
    }

    public boolean isStackable(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return true;
        }
        return item.getMaxStackSize() > 1;
    }
}