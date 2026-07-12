package cn.aetheris.yuki.check.impl.player.breaking.invalid;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "InvalidBreakA (Invalid)", type = CheckType.BREAK, configName = "InvalidBreakA", decay = 0.25)
public final class InvalidBreakA extends Check implements BlockBreakCheck {
    public InvalidBreakA(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.faceId == 255 && blockBreak.action == DiggingAction.CANCELLED_DIGGING && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)) {
            return;
        }

        if (blockBreak.faceId < 0 || blockBreak.faceId > 5) {
            if (flagAndAlert("face= " + blockBreak.faceId + "\naction= " + blockBreak.action) && shouldModifyPackets()) {
                blockBreak.cancel();
            }
        }
    }
}