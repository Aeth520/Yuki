package cn.aetheris.yuki.protocol.nms;

import com.github.retrooper.packetevents.util.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private final List<Vector3i> blocks;

    public Cluster() {
        this.blocks = new ArrayList<>();
    }

    public void addBlock(Vector3i block) {
        blocks.add(block);
    }


    public double[] getCenter() {
        double x = 0, y = 0, z = 0;
        for (Vector3i block : blocks) {
            x += block.getX();
            y += block.getY();
            z += block.getZ();
        }
        return new double[]{x / blocks.size(), y / blocks.size(), z / blocks.size()};
    }
}

