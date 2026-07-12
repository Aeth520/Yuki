package cn.aetheris.yuki.check.impl.player.scaffold.invalid;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3f;

@CheckData(name = "ScaffoldI (Invalid)",
        configName = "ScaffoldI",
        type = CheckType.SCAFFOLD,
        experimental = true,
        decay = 0.25)
public final class ScaffoldI extends BlockPlaceCheck {

    public ScaffoldI(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(BlockPlace place) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_12)) return; 

        final Vector3f cursor = place.getCursor();

        int face = place.getPlayerFacing().getFaceValue();

        double blockX = cursor.getX();
        double blockY = cursor.getY();
        double blockZ = cursor.getZ();

        boolean invalidX = blockX > 1.0 || blockX < 0.0;
        boolean invalidY = blockY > 1.0 || blockY < 0.0;
        boolean invalidZ = blockZ > 1.0 || blockZ < 0.0;

        if (invalidX || invalidY || invalidZ) {
            if (flagAndAlert("f= " + face + "\ns= " + (blockX + blockY + blockZ))) {
                place.resync();
                player.onPacketCancel();
            }
        } else {
            rewardVL();
        }
    }
}


