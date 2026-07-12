package cn.aetheris.yuki.player;


import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.impl.player.badpackets.packetorder.BadPacketsO;
import cn.aetheris.yuki.check.impl.player.badpackets.packetorder.processor.PacketActionProcessor;
import cn.aetheris.yuki.check.util.exempts.ExemptProcessor;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.check.util.handler.PayloadHandler;
import cn.aetheris.yuki.check.util.processor.clickprocessor.ClickProcessor;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.listener.bukkit.PlayerAttackListener;
import cn.aetheris.yuki.listener.packets.CheckManagerListener;
import cn.aetheris.yuki.functionality.CheckManager;
import cn.aetheris.yuki.functionality.LastInstanceManager;
import cn.aetheris.yuki.functionality.PunishmentManager;
import cn.aetheris.yuki.functionality.SetbackTeleportUtil;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.predictionengine.MovementCheckRunner;
import cn.aetheris.yuki.predictionengine.PointThreeEstimator;
import cn.aetheris.yuki.predictionengine.UncertaintyHandler;
import cn.aetheris.yuki.util.change.PlayerBlockHistory;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.*;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.entity.PacketEntityHappyGhast;
import cn.aetheris.yuki.entity.PacketEntitySelf;
import cn.aetheris.yuki.entity.tag.SyncedTags;
import cn.aetheris.yuki.util.enums.FluidTag;
import cn.aetheris.yuki.util.enums.InventoryDesyncStatus;
import cn.aetheris.yuki.util.enums.Pose;
import cn.aetheris.yuki.util.latency.*;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.util.location.PacketLocation;
import cn.aetheris.yuki.util.materials.Materials;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.math.TrigHandler;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.protocol.nms.BlockProperties;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.protocol.nms.GetBoundingBox;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import cn.aetheris.yuki.util.message.ColorUtils;
import cn.aetheris.yuki.util.time.TimeUtils;
import cn.aetheris.yuki.util.update.BlockBreak;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEquippable;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.dimension.DimensionType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlayerData implements PlayerAPI {
    public final UUID uuid;
    public final User user;
    
    
    
    public final Queue<Pair<Short, Long>> transactionsSent = new ConcurrentLinkedQueue<>();
    public final CheckManager checkManager;
    public final PunishmentManager punishmentManager;
    public final MovementCheckRunner movementCheckRunner;
    public final SyncedTags tagManager;
    public final PacketActionProcessor packetActionProcessor;
    public final Set<Short> didWeSendThatTrans = ConcurrentHashMap.newKeySet();
    public final RotateProcessor rotateProcessor;
    public final AtomicInteger lastTransactionSent = new AtomicInteger(0);
    public final AtomicInteger lastTransactionReceived = new AtomicInteger(0);
    public final ClickProcessor clickProcessor;
    public final long joinTime = System.currentTimeMillis();
    public final UncertaintyHandler uncertaintyHandler;
    public final VehicleData vehicleData = new VehicleData();
    public final CompensatedWorld compensatedWorld;
    public final CompensatedEntities compensatedEntities;
    public final LatencyUtils latencyUtils = new LatencyUtils(this);
    public final PointThreeEstimator pointThreeEstimator;
    public final TrigHandler trigHandler = new TrigHandler(this);
    public final PacketStateData packetStateData = new PacketStateData();
    public final AtomicInteger cancelledPackets = new AtomicInteger(0);
    
    public final double[][] possibleEyeHeights = new double[3][];
    public final Queue<BlockPlaceSnapshot> placeUseItemPackets = new LinkedBlockingQueue<>();
    public final Queue<BlockBreak> queuedBreaks = new LinkedBlockingQueue<>();
    public final PlayerBlockHistory blockHistory = new PlayerBlockHistory();
    public final ArrayDeque<RotationData> pendingRotations = new ArrayDeque<>();
    public final ArrayDeque<Movement> movementThisTick = new ArrayDeque<>(8);
    public final List<Movement> finalMovementsThisTick = new ObjectArrayList<>();
    public final LongSet visitedBlocks = new LongOpenHashSet();
    private final List<Integer> sensitivity = new EvictingList<>(14);
    private final List<Long> longTermPingList = new LinkedList<>();
    private final List<Long> pingList = new LinkedList<>();
    private final AtomicInteger transactionIDCounter = new AtomicInteger(0);
    
    
    public ExemptProcessor exemptProcessor;
    public long lastTransSent = 0;
    public long lastTransReceived = 0;
    public double lastWasClimbing = 0;
    public boolean canSwimHop = false;
    public int riptideSpinAttackTicks = 0;
    public int powderSnowFrozenTicks = 0;
    public boolean hasGravity = true;
    public boolean playerEntityHasGravity = true;
    public double gravity;
    public float friction;
    public double speed;
    public Vector3d filterMojangStupidityOnMojangStupidity = new Vector3d();
    public double x;
    public double y;
    public double z;
    public double lastX;
    public double lastY;
    public double lastZ;
    public float yaw;
    public float pitch;
    public float lastYaw;
    public float lastPitch;
    public boolean serverOnGround;
    public boolean onGround;
    public boolean lastOnGround;
    public boolean isSneaking;
    public boolean wasSneaking;
    public boolean isSprinting;
    public boolean lastSprinting;
    public boolean isFlying;
    public boolean canFly;
    public boolean wasFlying;
    public boolean isSwimming;
    public boolean wasSwimming;
    public boolean isClimbing;
    public boolean isGliding;
    public boolean wasGliding;
    public boolean isRiptidePose = false;
    public boolean isVoid;
    public int entityID;
    public boolean isSentRotate;
    public volatile boolean punish;
    public volatile boolean cancelCommand;
    @Nullable
    public volatile Player bukkitPlayer;
    public int lastServerChangeSlot;
    
    public Vector3dm clientVelocity = new Vector3dm();
    public Pose pose = Pose.STANDING;
    public Pose lastPose = Pose.STANDING;
    public int vehicleTicks;
    public boolean isInBed = false;
    public boolean lastInBed = false;
    public volatile long transactionPing = 0;
    public long lastBlockDig;
    public int windchargeAttackTick;
    public int respawnTick;
    public VectorData predictedVelocity = new VectorData(new Vector3dm(), VectorData.VectorType.Normal);
    public int food = 20;
    public long cancelledBlockTicks;
    public float depthStriderLevel;
    public float sneakingSpeedMultiplier = 0.3f;
    public float flySpeed;
    public Vector3dm actualMovement = new Vector3dm();
    
    public boolean clientClaimsLastOnGround;
    
    public boolean wasTouchingWater = false;
    public boolean wasWasTouchingWater = false;
    public boolean wasTouchingLava = false;
    
    public boolean slightlyTouchingLava = false;
    
    public boolean slightlyTouchingWater = false;
    public boolean wasEyeInWater = false;
    public FluidTag fluidOnEyes;
    public Vector3dm stuckSpeedMultiplier = new Vector3dm(1, 1, 1);
    
    
    public boolean lastSprintingForSpeed;
    public boolean worldChange;
    public boolean respawn;
    public boolean verticalCollision;
    public boolean clientControlledVerticalCollision;
    public double fallDistance;
    public boolean digging;
    
    public boolean skippedTickInActualMovement = false;
    public boolean placing;
    public CompensatedFireworks compensatedFireworks;
    public SimpleCollisionBox boundingBox = GetBoundingBox.getBoundingBoxFromPosAndSizeRaw(x, y, z, 0.6f, 1.8f);
    
    public boolean isSlowMovement = false;
    public boolean isBedrockPlayer = false;
    public boolean isAttacking;
    public boolean basicDigging;
    public boolean finishDigging;
    public boolean dropItem;
    public boolean fireworkBoost;
    public int fireworkBoostTicks;
    
    public int movementPackets = 0;
    public VelocityData firstBreadKB = null;
    public VelocityData likelyKB = null;
    public VelocityData firstBreadExplosion = null;
    public VelocityData likelyExplosions = null;
    public int minAttackSlow = 0;
    public int maxAttackSlow = 0;
    public boolean softHorizontalCollision;
    public GameMode gamemode;
    public DimensionType dimensionType;
    public boolean horizontalCollision;
    public Vector3d bedPosition;
    public long lastBlockPlaceUseItem = 0;
    public long lastBlockBreak = 0;
    
    
    
    
    
    
    public boolean couldSkipTick = false;
    public MainSupportingBlockData mainSupportingBlockData = new MainSupportingBlockData(null, false);
    
    public LastInstanceManager lastInstanceManager;
    public int totalFlyingPacketsSent;
    
    public Vector3dm baseTickAddition = new Vector3dm();
    public Vector3dm baseTickWaterPushing = new Vector3dm();
    public Vector3dm startTickClientVel = new Vector3dm();
    public long lastAttack = 0;
    public @Nullable String worldName;
    public int totalMovePacketsSent;
    public boolean serverOpenedInventoryThisTick;
    
    public volatile boolean noModifyPacketPermission = false;
    public volatile boolean noSetbackPermission = false;
    public boolean wasLastPredictionCompleteChecked;
    public boolean isJumping;
    public boolean lastJumping;
    @Setter
    @Getter
    public long lastFlying;
    
    
    public volatile boolean bypass = false;
    public int sinceWeaponShootTicks;
    private boolean forceStuckSpeed = true;
    public boolean lagging;
    public long lastFlyingDelay;
    public long artemisLastDelayedFlyingPacket;
    public PacketEntity target;
    public PacketEntity lastTarget;
    public String teamName;
    public int moveTick;
    public boolean moving;
    public boolean inWeb;
    public boolean position, lastPosition, lastLastPosition;
    @Getter
    @Setter
    public PacketLocation locationData = new PacketLocation(0.0, 0.0, 0.0);
    @Getter
    @Setter
    public PacketLocation lastLocationData = new PacketLocation(0.0, 0.0, 0.0);
    public boolean rotate, lastRotate, lastLastRotate;
    public double deltaX;
    public double deltaY;
    public double deltaZ;
    public double lastDeltaX;
    public double lastDeltaY;
    public double lastDeltaZ;
    public double deltaXZ;
    public double lastDeltaXZ;
    public double acceleration;
    public double lastAcceleration;
    @Setter
    @Getter
    public int standTicks;
    public long lastDelayedMovePacket;
    public double vehicleX;
    public double vehicleY;
    public double vehicleZ;
    public double lastVehicleX;
    public double lastVehicleY;
    public double lastVehicleZ;
    public double vehicleDeltaX;
    public double vehicleDeltaY;
    public double vehicleDeltaZ;
    public double vehicleDeltaXZ;
    private boolean forceSlowMovement = true;
    public int sinceBukkitCancelMovementTicks;
    public int sinceMythicMobTicks;
    public int sinceMythicMobItemAttackTicks;
    public int sinceBreweryPushTicks;
    public int sinceGSitActionTick;
    public int sinceRiptideSpinTick;
    public int sinceMyPitActionTick;
    public int sinceChangeGamemodeTick;
    public int elytraTicks;
    public int outRidingTicks;
    public boolean hasInventoryOpen;
    public long lastInventoryOpen;
    public InventoryDesyncStatus inventoryDesyncStatus;
    public int velocitySinceTick;
    private long tranDelay;
    private long lastTranDelay;
    private long firstAttackTime = -1;
    private boolean isSkipTimeOut;
    private int verusTransactionIDCounter = -32768;
    private int transactionSkip;
    private int transactionJoinWaitTime;
    private volatile long lastTransactionPing = 0;
    @Getter
    private volatile int averagePing;
    @Setter
    @Getter
    private int currentSlot;
    private volatile long lastTransactionTime = System.nanoTime();
    private volatile PacketTracker viaPacketTracker;
    @Getter
    private volatile long playerClockAtLeast = System.nanoTime();
    
    private boolean debugPacketCancel = false;
    private int spamThreshold = 100;
    private int maxTransactionTime = 60;
    @Getter
    @Setter
    private boolean experimentalChecks = false;
    private volatile @Nullable UserConnection viaUserConnection;
    
    
    
    
    private boolean asyncTransactionSend = false;

    public PlayerData(User user) {
        this.user = user;
        this.outRidingTicks = 300;
        this.dimensionType = user.getDimensionType();
        this.sinceWeaponShootTicks = 500;
        this.sinceMythicMobTicks = 500;
        this.sinceBukkitCancelMovementTicks = 500;
        this.sinceBreweryPushTicks = 100;
        this.sinceMythicMobItemAttackTicks = 500;
        this.sinceGSitActionTick = 500;
        this.sinceMyPitActionTick = 500;
        this.sinceChangeGamemodeTick = 200;
        this.elytraTicks = 300;
        this.respawnTick = 500;
        this.windchargeAttackTick = 200;
        this.sinceRiptideSpinTick = 200;
        this.fireworkBoostTicks = 200;
        this.uuid = user.getUUID();

        if (gamemode == null) {
            gamemode = GameMode.SURVIVAL;
        }

        boundingBox = GetBoundingBox.getBoundingBoxFromPosAndSizeRaw(x, y, z, 0.6f, 1.8f);

        compensatedFireworks = new CompensatedFireworks(this);

        lastInstanceManager = new LastInstanceManager(this);
        exemptProcessor = new ExemptProcessor(this);
        clickProcessor = new ClickProcessor(this);
        packetActionProcessor = new PacketActionProcessor(this);
        checkManager = new CheckManager(this);
        rotateProcessor = getCheckManager().getRotationCheck(RotateProcessor.class);
        punishmentManager = new PunishmentManager(this);
        movementCheckRunner = new MovementCheckRunner(this);
        tagManager = new SyncedTags(this);

        compensatedWorld = new CompensatedWorld(this);
        compensatedEntities = new CompensatedEntities(this);
        uncertaintyHandler = new UncertaintyHandler(this); 
        pointThreeEstimator = new PointThreeEstimator(this);


        if (getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)) {
            final float scale = (float) compensatedEntities.getSelf().getAttributeValue(Attributes.SCALE);
            possibleEyeHeights[2] = new double[]{0.4 * scale, 1.62 * scale, 1.27 * scale}; 
            possibleEyeHeights[1] = new double[]{1.27 * scale, 1.62 * scale, 0.4 * scale}; 
            possibleEyeHeights[0] = new double[]{1.62 * scale, 1.27 * scale, 0.4 * scale}; 

        } else if (getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) { 
            possibleEyeHeights[2] = new double[]{0.4, 1.62, 1.54}; 
            possibleEyeHeights[1] = new double[]{1.54, 1.62, 0.4}; 
            possibleEyeHeights[0] = new double[]{1.62, 1.54, 0.4}; 
        } else {
            possibleEyeHeights[1] = new double[]{(double) (1.62f - 0.08f), (double) (1.62f)}; 
            possibleEyeHeights[0] = new double[]{(double) (1.62f), (double) (1.62f - 0.08f)}; 
        }
        onReload();
    }

    private static boolean isGlider(ItemStack stack, EquipmentSlot slot) {
        if (!stack.hasComponent(ComponentTypes.GLIDER) || stack.getDamageValue() >= (stack.getMaxDamage() - 1)) {
            return false;
        }

        Optional<ItemEquippable> equippable = stack.getComponent(ComponentTypes.EQUIPPABLE);
        return equippable.isPresent() && equippable.get().getSlot() == slot;
    }

    public void onPacketCancel() {
        if (spamThreshold != -1 && cancelledPackets.incrementAndGet() > spamThreshold && !isClientACUser()) {
            LogUtils.console("&c踢出玩家 &f" + getName() + " &c因为其发送了大量的数据包! &7(&b" + cancelledPackets + "&7)");
            disconnect(Component.translatable(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("kick.packet").replace("%pps%", cancelledPackets.incrementAndGet() + "")));
            cancelledPackets.set(0);
        }
    }

    public Set<VectorData> getPossibleVelocities() {
        Set<VectorData> set = new HashSet<>();

        if (firstBreadKB != null) {
            set.add(new VectorData(firstBreadKB.vector.clone(), VectorData.VectorType.Knockback).returnNewModified(VectorData.VectorType.FirstBreadKnockback));
        }

        if (likelyKB != null) {
            
            set.add(new VectorData(likelyKB.vector.clone(), VectorData.VectorType.Knockback));
        }

        set.addAll(getPossibleVelocitiesMinusKnockback());
        return set;
    }

    public Set<VectorData> getPossibleVelocitiesMinusKnockback() {
        Set<VectorData> possibleMovements = new HashSet<>();
        possibleMovements.add(new VectorData(clientVelocity, VectorData.VectorType.Normal));

        
        
        if (canSwimHop && !onGround) {
            possibleMovements.add(new VectorData(clientVelocity.clone().setY(0.3f), VectorData.VectorType.Swimhop));
        }

        
        
        
        if (riptideSpinAttackTicks >= 0 && Collections.max(uncertaintyHandler.riptideEntities) > 0) {
            possibleMovements.add(new VectorData(clientVelocity.clone().multiply(-0.2), VectorData.VectorType.Trident));
        }

        if (lastWasClimbing != 0) {
            possibleMovements.add(new VectorData(clientVelocity.clone().setY(lastWasClimbing + baseTickAddition.getY()), VectorData.VectorType.Climbable));
        }

        
        
        for (VectorData data : new HashSet<>(possibleMovements)) {
            for (BlockFace direction : uncertaintyHandler.slimePistonBounces) {
                if (direction.getModX() != 0) {
                    possibleMovements.add(data.returnNewModified(data.vector.clone().setX(direction.getModX()), VectorData.VectorType.SlimePistonBounce));
                } else if (direction.getModY() != 0) {
                    possibleMovements.add(data.returnNewModified(data.vector.clone().setY(direction.getModY()), VectorData.VectorType.SlimePistonBounce));
                } else if (direction.getModZ() != 0) {
                    possibleMovements.add(data.returnNewModified(data.vector.clone().setZ(direction.getModZ()), VectorData.VectorType.SlimePistonBounce));
                }
            }
        }

        return possibleMovements;
    }

    public void baseTickAddWaterPushing(Vector3dm vector) {
        baseTickWaterPushing.add(vector);
    }

    public void baseTickAddVector(Vector3dm vector) {
        clientVelocity.add(vector);
    }

    public void trackBaseTickAddition(Vector3dm vector) {
        baseTickAddition.add(vector);
    }

    
    
    
    
    public boolean addTransactionResponse(short id) {
        if (!transactionsSent.isEmpty() && id > transactionsSent.peek().first() && !TimeUtils.hasExpired(joinTime, transactionJoinWaitTime)) {
            
            
            
            if (transactionsSent.peek() != null) {
                getCheckManager().getCheck(BadPacketsO.class).startFlag("sent= " + id + "\nactually= " + transactionsSent.peek().first(), transactionsSent.size());
            }
        }
        Pair<Short, Long> data = null;
        boolean hasID = false;
        int skipped = 0;
        for (Pair<Short, Long> iterator : transactionsSent) {
            if (iterator.first() == id) {
                hasID = true;
                break;
            }
            skipped++;
        }

        if (hasID) {
            
            if (viaPacketTracker != null)
                viaPacketTracker.setIntervalPackets(viaPacketTracker.getIntervalPackets() - 1);

            if (skipped > transactionSkip && !TimeUtils.hasExpired(joinTime, transactionJoinWaitTime))

                getCheckManager().getCheck(BadPacketsO.class).startFlag("skipped= " + skipped, skipped);

            do {
                data = transactionsSent.poll();
                if (data == null)
                    break;

                lastTransactionReceived.incrementAndGet();
                lastTransReceived = System.currentTimeMillis();
                lastTransactionPing = transactionPing;
                transactionPing = (System.nanoTime() - data.second());
                playerClockAtLeast = data.second();
                averagePing = (averagePing * 4 + getKeepAlivePing()) / 5;
            } while (data.first() != id);

            long now = System.currentTimeMillis();
            CheckManagerListener.handleQueuedPlaces(this, false, 0, 0, now);
            CheckManagerListener.handleQueuedBreaks(this, false, 0, 0, now);
            latencyUtils.handleNettySyncTransaction(lastTransactionReceived.get());
        }

        
        return data != null;
    }

    public void sendTransaction() {
        sendTransaction(asyncTransactionSend);
    }

    public void sendTransaction(boolean async) {
        if (user.getEncoderState() != ConnectionState.PLAY) return;

        long currentTime = System.nanoTime();

        if (bypass && (currentTime - getPlayerClockAtLeast()) > 15e9) {
            return;
        }

        final String tranType;

        if (!TimeUtils.hasExpired(joinTime, 5)) {
            tranType = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("function.transaction.type", "GrimAC");
        } else {
            tranType = "GrimAC";
        }

        short transactionID = getTransactionID(tranType);

        lastTransSent = System.currentTimeMillis();
        lastTransactionTime = currentTime;

        try {
            PacketWrapper<?> packet = createPacket(transactionID);

            if (async) {
                ChannelHelper.runInEventLoop(user.getChannel(), () -> {
                    addTransactionSend(transactionID);
                    user.writePacket(packet);
                });
            } else {
                addTransactionSend(transactionID);
                user.writePacket(packet);
            }
        } catch (Exception ignored) {
        }
    }

    private short getTransactionID(String tranType) {
        switch (tranType) {
            case "GrimAC":
                isSkipTimeOut = false;
                return (short) (-1 * (transactionIDCounter.getAndIncrement() & 0x7FFF));
            case "Verus":
                isSkipTimeOut = false;
                short verusID = (short) verusTransactionIDCounter;
                verusTransactionIDCounter = (verusTransactionIDCounter + 1) % 32767;
                return verusID;
            case "Intave":
                isSkipTimeOut = true;
                return moving ? (short) 1 : (short) (ThreadLocalRandom.current().nextInt(32767) + 1);
            default:
                return (short) (-1 * (transactionIDCounter.getAndIncrement() & 0x7FFF));
        }
    }

    private PacketWrapper<?> createPacket(short transactionID) {
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
            return new WrapperPlayServerPing(transactionID);
        } else {
            return new WrapperPlayServerWindowConfirmation((byte) 0, transactionID, false);
        }
    }

    public void addTransactionSend(short id) {
        didWeSendThatTrans.add(id);
    }

    public boolean isEyeInFluid(FluidTag tag) {
        return this.fluidOnEyes == tag;
    }

    public double getEyeHeight() {
        return getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? pose.eyeHeight
                : isSneaking ? 1.54f : 1.62f;
    }

    public void timedOut() {
        disconnect(Component.translatable(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("kick.timed-out")));
    }

    public float getMaxUpStep() {
        final PacketEntitySelf self = compensatedEntities.self;
        final PacketEntity riding = self.getRiding();
        if (riding == null) return (float) self.getAttributeValue(Attributes.STEP_HEIGHT);

        if (riding.isBoat) {
            return 0f;
        }

        float value = (float) riding.getAttributeValue(Attributes.STEP_HEIGHT);
        if (riding.isHappyGhast) {
            return ((PacketEntityHappyGhast) riding).isControllingPassenger() ? Math.max(value, 1.0F) : value;
        }

        
        return value;
    }

    public void pollData() {
        
        
        
        
        
        if (lastTransSent != 0 && lastTransSent + 80 < System.currentTimeMillis()) {
            sendTransaction(true); 
        }
        if ((System.nanoTime() - getPlayerClockAtLeast()) > PluginLoader.INSTANCE.getConfigManager().getMaxPingTransaction() * 1e9) {
            if (!isSkipTimeOut) {
                timedOut();
            }
        }

        if (PluginLoader.INSTANCE.getPlayerDataManager().exemptUsers.contains(user)) {
            PluginLoader.INSTANCE.getPlayerDataManager().remove(user);
        }

        if (viaPacketTracker == null && HookInit.getViaPluginHook().isEnabled() && uuid != null) {
            UserConnection connection = Via.getManager().getConnectionManager().getConnectedClient(uuid);
            viaPacketTracker = connection != null ? connection.getPacketTracker() : null;
            this.viaUserConnection = connection;
        }

        if (uuid != null && this.bukkitPlayer == null) {
            this.bukkitPlayer = Bukkit.getPlayer(uuid);
            updatePermissions();
        }
    }

    public void disconnect(Component reason) {
        final Player player = getBukkitPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        String textReason;
        if (reason instanceof TranslatableComponent translatableComponent) {
            textReason = translatableComponent.key();
        } else {
            textReason = LegacyComponentSerializer.legacySection().serialize(reason);
        }
        LogUtils.console("Disconnecting " + user.getProfile().getName() + " for " + ColorUtils.stripColor(textReason));
        try {
            user.sendPacket(new WrapperPlayServerDisconnect(reason));
        } catch (Exception ignored) { 
            Yuki.getInstance().getLogger().warning("Failed to send disconnect packet to disconnect " + user.getProfile().getName() + "! Disconnecting anyways.");
        }
        user.closeConnection();
        MHDFScheduler.getEntityScheduler().runTask(Yuki.getInstance(), bukkitPlayer, () -> {
            player.kickPlayer(textReason);
        }, null);
        PluginLoader.INSTANCE.getPlayerDataManager().onDisconnect(user);
    }

    public void updateVelocityMovementSkipping() {
        if (!couldSkipTick) {
            couldSkipTick = pointThreeEstimator.determineCanSkipTick(BlockProperties.getFrictionInfluencedSpeed((float) (speed * (isSprinting ? 1.3 : 1)), this), getPossibleVelocitiesMinusKnockback());
        }

        Set<VectorData> knockback = new HashSet<>();
        if (firstBreadKB != null) knockback.add(new VectorData(firstBreadKB.vector, VectorData.VectorType.Knockback));
        if (likelyKB != null) knockback.add(new VectorData(likelyKB.vector, VectorData.VectorType.Knockback));

        boolean kbPointThree = pointThreeEstimator.determineCanSkipTick(BlockProperties.getFrictionInfluencedSpeed((float) (speed * (isSprinting ? 1.3 : 1)), this), knockback);
        checkManager.getKnockbackHandler().setPointThree(kbPointThree);

        Set<VectorData> explosion = new HashSet<>();
        if (firstBreadExplosion != null)
            explosion.add(new VectorData(firstBreadExplosion.vector, VectorData.VectorType.Explosion));
        if (likelyExplosions != null)
            explosion.add(new VectorData(likelyExplosions.vector, VectorData.VectorType.Explosion));

        boolean explosionPointThree = pointThreeEstimator.determineCanSkipTick(BlockProperties.getFrictionInfluencedSpeed((float) (speed * (isSprinting ? 1.3 : 1)), this), explosion);
        checkManager.getExplosionHandler().setPointThree(explosionPointThree);

        if (kbPointThree || explosionPointThree) {
            uncertaintyHandler.lastPointThree.reset();
        }
    }


    public boolean isPointThree() {
        return getClientVersion().isOlderThan(ClientVersion.V_1_18_2);
    }

    public double getMovementThreshold() {
        return isPointThree() ? 0.03 : 0.0002;
    }

    public ClientVersion getClientVersion() {
        return Objects.requireNonNullElseGet(user.getClientVersion(), () -> ClientVersion.getById(Yuki.getInstance().getPacketEventsManager().getServerVersion().getProtocolVersion()));
    }

    
    
    
    
    
    
    
    
    
    
    
    public boolean isTickingReliablyFor(int ticks) {
        
        
        return !canSkipTicks() || (inVehicle()
                || !uncertaintyHandler.lastPointThree.hasOccurredSince(ticks))
                && !uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(1);
    }

    
    @Override
    public void updatePermissions() {
        if (bukkitPlayer == null) return;
        this.noModifyPacketPermission = bukkitPlayer.hasPermission("yuki.exempt.modifypacket");
        this.noSetbackPermission = bukkitPlayer.hasPermission("yuki.exempt.setback");

        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            for (AbstractCheck check : getCheckManager().allChecks.values()) {
                if (check instanceof Check) {
                    ((Check) check).updateExempted();
                }
            }
        });
    }

    public boolean inVehicle() {
        return compensatedEntities.self.inVehicle();
    }

    public CompensatedInventory getInventory() {
        return checkManager.getInventory();
    }

    @Override
    public int getTransactionPing() {
        return MathUtil.floor(transactionPing / 1e6);
    }

    public double[] getPossibleEyeHeights() { 
        
        if (this.getClientVersion().isOlderThan(ClientVersion.V_1_9)) {
            return this.isSneaking ? this.possibleEyeHeights[1] : this.possibleEyeHeights[0];
        } else {
            
            return switch (pose) {
                case FALL_FLYING, 
                     SPIN_ATTACK, 
                     SWIMMING -> 
                        this.possibleEyeHeights[2]; 
                case NINE_CROUCHING, CROUCHING ->
                        this.possibleEyeHeights[1]; 
                default ->
                        this.possibleEyeHeights[0]; 
            };
        }
    }

    @Override
    public int getCps() {
        return clickProcessor.getCps();
    }


    public int getLastTransactionPing() {
        return MathUtil.floor(lastTransactionPing / 1e6);
    }

    @Override
    public int getKeepAlivePing() {
        if (bukkitPlayer == null) return -1;
        return PacketEvents.getAPI().getPlayerManager().getPing(bukkitPlayer);
    }

    @Override
    public int getLastCps() {
        return clickProcessor.getLastCps();
    }

    public boolean wouldCollisionResultFlagGroundSpoof(double inputY, double collisionY) {
        boolean verticalCollision = inputY != collisionY;
        boolean calculatedOnGround = verticalCollision && inputY < 0.0D;

        
        if (exemptOnGround()) return false;

        
        if (inputY == -SimpleCollisionBox.COLLISION_EPSILON && collisionY > -SimpleCollisionBox.COLLISION_EPSILON && collisionY <= 0)
            return false;

        return calculatedOnGround != onGround;
    }

    public boolean exemptOnGround() {
        return inVehicle()
                || Collections.max(uncertaintyHandler.pistonX) != 0
                || Collections.max(uncertaintyHandler.pistonY) != 0
                || Collections.max(uncertaintyHandler.pistonZ) != 0
                || uncertaintyHandler.isStepMovement
                || isFlying
                || canFly
                || uncertaintyHandler.isSteppingNearShulker
                || uncertaintyHandler.isSteppingOnFence
                || compensatedEntities.getSelf().isDead
                || isInBed
                || lastInBed
                || uncertaintyHandler.lastFlyingStatusChange.hasOccurredSince(30)
                || uncertaintyHandler.lastHardCollidingLerpingEntity.hasOccurredSince(3)
                || uncertaintyHandler.isOrWasNearGlitchyBlock;
    }

    public SetbackTeleportUtil getSetbackTeleportUtil() {
        return checkManager.getSetbackUtil();
    }

    public void handleMountVehicle(int vehicleID) {
        compensatedEntities.serverPlayerVehicle = vehicleID;
        TrackerData data = compensatedEntities.getTrackedEntity(vehicleID);

        if (data != null) {
            
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9) && getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                
                if (EntityTypes.isTypeInstanceOf(data.getEntityType(), EntityTypes.BOAT) ||
                        EntityTypes.isTypeInstanceOf(data.getEntityType(), EntityTypes.ABSTRACT_HORSE) ||
                        data.getEntityType() == EntityTypes.PIG ||
                        data.getEntityType() == EntityTypes.STRIDER ||
                        data.getEntityType() == EntityTypes.CAMEL ||
                        data.getEntityType() == EntityTypes.HAPPY_GHAST) {
                    
                    
                    user.writePacket(new WrapperPlayServerEntityVelocity(vehicleID, new Vector3d()));
                }
            }
        }

        
        sendTransaction();

        latencyUtils.addRealTimeTask(lastTransactionSent.get(), () -> this.vehicleData.wasVehicleSwitch = true);
    }

    public int getRidingVehicleId() {
        return compensatedEntities.getPacketEntityID(compensatedEntities.self.getRiding());
    }

    public void handleDismountVehicle(PacketSendEvent event) {
        EntityType entityType = getVehicleType();
        
        sendTransaction();

        compensatedEntities.serverPlayerVehicle = null;
        event.getTasksAfterSend().add(() -> {
            if (inVehicle()) {
                int ridingId = getRidingVehicleId();
                TrackerData data = compensatedEntities.serverPositionsMap.get(ridingId);
                if (data != null) {
                    user.writePacket(new WrapperPlayServerEntityTeleport(ridingId, new Vector3d(data.getX(), data.getY(), data.getZ()), data.getYaw(), data.getPitch(), false));
                }
            }
        });

        latencyUtils.addRealTimeTask(lastTransactionSent.get(), () -> {
            this.vehicleData.wasVehicleSwitch = true;
            
            
            if (getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_14) ||
                    (getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_5)
                            && EntityTypes.MINECART == entityType)) {
                compensatedEntities.hasSprintingAttributeEnabled = false;
            }
        });
    }

    public boolean canGlide() {
        
        if (getClientVersion().isOlderThan(ClientVersion.V_1_21_2)
                || Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_21_2)) {
            final ItemStack chestPlate = getInventory().getChestplate();
            return chestPlate.getType() == ItemTypes.ELYTRA && chestPlate.getDamageValue() < chestPlate.getMaxDamage() - 1;
        }

        final CompensatedInventory inventory = getInventory();
        
        
        return isGlider(inventory.getHelmet(), EquipmentSlot.CHEST_PLATE)
                || isGlider(inventory.getChestplate(), EquipmentSlot.LEGGINGS)
                || isGlider(inventory.getLeggings(), EquipmentSlot.BOOTS)
                || isGlider(inventory.getBoots(), EquipmentSlot.OFF_HAND);
    }

    public void resyncPose() {
        if (getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14) && bukkitPlayer != null) {
            bukkitPlayer.setSneaking(!bukkitPlayer.isSneaking());
        }
    }

    public boolean canUseGameMasterBlocks() {
        
        
        return getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_10) || (gamemode == GameMode.CREATIVE && compensatedEntities.self.opLevel >= 2);
    }

    public boolean isInWaterOrRain() {
        return compensatedWorld.isRaining || Collisions.hasMaterial(this, boundingBox.copy().expand(0.1f), (block) -> Materials.isWater(CompensatedWorld.blockVersion, block.first()));
    }

    @Contract(pure = true)
    public boolean supportsEndTick() {
        return supportsEndTickPreVia() && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2);
    }

    @Contract(pure = true)
    public boolean supportsEndTickPreVia() {
        return getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2);
    }

    @Contract(pure = true)
    public boolean canSkipTicks() {
        return getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) && !supportsEndTick();
    }

    @Contract(pure = true)
    public boolean canSkipTicksPreVia() {
        return getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) && !supportsEndTickPreVia();
    }

    @Override
    public void runSafely(Runnable runnable) {
        ChannelHelper.runInEventLoop(this.user.getChannel(), runnable);
    }

    @Override
    public int getLastTransactionReceived() {
        return lastTransactionReceived.get();
    }

    @Override
    public int getLastTransactionSent() {
        return lastTransactionSent.get();
    }

    @Override
    public void addRealTimeTask(int transaction, Runnable runnable) {
        latencyUtils.addRealTimeTask(transaction, runnable);
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public UUID getUniqueId() {
        return user.getProfile().getUUID();
    }

    @Override
    public String getBrand() {
        return getCheckManager().getCheck(PayloadHandler.class).getBrand();
    }

    @Override
    public @Nullable String getBukkitWorldName() {
        return bukkitPlayer != null ? bukkitPlayer.getWorld().getName() : null;
    }

    @Override
    public @Nullable UUID getBukkitWorldUID() {
        return bukkitPlayer != null ? bukkitPlayer.getWorld().getUID() : null;
    }

    @Override
    public String getVersionName() {
        return getClientVersion().getReleaseName();
    }

    @Override
    public double getHorizontalSensitivity() {
        return rotateProcessor.getSensitivityX();
    }

    @Override
    public double getVerticalSensitivity() {
        return rotateProcessor.getSensitivityY();
    }

    @Override
    public boolean isTeleporting() {
        return packetStateData.lastPacketWasTeleport;
    }

    @Override
    public boolean isVanillaMath() {
        return trigHandler.isVanillaMath();
    }

    @Override
    public Collection<? extends AbstractCheck> getChecks() {
        return getCheckManager().allChecks.values();
    }

    public void runNettyTaskInMs(Runnable runnable, int ms) throws IllegalStateException {
        final Player bukkitPlayer = Bukkit.getPlayer(user.getName());

        if (bukkitPlayer == null) {
            return;
        }

        final Channel channel = (Channel) PacketEvents.getAPI().getPlayerManager().getChannel(bukkitPlayer);

        if (!channel.isRegistered()) {
            channel.eventLoop().register(channel);
        }

        if (!channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(() -> channel.eventLoop().schedule(runnable, ms, TimeUnit.MILLISECONDS));
        } else {
            channel.eventLoop().schedule(runnable, ms, TimeUnit.MILLISECONDS);
        }
    }

    public void onReload() {
        forceStuckSpeed = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.force-mitigate-types.stuck-move", true);
        forceSlowMovement = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.force-mitigate-types.slow-move", true);
        spamThreshold = PluginLoader.INSTANCE.getConfigManager().getConfig().getIntElse("function.limit.packet", 100);
        transactionSkip = PluginLoader.INSTANCE.getConfigManager().getConfig().getIntElse("BadPacketsO.skips", 0);
        transactionJoinWaitTime = PluginLoader.INSTANCE.getConfigManager().getConfig().getIntElse("BadPacketsO.join-time", 5);
        asyncTransactionSend = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.transaction.netty", false);
    }


    public long elapsedMS(long now, long time) {
        return (long) ((double) (now - time) / 1000000.0);
    }


    @Override
    public boolean isMoveLagging() {
        long now = System.currentTimeMillis();
        return now - lastDelayedMovePacket < 220L || packetStateData.lastPacketWasTeleport;
    }

    @Override
    public boolean isFlyingLagging() {
        return exemptProcessor.isExempt(ExemptType.NFPGAY) ? !TimeUtils.elapsed(artemisLastDelayedFlyingPacket, 250L) && !moving : !TimeUtils.elapsed(artemisLastDelayedFlyingPacket, 250L);
    }

    @SneakyThrows
    @Override
    public double getTPS() {
        if (bukkitPlayer == null) {
            return 0;
        }
        return PaperUtils.getTPS(bukkitPlayer.getLocation(), false).get();
    }

    @Override
    public String getChannel() {
        return getCheckManager().getCheck(PayloadHandler.class).getChannel();
    }

    public void randomiseAim(Player player, Location location) {
        location.clone().setPitch((float) Math.max(-90, ThreadLocalRandom.current().nextInt(90)));
        location.clone().setYaw((float) Math.max(-180, ThreadLocalRandom.current().nextInt(180)));
        PaperUtils.teleport(player, location);
        locationData.setPitch(location.getPitch());
        locationData.setYaw(location.getYaw());
        locationData.setX(location.getX());
        locationData.setY(location.getY());
        locationData.setZ(location.getZ());
        setYaw(location.getYaw());
        setPitch(location.getPitch());
    }

    @Override
    public boolean isClientACUser() {
        return false;
    }

    @Override
    public void mitigateDamage(String name) {
        if (!PlayerAttackListener.user.contains(name) && PluginLoader.INSTANCE.getConfigManager().isMitigateReduceDamage()) {
            PlayerAttackListener.user.add(name);
        }
    }

    public void mitigateDamage() {
        final String name = getName();
        if (!PlayerAttackListener.user.contains(name) && PluginLoader.INSTANCE.getConfigManager().isMitigateReduceDamage()) {
            PlayerAttackListener.user.add(name);
        }
    }


    public boolean hasAttackedSince(long time) {
        return System.currentTimeMillis() - lastAttack < time;
    }

    public boolean hasBlockPlaceSince(long time) {
        return System.currentTimeMillis() - lastBlockPlaceUseItem < time;
    }


    @Override
    public void addRealTimeTaskAsync(int transaction, Runnable runnable) {
        latencyUtils.addRealTimeTaskAsync(transaction, runnable);
    }


    @Override
    public int calculateSensitivity() {
        if (MathUtil.getDistinct(getSensitivity()) != getSensitivity().size()) {
            final Set<Integer> prev = new HashSet<>();
            for (int i : getSensitivity()) {
                if (prev.contains(i / 5)) {
                    return i;
                } else prev.add(i / 5);
            }
        }

        return -1;
    }


    public PacketEntity getVehicle() {
        return compensatedEntities.self.riding;
    }


    public EntityType getVehicleType() {
        return inVehicle() ? getVehicle().type : null;
    }


    public void addMovementThisTick(Movement movement) {
        if (this.movementThisTick.size() >= 100) {
            Movement movement1 = this.movementThisTick.removeFirst();
            Movement movement2 = this.movementThisTick.removeFirst();
            Movement movement3 = new Movement(movement1.from(), movement2.to(), false);
            this.movementThisTick.addFirst(movement3);
        }

        this.movementThisTick.add(movement);
    }

    public int getViaTranslatedClientBlockID(int blockStateId) {
        if (this.viaUserConnection == null) {
            return blockStateId;
        }

        final ProtocolVersion clientVersion = this.viaUserConnection.getProtocolInfo().protocolVersion();
        final ProtocolVersion serverVersion = this.viaUserConnection.getProtocolInfo().serverProtocolVersion();

        final List<ProtocolPathEntry> protocolPath = Via.getManager().getProtocolManager().getProtocolPath(clientVersion, serverVersion);
        if (protocolPath == null) {
            return blockStateId;
        }

        for (int i = protocolPath.size() - 1; i >= 0; i--) {
            final Protocol<?, ?, ?, ?> protocol = protocolPath.get(i).protocol();
            if (protocol.getMappingData() != null && protocol.getMappingData().getBlockStateMappings() != null) {
                blockStateId = protocol.getMappingData().getNewBlockStateId(blockStateId);
            }
        }

        return blockStateId;
    }


    public record Movement(Vector3d from, Vector3d to, boolean axisIndependant) {
    }

}