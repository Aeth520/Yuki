package cn.aetheris.yuki.check.impl.movement.elytra;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.util.NumberConversions;


@CheckData(name = "ElytraK (Motion)", configName = "ElytraK", type = CheckType.ELYTRA, description = "invalid elytra motion change", decay = 0.6, setback = 10, experimental = true)
public final class ElytraK extends Check implements PostPredictionCheck {

    private long lastFlag;
    private boolean setback;

    private int elytraTicks;

    public ElytraK(PlayerData player) {
        super(player);
        elytraTicks = 0;
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isTickPacket(event.getPacketType())) {
            return;
        }

        if (!Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_8)) {
            return;
        }

        if (isExempt(ExemptType.INVALID_GAMEMODE, ExemptType.JOIN, ExemptType.WAS_SWIMMING, ExemptType.SWIMMING, ExemptType.LIQUID, ExemptType.RESPAWN, ExemptType.TELEPORT, ExemptType.SERVER_SENT_PULLBACK, ExemptType.RIPTIDE)) {
            return;
        }

        if (!player.isGliding()) {
            return;
        }

        final SimpleCollisionBox box = player.getBoundingBox().copy().expand(0.0, 0.5, 0.0);
        final int blockX = NumberConversions.floor(box.maxX);
        final int blockY = NumberConversions.floor(box.maxY);
        final int blockZ = NumberConversions.floor(box.maxZ);

        if (!player.getCompensatedWorld().getBlock(blockX, blockY, blockZ).getType().isAir()) {
            return;
        }

        final double deltaXZ = player.getDeltaXZ();
        final double deltaY = player.getDeltaY();
        final double lastDeltaY = player.getLastDeltaY();
        final boolean riseMotion = player.getY() > player.getLastY() && player.getDeltaXZ() == 0;
        boolean onGround = player.isOnGround() && player.isClientClaimsLastOnGround();
        this.elytraTicks++;

        if (riseMotion && !onGround) {
            if (deltaY == lastDeltaY && deltaY != 0 && lastDeltaY != 0 && elytraTicks > 15) {
                flagAndAlert("(smooth)\nnow= " + deltaY + "\nlast= " + lastDeltaY + "\ntick= " + elytraTicks);
            }
            if (deltaY > 3 && player.getFireworkBoostTicks() < 60 && elytraTicks > 15) {
                flagAndAlert("(bigChange)\nnow= " + deltaY + "\ntick= " + elytraTicks);
            }
            elytraTicks = 0;
        }

        if (!onGround && (player.getY() < player.getLastY() && player.getDeltaXZ() == 0) && elytraTicks > 5) {
            if (player.getPitch() > 79) {
                return;
            }
            flagAndAlert("(fall)\np= " + player.getPitch());
            elytraTicks = 0;
        }

        if (deltaXZ > 0.75F) {
            if (deltaY == 0 && lastDeltaY == 0 && !onGround) {
                if (time() - lastFlag < 500L) {
                    return;
                }
                if (buffer++ > 30) {
                    if (flagAndAlert("(machine)\nnow= " + deltaY + "\nlast= " + lastDeltaY + "\ndxz= " + deltaXZ + "\nb= " + buffer + "\nfd= " + player.getFallDistance() + "\ntick= " + elytraTicks)) {
                        event.setCancelled(true);
                        setback = true;
                        rewardBufferAndVL();
                        player.resyncPose();
                        lastFlag = time();
                    }
                }
            } else {
                buffer = 0;
                setback = false;
            }
        }
        if (isTickPacket(event.getPacketType())) {
            elytraTicks = 0;
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (setback) {
            setback = false;
            buffer = 0;
            rewardBufferAndVL();
            setbackIfAboveSetbackVL();
        }
    }
}