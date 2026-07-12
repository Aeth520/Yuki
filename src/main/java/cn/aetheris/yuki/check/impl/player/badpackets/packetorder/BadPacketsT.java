package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(name = "BadPacketsT", type = CheckType.BADPACKETS, description = "Invalid Sneak/Sprint sent", decay = 0.25)
public final class BadPacketsT extends Check implements PostPredictionCheck {

    private final boolean unSupoort;
    private boolean sentSprint;
    private boolean sentSneak;
    private float invalid;
    private boolean sent;

    public BadPacketsT(PlayerData player) {
        super(player);
        unSupoort = player.getClientVersion().isNewerThan(ClientVersion.V_1_16_4);
        invalid = 0;
        sent = false;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event);
            boolean sprint = false, sneak = false;

            if (isExempt(ExemptType.INVALID_GAMEMODE)) {
                return;
            }

            switch (action.getAction()) {
                case START_SNEAKING, STOP_SNEAKING -> sneak = true;
                case START_SPRINTING, STOP_SPRINTING -> sprint = true;
            }

            boolean alreadySent = (sprint && sentSprint) || (sneak && sentSneak);
            if (alreadySent && !unSupoort) {
                flagAndAlert("(Action) \nsprint= " + sentSprint + "\nsneak= " + sentSneak);
            }

            sentSprint = sprint;
            sentSneak = sneak;

            if (sprint || sneak) {
                sent = true;
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

            if (isExempt(ExemptType.INVALID_GAMEMODE)) {
                return;
            }

            if (sent) {
                if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                    if (flagAndAlert("(Wtap) \ns= " + sent)) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    invalid++;
                }
            }
        }

        if (isFlying(event.getPacketType())) {
            sentSprint = sentSneak = false;
            sent = false;
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicksPreVia()) return;

        if (isExempt(ExemptType.INVALID_GAMEMODE)) {
            return;
        }

        if (player.isTickingReliablyFor(3) && !player.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(0)) {
            invalid *= 0.05f;
        }

        if (invalid > 1) {
            flagAndAlert("(Wtap-Prediction) \ni= " + invalid);
        } else {
            rewardBufferAndVL();
        }

        invalid = 0;
    }
}