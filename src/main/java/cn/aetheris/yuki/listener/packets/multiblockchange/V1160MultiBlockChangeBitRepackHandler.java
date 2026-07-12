package cn.aetheris.yuki.listener.packets.multiblockchange;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import io.netty.buffer.ByteBuf;


public final class V1160MultiBlockChangeBitRepackHandler implements VersionedMultiBlockChangeHandler {
    static final int MASK_LOCAL = 0xFFF;  
    
    private static final int SHIFT_STATE = 17;        
    private static final int MASK_STATE = 0x7FFF;    
    
    private static final boolean HAS_TRUST_EDGES =
            Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_19_4);

    @Override
    public void handleMultiBlockChange(PlayerData player, PacketSendEvent event) {
        
        ByteBuf buf = (ByteBuf) event.getByteBuf();

        
        long sectionEncodedPosition = ByteBufHelper.readLong(buf);

        if (HAS_TRUST_EDGES) {       
            buf.skipBytes(1);
        }

        
        int recordCount = ByteBufHelper.readVarInt(buf);
        int[] packed = new int[recordCount];

        
        
        int secX = (int) (sectionEncodedPosition >> 42);
        int secZ = (int) (sectionEncodedPosition << 22 >> 42);
        int secY = (int) (sectionEncodedPosition << 44 >> 44);

        int baseX = secX << 4;
        int baseY = secY << 4;
        int baseZ = secZ << 4;

        boolean sendTx = false;

        
        long now = System.currentTimeMillis();
        for (int i = 0; i < recordCount; i++) {

            long data = readVarLong(buf);               

            int local = (int) (data & 0xFFFL);         

            packed[i] = repackFromLong(data);

            
            if (!sendTx) {
                int lx = (local >>> 8) & 0xF;
                int lz = (local >>> 4) & 0xF;
                int ly = local & 0xF;

                int wx = baseX + lx, wy = baseY + ly, wz = baseZ + lz;

                if (Math.abs(wx - player.x) < RANGE &&
                        Math.abs(wy - player.y) < RANGE &&
                        Math.abs(wz - player.z) < RANGE &&
                        player.lastTransSent + TRANSACTION_COOLDOWN_MS < now) {
                    sendTx = true;
                }
            }
        }

        if (sendTx)
            player.sendTransaction();

        
        player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {

            
            int sX = (int) (sectionEncodedPosition >> 42);
            int sY = (int) (sectionEncodedPosition << 44 >> 44);
            int sZ = (int) (sectionEncodedPosition << 22 >> 42);

            int bx = sX << 4, by = sY << 4, bz = sZ << 4;

            for (int rec : packed) {
                int stateId = (rec >>> SHIFT_STATE) & MASK_STATE;
                int lx = (rec >>> 8) & 0xF;
                int lz = (rec >>> 4) & 0xF;
                int ly = rec & 0xF;

                int wx = bx + lx;
                int wy = by + ly;
                int wz = bz + lz;

                player.compensatedWorld.updateBlock(wx, wy, wz, stateId);
            }
        });
    }

    public int repackFromLong(long data) {
        
        int blockState = (int) ((data >>> 12) & MASK_STATE);

        
        int local = (int) (data & MASK_LOCAL);

        
        return (blockState << SHIFT_STATE) | local;
    }
}