package cn.aetheris.yuki.data.block;


import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import com.github.retrooper.packetevents.protocol.world.BlockFace;

import java.util.List;

public record PistonTemplate(BlockFace dir,
                             List<SimpleCollisionBox> boxes,
                             boolean push, boolean slime, boolean honey) {
}
