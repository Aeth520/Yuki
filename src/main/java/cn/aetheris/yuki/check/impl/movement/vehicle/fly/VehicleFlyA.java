package cn.aetheris.yuki.check.impl.movement.vehicle.fly;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;

@CheckData(name = "VehicleFlyA", configName = "VehicleFlyA", type = CheckType.VEHICLE, description = "Invalid Vehicle Motion", decay = 0.25, setback = 12)
public final class VehicleFlyA extends Check implements PacketCheck {

    WrapperPlayClientVehicleMove vehicleMove;
    private boolean lastGround;
    private int ticks;
    private double lastY;
    private boolean lastGravity;

    public VehicleFlyA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE) {
            vehicleMove = new WrapperPlayClientVehicleMove(event);

            if (player.packetStateData.lastPacketWasTeleport) return;

            double deltaY = vehicleMove.getPosition().getY() - lastY;

            if (player.bukkitPlayer == null) {
                return;
            }

            if (player.inVehicle() && player.bukkitPlayer.getVehicle() != null) {

                final String entityName = player.compensatedEntities.getSelf().getRiding().getType().getName().toString();

                final boolean gravity = player.compensatedEntities.getSelf().getRiding().hasGravity;
                final boolean ground = player.bukkitPlayer.getVehicle().isOnGround();
                final boolean isBoat = player.compensatedEntities.getSelf().getRiding().isBoat();

                if (gravity && lastGravity) {
                    if (++ticks > 3) {

                        boolean flagged = false;

                        if (deltaY > 2.5 && isBoat && !ground && !lastGround) {
                            flagged = flagAndAlert("Moving upwards \ndy= " + deltaY + "\ntype= " + entityName);
                        } else if (deltaY > 4 && isBoat && player.getY() > player.getLastY()) {
                            flagged = flagAndAlert("Moving upwards (2) \ndy= " + deltaY + "\ntype= " + entityName);
                        } else if (deltaY > 7.5 && player.getY() > player.getLastY()) {
                            flagged = flagAndAlert("Moving upwards (3) \ndy= " + deltaY + "\ntype= " + entityName);
                        }

                        if (flagged) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                            setbackIfAboveSetbackVL();
                            setLast(ground);
                            player.bukkitPlayer.leaveVehicle();
                            return;
                        }
                    }
                }
                lastGravity = gravity;
                setLast(ground);
            }

            if (player.getRidingVehicleId() == -1) {
                ticks = 0;
            }
        }
    }


    private void setLast(boolean ground) {
        lastGround = ground;
        lastY = vehicleMove.getPosition().getY();
    }
}
