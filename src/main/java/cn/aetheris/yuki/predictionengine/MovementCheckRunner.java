package cn.aetheris.yuki.predictionengine;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.impl.movement.groundspoof.GroundSpoofA;
import cn.aetheris.yuki.check.impl.movement.movementvalidation.MovementValidation;
import cn.aetheris.yuki.check.impl.movement.vehicle.nosaddle.NoSaddleB;
import cn.aetheris.yuki.check.type.PositionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.functionality.SetbackTeleportUtil;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.movementtick.*;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineNormal;
import cn.aetheris.yuki.predictionengine.predictions.rideable.PredictionEngineBoat;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.VectorData;
import cn.aetheris.yuki.entity.*;
import cn.aetheris.yuki.util.enums.Pose;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.math.VectorUtils;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.protocol.nms.BoundingBoxSize;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.protocol.nms.GetBoundingBox;
import cn.aetheris.yuki.protocol.nms.Riptide;
import cn.aetheris.yuki.util.update.PositionUpdate;
import cn.aetheris.yuki.util.update.PredictionComplete;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;

public class MovementCheckRunner extends Check implements PositionCheck {
    
    public static double predictionNanos = 0.3 * 1e6;
    
    public static double longPredictionNanos = 0.3 * 1e6;

    public MovementCheckRunner(PlayerData player) {
        super(player);
    }

    public void processAndCheckMovementPacket(PositionUpdate data) {
        
        
        
        
        if (player.getSetbackTeleportUtil().insideUnloadedChunk()) {
            
            final boolean invalidVehicle = player.inVehicle() &&
                    (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_9) ||
                            player.getClientVersion().isOlderThan(ClientVersion.V_1_9));

            if (!invalidVehicle && !data.isTeleport()) {
                
                player.getSetbackTeleportUtil().executeForceResync();
            }
        }

        long start = System.nanoTime();
        check(data);
        long length = System.nanoTime() - start;

        if (!player.bypass) {
            predictionNanos = (predictionNanos * 499 / 500d) + (length / 500d);
            longPredictionNanos = (longPredictionNanos * 19999 / 20000d) + (length / 20000d);
        }
    }

    private void handleTeleport(PositionUpdate update) {
        player.lastX = player.x;
        player.lastY = player.y;
        player.lastZ = player.z;

        
        
        
        
        
        
        
        
        
        if (!player.inVehicle()) {
            if (update.getTeleportData() == null) {
                player.clientVelocity.setX(0);
                player.clientVelocity.setY(0);
                player.clientVelocity.setZ(0);
                player.lastWasClimbing = 0; 
                player.canSwimHop = false; 
            } else {
                update.getTeleportData().modifyVector(player, player.clientVelocity);
            }
        }

        player.uncertaintyHandler.lastTeleportTicks.reset();

        
        player.checkManager.getExplosionHandler().forceExempt();
        player.checkManager.getKnockbackHandler().forceExempt();

        player.boundingBox = GetBoundingBox.getCollisionBoxForPlayer(player, player.x, player.y, player.z);

        
        PredictionComplete predictionComplete = new PredictionComplete(0, update, true);
        player.getSetbackTeleportUtil().onPredictionComplete(predictionComplete);
        player.uncertaintyHandler.lastHorizontalOffset = 0;
        player.uncertaintyHandler.lastVerticalOffset = 0;
    }

    private void check(PositionUpdate update) {
        if (update.isTeleport()) {
            handleTeleport(update);
            return;
        }

        player.movementPackets++;

        player.onGround = update.isOnGround();

        
        
        
        if (!player.isFlying && player.isSneaking && Collisions.isAboveGround(player)) {
            
            
            
            
            double posX = Math.max(0.05, MathUtil.clamp(player.actualMovement.getX(), -16, 16) + 0.05);
            double posZ = Math.max(0.05, MathUtil.clamp(player.actualMovement.getZ(), -16, 16) + 0.05);
            double negX = Math.min(-0.05, MathUtil.clamp(player.actualMovement.getX(), -16, 16) - 0.05);
            double negZ = Math.min(-0.05, MathUtil.clamp(player.actualMovement.getZ(), -16, 16) - 0.05);

            Vector3dm NE = Collisions.maybeBackOffFromEdge(new Vector3dm(posX, 0, negZ), player, true);
            Vector3dm NW = Collisions.maybeBackOffFromEdge(new Vector3dm(negX, 0, negZ), player, true);
            Vector3dm SE = Collisions.maybeBackOffFromEdge(new Vector3dm(posX, 0, posZ), player, true);
            Vector3dm SW = Collisions.maybeBackOffFromEdge(new Vector3dm(negX, 0, posZ), player, true);

            boolean isEast = NE.getX() != posX || SE.getX() != posX;
            boolean isWest = NW.getX() != negX || SW.getX() != negX;
            boolean isNorth = NE.getZ() != negZ || NW.getZ() != negZ;
            boolean isSouth = SE.getZ() != posZ || SW.getZ() != posZ;

            if (isEast) player.uncertaintyHandler.lastStuckEast.reset();
            if (isWest) player.uncertaintyHandler.lastStuckWest.reset();
            if (isNorth) player.uncertaintyHandler.lastStuckNorth.reset();
            if (isSouth) player.uncertaintyHandler.lastStuckSouth.reset();

            if (isEast || isWest || isSouth || isNorth) {
                player.uncertaintyHandler.stuckOnEdge.reset();
            }
        }

        player.compensatedWorld.tickPlayerInPistonPushingArea();
        player.compensatedEntities.tick();

        
        
        
        
        
        
        
        if (player.vehicleData.wasVehicleSwitch || player.vehicleData.lastDummy) {
            player.uncertaintyHandler.lastVehicleSwitch.reset();
        }

        if (player.vehicleData.lastDummy) {
            player.clientVelocity.multiply(0.98); 
        }

        final PacketEntity riding = player.compensatedEntities.self.getRiding();
        if (player.vehicleData.wasVehicleSwitch || player.vehicleData.lastDummy) {
            update.setTeleport(true);

            player.vehicleData.lastDummy = false;
            player.vehicleData.wasVehicleSwitch = false;

            if (riding != null) {
                Vector3dm pos = new Vector3dm(player.x, player.y, player.z);
                SimpleCollisionBox interTruePositions = riding.getPossibleCollisionBoxes();

                
                final float scale = (float) riding.getAttributeValue(Attributes.SCALE);
                float width = BoundingBoxSize.getWidth(player, riding) * scale;
                float height = BoundingBoxSize.getHeight(player, riding) * scale;
                interTruePositions.expand(-width, 0, -width);
                interTruePositions.expandMax(0, -height, 0);

                Vector3dm cutTo = VectorUtils.cutBoxToVector(pos, interTruePositions);

                
                
                
                
                
                
                
                
                player.lastX = cutTo.getX();
                player.lastY = cutTo.getY();
                player.lastZ = cutTo.getZ();

                player.boundingBox = GetBoundingBox.getCollisionBoxForPlayer(player, player.lastX, player.lastY, player.lastZ);
            } else {
                
                
                if (new Vector3dm(player.lastX, player.lastY, player.lastZ).distance(new Vector3dm(player.x, player.y, player.z)) > 3) {
                    player.getSetbackTeleportUtil().executeForceResync(); 
                }

                handleTeleport(update);

                if (player.isClimbing) {
                    Vector3dm ladder = player.clientVelocity.clone().setY(0.2);
                    PredictionEngineNormal.staticVectorEndOfTick(player, ladder);
                    player.lastWasClimbing = ladder.getY();
                }
                return;
            }
        }

        if (player.isInBed != player.lastInBed) {
            update.setTeleport(true);
        }
        player.lastInBed = player.isInBed;

        
        if (player.isInBed) return;

        if (!player.inVehicle()) {
            player.speed = player.compensatedEntities.self.getAttributeValue(Attributes.MOVEMENT_SPEED);
            if (player.hasGravity != player.playerEntityHasGravity) {
                player.pointThreeEstimator.updatePlayerGravity();
            }
            player.hasGravity = player.playerEntityHasGravity;
        }

        
        
        
        
        
        
        
        
        if (player.inVehicle()) {
            
            player.checkManager.getExplosionHandler().forceExempt();

            
            riding.setPositionRaw(player, new SimpleCollisionBox(player.x, player.y, player.z, player.x, player.y, player.z));

            if (riding instanceof PacketEntityTrackYaw boat) {
                boat.packetYaw = player.yaw;
                boat.interpYaw = player.yaw;
                boat.steps = 0;
            }

            if (player.hasGravity != riding.hasGravity) {
                player.pointThreeEstimator.updatePlayerGravity();
            }
            player.hasGravity = riding.hasGravity;

            
            if (riding instanceof PacketEntityRideable) {
                NoSaddleB control = player.checkManager.getCheck(NoSaddleB.class);

                ItemType requiredItem = riding.type == EntityTypes.PIG ? ItemTypes.CARROT_ON_A_STICK : ItemTypes.WARPED_FUNGUS_ON_A_STICK;
                ItemStack mainHand = player.getInventory().getHeldItem();
                ItemStack offHand = player.getInventory().getOffHand();

                boolean correctMainHand = mainHand.getType() == requiredItem;
                boolean correctOffhand = offHand.getType() == requiredItem;

                if (!correctMainHand && !correctOffhand) {
                    
                    control.flagAndAlert();
                } else {
                    control.rewardPlayer();
                }
            }
        }

        if (player.isFlying) {
            player.fallDistance = 0;
            player.uncertaintyHandler.lastFlyingTicks.reset();
        }

        player.isClimbing = Collisions.onClimbable(player, player.lastX, player.lastY, player.lastZ);

        player.clientControlledVerticalCollision = Math.abs(player.y % (1 / 64D)) < 0.00001;

        
        player.actualMovement = new Vector3dm(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);

        final double deltaX = player.x - player.lastX;
        final double deltaY = player.y - player.lastY;
        final double deltaZ = player.z - player.lastZ;

        Vector3d moveVector = update.getFrom().subtract(update.getTo());
        double distanceSquared = moveVector.lengthSquared();

        if (distanceSquared > 400.0) {
            final MovementValidation check = player.getCheckManager().getCheck(MovementValidation.class);
            final GroundSpoofA check2 = player.getCheckManager().getCheck(GroundSpoofA.class);
            if (check != null) {
                check.flagAndAlert("ds= " + Math.sqrt(distanceSquared) + "\ndx= " + deltaX + "\ndy= " + deltaY + "\ndz= " + deltaZ);
                player.getSetbackTeleportUtil().executeViolationSetback();
                player.getSetbackTeleportUtil().executeTeleport(player.getLastLocationData().clone().add(0, 1.0, 0));
                if (check2 != null) check2.flipPlayerGroundStatus = true;
            }
        }

        if (player.isSprinting != player.lastSprinting) {
            player.compensatedEntities.hasSprintingAttributeEnabled = player.isSprinting;
        }

        player.lastJumping = player.isJumping;
        player.isJumping = player.packetStateData.knownInput.jump();

        boolean oldFlying = player.isFlying;
        boolean oldGliding = player.isGliding;
        boolean oldSprinting = player.isSprinting;
        boolean oldSneaking = player.isSneaking;

        
        
        if (player.inVehicle()) {
            
            
            player.isFlying = false;
            player.isGliding = false;
            player.isSprinting &= riding instanceof PacketEntityCamel; 
            player.isSneaking = false;

            if (riding.type != EntityTypes.PIG && riding.type != EntityTypes.STRIDER) {
                player.isClimbing = false;
            }
        }

        
        
        
        
        
        
        if (!player.inVehicle()) {
            player.speed += player.compensatedEntities.hasSprintingAttributeEnabled ? player.speed * 0.3f : 0;
        }

        boolean clientClaimsRiptide = player.packetStateData.tryingToRiptide;
        if (player.packetStateData.tryingToRiptide) {
            long currentTime = System.currentTimeMillis();
            boolean isInWater = player.isInWaterOrRain();

            if (currentTime - player.packetStateData.lastRiptide < 450 || !isInWater) {
                player.packetStateData.tryingToRiptide = false;
            }

            player.packetStateData.lastRiptide = currentTime;
        }

        SimpleCollisionBox steppingOnBB = GetBoundingBox.getCollisionBoxForPlayer(player, player.x, player.y, player.z).copy().expand(player.getMovementThreshold()).offset(0, -1, 0);
        SimpleCollisionBox fixedSteppingOnBB = GetBoundingBox.getCollisionBoxForPlayer(player, player.x, player.y, player.z).copy().expand(player.getMovementThreshold() + 0.15).offset(0, -1, 0);
        Collisions.hasMaterial(player, fixedSteppingOnBB, (pair) -> {
            WrappedBlockState data = pair.first();
            if (data.getType() == StateTypes.SLIME_BLOCK && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) {
                player.uncertaintyHandler.isSteppingOnSlime = true;
                player.uncertaintyHandler.isSteppingOnBouncyBlock = true;
            }
            if (data.getType() == StateTypes.HONEY_BLOCK) {
                if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_14)
                        && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) {
                    player.uncertaintyHandler.isSteppingOnBouncyBlock = true;
                }
                player.uncertaintyHandler.isSteppingOnHoney = true;
            }
            return false;
        });

        Collisions.hasMaterial(player, steppingOnBB, (pair) -> {
            WrappedBlockState data = pair.first();
            if (BlockTags.BEDS.contains(data.getType()) && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_12)) {
                player.uncertaintyHandler.isSteppingOnBouncyBlock = true;
            }
            if (BlockTags.ICE.contains(data.getType())) {
                player.uncertaintyHandler.isSteppingOnIce = true;
            }
            if (data.getType() == StateTypes.BUBBLE_COLUMN && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
                player.uncertaintyHandler.isSteppingNearBubbleColumn = true;
            }
            if (data.getType() == StateTypes.SCAFFOLDING) {
                player.uncertaintyHandler.isSteppingNearScaffolding = true;
            }
            return false;
        });

        player.uncertaintyHandler.thisTickSlimeBlockUncertainty = player.uncertaintyHandler.nextTickSlimeBlockUncertainty;
        player.uncertaintyHandler.nextTickSlimeBlockUncertainty = 0;

        SimpleCollisionBox expandedBB = GetBoundingBox.getBoundingBoxFromPosAndSize(player, player.lastX, player.lastY, player.lastZ, 0.001f, 0.001f);

        
        if (player.actualMovement.lengthSquared() < 2500)
            expandedBB.expandToAbsoluteCoordinates(player.x, player.y, player.z);

        expandedBB.expand(Pose.STANDING.width / 2, 0, Pose.STANDING.width / 2);
        expandedBB.expandMax(0, Pose.STANDING.height, 0);

        
        
        
        boolean isGlitchy = player.uncertaintyHandler.isNearGlitchyBlock;

        player.uncertaintyHandler.isNearGlitchyBlock = player.getClientVersion().isOlderThan(ClientVersion.V_1_9)
                && Collisions.hasMaterial(player, expandedBB.copy().expand(0.2),
                checkData -> BlockTags.ANVIL.contains(checkData.first().getType())
                        || checkData.first().getType() == StateTypes.CHEST || checkData.first().getType() == StateTypes.TRAPPED_CHEST);

        player.uncertaintyHandler.isOrWasNearGlitchyBlock = isGlitchy || player.uncertaintyHandler.isNearGlitchyBlock;
        player.uncertaintyHandler.checkForHardCollision();

        if (player.isFlying != player.wasFlying)
            player.uncertaintyHandler.lastFlyingStatusChange.reset();

        if (!player.inVehicle() && (Math.abs(player.x) == 2.9999999E7D || Math.abs(player.z) == 2.9999999E7D)) {
            player.uncertaintyHandler.lastThirtyMillionHardBorder.reset();
        }

        if (player.isFlying && player.getClientVersion().isOlderThan(ClientVersion.V_1_13) && player.compensatedWorld.containsLiquid(player.boundingBox)) {
            player.uncertaintyHandler.lastUnderwaterFlyingHack.reset();
        }

        boolean couldBeStuckSpeed = Collisions.checkStuckSpeed(player, player.getMovementThreshold());
        boolean couldLeaveStuckSpeed = player.isPointThree() && Collisions.checkStuckSpeed(player, -player.getMovementThreshold());
        player.uncertaintyHandler.claimingLeftStuckSpeed = !player.inVehicle() && player.stuckSpeedMultiplier.getX() < 1 && !couldLeaveStuckSpeed;

        if (couldBeStuckSpeed) {
            player.uncertaintyHandler.lastStuckSpeedMultiplier.reset();
        }

        player.startTickClientVel = player.clientVelocity;

        boolean wasChecked = false;

        
        if (player.compensatedEntities.self.isDead || (riding != null && riding.isDead)) {
            
            player.predictedVelocity = new VectorData(new Vector3dm(), VectorData.VectorType.Dead);
            player.clientVelocity = new Vector3dm();

        } else if (player.bypass || (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_8) && player.gamemode == GameMode.SPECTATOR) || player.isFlying) {
            
            
            
            
            player.predictedVelocity = new VectorData(player.actualMovement, VectorData.VectorType.Spectator);
            player.clientVelocity = player.actualMovement.clone();
            player.gravity = 0;
            player.friction = 0.91f;
            PredictionEngineNormal.staticVectorEndOfTick(player, player.clientVelocity);
        } else if (riding == null) {
            wasChecked = true;

            player.depthStriderLevel = (float) player.compensatedEntities.self.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
            player.sneakingSpeedMultiplier = (float) player.compensatedEntities.self.getAttributeValue(Attributes.SNEAKING_SPEED);

            
            player.verticalCollision = false;

            
            
            if (player.lastOnGround && player.packetStateData.tryingToRiptide && !player.inVehicle()) {
                Vector3dm pushingMovement = Collisions.collide(player, 0, 1.1999999F, 0);
                player.verticalCollision = pushingMovement.getY() != 1.1999999F;
                double currentY = player.clientVelocity.getY();

                if (likelyGroundRiptide(pushingMovement)) {
                    player.uncertaintyHandler.thisTickSlimeBlockUncertainty = Math.abs(Riptide.getRiptideVelocity(player).getY()) + (currentY > 0 ? currentY : 0);
                    player.uncertaintyHandler.nextTickSlimeBlockUncertainty = Math.abs(Riptide.getRiptideVelocity(player).getY()) + (currentY > 0 ? currentY : 0);

                    player.lastOnGround = false;
                    player.lastY += pushingMovement.getY();
                    PlayerBaseTick.updatePlayerPose(player);
                    player.boundingBox = GetBoundingBox.getPlayerBoundingBox(player, player.lastX, player.lastY, player.lastZ);
                    player.actualMovement = new Vector3dm(player.x - player.lastX, player.y - player.lastY, player.z - player.lastZ);

                    player.couldSkipTick = true;

                    Collisions.handleInsideBlocks(player);
                }
            }

            PlayerBaseTick.doBaseTick(player);
            new MovementTickerPlayer(player).livingEntityAIStep();
            PlayerBaseTick.updatePowderSnow(player);
            PlayerBaseTick.updatePlayerPose(player);
        } else if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9) && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            wasChecked = true;
            
            
            
            if (riding.isBoat) {
                PlayerBaseTick.doBaseTick(player);
                
                new PredictionEngineBoat(player).guessBestMovement(0.1f, player);
            } else if (riding instanceof PacketEntityCamel) {
                PlayerBaseTick.doBaseTick(player);
                new MovementTickerCamel(player).livingEntityAIStep();
            } else if (riding instanceof PacketEntityHappyGhast) {
                PlayerBaseTick.doBaseTick(player);
                new MovementTickerHappyGhast(player).livingEntityAIStep();
            } else if (riding instanceof PacketEntityHorse) {
                PlayerBaseTick.doBaseTick(player);
                new MovementTickerHorse(player).livingEntityAIStep();
            } else if (riding.type == EntityTypes.PIG) {
                PlayerBaseTick.doBaseTick(player);
                new MovementTickerPig(player).livingEntityAIStep();
            } else if (riding.type == EntityTypes.STRIDER) {
                PlayerBaseTick.doBaseTick(player);
                new MovementTickerStrider(player).livingEntityAIStep();
                MovementTickerStrider.floatStrider(player);
                Collisions.handleInsideBlocks(player);
            } else if (player.wasTouchingWater) {
                PlayerBaseTick.doBaseTick(player);
                new MovementTickerNautilus(player).livingEntityAIStep();
            } else {
                wasChecked = false;
            }
        } 

        
        double offset = player.predictedVelocity.vector.distance(player.actualMovement);
        offset = player.uncertaintyHandler.reduceOffset(offset);

        if (player.packetStateData.tryingToRiptide != clientClaimsRiptide && !player.isSwimming && !player.isGliding && PluginLoader.INSTANCE.getConfigManager().isMitigateTridentRiptiding()) {
            player.getSetbackTeleportUtil().executeForceResync(); 
            LogUtils.sync("&b" + player.getName() + "&7 ForceResync for use riptide into invalid ground");
        }


        
        
        
        
        
        if (player.getSetbackTeleportUtil().getRequiredSetBack() != null && player.getSetbackTeleportUtil().getRequiredSetBack().getTicksComplete() == 1) {
            Vector3dm setbackVel = player.getSetbackTeleportUtil().getRequiredSetBack().getVelocity();
            
            
            
            if (player.predictedVelocity.isJump()
                    && !player.wasTouchingLava && !player.wasTouchingWater
                    && ((setbackVel != null && setbackVel.getY() >= 0) || !Collisions.slowCouldPointThreeHitGround(player, player.lastX, player.lastY, player.lastZ))) {
                if (PluginLoader.INSTANCE.getConfigManager().isMitigateVelocityJump()) {
                    player.getSetbackTeleportUtil().executeForceResync();
                    LogUtils.sync("&b" + player.getName() + "&7 ForceResync for invalid jump velocity motion");
                }
            }
            if (PluginLoader.INSTANCE.getConfigManager().isMitigateVelocityInvalid()) {
                boolean exempt = player.getExemptProcessor().isExempt(ExemptType.WEAPON_SHOOT);
                boolean lavaBugFix = player.wasTouchingLava && player.predictedVelocity.isJump() &&
                        player.predictedVelocity.vector.getY() < 0.06 && player.predictedVelocity.vector.getY() > -0.02;
                
                if (!player.predictedVelocity.isKnockback() && !exempt && !lavaBugFix && player.getSetbackTeleportUtil().getRequiredSetBack().getVelocity() != null) {
                    
                    player.getSetbackTeleportUtil().executeForceResync();
                    LogUtils.sync("&b" + player.getName() + "&7 ForceResync for invalid velocity motion");
                }
            }
        }
        
        if (player.getSetbackTeleportUtil().blockOffsets) offset = 0;

        if (player.skippedTickInActualMovement || !wasChecked)
            player.uncertaintyHandler.lastPointThree.reset();

        
        player.checkManager.onPredictionFinish(new PredictionComplete(offset, update, wasChecked));

        player.wasLastPredictionCompleteChecked = wasChecked;

        
        if (player.bukkitPlayer != null && player.isGliding && player.predictedVelocity.isJump() && player.isSprinting
                && PluginLoader.INSTANCE.getConfigManager().isMitigateElytraSprint()) {
            SetbackTeleportUtil.SetbackPosWithVector lastKnownGoodPosition = player.getSetbackTeleportUtil().lastKnownGoodPosition;
            lastKnownGoodPosition.setVector(lastKnownGoodPosition.getVector().multiply(new Vector3dm(0.6 * 0.91, 1, 0.6 * 0.91)));
            player.getSetbackTeleportUtil().executeNonSimulatingSetback();
            LogUtils.sync("&b" + player.getName() + "&7 ForceResync for invalid eltra motion");
        }

        if (!wasChecked) {
            
            player.checkManager.getExplosionHandler().forceExempt();
            player.checkManager.getKnockbackHandler().forceExempt();
        }

        player.lastOnGround = player.onGround;
        player.lastSprinting = player.isSprinting;
        player.lastSprintingForSpeed = player.isSprinting;
        player.wasFlying = player.isFlying;
        player.wasGliding = player.isGliding;
        player.wasSwimming = player.isSwimming;
        player.wasSneaking = player.isSneaking;
        player.packetStateData.tryingToRiptide = false;

        
        if (player.inVehicle()) {
            player.isFlying = oldFlying;
            player.isGliding = oldGliding;
            player.isSprinting = oldSprinting;
            player.isSneaking = oldSneaking;
        }

        player.riptideSpinAttackTicks--;
        if (player.predictedVelocity.isTrident())
            player.riptideSpinAttackTicks = 20;

        player.uncertaintyHandler.lastMovementWasZeroPointZeroThree = !player.inVehicle() && player.skippedTickInActualMovement;
        player.uncertaintyHandler.lastMovementWasUnknown003VectorReset = !player.inVehicle() && player.couldSkipTick && player.predictedVelocity.isKnockback();
        player.couldSkipTick = false;

        
        
        
        
        player.uncertaintyHandler.wasZeroPointThreeVertically = !player.inVehicle() &&
                ((player.uncertaintyHandler.lastMovementWasZeroPointZeroThree && player.pointThreeEstimator.controlsVerticalMovement())
                        || !player.pointThreeEstimator.canPredictNextVerticalMovement() || !player.pointThreeEstimator.isWasAlwaysCertain());

        player.uncertaintyHandler.lastPacketWasGroundPacket = player.uncertaintyHandler.onGroundUncertain;
        player.uncertaintyHandler.onGroundUncertain = false;

        player.vehicleData.vehicleForward = (float) Math.min(0.98, Math.max(-0.98, player.vehicleData.nextVehicleForward));
        player.vehicleData.vehicleHorizontal = (float) Math.min(0.98, Math.max(-0.98, player.vehicleData.nextVehicleHorizontal));
        if (player.onGround) { 
            player.vehicleData.horseJump = player.vehicleData.nextHorseJump;
            player.vehicleData.nextHorseJump = 0;
        }

        player.vehicleData.camelDashCooldown = Math.max(0, player.vehicleData.camelDashCooldown - 1);

        player.minAttackSlow = 0;
        player.maxAttackSlow = 0;

        player.likelyKB = null;
        player.firstBreadKB = null;
        player.firstBreadExplosion = null;
        player.likelyExplosions = null;

        player.trigHandler.setOffset(offset);
        player.pointThreeEstimator.endOfTickTick();
    }

    
    private boolean likelyGroundRiptide(Vector3dm pushingMovement) {
        
        double riptideYResult = Riptide.getRiptideVelocity(player).getY();

        double riptideDiffToBase = Math.abs(player.actualMovement.getY() - riptideYResult);
        double riptideDiffToGround = Math.abs(player.actualMovement.getY() - riptideYResult - pushingMovement.getY());

        
        
        return riptideDiffToGround < riptideDiffToBase;
    }
}
