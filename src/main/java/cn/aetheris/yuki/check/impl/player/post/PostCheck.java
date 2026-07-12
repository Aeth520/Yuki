package cn.aetheris.yuki.check.impl.player.post;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingQueue;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;

import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client.*;

@CheckData(name = "PacketOrder (Post)",
        configName = "Post",
        description = "Packet Order",
        type = CheckType.POST,
        setback = 6)
public final class PostCheck extends Check implements PostPredictionCheck {

    private final ArrayDeque<PacketTypeCommon> post = new ArrayDeque<>();
    
    
    private final List<String> flags = new EvictingQueue<>(10);
    private boolean sentFlying = false;
    private int isExemptFromSwingingCheck = Integer.MIN_VALUE;

    public PostCheck(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (predictionComplete.isChecked() && shouldModifyPackets()) {

            if ((player.isInBed || player.lastInBed) && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_4)) {
                return;
            }

            if (isExempt(ExemptType.CLIENT_ANTICHEAT)) return;

            if (!flags.isEmpty()) {
                
                
                
                if (player.isTickingReliablyFor(3) && !player.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(0)) {
                    for (String flag : flags) {
                        if (flagWithSetback()) {
                            alert(flag);
                            player.mitigateDamage();
                            if (flag.contains("HELD_ITEM_CHANGE")) {
                                shuffleAboveSetbackVL();
                            }
                        }
                    }
                }

                flags.clear();
            }
        }
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_ANIMATION) {
            WrapperPlayServerEntityAnimation animation = new WrapperPlayServerEntityAnimation(event);
            if (animation.getEntityId() == player.entityID) {
                if (animation.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM ||
                        animation.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND) {
                    isExemptFromSwingingCheck = player.lastTransactionSent.get();
                }
            }
        }
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (isTickPacket(event.getPacketType())) { 
            post.clear();
            sentFlying = true;
        } else {
            
            PacketTypeCommon packetType = event.getPacketType();
            if (isTransaction(packetType) && player.packetStateData.lastTransactionPacketWasValid) {
                if (sentFlying && !post.isEmpty()) {
                    flags.add(post.getFirst().toString().toLowerCase(Locale.ROOT).replace("_", " ") + " v" + player.getClientVersion().getReleaseName());
                }
                post.clear();
                sentFlying = false;
            } else if (PLAYER_ABILITIES.equals(packetType)
                    || (HELD_ITEM_CHANGE.equals(packetType) && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8))
                    || INTERACT_ENTITY.equals(packetType) || PLAYER_BLOCK_PLACEMENT.equals(packetType)
                    || USE_ITEM.equals(packetType) || PLAYER_DIGGING.equals(packetType)) {
                if (sentFlying) post.add(event.getPacketType());
            } else if (CLICK_WINDOW.equals(packetType) && player.getClientVersion().isOlderThan(ClientVersion.V_1_13)) {
                
                if (sentFlying) post.add(event.getPacketType());
            } else if (ANIMATION.equals(packetType)
                    && (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) 
                    || Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)) 
                    && player.getClientVersion().isOlderThan(ClientVersion.V_1_13) 
                    && isExemptFromSwingingCheck < player.lastTransactionReceived.get()) { 
                if (sentFlying) post.add(event.getPacketType());
            } else if (ENTITY_ACTION.equals(packetType) 
                    && (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) || new WrapperPlayClientEntityAction(event).getAction() != WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA)) {
                if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19_3) && player.inVehicle()) {
                    return;
                }
                if (sentFlying) post.add(event.getPacketType());
            }
        }
    }
}
