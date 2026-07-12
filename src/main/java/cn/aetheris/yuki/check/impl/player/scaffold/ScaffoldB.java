package cn.aetheris.yuki.check.impl.player.scaffold;

import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.time.Watch;
import cn.aetheris.yuki.util.update.BlockPlace;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "ScaffoldB (Analysis)",
        configName = "ScaffoldB",
        decay = 0.86,
        description = "Scaffold analysis",
        experimental = true)
public final class ScaffoldB extends BlockPlaceCheck {
    private final Watch checkScaffold = new Watch();
    private final Watch click = new Watch();
    private final Watch place = new Watch();
    private final Watch lastAnalysisTime = new Watch();
    private final List<Long> sides = new ArrayList<>();
    private final List<Long> surfaces = new ArrayList<>();
    private final List<Long> failed = new ArrayList<>();
    private final List<Integer> sneakTimer = new ArrayList<>();
    private final List<Integer> placeTimer = new ArrayList<>();
    private final List<Vector3dm> hitLocations = new ArrayList<>();
    private boolean checkingScaffold = false;
    private double buffer1 = 0;
    private double checkBuffer1 = 0;
    private double checkBuffer2 = 0;
    private double checkBuffer3 = 0;
    private double PREVdIFF = 0;
    private double placeSpeed = 0;
    private int placeCounter = 0;
    private int tickCounter = 0;
    private int lastJump = 0;
    private int lastSneak = 0;
    private int sneakTiming = 0;
    private double godbridgeInARow = 0;

    private double diffTooLowInARow = 0;
    private int sneakTick = 0;
    private int placeTick = 0;
    private int dragClick = 0;
    private final int dragClickBuffer = 0;
    private int godBridge = 0;
    private double buffer = 0;
    private double buffer2 = 0;
    private double prevScore = 0;
    private int lastPlaceY = -1;
    private double tooShortSneak = 0;
    private boolean ua = false;

    public ScaffoldB(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        
        if (!place.isBlock()) {
            return;
        }
        Vector3dm blockPos;
        if (place.getHitData() != null) {
            blockPos = player.getLocationData().toVector().subtract(place.getHitData().getBlockHitLocation());
        } else {
            return;
        }
        boolean yaw = player.rotateProcessor.getDeltaYaw() >= 0 && player.rotateProcessor.getDeltaYaw() < 4;
        boolean pitch = player.rotateProcessor.getDeltaPitch() > 0.2 && player.rotateProcessor.getDeltaPitch() < 4;
        boolean jittered = yaw && pitch;
        float pYaw = MathUtil.wrapAngleTo180_float(player.rotateProcessor.yaw);
        float offsetToNear45Deg = Math.abs(pYaw - ((float) (int) pYaw / 45) * 45f);
        if(offsetToNear45Deg < 1){

        }
        hitLocations.add(blockPos);
        if (hitLocations.size() > 3) {
            
            double XMean = 0;
            double YMean = 0;
            double ZMean = 0;
            for (Vector3dm loc : hitLocations) {
                XMean += loc.getX();
                YMean += loc.getY();
                ZMean += loc.getZ();
            }
            XMean /= hitLocations.size();
            YMean /= hitLocations.size();
            ZMean /= hitLocations.size();

            double XStd = 0;
            double YStd = 0;
            double ZStd = 0;
            for (Vector3dm loc : hitLocations) {
                XStd += MathUtil.square(loc.getBlockX() - XMean);
                YStd += MathUtil.square(loc.getBlockY() - YMean);
                ZStd += MathUtil.square(loc.getBlockZ() - ZMean);
            }
            XStd /= hitLocations.size();
            YStd /= hitLocations.size();
            ZStd /= hitLocations.size();
            double combinedStd = MathUtil.square(
                    XStd * XStd +
                            YStd * YStd +
                            ZStd * ZStd
            );
            double diff = Math.abs(prevScore - combinedStd);
            double diff2 = Math.abs(PREVdIFF - diff);

            if (!lastAnalysisTime.hasTimeElapsed(6000)) {
                if ((diff < 0.001 || diff2 < 0.01) && diff > 0.00001 && combinedStd > 0.00001) {



                }
            }

            prevScore = combinedStd;
            PREVdIFF = diff;
            lastAnalysisTime.reset();
            hitLocations.clear();
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {
            diffTooLowInARow = Math.max(diffTooLowInARow - 0.2 / 20.0, 0);
            tickCounter++;
            if (tickCounter > 30) {
                placeSpeed = (double) placeCounter / tickCounter;
                place.reset();
                tickCounter = 0;
                placeCounter = 0;
            }
            lastJump++;
            lastSneak++;
            sneakTick++;
            placeTick++;
            final SimpleCollisionBox box = player.getBoundingBox().copy();
            
            final int blockX = NumberConversions.floor((box.maxX + box.minX) / 2);
            final int blockY = NumberConversions.floor(box.minY - 0.5);
            final int blockZ = NumberConversions.floor((box.maxZ + box.minZ) / 2);
            boolean underAir = player.getCompensatedWorld().getBlock(
                    new Vector3dm(
                            blockX,
                            blockY,
                            blockZ
                    )
            ).getType().equals(StateTypes.AIR);
            if (player.isSneaking()) {
                lastSneak = 0;
                if (sneakTiming == 0) {
                    ua = underAir;
                }
                sneakTiming++;
            } else {
                if (sneakTiming < 5 && !underAir && ua) {
                    tooShortSneak++;
                    if (tooShortSneak > 3) tooShortSneak = 0;
                }
                sneakTiming = 0;
            }
            if (Math.abs(player.getDeltaY() - 0.42) < 0.05) {
                lastJump = 0;
            }

            checkingScaffold = !checkScaffold.hasTimeElapsed(2000);

            if (!checkingScaffold) {
                
                sides.clear();
                surfaces.clear();
            }

            tooShortSneak = Math.max(0, tooShortSneak - 0.13);

            checkBuffer1 = Math.max(0, buffer1 - 0.08);
            checkBuffer2 = Math.max(0, checkBuffer2 - 0.17);
            checkBuffer3 = Math.max(0, checkBuffer2 - 0.17);

            
            buffer1 = Math.max(0, buffer1 - 0.2);
            if (player.getRotateProcessor().getDeltaYaw() > 20) {
                buffer1 = Math.min(buffer1 + 1, 5);
            }

            long maxTime = 2000;
            sides.removeIf(time -> time() - time > maxTime);
            surfaces.removeIf(time -> time() - time > maxTime);
            failed.removeIf(time -> time() - time > maxTime);

            if (sides.size() > 300 || surfaces.size() > 300 || failed.size() > 300) {
                if (flagAndAlert("(Fast)\ns= " + sides.size() + "\nsf= " + surfaces.size() + "\nf= " + failed.size() + "\nf= " + failed.size())) {
                    event.setCancelled(true);
                }
            }

            while (sides.size() > 300) {
                sides.remove(0);
            }
            while (surfaces.size() > 300) {
                surfaces.remove(0);
            }
            while (failed.size() > 300) {
                surfaces.remove(0);
            }
        } else if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event);

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                sneakTick = 0;
            } else if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
                sneakTimer.add(sneakTick);
                while (sneakTimer.size() > 10) {
                    sneakTimer.remove(0);
                }
                if (sneakTimer.size() == 10 && checkingScaffold) {
                    double stdDev = MathUtil.getStandardDeviation(sneakTimer);
                    if (stdDev < 0.2) {
                        if (buffer++ > 3) {
                            if (flagAndAlert("(Change)\nstd= " + stdDev)) {
                                player.mitigateDamage();
                            }
                        }
                    } else {
                        buffer -= 0.2;
                        if (buffer1 < 0) {
                            buffer = 0;
                        }
                    }
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement placement = new WrapperPlayClientPlayerBlockPlacement(event);

            ItemStack placedWith = player.getInventory().getHeldItem();
            if (placement.getHand() == InteractionHand.OFF_HAND) {
                placedWith = player.getInventory().getOffHand();
            }

            if(placedWith.getType().getPlacedType() == null) return;

            placeCounter++;
            checkScaffold.reset();

            boolean cancelPlace = false;

            
            BlockFace face = placement.getFace();
            switch (face) {
                case WEST, EAST, SOUTH, NORTH -> {
                    placeTimer.add(placeTick);
                    placeTick = 0;
                    sides.add(time());
                }
                case UP, DOWN -> {
                    placeTimer.add(placeTick);
                    placeTick = 0;
                    surfaces.add(time());
                }

                case OTHER -> failed.add(time());
            }
            if (placeTimer.size() > 15) {
                placeTimer.remove(0);
                double score = MathUtil.getStandardDeviation(placeTimer) * MathUtil.getAverage(placeTimer);
                if (score < 1.2 && score > 0.7) {




                } else {
                    buffer2 -= 0.4;
                    if (buffer2 < 0) buffer2 = 0;
                }
            }
            int c = surfaces.size() + failed.size();
            double possibility = sides.isEmpty() ? 0 : Math.min((1 - ((double) c / sides.size())), 1);

            if (buffer1 > 3 && !sides.isEmpty() && possibility > 0.5) {
                if (checkBuffer1++ > 3) {
                    if (flagAndAlert("(Auto#1)p= " + String.format("%.2f%%", possibility * 100))) {
                        cancelPlace = true;
                    }
                }
            } else {
                checkBuffer1 = Math.max(0, buffer1 - 0.1);
            }

            if (!sides.isEmpty() && possibility > 0.5 && lastJump > 5) {
                if (checkBuffer2++ > 7) {
                    if (flagAndAlert("(Auto#2)\np= " + String.format("%.2f%%", possibility * 100))) {
                        cancelPlace = true;
                    }
                }
            }

            if (sides.size() > 3 && lastSneak > 5 && surfaces.isEmpty()) {
                if (checkBuffer3++ > 5) {
                    if (flagAndAlert("(Sneak)\nls= " + lastSneak + "\nsd= " + sides.size())) {
                        cancelPlace = true;
                    }
                }
            }

            if (sides.size() > 4 && lastSneak > 2 && surfaces.isEmpty() && tooShortSneak > 2.6) {
                if (checkBuffer3++ > 5) {
                    if (flagAndAlert("(Sneak#2)\nls= " + lastSneak + "\nsd= " + sides.size() + "\ntss= " + (int) tooShortSneak)) {
                        cancelPlace = true;
                    }
                }
            }
            if (face != BlockFace.OTHER && face != BlockFace.UP && face != BlockFace.DOWN) {
                if (placement.getBlockPosition().getY() == lastPlaceY) {
                    godBridge++;
                } else {
                    godBridge = 0;
                }
                lastPlaceY = placement.getBlockPosition().getY();
            }
            if (click.hasTimeElapsed(200)) {
                dragClick = 0;
            } else {
                dragClick++;
            }

            if (dragClick > 30 && godBridge > 3 && placeSpeed > 0.6) {
                if (godbridgeInARow ++ > 3 && flagAndAlert("(GodBridge)\ndc= " + dragClick)) {
                    cancelPlace = true;
                }
            } else {
                godbridgeInARow = Math.max(godbridgeInARow - (placeSpeed <= 0.6 ? 1 : 0.6), 0);
            }
            click.reset();

            if (cancelPlace) {
                player.mitigateDamage();
                event.setCancelled(true);
            }
        }
    }

}