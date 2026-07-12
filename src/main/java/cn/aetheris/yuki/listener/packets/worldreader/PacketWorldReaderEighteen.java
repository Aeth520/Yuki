package cn.aetheris.yuki.listener.packets.worldreader;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.HeightmapType;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_18;
import com.github.retrooper.packetevents.protocol.world.dimension.DimensionTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class PacketWorldReaderEighteen extends BasePacketWorldReader {

    private static final ChunkReader_v1_18 CHUNK_READER_V_1_18 = new ChunkReader_v1_18();
    private static final boolean PRE_1_21_5 = Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_21_5);

    
    @Override
    public void handleMapChunk(PlayerData player, PacketSendEvent event) {
        PacketWrapper<?> wrapper = new PacketWrapper<>(event);

        int x = wrapper.readInt();
        int z = wrapper.readInt();

        
        if (PRE_1_21_5)
            wrapper.readNBT();
        else
            wrapper.readMap(HeightmapType::read, PacketWrapper::readLongArray);

        
        BaseChunk[] chunks = CHUNK_READER_V_1_18.read(
                DimensionTypes.OVERWORLD, null, null, true, false, false,
                event.getUser().getTotalWorldHeight() >> 4,
                wrapper.readVarInt(), 
                wrapper
        );

        
        for (int i = 0; i < chunks.length; i++) {
            Chunk_v1_18 chunk = (Chunk_v1_18) chunks[i];
            if (chunk != null) {
                
                chunks[i] = new Chunk_v1_18(chunk.getBlockCount(), chunk.getChunkData(), null);
            }
        }

        addChunkToCache(event, player, chunks, true, x, z);

        event.setLastUsedWrapper(null); 
    }
}