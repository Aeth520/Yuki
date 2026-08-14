package cn.aetheris.yuki.check.type;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.movement.VectorData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;


public class InventoryCheck extends BlockPlaceCheck implements PacketCheck, BlockBreakCheck {

    protected static final long NONE = Long.MAX_VALUE;
    protected long closeTransaction = NONE;
    protected int closePacketsToSkip;

    public InventoryCheck(PlayerData player) {
        super(player);
    }


    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.CLICK_WINDOW) {
            final boolean shouldMitigate = PluginLoader.INSTANCE.getConfigManager().isMitigateInventory();
            if (shouldMitigate && closeTransaction != NONE && !player.bypass && !player.noModifyPacketPermission && !isExempted()) {
                event.setCancelled(true);
                player.onPacketCancel();
                player.getInventory().requiresRefresh = true;
                LogUtils.debug("&b " + player.getName() + " has been closed inventory (&b"
                        + closeTransaction + "&7/&b" + NONE + "&7)");
            }
        } else if (packetType == PacketType.Play.Client.CLOSE_WINDOW) {
            if (closeTransaction != NONE && closePacketsToSkip-- <= 0) {
                closeTransaction = NONE;
            }
        }
    }

    private void packetClose() {
        if (closeTransaction != NONE) {
            return;
        }

        final int windowId = player.getInventory().openWindowID;
        player.user.writePacket(new WrapperPlayServerCloseWindow(windowId));
        closePacketsToSkip = 1;

        PacketEvents.getAPI().getProtocolManager().receivePacket(
                player.user.getChannel(), new WrapperPlayClientCloseWindow(windowId)
        );

        player.sendTransaction();
        final int transaction = player.lastTransactionSent.get();
        closeTransaction = transaction;

        player.latencyUtils.addRealTimeTask(transaction, () -> {
            if (closeTransaction == transaction) {
                closeTransaction = NONE;
            }
        });
        player.user.flushPackets();
    }


    public void closeInventory() {
        diffCloseType();
    }

    private void diffCloseType() {
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
            packetClose();
            return;
        }
        if (player.bukkitPlayer == null) {
            player.user.closeInventory();
        } else {
            Bukkit.getScheduler().runTask(Yuki.getInstance(),
                    () -> player.bukkitPlayer.closeInventory());
        }
    }


    public VectorData.MoveVectorData findMovement(VectorData vectorData) {
        if (vectorData instanceof VectorData.MoveVectorData) {
            return (VectorData.MoveVectorData) vectorData;
        }
        while (vectorData != null) {
            vectorData = vectorData.lastVector;
            if (vectorData instanceof VectorData.MoveVectorData) {
                return (VectorData.MoveVectorData) vectorData;
            }
        }
        return null;
    }
}
