package cn.aetheris.yuki.check.impl.movement.noslow;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.player.inventory.InventoryG;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.type.PositionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.util.materials.Materials;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.PositionUpdate;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "NoSlowG (Web)", configName = "NoSlowG", type = CheckType.NOSLOW, experimental = true)
public class NoSlowG extends Check implements PositionCheck, PacketCheck {

    public static final BlockFace[] relatives = new BlockFace[]{
            BlockFace.EAST,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.NORTH_EAST,
            BlockFace.NORTH_WEST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_WEST
    };
    private static final Material webMaterial;

    static {
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
            webMaterial = Material.COBWEB;
        } else {
            webMaterial = Material.valueOf("WEB");
        }
    }

    private double preMotionY = 0.0D;

    public NoSlowG(@NotNull PlayerData player) {
        super(player);
    }


    @Override
    public void onPositionUpdate(PositionUpdate update) {
        if (update.isTeleport()) {
            return;
        }

        final Vector3d to = update.getTo();
        final Vector3d from = update.getFrom();

        if (isExempt(ExemptType.ELYTRA_FLYING, ExemptType.RESPAWN, ExemptType.INVALID_GAMEMODE, ExemptType.FLYING, ExemptType.CLIENT_ANTICHEAT, ExemptType.TELEPORT, ExemptType.GSIT_ACTION, ExemptType.BREWERRY_PUSH, ExemptType.BED)) {
            return;
        }

        if (player.getBukkitPlayer() == null) {
            return;
        }

        final double walkSpeed = player.getBukkitPlayer().getWalkSpeed();

        if (Math.abs(walkSpeed - 0.2F) > 0.02F) {
            return;
        }
        Location bukkitLocation = new Location(player.getBukkitPlayer().getWorld(), update.getFrom().getX(), update.getFrom().getY(), update.getFrom().getZ());
        Block a = bukkitLocation.getBlock();
        Block b = bukkitLocation.getBlock().getRelative(BlockFace.UP);
        MHDFScheduler.getRegionScheduler().runTask(Yuki.getInstance(), bukkitLocation, () -> {
            if (a.getType() == Material.AIR && a.getRelative(BlockFace.DOWN).getType() == webMaterial) {
                boolean ground = player.isOnGround();
                Block down = bukkitLocation.getBlock().getRelative(BlockFace.DOWN);
                if (ground) {
                    boolean pass = false;
                    for (BlockFace face : relatives) {
                        Block downrel = down.getRelative(face);
                        if (downrel.getType() != Material.AIR && downrel.getType() != webMaterial) {
                            pass = true;
                        }
                    }
                    if (update.getTo().getY() < update.getFrom().getY()) {
                        pass = true;
                    }
                    if (update.getFrom().getY() % 1D != 0D) {
                        pass = true;
                    }
                    if (!pass) {
                        flagAndAlert("(Spoof)\nval= " + (update.getTo().getY() - update.getFrom().getY()));
                    }
                }
            }

            final WrappedBlockState toBlock = player.getCompensatedWorld().getBlock(to.toVector3i());

            
            if (a.getType() == webMaterial || b.getType() == webMaterial) {
                
                double motY = to.getY() - from.getY();
                if (motY != 0) {
                    double maxSpeed = player.getLikelyKB() != null ? 0.25 : 0.1;
                    final SimpleCollisionBox aabb = player.getBoundingBox().copy().expand(1.5, 0, 1.5);
                    final boolean nearSoulBlock = player.getCompensatedWorld().getBlock(aabb.getMinX(), aabb.getMinY(), aabb.getMinZ()).getType() == StateTypes.SOUL_SAND;
                    if (Math.abs(motY) > maxSpeed && Math.abs(motY) >= Math.abs(preMotionY) && !Materials.isStairs(toBlock.getType()) && !nearSoulBlock) {
                        if (flagAndAlert("(VertB)\nd= " + Math.round(motY * 1000) / 1000F)) {
                            if (Math.random() < 0.5 || motY > 0.5) {
                                player.getSetbackTeleportUtil().executeTeleport(player.getLocationData().clone().add(0, 0.5, 0));
                                player.onPacketCancel();
                                player.mitigateDamage();
                            }
                        }
                    }
                }
                
                double distance = MathUtil.getHorizontalDistance(from, to);
                double limit = 0.121;
                boolean ground = player.isOnGround();
                if (player.velocitySinceTick <= 10) {
                    limit += 0.9;
                }
                if (player.getCompensatedEntities().getSelf().getPotionEffectLevel(PotionTypes.SPEED).isPresent()) {
                    limit += player.getCompensatedEntities().getSelf().getPotionEffectLevel(PotionTypes.SPEED).getAsInt() * 0.005;
                }
                
                final InventoryG check = player.getCheckManager().getCheck(InventoryG.class);
                if (check != null && !check.getVelocityData().isEmpty()) {
                    if (check.getHorizontalVelocity() != 0) limit += check.getHorizontalVelocity();
                    if (check.getVerticalVelocity() != 0) limit += check.getHorizontalVelocity();
                }
                if (player.getPredictedVelocity().isExplosion()) {
                    limit += 3.5;
                }

                if (player.isSneaking() && ground) {
                    limit -= 0.06;
                }
                if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                    if (player.isGliding()) {
                        limit += 0.2;
                    }
                }
                float speed = (float) player.getCompensatedEntities().getSelf().getAttributeValue(Attributes.MOVEMENT_SPEED);
                if (!player.isSprinting()) {
                    speed *= 1.3f;
                }
                if (speed > 0.14) {
                    limit += 0.3;
                }
                if (toBlock.getType() == StateTypes.COBWEB) {
                    if (distance > limit) {
                        flagAndAlert("(HorA)\nd= " + Math.round(distance * 1000) / 1000F);
                    }
                    if (!ground && to.getY() < from.getY()) {
                        limit = 0.075;
                        if (player.velocitySinceTick <= 10) {
                            limit += 0.9;
                        }
                        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                            limit += 0.01; 
                        }
                        if (distance > limit) {
                            flagAndAlert("(HorB)\nd= " + Math.round(distance * 1000) / 1000F);

                        }
                    }
                }
            }
            this.preMotionY = to.getY() - from.getY();
        });
    }

}

