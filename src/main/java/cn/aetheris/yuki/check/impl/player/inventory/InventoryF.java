package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(name = "InventoryF (ACTION)",
        configName = "InventoryF",
        type = CheckType.INVENTORY,
        decay = 0.55,
        setback = 7,
        experimental = true)
public final class InventoryF extends InventoryCheck {

    boolean exempt = false;
    long lastFlag;
    int sprintTick;
    int sneakTick;

    public InventoryF(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {
            if (isExempt(ExemptType.TELEPORT, ExemptType.FLYING, ExemptType.LIQUID, ExemptType.SWIMMING))
                return;

            if (player.uncertaintyHandler.isSteppingNearScaffolding || player.packetStateData.lastPacketWasOnePointSeventeenDuplicate)
                return;

            if (exempt) return;

            boolean isSprint = player.isSprinting;
            boolean isSneak = player.isSneaking;

            sprintTick = isSprint ? ++sprintTick : 0;
            sneakTick = isSneak ? ++sneakTick : 0;

            boolean invalidTick = sneakTick > 3 || sprintTick > 3;
            boolean moving = player.moving;
            boolean invalid = invalidTick && player.getClientVersion().isOlderThan(ClientVersion.V_1_16);
            boolean V1_13Invalid = invalidTick && moving && player.getClientVersion().isNewerThan(ClientVersion.V_1_16); 

            if (player.hasInventoryOpen) {
                if (V1_13Invalid || invalid) {

                    if (time() - lastFlag < 600L) {
                        return;
                    }
                    if (buffer++ > 5) {
                        if (flagAndAlertWithSetback("b= " + buffer + "\ns= " + sneakTick + "\np= " + sprintTick + "\nm= " + moving)) {
                            closeInventory();
                            event.setCancelled(true);
                            player.onPacketCancel();
                            buffer = 0.0;
                        }
                        lastFlag = time();
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);
            WrapperPlayClientEntityAction.Action action = wrapper.getAction();

            if (action == WrapperPlayClientEntityAction.Action.STOP_SNEAKING
                    || action == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                exempt = true;
            }
        } else if (isTransaction(event.getPacketType())) {
            exempt = false;
        }
    }
}