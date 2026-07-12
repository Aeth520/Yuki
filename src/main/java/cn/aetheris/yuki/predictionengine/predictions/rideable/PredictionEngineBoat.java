package cn.aetheris.yuki.predictionengine.predictions.rideable;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngine;
import cn.aetheris.yuki.block.collision.CollisionData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.movement.VectorData;
import cn.aetheris.yuki.util.enums.BoatEntityStatus;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.BlockProperties;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.protocol.nms.GetBoundingBox;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PredictionEngineBoat extends PredictionEngine {
    public PredictionEngineBoat(PlayerData player) {
        player.uncertaintyHandler.collidingEntities.add(0); 
        player.vehicleData.midTickY = 0;

        
        player.vehicleData.oldStatus = player.vehicleData.status;
        player.vehicleData.status = getStatus(player);
    }

    private static BoatEntityStatus getStatus(PlayerData player) {
        BoatEntityStatus status = isUnderwater(player);
        if (status != null) {
            player.vehicleData.waterLevel = player.boundingBox.maxY;
            return status;
        } else if (checkInWater(player)) {
            return BoatEntityStatus.IN_WATER;
        } else {
            float friction = getGroundFriction(player);
            if (friction > 0.0F) {
                player.vehicleData.landFriction = friction;
                return BoatEntityStatus.ON_LAND;
            } else {
                return BoatEntityStatus.IN_AIR;
            }
        }
    }

    private static @Nullable BoatEntityStatus isUnderwater(@NotNull PlayerData player) {
        SimpleCollisionBox axisalignedbb = player.boundingBox;
        double d0 = axisalignedbb.maxY + 0.001D;
        int i = MathUtil.floor(axisalignedbb.minX);
        int j = MathUtil.ceil(axisalignedbb.maxX);
        int k = MathUtil.floor(axisalignedbb.maxY);
        int l = MathUtil.ceil(d0);
        int i1 = MathUtil.floor(axisalignedbb.minZ);
        int j1 = MathUtil.ceil(axisalignedbb.maxZ);
        boolean flag = false;

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    double level = player.compensatedWorld.getWaterFluidLevelAt(k1, l1, i2);
                    if (d0 < l1 + level) {
                        if (!player.compensatedWorld.isWaterSourceBlock(k1, l1, i2)) {
                            return BoatEntityStatus.UNDER_FLOWING_WATER;
                        }

                        flag = true;
                    }
                }
            }
        }

        return flag ? BoatEntityStatus.UNDER_WATER : null;
    }

    private static boolean checkInWater(PlayerData PlayerData) {
        SimpleCollisionBox axisalignedbb = PlayerData.boundingBox;
        int i = MathUtil.floor(axisalignedbb.minX);
        int j = MathUtil.ceil(axisalignedbb.maxX);
        int k = MathUtil.floor(axisalignedbb.minY);
        int l = MathUtil.ceil(axisalignedbb.minY + 0.001D);
        int i1 = MathUtil.floor(axisalignedbb.minZ);
        int j1 = MathUtil.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        PlayerData.vehicleData.waterLevel = -Double.MAX_VALUE;

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    double level = PlayerData.compensatedWorld.getWaterFluidLevelAt(k1, l1, i2);
                    if (level > 0) {
                        float f = (float) ((float) l1 + level);
                        PlayerData.vehicleData.waterLevel = Math.max(f, PlayerData.vehicleData.waterLevel);
                        flag |= axisalignedbb.minY < (double) f;
                    }
                }
            }
        }

        return flag;
    }

    public static float getGroundFriction(PlayerData player) {
        SimpleCollisionBox axisalignedbb = player.boundingBox;
        SimpleCollisionBox axisalignedbb1 = new SimpleCollisionBox(axisalignedbb.minX, axisalignedbb.minY - 0.001D, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ, false);
        int i = (int) (Math.floor(axisalignedbb1.minX) - 1);
        int j = (int) (Math.ceil(axisalignedbb1.maxX) + 1);
        int k = (int) (Math.floor(axisalignedbb1.minY) - 1);
        int l = (int) (Math.ceil(axisalignedbb1.maxY) + 1);
        int i1 = (int) (Math.floor(axisalignedbb1.minZ) - 1);
        int j1 = (int) (Math.ceil(axisalignedbb1.maxZ) + 1);

        float f = 0.0F;
        int k1 = 0;

        for (int l1 = i; l1 < j; ++l1) {
            for (int i2 = i1; i2 < j1; ++i2) {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
                if (j2 != 2) {
                    for (int k2 = k; k2 < l; ++k2) {
                        if (j2 <= 0 || k2 != k && k2 != l - 1) {
                            WrappedBlockState blockData = player.compensatedWorld.getBlock(l1, k2, i2);
                            StateType blockMaterial = blockData.getType();

                            if (blockMaterial != StateTypes.LILY_PAD && CollisionData.getData(blockMaterial).getMovementCollisionBox(player, player.getClientVersion(), blockData, l1, k2, i2).isIntersected(axisalignedbb1)) {
                                f += BlockProperties.getMaterialFriction(player, blockMaterial);
                                ++k1;
                            }
                        }
                    }
                }
            }
        }

        return f / (float) k1;
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(PlayerData player, Set<VectorData> possibleVectors, float speed) {
        List<VectorData> vectors = new ArrayList<>();

        for (VectorData data : possibleVectors) {
            
            data.input = new Vector3dm(player.vehicleData.vehicleForward, 0, player.vehicleData.vehicleHorizontal);

            for (int applyStuckSpeed = 1; applyStuckSpeed >= 0; applyStuckSpeed--) {
                if (applyStuckSpeed == 0 && player.isForceStuckSpeed()) break;

                
                
                
                if (player.vehicleData.vehicleForward == 0) {
                    Vector3dm vector = data.vector.clone();
                    controlBoat(player, vector, true);
                    if (applyStuckSpeed != 0) vector.multiply(player.stuckSpeedMultiplier);
                    vectors.add(data.returnNewModified(vector, VectorData.VectorType.InputResult));
                }

                controlBoat(player, data.vector, false);
                if (applyStuckSpeed != 0) data.vector.multiply(player.stuckSpeedMultiplier);
                vectors.add(data);
            }
        }

        return vectors;
    }

    @Override
    public Set<VectorData> fetchPossibleStartTickVectors(PlayerData player) {
        Set<VectorData> vectors = player.getPossibleVelocities();
        addFluidPushingToStartingVectors(player, vectors);

        for (VectorData data : vectors) {
            floatBoat(player, data.vector);
        }

        return vectors;
    }

    @Override
    public void endOfTick(PlayerData player, double d) {
        super.endOfTick(player, d);
        Collisions.handleInsideBlocks(player);
    }

    @Override
    public boolean canSwimHop(PlayerData player) {
        return false;
    }

    private void floatBoat(PlayerData player, Vector3dm vector) {
        double d1 = player.hasGravity ? -0.04f : 0;
        double d2 = 0.0D;
        float invFriction = 0.05F;

        if (player.vehicleData.oldStatus == BoatEntityStatus.IN_AIR && player.vehicleData.status != BoatEntityStatus.IN_AIR && player.vehicleData.status != BoatEntityStatus.ON_LAND) {
            player.vehicleData.waterLevel = player.lastY + player.boundingBox.maxY - player.boundingBox.minY;

            player.lastY = getWaterLevelAbove(player) - 0.5625F + 0.101D;
            player.boundingBox = GetBoundingBox.getCollisionBoxForPlayer(player, player.lastX, player.lastY, player.lastZ);
            player.actualMovement = new Vector3dm(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);
            vector.setY(0);

            player.vehicleData.lastYd = 0.0D;
            player.vehicleData.status = BoatEntityStatus.IN_WATER;
        } else {
            if (player.vehicleData.status == BoatEntityStatus.IN_WATER) {
                d2 = (player.vehicleData.waterLevel - player.lastY) / (player.boundingBox.maxY - player.boundingBox.minY);
                invFriction = 0.9F;
            } else if (player.vehicleData.status == BoatEntityStatus.UNDER_FLOWING_WATER) {
                d1 = -7.0E-4D;
                invFriction = 0.9F;
            } else if (player.vehicleData.status == BoatEntityStatus.UNDER_WATER) {
                d2 = 0.01F;
                invFriction = 0.45F;
            } else if (player.vehicleData.status == BoatEntityStatus.IN_AIR) {
                invFriction = 0.9F;
            } else if (player.vehicleData.status == BoatEntityStatus.ON_LAND) {
                invFriction = player.vehicleData.landFriction;
                player.vehicleData.landFriction /= 2.0F;
            }

            vector.setX(vector.getX() * invFriction);
            vector.setY(vector.getY() + d1);
            vector.setZ(vector.getZ() * invFriction);

            if (d2 > 0.0D) {
                double yVel = vector.getY();
                vector.setY((yVel + d2 * 0.06153846016296973D) * 0.75D);
            }
        }
    }

    public float getWaterLevelAbove(PlayerData player) {
        SimpleCollisionBox axisalignedbb = player.boundingBox;
        int i = (int) Math.floor(axisalignedbb.minX);
        int j = (int) Math.ceil(axisalignedbb.maxX);
        int k = (int) Math.floor(axisalignedbb.maxY);
        int l = (int) Math.ceil(axisalignedbb.maxY - player.vehicleData.lastYd);
        int i1 = (int) Math.floor(axisalignedbb.minZ);
        int j1 = (int) Math.ceil(axisalignedbb.maxZ);

        label39:
        for (int k1 = k; k1 < l; ++k1) {
            float f = 0.0F;

            for (int l1 = i; l1 < j; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    double level = player.compensatedWorld.getWaterFluidLevelAt(l1, k1, i2);

                    f = (float) Math.max(f, level);

                    if (f >= 1.0F) {
                        continue label39;
                    }
                }
            }

            if (f < 1.0F) {
                return (float) k1 + f;
            }
        }

        return (float) (l + 1);
    }

    private void controlBoat(PlayerData player, Vector3dm vector, boolean intermediate) {
        float f = 0.0F;
        if (player.vehicleData.vehicleHorizontal != 0 && (!intermediate && player.vehicleData.vehicleForward == 0)) {
            f += 0.005F;
        }

        
        if (intermediate || player.vehicleData.vehicleForward > 0.1) {
            f += 0.04F;
        }

        if (intermediate || player.vehicleData.vehicleForward < -0.01) {
            f -= 0.005F;
        }

        vector.add(new Vector3dm(player.trigHandler.sin(-player.yaw * ((float) Math.PI / 180F)) * f, 0, (double) (player.trigHandler.cos(player.yaw * ((float) Math.PI / 180F)) * f)));
    }
}
