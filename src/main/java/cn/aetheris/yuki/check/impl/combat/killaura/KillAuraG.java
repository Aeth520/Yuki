package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.util.lists.EvictingQueue;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

import java.util.LinkedList;
import java.util.List;

@CheckData(name = "KillAuraG (Accuracy)", type = CheckType.KILLAURA, configName = "KillAuraG", description = "check for invalid hit accuracy", decay = 0.45)
public final class KillAuraG extends Check implements PacketCheck {

    private long lastFlag;
    private int maxAccuracy;
    private int sampleSize;
    private int minCps;
    private double minDistance;
    private boolean hit = false;
    private boolean shouldCancel = false;
    private EvictingQueue<Boolean> hitList;
    private EvictingQueue<Vector3dm> positionList;
    private int lastAttack = 0;
    private int maxCombatDuration;
    private double minAverageTargetMovement;
    private PacketEntity lastTarget = null;

    public KillAuraG(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity action = new WrapperPlayClientInteractEntity(event);
            if (action.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return;
            }

            if (player.getDeltaXZ() < 0.27F) {
                return;
            }

            final PacketEntity target = player.compensatedEntities.entityMap.get(action.getEntityId());
            if (target == null) {
                return;
            }

            if (target.getType() != EntityTypes.PLAYER) {
                rewardBufferAndVL();
                return;
            }

            if (target.getPossibleCollisionBoxes().distance(player.boundingBox) > minDistance) {
                hit = true;
            }

            if (target != lastTarget || (lastAttack - ((int) time() / 1000)) > maxCombatDuration) {
                hitList.clear();
                positionList.clear();
            }

            lastTarget = target;
            lastAttack = (int) time() / 1000;
            if (hitList.size() == sampleSize) {
                double accuracy = MathUtil.calculateTruePercentage(hitList);
                double averageTargetMovement = getAverageTargetMovement();
                double cps = player.getCps();
                if (cps > minCps) {
                    if (accuracy >= maxAccuracy && averageTargetMovement >= minAverageTargetMovement) {
                        if (buffer++ > 7 && time() - lastFlag >= 500L) {
                            if (flagAndAlert("accuracy= " + accuracy + "%")) {
                                lastFlag = time();
                                player.mitigateDamage();
                                if (shouldCancel) {
                                    event.setCancelled(true);
                                    player.onPacketCancel();
                                }
                            }
                        }
                    }
                }
            }
            positionList.add(target.getPossibleCollisionBoxes().max());

        } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            hitList.add(hit);
            hit = false;
        }
    }


    private double getAverageTargetMovement() {
        List<Double> movements = new LinkedList<>();
        Vector3dm lastPosition = null;
        for (Vector3dm position : positionList) {
            position.setY(0);
            if (lastPosition != null) {
                movements.add(position.distance(lastPosition));
            }
            lastPosition = position;
        }
        return MathUtil.getAverageDouble(movements);
    }

    @Override
    public void reload() {
        super.reload();
        minCps = getConfig().getIntElse(getConfigName() + ".min-cps", 5);
        maxAccuracy = Math.min(100, getConfig().getIntElse(getConfigName() + ".accuracy", 95));
        sampleSize = getConfig().getIntElse(getConfigName() + ".size", 25);
        minDistance = getConfig().getDoubleElse(getConfigName() + ".reach", 1.45);
        maxCombatDuration = getConfig().getIntElse(getConfigName() + ".combat-time", 10);
        minAverageTargetMovement = getConfig().getDoubleElse(getConfigName() + ".min-average-target-movement", 0.3);
        shouldCancel = getConfig().getBooleanElse(getConfigName() + ".should-cancel", false);
        hitList = new EvictingQueue<>(sampleSize);
        positionList = new EvictingQueue<>(sampleSize);
    }
}
