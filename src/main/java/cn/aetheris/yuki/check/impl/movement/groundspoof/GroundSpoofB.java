package cn.aetheris.yuki.check.impl.movement.groundspoof;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.ghostblock.GhostBlockUtil;
import cn.aetheris.yuki.util.time.TimeUtils;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckData(name = "GroundSpoofB", configName = "GroundSpoofB", type = CheckType.GROUNDSPOOF, description = "Spoofed Ground Stats", setback = 16, decay = 0.65)
public final class GroundSpoofB extends Check implements PostPredictionCheck {

    boolean isIgnoreGhostBlock;
    int tick;

    public GroundSpoofB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_8) && player.gamemode == GameMode.SPECTATOR)
            return;

        if (player.exemptOnGround() || !predictionComplete.isChecked()) return;

        if (player.isRiptidePose() || player.sinceRiptideSpinTick < 20) return;

        if (player.getSetbackTeleportUtil().blockOffsets) return;

        if (predictionComplete.getData().isTeleport()) return;

        if (GhostBlockUtil.isGhostBlock(player) && !isIgnoreGhostBlock) {
            rewardBufferAndVL();
            return;
        }

        if (!TimeUtils.hasExpired(player.joinTime, 10)) return;

        if (player.compensatedEntities.getSelf().getType() == EntityTypes.WIND_CHARGE) return;

        if (player.getSetbackTeleportUtil().insideUnloadedChunk()) return;

        if (isExempt(ExemptType.BREWERRY_PUSH,
                ExemptType.GSIT_ACTION,
                ExemptType.WEAPON_SHOOT,
                ExemptType.VEHICLE,
                ExemptType.RESPAWN)) return;

        tick = !player.clientClaimsLastOnGround ? tick + 1 : 0;

        if (player.clientClaimsLastOnGround != player.onGround) {
            double add = tick > 8 ? 1.35 : 0.75;
            buffer += add;
            if (buffer > 5.7) {
                if (flagAndAlert("out= " + player.clientClaimsLastOnGround + "\nplay= " + player.onGround + "\nt= " + tick + "\ni= " + isIgnoreGhostBlock + "\ns= " + (getViolations() > getSetbackVL()) + "\nf= " + player.getFallDistance() + "\nx= " + player.x + "\ny= " + player.y + "\nz= " + player.z)) {
                    setbackIfAboveSetbackVL();
                    rewardBufferAndVL();
                }
            }
            player.checkManager.getCheck(GroundSpoofA.class).flipPlayerGroundStatus = true;
        } else {
            rewardBufferAndVL();
        }
    }

    @Override
    public void reload() {
        super.reload();
        isIgnoreGhostBlock = getConfig().getBooleanElse(getConfigName() + ".ignore-ghost", true);
    }
}
