package cn.aetheris.yuki.check.impl.player.breaking.wrong;


import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.update.BlockBreak;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "WrongBreakC", type = CheckType.BREAK, configName = "WrongBreakC", decay = 0.235, description = "No break delay", experimental = true)
public final class WrongBreakC extends Check implements BlockBreakCheck {

    private boolean hasFinished;
    private int flags;
    private int minTicks;
    private int ticks;

    public WrongBreakC(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action == DiggingAction.START_DIGGING) {
            if (hasFinished) {
                if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                    if (flagAndAlert()) {
                        blockBreak.cancel();
                    }
                } else {
                    flags++;
                }
            }
        }

        if (blockBreak.action == DiggingAction.FINISHED_DIGGING) {
            hasFinished = true;
            ticks = 0;
        }

        if (blockBreak.action == DiggingAction.START_DIGGING
                && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)
                && ticks < minTicks
                && PluginLoader.INSTANCE.getConfigManager().isMitigateInvalidBreak()) {
            blockBreak.cancel();
            LogUtils.mitigate("&b" + player.getName() + "&7 has been reset break (&b" + ticks + "&7)");
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !player.packetStateData.lastPacketWasTeleport && !player.packetStateData.lastPacketWasOnePointSeventeenDuplicate) {
            hasFinished = false;
            ticks++;
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.skippedTickInActualMovement) {
            for (; flags > 0; flags--) {
                flagAndAlert();
            }
        }

        flags = 0;
    }

    @Override
    public void reload() {
        super.reload();
        minTicks = MathUtil.clamp(getConfig().getIntElse("mitigates.min-break-delay", 3), 0, 6);
    }
}
