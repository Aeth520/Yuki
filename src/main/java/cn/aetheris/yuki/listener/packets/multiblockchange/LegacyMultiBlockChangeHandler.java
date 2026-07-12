package cn.aetheris.yuki.listener.packets.multiblockchange;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;

public class LegacyMultiBlockChangeHandler implements VersionedMultiBlockChangeHandler {

    
    @Override
    public void handleMultiBlockChange(PlayerData player, PacketSendEvent event) {
        WrapperPlayServerMultiBlockChange multiBlockChange = new WrapperPlayServerMultiBlockChange(event);

        final var blocks = multiBlockChange.getBlocks();
        for (WrapperPlayServerMultiBlockChange.EncodedBlock blockChange : blocks) {
            
            if (Math.abs(blockChange.getX() - player.x) < RANGE && Math.abs(blockChange.getY() - player.y) < RANGE && Math.abs(blockChange.getZ() - player.z) < RANGE && player.lastTransSent + TRANSACTION_COOLDOWN_MS < System.currentTimeMillis()) {
                player.sendTransaction();
                break;
            }
        }

        
        player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
            for (WrapperPlayServerMultiBlockChange.EncodedBlock blockChange : blocks) {
                player.compensatedWorld.updateBlock(blockChange.getX(), blockChange.getY(), blockChange.getZ(), blockChange.getBlockId());
            }
        });
    }
}