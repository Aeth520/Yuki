package cn.aetheris.yuki.check.impl.player.xray;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.api.events.PlayerFoundOreEvent;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.Cluster;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@CheckData(name = "XRayA (Heuristics)", configName = "XRayA", description = "Using XRay / mining bot", decay = 1, type = CheckType.XRAY)
public final class XRayA extends Check implements PacketCheck {

    private final ArrayList<Vector3i> miningOresPos = new ArrayList<>();
    private final ArrayList<Vector3i> miningBlocksPos = new ArrayList<>();
    private Vector3i lastMiningPos = null;
    private Vector3i lastOrePos = null;
    private Cluster cluster = null;
    private Cluster lastCluster = null;
    private List<String> checkBlocks = new LinkedList<>();

    public XRayA(PlayerData player) {
        super(player);
    }

    public void reset() {
        lastCluster = null;
        cluster = null;
        lastMiningPos = null;
        lastOrePos = null;
        miningOresPos.clear();
        miningBlocksPos.clear();
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {
            double dist = lastMiningPos == null ? 0 : (
                    MathUtil.square(lastMiningPos.x - player.getLocationData().getX()) +
                            MathUtil.square(lastMiningPos.y - player.getLocationData().getY()) +
                            MathUtil.square(lastMiningPos.z - player.getLocationData().getZ()));
            if (dist > 20 * 20) {
                reset();
            }
        }

        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;
        WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
        if (digging.getAction() != DiggingAction.START_DIGGING && digging.getAction() != DiggingAction.FINISHED_DIGGING)
            return;
        double dist = lastMiningPos == null ? 0 : (MathUtil.square(lastMiningPos.x - digging.getBlockPosition().x) + MathUtil.square(lastMiningPos.y - digging.getBlockPosition().y) + MathUtil.square(lastMiningPos.z - digging.getBlockPosition().z));
        if (dist > 10 * 10) {
            reset();
        }
        boolean havingCheck = false;
        lastMiningPos = digging.getBlockPosition();
        StateType blockType = player.getCompensatedWorld().getBlockType(digging.getBlockPosition().x, digging.getBlockPosition().y, digging.getBlockPosition().z);
        if (checkBlocks.contains(blockType.getName())) {
            if (miningOresPos.contains(digging.getBlockPosition())) {
                return;
            }
            PlayerFoundOreEvent oreEvent = new PlayerFoundOreEvent(player, blockType.getName(), digging.getBlockPosition());
            Bukkit.getPluginManager().callEvent(oreEvent);
            if (oreEvent.isCancelled()) {
                return;
            }
            double distOre = lastOrePos == null ? 0 : (MathUtil.square(lastOrePos.x - digging.getBlockPosition().x) + MathUtil.square(lastOrePos.y - digging.getBlockPosition().y) + MathUtil.square(lastOrePos.z - digging.getBlockPosition().z));
            lastOrePos = digging.getBlockPosition();

            miningOresPos.add(digging.getBlockPosition());
            if (distOre > 6 * 6) {
                lastCluster = cluster;
                cluster = new Cluster();
                for (Vector3i i : miningOresPos)
                    cluster.addBlock(i);
                miningOresPos.clear();
                havingCheck = true;
            }
        } else {
            if (miningBlocksPos.contains(digging.getBlockPosition())) {
                return;
            }
            miningBlocksPos.add(digging.getBlockPosition());
        }
        if (miningOresPos.size() > 35) {
            miningOresPos.remove(0);
        }
        if (miningBlocksPos.size() > 70) {
            miningBlocksPos.remove(0);
        }
        if (cluster != null && lastCluster != null) {
            if (!havingCheck) return;
            List<Vector3i> currentBlocks = new ArrayList<>(miningBlocksPos);
            miningBlocksPos.clear();
            lastCluster = null;

            double[] center1 = cluster.getCenter();
            double[] center2 = lastCluster.getCenter();
            double distClusterSq = MathUtil.square(center1[0] - center2[0]) +
                    MathUtil.square(center1[1] - center2[1]) +
                    MathUtil.square(center1[2] - center2[2]);
            if (distClusterSq > 60 * 60) return;

            double[] startPoint = lastCluster.getCenter();
            double[] endPoint = cluster.getCenter();
            double[] direction = {
                    endPoint[0] - startPoint[0],
                    endPoint[1] - startPoint[1],
                    endPoint[2] - startPoint[2]
            };
            double length = Math.sqrt(
                    Math.pow(endPoint[0] - startPoint[0], 2) +
                            Math.pow(endPoint[1] - startPoint[1], 2) +
                            Math.pow(endPoint[2] - startPoint[2], 2)
            );
            if (length > 0) {
                direction[0] /= length;
                direction[1] /= length;
                direction[2] /= length;
            }

            double[] score = calculatePathFitness(currentBlocks, startPoint, direction, length);
            double finalScore = score[0] * 0.5 + score[1] * 0.5 + score[2] * 0.2;
            finalScore = Math.max(0, Math.min(1, finalScore));

            if (finalScore > 0.65) {
                flagAndAlert(
                        "s0= " + String.format("%.2f%%", finalScore) + "\n" +
                                "s1= " + score[0] + "\n" +
                                "s2= " + score[1] + "\n" +
                                "s3= " + score[2]
                );
            }
        }
    }

    public double[] calculatePathFitness(List<Vector3i> playerPath, double[] startPoint, double[] direction, double pathLength) {
        if (playerPath.isEmpty()) return new double[]{0, 0, 0};

        double[] playerDirection = calculatePathDirection(playerPath);
        double angle = angleBetweenVectors(playerDirection, direction);

        double avgDistance = 0;
        for (Vector3i block : playerPath) {
            double[] point = {block.getX(), block.getY(), block.getZ()};
            avgDistance += distanceToLine(point, startPoint, direction);
        }
        avgDistance /= playerPath.size();

        double progressRatio = getRatio(playerPath, startPoint, direction, pathLength);

        double angleScore = 1 - angle / Math.PI;
        double distanceScore = 1 - Math.min(1, avgDistance / 5);
        double progressScore = 1 - Math.abs(progressRatio - 0.5) * 2;

        return new double[]{angleScore, distanceScore, progressScore};
    }

    private double getRatio(List<Vector3i> playerPath, double[] startPoint, double[] direction, double pathLength) {
        double avgProgress = 0;
        for (Vector3i block : playerPath) {
            double[] point = {block.getX(), block.getY(), block.getZ()};
            double[] vecToPoint = {
                    point[0] - startPoint[0],
                    point[1] - startPoint[1],
                    point[2] - startPoint[2]
            };
            double progress = dotProduct(vecToPoint, direction);
            avgProgress += progress;
        }
        avgProgress /= playerPath.size();
        return Math.min(1, Math.max(0, avgProgress / pathLength));
    }

    private double[] calculatePathDirection(List<Vector3i> path) {
        if (path.size() < 2) return new double[]{0, 0, 0};

        Vector3i first = path.get(0);
        Vector3i last = path.get(path.size() - 1);

        double[] direction = {
                last.getX() - first.getX(),
                last.getY() - first.getY(),
                last.getZ() - first.getZ()
        };

        double length = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1] + direction[2] * direction[2]);
        if (length > 0) {
            direction[0] /= length;
            direction[1] /= length;
            direction[2] /= length;
        }
        return direction;
    }

    private double distanceToLine(double[] point, double[] linePoint, double[] direction) {
        double[] ap = {
                point[0] - linePoint[0],
                point[1] - linePoint[1],
                point[2] - linePoint[2]
        };

        double[] cross = crossProduct(ap, direction);
        double crossNorm = Math.sqrt(cross[0] * cross[0] + cross[1] * cross[1] + cross[2] * cross[2]);
        double dirNorm = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1] + direction[2] * direction[2]);

        return crossNorm / dirNorm;
    }

    private double[] crossProduct(double[] a, double[] b) {
        return new double[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private double angleBetweenVectors(double[] v1, double[] v2) {
        double dot = dotProduct(v1, v2);
        double norm1 = Math.sqrt(dotProduct(v1, v1));
        double norm2 = Math.sqrt(dotProduct(v2, v2));
        return Math.acos(dot / (norm1 * norm2));
    }

    @Override
    public void reload() {
        checkBlocks = getConfig().getStringList(getConfigName() + ".blocks");
    }
}
