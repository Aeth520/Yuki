package cn.aetheris.yuki.check.impl.player.breaking.multi;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockBreak;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3i;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "MultiBreakA (Predict)", type = CheckType.BREAK, configName = "MultiBreakA", description = "check for multibreak", decay = 0.25)
public final class MultiBreakA extends Check implements BlockBreakCheck {
    private final List<String> flags = new ArrayList<>();
    private boolean hasBroken;
    private BlockFace lastFace;
    private Vector3i lastPos;

    public MultiBreakA(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action == DiggingAction.CANCELLED_DIGGING) {
            return;
        }

        if (hasBroken && (blockBreak.face != lastFace || !blockBreak.position.equals(lastPos))) {
            final String verbose = "f= " + blockBreak.face + "\nlf= " + lastFace
                    + "\np= " + PluginLoader.INSTANCE.getLangManger().toUnlabledString(blockBreak.position)
                    + "\nlp= " + PluginLoader.INSTANCE.getLangManger().toUnlabledString(lastPos);
            if (!player.canSkipTicksPreVia()) {
                if (flagAndAlert(verbose) && shouldModifyPackets()) {
                    blockBreak.cancel();
                }
            } else {
                flags.add(verbose);
            }
        }

        lastFace = blockBreak.face;
        lastPos = blockBreak.position;
        hasBroken = true;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.gamemode == GameMode.SPECTATOR || isTickPacket(event.getPacketType())) {
            hasBroken = false;
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicksPreVia()) return;

        if (player.isTickingReliablyFor(3)) {
            for (String verbose : flags) {
                flagAndAlert(verbose);
            }
        }

        flags.clear();
    }
}