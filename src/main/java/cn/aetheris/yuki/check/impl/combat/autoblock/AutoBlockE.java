package cn.aetheris.yuki.check.impl.combat.autoblock;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;

@CheckData(name = "AutoBlockE (Boat)",
        configName = "AutoBlockE",
        description = "Attacking or using items while rowing a boat",
        type = CheckType.AUTOBLOCK,
        decay = 0.75,
        experimental = true
)
public final class AutoBlockE extends Check implements PacketCheck {
    public AutoBlockE(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY && isCheckActive()
                && flagAndAlert("(INTERACT)")) {
            event.setCancelled(true);
            player.onPacketCancel();
        }

        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM && isCheckActive()
                && flagAndAlert("(USE)")) {
            event.setCancelled(true);
            player.onPacketCancel();
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT && isCheckActive()
                && flagAndAlert(new WrapperPlayClientPlayerBlockPlacement(event).getFace() == BlockFace.OTHER ? "(PLACE-OTHER)" : "(PLACE))")) {
            event.setCancelled(true);
            player.onPacketCancel();
        }
    }

    public boolean isCheckActive() {
        return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) && !player.vehicleData.wasVehicleSwitch 
                && player.inVehicle() && player.compensatedEntities.getSelf().getRiding().getType().equals(EntityTypes.BOAT)
                && (player.vehicleData.nextVehicleForward != 0 || player.vehicleData.nextVehicleHorizontal != 0);
    }
}