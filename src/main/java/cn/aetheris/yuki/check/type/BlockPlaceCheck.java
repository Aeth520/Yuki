package cn.aetheris.yuki.check.type;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.HitboxData;
import cn.aetheris.yuki.block.collision.datatypes.CollisionBox;
import cn.aetheris.yuki.block.collision.datatypes.ComplexCollisionBox;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class BlockPlaceCheck extends Check implements RotationCheck, PostPredictionCheck {
    private static final List<StateType> weirdBoxes = new ArrayList<>();
    private static final List<StateType> buggyBoxes = new ArrayList<>();

    static {
        weirdBoxes.addAll(new ArrayList<>(BlockTags.FENCES.getStates()));
        weirdBoxes.addAll(new ArrayList<>(BlockTags.WALLS.getStates()));
        weirdBoxes.add(StateTypes.LECTERN);

        buggyBoxes.addAll(new ArrayList<>(BlockTags.DOORS.getStates()));
        buggyBoxes.addAll(new ArrayList<>(BlockTags.STAIRS.getStates()));
        buggyBoxes.add(StateTypes.CHEST);
        buggyBoxes.add(StateTypes.TRAPPED_CHEST);
        buggyBoxes.add(StateTypes.CHORUS_PLANT);

        
        buggyBoxes.add(StateTypes.KELP);
        buggyBoxes.add(StateTypes.KELP_PLANT);
        buggyBoxes.add(StateTypes.TWISTING_VINES);
        buggyBoxes.add(StateTypes.TWISTING_VINES_PLANT);
        buggyBoxes.add(StateTypes.WEEPING_VINES);
        buggyBoxes.add(StateTypes.WEEPING_VINES_PLANT);
        buggyBoxes.add(StateTypes.REDSTONE_WIRE);
    }

    private final SimpleCollisionBox[] boxes = new SimpleCollisionBox[ComplexCollisionBox.DEFAULT_MAX_COLLISION_BOX_SIZE];
    protected int cancelVL;

    public BlockPlaceCheck(PlayerData player) {
        super(player);
    }

    
    public void onBlockPlace(final BlockPlace place) {
    }

    
    public void onPostFlyingBlockPlace(BlockPlace place) {
    }

    @Override
    public void reload() {
        super.reload();
        this.cancelVL = getConfig().getIntElse(getConfigName() + ".cancel-vl", 5);
    }

    public boolean isBridging(BlockPlace place) {
        if (player.getPredictedVelocity().isJump()
                || player.getPredictedVelocity().isKnockback()
                || player.getPredictedVelocity().isExplosion()
                || player.inVehicle()
                || player.isCanFly()
                || player.isFlying()) return false;
        return player.getCompensatedWorld().getBlock(player.getLocationData().clone().subtract(0, 2.5, 0).toVector()).getType() == StateTypes.AIR
                && (player.pitch > 70 || player.lastPitch > 70)
                && place.isFaceHorizontal();
    }

    protected boolean shouldCancel() {
        return cancelVL >= 0 && violations >= cancelVL;
    }

    protected SimpleCollisionBox getCombinedBox(final BlockPlace place) {
        
        Vector3i clicked = place.position;
        CollisionBox placedOn = HitboxData.getBlockHitbox(player, place.getMaterial(), player.getClientVersion(), player.compensatedWorld.getBlock(clicked), true, clicked.getX(), clicked.getY(), clicked.getZ());

        int size = placedOn.downCast(boxes);

        placedOn.downCast(boxes);

        SimpleCollisionBox combined = new SimpleCollisionBox(clicked.getX(), clicked.getY(), clicked.getZ());
        for (int i = 0; i < size; i++) {
            SimpleCollisionBox box = boxes[i];
            double minX = Math.max(box.minX, combined.minX);
            double minY = Math.max(box.minY, combined.minY);
            double minZ = Math.max(box.minZ, combined.minZ);
            double maxX = Math.min(box.maxX, combined.maxX);
            double maxY = Math.min(box.maxY, combined.maxY);
            double maxZ = Math.min(box.maxZ, combined.maxZ);
            combined = new SimpleCollisionBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        if (weirdBoxes.contains(place.getPlacedAgainstMaterial())) {
            
            combined = new SimpleCollisionBox(clicked.getX() + 1, clicked.getY() + 1, clicked.getZ() + 1, clicked.getX(), clicked.getY(), clicked.getZ());
        }

        if (buggyBoxes.contains(place.getPlacedAgainstMaterial())) {
            
            combined = new SimpleCollisionBox(clicked.getX() + 1, clicked.getY() + 1, clicked.getZ() + 1, clicked.getX(), clicked.getY(), clicked.getZ());
        }

        return combined;
    }
}
