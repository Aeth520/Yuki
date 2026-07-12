package cn.aetheris.yuki.check.impl.player.scaffold.invalid;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.util.Vector3f;

@CheckData(name = "ScaffoldH (Cursor)",
        type = CheckType.SCAFFOLD,
        configName = "ScaffoldH",
        setback = 1,
        description = "Invalid Placement")
public final class ScaffoldH extends BlockPlaceCheck {
    public ScaffoldH(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        Vector3f cursor = place.getCursor();
        if (cursor == null) return;

        if (!Float.isFinite(cursor.getX())
                || !Float.isFinite(cursor.getY())
                || !Float.isFinite(cursor.getZ())) {

            String result = "X=" + cursor.getX()
                    + "\nY= " + cursor.getY()
                    + "\nZ= " + cursor.getZ();

            if (flagWithSetback()
                    && shouldCancel()) {
                place.resync();
                alert(result);
            }
        }
    }
}
