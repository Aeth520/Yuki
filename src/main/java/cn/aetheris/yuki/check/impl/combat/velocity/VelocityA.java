package cn.aetheris.yuki.check.impl.combat.velocity;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.util.Pair;
import cn.aetheris.yuki.data.movement.VectorData;
import cn.aetheris.yuki.data.movement.VelocityData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.LinkedList;


@CheckData(name = "VelocityA (Prediction)",
        alternativeName = "VelocityA",
        configName = "VelocityA",
        description = "Ignoring horizontal/vertical knockback",
        type = CheckType.VELOCITY,
        setback = 10, decay = 0.025)
public final class VelocityA extends Check implements PostPredictionCheck {
    Deque<VelocityData> firstBreadMap = new LinkedList<>();

    Deque<VelocityData> lastKnockbackKnownTaken = new LinkedList<>();
    VelocityData firstBreadOnlyKnockback = null;
    @Getter
    boolean knockbackPointThree = false;

    boolean tempFixedSync;

    double offsetToFlag;
    double maxAdv, immediate, ceiling, multiplier;

    double threshold;

    public VelocityA(PlayerData player) {
        super(player);
    }


    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(event);
            int entityId = velocity.getEntityId();

            PlayerData player = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            
            
            if (player.compensatedEntities.serverPlayerVehicle != null && entityId != player.compensatedEntities.serverPlayerVehicle) {
                return;
            }
            if (player.compensatedEntities.serverPlayerVehicle == null && entityId != player.entityID) {
                return;
            }

            
            
            Vector3d playerVelocity = velocity.getVelocity();

            
            if (playerVelocity.getY() == -0.04) {
                velocity.setVelocity(playerVelocity.add(new Vector3d(0, 1 / 8000D, 0)));
                playerVelocity = velocity.getVelocity();
                event.markForReEncode(true);
            }

            
            player.sendTransaction();
            addPlayerKnockback(entityId, player.lastTransactionSent.get(), new Vector3dm(playerVelocity.getX(), playerVelocity.getY(), playerVelocity.getZ()));
            event.getTasksAfterSend().add(player::sendTransaction);
        }
    }

    @NotNull
    public Pair<VelocityData, Vector3dm> getFutureKnockback() {
        
        if (!firstBreadMap.isEmpty()) {
            VelocityData data = firstBreadMap.peek();
            return new Pair<>(data, data != null ? data.vector : null);
        }

        
        if (!lastKnockbackKnownTaken.isEmpty()) {
            VelocityData data = lastKnockbackKnownTaken.peek();
            return new Pair<>(data, data != null ? data.vector : null);
        }

        
        if (player.firstBreadKB != null && player.likelyKB == null) {
            VelocityData data = player.firstBreadKB;
            return new Pair<>(data, data.vector.clone());
        } else if (player.likelyKB != null) { 
            VelocityData data = player.likelyKB;
            return new Pair<>(data, data.vector.clone());
        }
        return new Pair<>(null, null);
    }

    private void addPlayerKnockback(int entityID, int breadOne, Vector3dm knockback) {
        firstBreadMap.add(new VelocityData(entityID, breadOne, player.getSetbackTeleportUtil().isSendingSetback, knockback));
    }

    public VelocityData calculateRequiredKB(int entityID, int transaction, boolean isJustTesting) {
        tickKnockback(transaction);

        VelocityData returnLastKB = null;
        for (VelocityData data : lastKnockbackKnownTaken) {
            if (data.entityID == entityID)
                returnLastKB = data;
        }

        if (!isJustTesting) {
            lastKnockbackKnownTaken.clear();
        }
        return returnLastKB;
    }

    private void tickKnockback(int transactionID) {
        firstBreadOnlyKnockback = null;
        if (firstBreadMap.isEmpty()) return;
        VelocityData data = firstBreadMap.peek();
        while (data != null) {
            if (data.transaction == transactionID) { 
                firstBreadOnlyKnockback = new VelocityData(data.entityID, data.transaction, data.isSetback, data.vector);
                
                break; 
            } else if (data.transaction < transactionID) { 
                if (firstBreadOnlyKnockback != null) { 
                    lastKnockbackKnownTaken.add(new VelocityData(data.entityID, data.transaction, data.vector, data.isSetback, data.offset));
                } else {
                    
                    lastKnockbackKnownTaken.add(new VelocityData(data.entityID, data.transaction, data.isSetback, data.vector));
                }
                firstBreadOnlyKnockback = null;
                firstBreadMap.poll();
                data = firstBreadMap.peek();
            } else { 
                break;
            }
        }
    }

    public void forceExempt() {
        
        if (player.firstBreadKB != null) {
            player.firstBreadKB.offset = 0;
        }

        if (player.likelyKB != null) {
            player.likelyKB.offset = 0;
        }
    }

    public void setPointThree(boolean isPointThree) {
        knockbackPointThree = knockbackPointThree || isPointThree;
    }

    public void handlePredictionAnalysis(double offset) {
        if (player.firstBreadKB != null) {
            player.firstBreadKB.offset = Math.min(player.firstBreadKB.offset, offset);
        }

        if (player.likelyKB != null) {
            player.likelyKB.offset = Math.min(player.likelyKB.offset, offset);
        }
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        double offset = predictionComplete.getOffset();
        if (!predictionComplete.isChecked() || predictionComplete.getData().isTeleport()) {
            forceExempt();
            return;
        }

        boolean wasZero = knockbackPointThree;
        knockbackPointThree = false;

        if (player.likelyKB == null && player.firstBreadKB == null) {
            return;
        }

        if (player.predictedVelocity.isFirstBreadKb()) {
            firstBreadOnlyKnockback = null;
            firstBreadMap.poll(); 
        }

        if (wasZero || player.predictedVelocity.isKnockback()) {
            
            if (player.firstBreadKB != null) {
                player.firstBreadKB.offset = Math.min(player.firstBreadKB.offset, offset);
            }

            
            if (player.likelyKB != null) {
                player.likelyKB.offset = Math.min(player.likelyKB.offset, offset);
            }
        }

        if (EntityTypes.isTypeInstanceOf(player.compensatedEntities.getSelf().getType(), EntityTypes.CAMEL)) return;

        if (player.likelyKB != null) {
            final double offsetKnockBack = player.likelyKB.offset;
            if (offsetKnockBack > offsetToFlag) {

                if (tempFixedSync && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_17) && offsetKnockBack > 0.28 && offsetKnockBack < 0.3 && player.getDeltaXZ() < 8) {
                    return;
                }

                threshold = Math.min(threshold + offsetKnockBack, ceiling);
                if (player.likelyKB.isSetback) { 
                    player.getSetbackTeleportUtil().executeViolationSetback();
                } else if (flagAndAlert(offsetKnockBack == Integer.MAX_VALUE ? "ignored knockback"
                        : "o= " + formatOffset(offsetKnockBack)
                        + "\ns= " + (offsetKnockBack >= immediate || threshold >= maxAdv)
                        + "\nv= " + player.likelyKB.isSetback
                        + "\nk= " + player.predictedVelocity.isKnockback())) { 
                    if (offsetKnockBack >= immediate || threshold >= maxAdv) {
                        setbackIfAboveSetbackVL();
                    }
                } else {
                    rewardVL();
                }
            } else if (threshold > 0.05) {
                threshold *= multiplier;
            }
        }
    }

    public boolean shouldIgnoreForPrediction(VectorData data) {
        if (data.isKnockback() && data.isFirstBreadKb()) {
            return player.firstBreadKB.offset > offsetToFlag;
        }
        return false;
    }

    public boolean wouldFlag() {
        return (player.likelyKB != null && player.likelyKB.offset > offsetToFlag) || (player.firstBreadKB != null && player.firstBreadKB.offset > offsetToFlag);
    }

    public VelocityData calculateFirstBreadKnockBack(int entityID, int transaction) {
        tickKnockback(transaction);
        if (firstBreadOnlyKnockback != null && firstBreadOnlyKnockback.entityID == entityID)
            return firstBreadOnlyKnockback;
        return null;
    }

    @Override
    public void reload() {
        super.reload();
        final String strictness = getConfig().getStringElse("VelocityA.strictness", "balanced");
        offsetToFlag = switch (strictness) {
            case "strict" -> 0.0001;
            case "lenient" -> 0.045;
            case "chaos" -> 0.70;
            default -> 0.035;
        };
        maxAdv = switch (strictness) {
            case "strict" -> 1;
            case "lenient" -> 4;
            case "chaos" -> 8;
            default -> 3;
        };
        immediate = switch (strictness) {
            case "strict" -> 0.02;
            case "lenient" -> 0.06;
            case "chaos" -> 0.95;
            default -> 0.05;
        };
        multiplier = switch (strictness) {
            case "strict" -> 0.9985;
            case "lenient" -> 0.9995;
            case "chaos" -> 0.99995;
            default -> 0.999;
        };
        ceiling = switch (strictness) {
            case "strict" -> 1;
            case "lenient" -> 5;
            case "chaos" -> 8;
            default -> 3.5;
        };
        tempFixedSync = getConfig().getBooleanElse(getConfigName() + ".temp-fixed-sync", false);
    }
}
