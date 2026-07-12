package cn.aetheris.yuki.block.chunk;


import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import lombok.Data;

@Data
public final class Column {
    private final int x;
    private final int z;
    private final BaseChunk[] chunks;
    private final int transaction;

    public Column(int x, int z, BaseChunk[] chunks, int transaction) {
        this.x = x;
        this.z = z;
        this.chunks = chunks;
        this.transaction = transaction;
    }

    
    
    public void mergeChunks(BaseChunk[] toMerge) {
        for (int i = 0; i < 16; i++) {
            if (toMerge[i] != null) chunks[i] = toMerge[i];
        }
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    public BaseChunk[] chunks() {
        return chunks;
    }

    public int transaction() {
        return transaction;
    }
}