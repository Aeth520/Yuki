package cn.aetheris.yuki.check.impl.player.scaffold;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.materials.Materials;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3f;

@CheckData(name = "ScaffoldC (Invalid)",
        configName = "ScaffoldC",
        description = "Invalid Placement",
        type = CheckType.SCAFFOLD
)

public final class ScaffoldC extends BlockPlaceCheck {
    public ScaffoldC(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        final Vector3f cursor = place.getCursor();
        if (cursor == null) return;

        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13))
            return; 

        double allowed = Materials.isShapeExceedsCube(place.getPlacedAgainstMaterial()) || place.getPlacedAgainstMaterial() == StateTypes.LECTERN ? 1.5 : 1;
        double minAllowed = 1 - allowed;

        if (cursor.getX() < minAllowed
                || cursor.getY() < minAllowed
                || cursor.getZ() < minAllowed
                || cursor.getX() > allowed
                || cursor.getY() > allowed
                || cursor.getZ() > allowed) {

            if (flagAndAlert() && shouldCancel()) {
                place.resync();
            }
        }
    }
}

