package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.enums.InventoryDesyncStatus;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "InventoryI (HEAD)",
        configName = "InventoryI",
        type = CheckType.INVENTORY,
        decay = 0.44,
        description = "Invalid head movement when player open inventory",
        experimental = true)
public final class InventoryI extends InventoryCheck {

    int ticks;

    public InventoryI(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

            boolean exempt = !isExempt(ExemptType.TELEPORT, ExemptType.VEHICLE, ExemptType.MOVE_LAGGING, ExemptType.LAGGING);

            if (player.hasInventoryOpen && player.inventoryDesyncStatus != InventoryDesyncStatus.NETHER_PORTAL) {
                ticks++;
            } else ticks = 0;
            if (flying.hasRotationChanged() && exempt) {

                float deltaYaw = player.getRotateProcessor().getDeltaYaw();
                float deltaPitch = player.getRotateProcessor().getDeltaPitch();

                boolean headMovement = deltaPitch > 2.5f || deltaYaw > 0.95f; 

                if ((long) ticks > player.getTransactionPing() / 50 + 5 && headMovement) {
                    if (flagAndAlert("dy= " + deltaYaw + "\ndp= " + deltaPitch + "\nt= " + ticks)) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}