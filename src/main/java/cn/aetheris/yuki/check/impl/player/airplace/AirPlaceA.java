package cn.aetheris.yuki.check.impl.player.airplace;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.change.BlockModification;
import cn.aetheris.yuki.util.materials.Materials;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.util.Vector3i;

@CheckData(name = "AirPlaceA (Invalid)", configName = "AirPlaceA", type = CheckType.AIRPLACE, description = "Air Placement", decay = 0.558)
public final class AirPlaceA extends BlockPlaceCheck {

    private long lastFlag;

    public AirPlaceA(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        
        if (!place.isBlock()) {
            return;
        }

        if (isExempt(ExemptType.TELEPORT, ExemptType.CLIENT_ANTICHEAT)) return;

        
        if (Materials.isGlassPane(place.getMaterial())
                || Materials.isGate(place.getMaterial())
                || Materials.isStairs(place.getMaterial())
                || Materials.isSlab(place.getMaterial())
                || Materials.isWall(place.getMaterial())) {
            return;
        }

        if (place.isCancelled()) {
            buffer = 0.0;
            return;
        }

        Vector3i blockPos = place.position;
        StateType placeAgainst = player.compensatedWorld.getBlockType(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        int currentTick = PluginLoader.INSTANCE.getTickManager().currentTick;
        Iterable<BlockModification> blockModifications = player.blockHistory.getRecentModifications((blockModification) -> currentTick - blockModification.tick() < 2
                && blockPos.equals(blockModification.location())
                && (blockModification.cause() == BlockModification.Cause.START_DIGGING || blockModification.cause() == BlockModification.Cause.HANDLE_NETTY_SYNC_TRANSACTION));

        for (BlockModification blockModification : blockModifications) {
            StateType stateType = blockModification.oldBlockContents().getType();
            if (!(stateType.isAir() || Materials.isNoPlaceLiquid(stateType))) {
                return;
            }
        }

        if (placeAgainst.isAir() || Materials.isNoPlaceLiquid(placeAgainst)) {
            if (time() - lastFlag < 500L) {
                return;
            }

            if (buffer++ > 3) {
                if (flagAndAlert("m= " + place.getMaterial().getName()
                        + "\ntick= " + PluginLoader.INSTANCE.getTickManager().currentTick) && shouldCancel()) {
                    place.resync();
                    player.mitigateDamage();
                    lastFlag = time();
                }
            }
        }
    }


    @Override
    public void reload() {
        super.reload();
        this.cancelVL = getConfig().getIntElse(getConfigName() + ".cancelVL", 0);
    }
}
