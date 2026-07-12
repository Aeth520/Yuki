package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.Pair2;
import cn.aetheris.yuki.util.lists.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;


@CheckData(
        name = "KillAuraJ (List)",
        type = CheckType.KILLAURA,
        configName = "KillAuraJ",
        decay = 0.86
)
public final class KillAuraJ extends Check implements PacketCheck {

    private EvictingList<Pair2<Boolean, Boolean>> diffs;
    private int sampleSize = 1600;

    private double minDiff;
    private double minCombatRatio;
    private double minInvalidSamples;

    public KillAuraJ(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            return;
        }

        if (isExempt(ExemptType.TELEPORT, ExemptType.RESPAWN,
                ExemptType.INVALID_GAMEMODE, ExemptType.FLYING,
                ExemptType.VEHICLE, ExemptType.JOIN)) {
            return;
        }

        if (diffs == null) {
            diffs = new EvictingList<>(sampleSize);
            return;
        }

        long diff = Math.abs(player.getTransactionPing() - player.getKeepAlivePing());
        boolean isInvalid = diff >= minDiff;
        boolean inCombat = player.hasAttackedSince(1200L);
        diffs.add(new Pair2<>(isInvalid, inCombat));

        if (diffs.isFull()) {
            check();
            diffs.clear();
        }
    }

    private void check() {
        int totalInvalid = 0;
        int combatInvalid = 0;
        int nonCombatInvalid = 0;

        for (Pair2<Boolean, Boolean> sample : diffs) {
            if (!sample.getX()) continue;

            totalInvalid++;
            if (sample.getY()) combatInvalid++;
            else nonCombatInvalid++;
        }

        if (totalInvalid == 0) return;

        double combatRatio = (double) combatInvalid / totalInvalid;

        if (combatRatio > minCombatRatio && combatInvalid >= minInvalidSamples) {
            if (buffer++ > 1) {
                if (flagAndAlert(
                        "y= " + combatInvalid +
                                "\nx= " + nonCombatInvalid +
                                "\ns= " + totalInvalid
                )) {
                    player.mitigateDamage();
                    if (player.packetStateData.isSlowedByUsingItem()) {
                        resetPlayerUseItem(player.bukkitPlayer);
                    }
                    diffs = new EvictingList<>(sampleSize);
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }

    @Override
    public void reload() {
        minDiff = getConfig().getDoubleElse(getConfigName() + ".min-diff", 80);
        sampleSize = getConfig().getIntElse(getConfigName() + ".max-sample", 1600);
        minInvalidSamples = getConfig().getDoubleElse(getConfigName() + ".min-invalid-samples", 16);
        minCombatRatio = getConfig().getDoubleElse(getConfigName() + ".min-combat-ratio", 0.9);

        this.diffs = new EvictingList<>(sampleSize);
    }
}
