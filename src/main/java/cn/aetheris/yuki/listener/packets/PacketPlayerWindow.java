package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntitySelf;
import cn.aetheris.yuki.util.enums.InventoryDesyncStatus;
import cn.aetheris.yuki.protocol.nms.NMSUtils;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;

import static com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus.Action;

public final class PacketPlayerWindow extends AbstractPacketListener {
    public PacketPlayerWindow() {
        super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !event.isCancelled()) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            if (player.hasInventoryOpen && isNearNetherPortal(player)) {
                handleInventoryClose(player, InventoryDesyncStatus.NETHER_PORTAL);
            }
        }

        
        if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            WrapperPlayClientClientStatus wrapper = new WrapperPlayClientClientStatus(event);

            if (wrapper.getAction() == Action.OPEN_INVENTORY_ACHIEVEMENT) {
                PlayerData player = getData(event.getUser());
                if (player == null) return;

                if (PluginLoader.INSTANCE.getConfigManager().isMitigateNoSlowInventory()) {
                    NMSUtils.resetItemUsage(player.getBukkitPlayer());
                }

                handleInventoryOpen(player);
            }
        }

        
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            final WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            if (wrapper.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.THROW) {
                player.dropItem = true;
            }

            if (PluginLoader.INSTANCE.getConfigManager().isMitigateNoSlowInventory()) {
                NMSUtils.resetItemUsage(player.getBukkitPlayer());
            }

            
            
            
            
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
                    && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                handleInventoryOpen(player);
            }

            if (player.getClientVersion().isNewerThan(ClientVersion.V_1_8)) {
                handleInventoryOpen(player);
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            handleInventoryClose(player, InventoryDesyncStatus.NOT_DESYNCED);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            player.sendTransaction();

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(),
                    () -> handleInventoryClose(player, InventoryDesyncStatus.NOT_DESYNCED));
        } else if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(event);

            PlayerData player = getData(event.getUser());
            if (player == null) return;

            player.sendTransaction();

            String legacyType = wrapper.getLegacyType();
            int modernType = wrapper.getType();
            InventoryDesyncStatus inventoryDesyncStatus = getContainerDesyncStatus(player, legacyType, modernType);

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(),
                    () -> {
                        if (inventoryDesyncStatus == InventoryDesyncStatus.NOT_DESYNCED) {
                            handleInventoryOpen(player);
                        } else {
                            handleInventoryClose(player, inventoryDesyncStatus);
                        }
                    });
        } else if (event.getPacketType() == PacketType.Play.Server.OPEN_HORSE_WINDOW) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            player.sendTransaction();

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(),
                    () -> handleInventoryOpen(player));
        } else if (event.getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            player.sendTransaction();

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(),
                    () -> handleInventoryClose(player, InventoryDesyncStatus.NOT_DESYNCED));
        }
    }

    private void handleInventoryOpen(PlayerData player) {
        if (!player.hasInventoryOpen) {
            player.lastInventoryOpen = System.currentTimeMillis();
        }

        player.hasInventoryOpen = true;
    }

    private void handleInventoryClose(PlayerData player, InventoryDesyncStatus desyncStatus) {
        player.hasInventoryOpen = false;
        player.inventoryDesyncStatus = desyncStatus;
    }

    public InventoryDesyncStatus getContainerDesyncStatus(PlayerData player, String legacyType, int modernType) {
        
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) &&
                ("minecraft:beacon".equals(legacyType) || modernType == 8)) {
            return player.inventoryDesyncStatus = InventoryDesyncStatus.BEACON;
        }

        if (isNearNetherPortal(player)) {
            return player.inventoryDesyncStatus = InventoryDesyncStatus.NETHER_PORTAL;
        }

        return player.inventoryDesyncStatus = InventoryDesyncStatus.NOT_DESYNCED;
    }

    public boolean isNearNetherPortal(PlayerData player) {
        
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_12_1) &&
                player.pointThreeEstimator.isNearNetherPortal) {
            PacketEntitySelf playerEntity = player.compensatedEntities.getSelf();
            
            return !playerEntity.inVehicle() && playerEntity.passengers.isEmpty();
        }

        return false;
    }
}