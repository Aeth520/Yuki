package cn.aetheris.yuki.check.impl.player.scaffold.invalid;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

@CheckData(name = "ScaffoldK (Invalid)",
        type = CheckType.SCAFFOLD,
        configName = "ScaffoldK")
public final class ScaffoldK extends BlockPlaceCheck {

    public ScaffoldK(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (place.getFaceId() == 255 && Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_8)) {
            return;
        }

        if (place.getFaceId() < 0 || place.getFaceId() > 5) {

            if (flagAndAlert("direction= " + place.getFaceId()) && shouldCancel()) {
                place.resync();
            }
        }
    }
}
