package cn.aetheris.yuki.check.impl.player.scaffold;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "ScaffoldD (Multi)",
        configName = "ScaffoldD",
        type = CheckType.SCAFFOLD,
        description = "Placed multiple blocks in a tick",
        decay = 0.65)
public final class ScaffoldD extends BlockPlaceCheck {

    private final List<String> flags = new ArrayList<>();
    private boolean hasPlaced;
    private BlockFace lastFace;
    private Vector3f lastCursor;
    private Vector3i lastPos;

    public ScaffoldD(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (!place.isBlock()) {
            buffer = 0.0;
            return;
        }

        if (isExempt(ExemptType.TELEPORT, ExemptType.CLIENT_ANTICHEAT, ExemptType.INVALID_GAMEMODE)) {
            buffer = 0.0;
            return;
        }

        final BlockFace face = place.getFace();
        final Vector3f cursor = place.getCursor();
        final Vector3i pos = place.position;

        if (hasPlaced && (face != lastFace || !cursor.equals(lastCursor) || !pos.equals(lastPos))) {
            final String verbose = "face= " + face + "\nlastFace= " + lastFace
                    + "\ncursor= " + PluginLoader.INSTANCE.getLangManager().toUnlabeledString(cursor) + "\nlastCursor= " + PluginLoader.INSTANCE.getLangManager().toUnlabeledString(lastCursor)
                    + "\npos= " + PluginLoader.INSTANCE.getLangManager().toUnlabeledString(pos) + "\nlastPos= " + PluginLoader.INSTANCE.getLangManager().toUnlabeledString(lastPos);
            if (!player.canSkipTicks()) {
                if (buffer++ > 3) {
                    if (flagAndAlert(verbose) && shouldCancel()) {
                        place.resync();
                    }
                }
            } else {
                rewardBufferAndVL();
                flags.add(verbose);
            }
        }

        lastFace = face;
        lastCursor = cursor;
        lastPos = pos;
        hasPlaced = true;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isExempt(ExemptType.INVALID_GAMEMODE) || isTickPacket(event.getPacketType())) {
            hasPlaced = false;
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicks()) return;

        if (player.isTickingReliablyFor(3)) {
            for (String verbose : flags) {
                flagAndAlert(verbose);
            }
        }

        flags.clear();
    }
}