package cn.aetheris.yuki.check.impl.player.baritone;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.location.PacketLocation;
import cn.aetheris.yuki.util.ray.RayLine;
import cn.aetheris.yuki.util.ray.RayUtils;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.*;

@CheckData(name = "BaritoneC (Predict)", configName = "BaritoneC", decay = 0.75, description = "The player is using baritone (rotation)", setback = 8, type = CheckType.BARITONE, experimental = true)
public final class BaritoneC extends Check implements PacketCheck {

    private static final List<String> PATTERNS = Arrays.asList("532", "542", "7432");
    private static final Set<String> INVALID_VALUES = new HashSet<>(List.of("6.0E-4"));

    private final List<PacketLocation> nowMoves = new LinkedList<>();
    private final List<PacketLocation> oldMoves = new LinkedList<>();

    public BaritoneC(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_POSITION) return;

        if (player.hasAttackedSince(200L)) {
            oldMoves.clear();
            nowMoves.clear();
        }

        nowMoves.add(player.getLocationData());
        oldMoves.add(player.getLastLocationData());

        if (oldMoves.size() >= 25 && nowMoves.size() >= 25) {
            analyzeMovement();
            oldMoves.clear();
            nowMoves.clear();
        }
    }

    private void analyzeMovement() {
        RayLine previousRay = null;
        double previousDeltaYaw = 0;
        int yawMoves = 0, pitchMoves = 0, invalidFlags = 0;
        String lastInvalidSensitivity = "";
        final StringBuilder patternBuilder = new StringBuilder();

        for (int i = 0; i < oldMoves.size(); i++) {
            PacketLocation from = oldMoves.get(i);

            for (PacketLocation to : oldMoves) {
                RayLine currentRay = new RayLine(to.getX() - from.getX(), to.getZ() - from.getZ());

                double deltaYaw = RayUtils.calculateRayLines(currentRay, previousRay == null ? currentRay : previousRay);
                double yawChange = Math.abs(RayUtils.wrapYaw(to.getYaw()) - RayUtils.wrapYaw(from.getYaw()));
                double pitchChange = Math.abs(to.getPitch() - from.getPitch());
                if (previousDeltaYaw == 0) previousDeltaYaw = deltaYaw;

                int yawDiff = (int) Math.round(Math.abs(deltaYaw - previousDeltaYaw));
                double scaledYaw = RayUtils.scaleVal(yawChange, 4);
                String scaledYawStr = String.valueOf(scaledYaw);

                patternBuilder.append(yawDiff);

                if (INVALID_VALUES.contains(scaledYawStr) && pitchChange < 1E-9) {
                    lastInvalidSensitivity = scaledYawStr;
                    invalidFlags++;
                }

                if (yawChange > 1E-9) yawMoves++;
                if (pitchChange > 1E-9) pitchMoves++;

                previousRay = currentRay;
                previousDeltaYaw = deltaYaw;
            }
        }

        if (yawMoves == 25 && pitchMoves == 0) {
            if (flagAndAlert("Invalid Pitch Change")) {
                player.mitigateDamage();
            }
        }

        if (invalidFlags > 0 && buffer++ > 7 && !player.hasAttackedSince(1000L)) {
            if (alert("Invalid sensitivity= " + lastInvalidSensitivity)) {
                player.mitigateDamage();
            }
        }









    }
}
