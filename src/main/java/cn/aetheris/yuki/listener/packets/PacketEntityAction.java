package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.movement.elytra.ElytraA;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

public final class PacketEntityAction extends AbstractPacketListener {

    public PacketEntityAction() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event);
            PlayerData player = getData(event.getUser());

            if (player == null) return;

            switch (action.getAction()) {
                case START_SPRINTING -> {
                    player.isSprinting = true;
                    player.worldChange = false;
                }
                case STOP_SPRINTING -> {
                    player.isSprinting = false;
                    player.worldChange = false;
                }
                case START_SNEAKING -> player.isSneaking = true;
                case STOP_SNEAKING -> player.isSneaking = false;
                case START_FLYING_WITH_ELYTRA -> {
                    if (player.onGround || player.lastOnGround) {
                        player.getSetbackTeleportUtil().executeForceResync();

                        if (player.bukkitPlayer != null) {
                            player.bukkitPlayer.setSneaking(!player.bukkitPlayer.isSneaking());
                        }

                        event.setCancelled(PluginLoader.INSTANCE.getConfigManager().isMitigateElytraOnGround());
                        player.onPacketCancel();
                        LogUtils.sync("&b" + player.getName() + "&7 ForceResync for use elytra but on ground");
                        break;
                    }

                    
                    if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) return;
                    player.checkManager.getCheck(ElytraA.class).onStartGliding(event);

                    
                    
                    if (player.canGlide()) {
                        player.isGliding = true;
                        player.pointThreeEstimator.updatePlayerGliding();
                    } else {
                        if (PluginLoader.INSTANCE.getConfigManager().isMitigateGhostElytra()) {
                            player.getSetbackTeleportUtil().executeForceResync();
                            if (player.bukkitPlayer != null) {
                                
                                player.bukkitPlayer.setSneaking(!player.bukkitPlayer.isSneaking());
                            }
                        }
                        event.setCancelled(PluginLoader.INSTANCE.getConfigManager().isMitigateGhostElytra());
                        player.onPacketCancel();
                        LogUtils.sync("&b" + player.getName() + "&7 ForceResync for use ghost elyra");
                    }
                }
                case START_JUMPING_WITH_HORSE -> {
                    int jumpBoost = action.getJumpBoost();
                    if (jumpBoost < 0) jumpBoost = 0;
                    if (jumpBoost >= 90) {
                        player.vehicleData.nextHorseJump = 1;
                    } else {
                        player.vehicleData.nextHorseJump = 0.4F + 0.4F * jumpBoost / 90.0F;
                    }
                }
            }
        }
    }
}