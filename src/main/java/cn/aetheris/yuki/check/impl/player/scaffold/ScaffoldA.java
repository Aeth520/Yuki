package cn.aetheris.yuki.check.impl.player.scaffold;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.Pair;
import cn.aetheris.yuki.protocol.nms.Ray;
import cn.aetheris.yuki.protocol.nms.ReachUtils;
import cn.aetheris.yuki.util.update.BlockPlace;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@CheckData(name = "ScaffoldA (Raytrace)",
        configName = "ScaffoldA",
        experimental = true,
        type = CheckType.SCAFFOLD)
public final class ScaffoldA extends BlockPlaceCheck {
    long lastFlag;
    boolean ignorePost;
    boolean isMatrix;

    public ScaffoldA(PlayerData player) {
        super(player);
        lastFlag = 0L;
        ignorePost = false;
        isMatrix = Bukkit.getPluginManager().getPlugin("Matrix") != null && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_16);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (place.getMaterial() == StateTypes.SCAFFOLDING || place.getMaterial() == StateTypes.FIRE) return;

        if (isMatrix) return;

        if (isExempt(ExemptType.TELEPORT, ExemptType.CLIENT_ANTICHEAT)) return;

        if (player.isDigging() || player.isBasicDigging() || player.isFinishDigging()) return;

        if (Collections.max(player.uncertaintyHandler.pistonX) != 0
                || Collections.max(player.uncertaintyHandler.pistonY) != 0
                || Collections.max(player.uncertaintyHandler.pistonZ) != 0) {
            return;
        }

        if (isExempt(ExemptType.TELEPORT)) return;

        if (player.gamemode == GameMode.SPECTATOR) return;

        if (buffer > 0 && !didRayTraceHit(place)) {

            ignorePost = true;
            
            if (flagAndAlert("(Pre)") && shouldCancel()) {
                place.resync();  
                player.mitigateDamage();

            }
        }
    }

    @Override
    public void onPostFlyingBlockPlace(BlockPlace place) {
        if (place.getMaterial() == StateTypes.SCAFFOLDING || place.getMaterial() == StateTypes.FIRE || place.getMaterial() == StateTypes.PISTON || place.getMaterial() == StateTypes.PISTON_HEAD)
            return;
        if (player.inVehicle()) {
            return;
        }
        if (player.gamemode == GameMode.SPECTATOR) return; 

        if (Collections.max(player.uncertaintyHandler.pistonX) != 0
                || Collections.max(player.uncertaintyHandler.pistonY) != 0
                || Collections.max(player.uncertaintyHandler.pistonZ) != 0) {
            return;
        }

        
        if (ignorePost) {
            ignorePost = false;
            return;
        }

        
        boolean hit = didRayTraceHit(place);
        
        if (!hit) {
            buffer = 1;
            if (flagAndAlert("(Post)")) {
                player.mitigateDamage();
            }
        } else {
            rewardBufferAndVL();
        }
    }

    private boolean didRayTraceHit(BlockPlace place) {
        SimpleCollisionBox box = new SimpleCollisionBox(place.position);

        List<Vector3f> possibleLookDirs = new LinkedList<>(Arrays.asList(
                new Vector3f(player.yaw, player.pitch, 0),
                new Vector3f(player.lastYaw, player.pitch, 0)
        ));

        final double[] possibleEyeHeights = player.getPossibleEyeHeights();

        
        double minEyeHeight = Double.MAX_VALUE;
        double maxEyeHeight = Double.MIN_VALUE;
        for (double height : possibleEyeHeights) {
            minEyeHeight = Math.min(minEyeHeight, height);
            maxEyeHeight = Math.max(maxEyeHeight, height);
        }

        SimpleCollisionBox eyePositions = new SimpleCollisionBox(player.x, player.y + minEyeHeight, player.z, player.x, player.y + maxEyeHeight, player.z);
        eyePositions.expand(player.getMovementThreshold());

        
        if (eyePositions.isIntersected(box)) {
            return true;
        }
        

        
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            possibleLookDirs.add(new Vector3f(player.lastYaw, player.lastPitch, 0));
        }

        
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
            possibleLookDirs = Collections.singletonList(new Vector3f(player.yaw, player.pitch, 0));
        }


        final double distance = player.compensatedEntities.getSelf().getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        for (double d : possibleEyeHeights) {
            for (Vector3f lookDir : possibleLookDirs) {
                
                Vector3d starting = new Vector3d(player.x, player.y + d, player.z);
                
                Ray trace = new Ray(player, starting.getX(), starting.getY(), starting.getZ(), lookDir.getX(), lookDir.getY());
                Pair<Vector3dm, BlockFace> intercept = ReachUtils.calculateIntercept(box, trace.getOrigin(), trace.getPointAtDistance(distance));

                if (intercept.first() != null) return true;
            }
        }

        return false;
    }
}