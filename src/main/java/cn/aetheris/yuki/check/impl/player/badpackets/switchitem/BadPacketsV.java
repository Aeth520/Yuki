package cn.aetheris.yuki.check.impl.player.badpackets.switchitem;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.ArrayDeque;

@CheckData(name = "BadPacketsV (Held)",
        type = CheckType.BADPACKETS,
        configName = "BadPacketsV",
        description = "Invalid slot change packet sent",
        decay = 0.755,
        setback = 4)
public final class BadPacketsV extends Check implements PostPredictionCheck {

    private final ArrayDeque<String> flags = new ArrayDeque<>();
    private boolean setback;

    public BadPacketsV(final PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            if (player.packetActionProcessor.isAttacking()
                    || player.packetActionProcessor.isRightClicking()
                    || player.packetActionProcessor.isOpeningInventory()
                    || player.packetActionProcessor.isReleasing()
                    || player.packetActionProcessor.isSprinting()
            ) {
                String verbose = "attacking= " + player.packetActionProcessor.isAttacking()
                        + "\nrightClicking= " + player.packetActionProcessor.isRightClicking()
                        + "\nopeningInventory= " + player.packetActionProcessor.isOpeningInventory()
                        + "\nreleasing= " + player.packetActionProcessor.isReleasing()
                        + "\nsprinting= " + player.packetActionProcessor.isSprinting();
                if (buffer++ > 5) {
                    if (player.canSkipTicksPreVia() && flags.add(verbose) && flagAndAlert(verbose)) {
                        if (player.packetActionProcessor.isRightClicking()) {
                            setback = true;
                            MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {
                                resetPlayerUseItem(player.bukkitPlayer);
                                player.mitigateDamage();
                            }, 5L);
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicksPreVia()) {
            if (setback) {
                setback = false;
                setbackIfAboveSetbackVL();
                resetPlayerUseItem(player.bukkitPlayer);
                player.mitigateDamage();
                shuffleAboveSetbackVL();
            }
            return;
        }

        if (player.isTickingReliablyFor(3)) {
            for (String verbose : flags) {
                if (flagAndAlert(verbose) && setback) {
                    setback = false;
                    setbackIfAboveSetbackVL();
                }
            }
        }

        setback = false;
        flags.clear();
    }
}