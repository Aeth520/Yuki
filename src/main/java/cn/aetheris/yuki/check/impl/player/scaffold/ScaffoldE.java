package cn.aetheris.yuki.check.impl.player.scaffold;


import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.math.VectorUtils;
import cn.aetheris.yuki.util.update.BlockPlace;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;

@CheckData(name = "ScaffoldE (Faraway)",
        configName = "ScaffoldE",
        type = CheckType.SCAFFOLD,
        description = "Placing blocks from too far away")
public final class ScaffoldE extends BlockPlaceCheck {

    private boolean onlyCrystal;

    public ScaffoldE(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        Vector3i blockPos = place.position;

        if (place.getMaterial() == StateTypes.SCAFFOLDING) return;

        if (isExempt(ExemptType.TELEPORT, ExemptType.CLIENT_ANTICHEAT, ExemptType.INVALID_GAMEMODE)) return;

        double min = Double.MAX_VALUE;
        final double[] possibleEyeHeights = player.getPossibleEyeHeights();
        for (double d : possibleEyeHeights) {
            SimpleCollisionBox box = new SimpleCollisionBox(blockPos);
            Vector3dm eyes = new Vector3dm(player.x, player.y + d, player.z);
            Vector3dm best = VectorUtils.cutBoxToVector(eyes, box);
            min = Math.min(min, eyes.distanceSquared(best));
        }

        double maxReach = player.compensatedEntities.getSelf().getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        final double threshold = player.getMovementThreshold();
        maxReach += Math.hypot(threshold, threshold);
        final ItemStack item = place.getItemStack();

        if (min > maxReach * maxReach) {
            if (onlyCrystal && (player.getInventory().getItemInHand(InteractionHand.OFF_HAND) != ItemTypes.END_CRYSTAL || player.getInventory().getItemInHand(InteractionHand.OFF_HAND) != ItemTypes.END_CRYSTAL)) {
                return;
            }
            if (flagAndAlert("d= " + Math.sqrt(min) + "\nm= " + maxReach + "\ni= " + item.getType().getName()) && shouldCancel()) {
                place.resync();
                player.onPacketCancel();
            }
        }
    }

    @Override
    public void reload() {
        super.reload();
        this.onlyCrystal = getConfig().getBooleanElse("FarPlace.only-crystal", false);
        this.cancelVL = getConfig().getIntElse("FarPlace.cancel-vl", 0);
    }
}
