package cn.aetheris.yuki.check.impl.movement.noslow;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "NoSlowA (Predication)", configName = "NoSlowA", type = CheckType.NOSLOW, description = "Invalid Blocking", setback = 15)
public final class NoSlowA extends Check implements PostPredictionCheck {

    
    
    public boolean didSlotChangeLastTick = false;
    public boolean flaggedLastTick = false;
    double offsetToFlag;
    double bestOffset = 1;
    private long lastFlag;

    private boolean switchSlotThisTick = false;

    public NoSlowA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            switchSlotThisTick = true;
        }
        if (isFlying(event.getPacketType())) {
            switchSlotThisTick = false;
        }
    }

    @Override
    public void onPredictionComplete(final PredictionComplete complete) {
        if (!complete.isChecked()) return;

        if (isExempt(ExemptType.JOIN,
                ExemptType.LIQUID,
                ExemptType.SWIMMING,
                ExemptType.WAS_SWIMMING,
                ExemptType.SLIGHTLY_TOUCHING_LIQUID,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.FLYING,
                ExemptType.ELYTRA_FLYING)) {
            return;
        }

        
        if (player.packetStateData.isSlowedByUsingItem()) {
            
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) && didSlotChangeLastTick) {
                didSlotChangeLastTick = false;
                flaggedLastTick = false;
            }

            final ItemStack useHand = player.getInventory().inventory.getSlot(player.packetStateData.getSlowedByUsingItemSlot()).getItem();
            final ItemStack mainHand = player.getInventory().getItemInHand(InteractionHand.MAIN_HAND);
            final ItemStack offHand = player.getInventory().getItemInHand(InteractionHand.OFF_HAND);


            
            
            
            if (switchSlotThisTick
                    && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
                    && player.isLastSprinting()
                    && player.isSprinting()
                    && player.isLastOnGround()
                    && !player.isOnGround()
                    && !player.isServerOnGround()) {
                return;
            }

            if (bestOffset > offsetToFlag) {
                if (flaggedLastTick) {
                    if (time() - lastFlag < 300L) {
                        return;
                    }
                    if (buffer++ > 1) {

                        if (flagAndAlertWithSetback(getVerbose(useHand, mainHand, offHand))) {
                            lastFlag = time();
                            buffer = 0;
                            shuffleAboveSetbackVL();
                            if (buffer >= 3) resetPlayerUseItem(player.bukkitPlayer);
                        }
                    }
                }
                flaggedLastTick = true;
            } else {
                rewardBufferAndVL();
                flaggedLastTick = false;
            }
        }
        bestOffset = 1;
    }

    private @NotNull String getVerbose(ItemStack useHand, ItemStack mainHand, ItemStack offHand) {
        String alert;
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_8)) {
            alert = "o= " + bestOffset
                    + "\ng= " + player.isOnGround() + " | " + player.isServerOnGround()
                    + "\nsc= " + player.getLastServerChangeSlot()
                    + "\nm= " + isAboveSetbackVl()
                    + "\nb= " + buffer
                    + "\nuse= " + useHand.getType().getName()
                    + "\nmain= " + mainHand.getType().getName()
                    + "\noff= " + offHand.getType().getName();
        } else {
            alert = "o= " + bestOffset
                    + "\ng= " + player.isOnGround() + " | " + player.isServerOnGround()
                    + "\nsc= " + player.getLastServerChangeSlot()
                    + "\nm= " + isAboveSetbackVl()
                    + "\nb= " + buffer
                    + "\nuse= " + useHand.getType().getName();
        }
        return alert;
    }


    public void handlePredictionAnalysis(double offset) {
        bestOffset = Math.min(bestOffset, offset);
    }

    @Override
    public void reload() {
        super.reload();
        offsetToFlag = getConfig().getDoubleElse(getConfigName() + ".threshold", 0.025);
    }
}