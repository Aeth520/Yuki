package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.block.ShulkerData;
import cn.aetheris.yuki.util.materials.Materials;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockAction;

public final class PacketBlockAction extends AbstractPacketListener {

    public PacketBlockAction() {
        super(PacketListenerPriority.HIGH);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.BLOCK_ACTION) return;

        PlayerData player = getData(event.getUser());
        if (player == null) return;

        WrapperPlayServerBlockAction action = new WrapperPlayServerBlockAction(event);
        Vector3i pos = action.getBlockPosition();

        player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
            WrappedBlockState state = player.compensatedWorld.getBlock(pos);
            if (!Materials.isShulker(state.getType())) return;

            boolean closing = action.getActionData() < 1;
            ShulkerData data = new ShulkerData(pos, player.lastTransactionSent.get(), closing);

            player.compensatedWorld.openShulkerBoxes.remove(data);
            player.compensatedWorld.openShulkerBoxes.add(data);
        });
    }
}
