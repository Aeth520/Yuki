package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.CheckPipeline;
import cn.aetheris.yuki.check.type.*;
import cn.aetheris.yuki.check.impl.combat.aim.*;
import cn.aetheris.yuki.check.impl.combat.analysis.*;
import cn.aetheris.yuki.check.impl.combat.autoblock.*;
import cn.aetheris.yuki.check.impl.combat.autoblock.prediction.AutoBlock1;
import cn.aetheris.yuki.check.impl.combat.autoblock.prediction.AutoBlock2;
import cn.aetheris.yuki.check.impl.combat.autoblock.prediction.AutoBlock3;
import cn.aetheris.yuki.check.impl.combat.autoblock.prediction.AutoBlock4;
import cn.aetheris.yuki.check.impl.combat.killaura.*;
import cn.aetheris.yuki.check.impl.combat.reach.*;
import cn.aetheris.yuki.check.impl.combat.velocity.*;
import cn.aetheris.yuki.check.impl.misc.chat.*;
import cn.aetheris.yuki.check.impl.misc.client.ClientA;
import cn.aetheris.yuki.check.impl.misc.ghostblock.GhostBlockMitigation;
import cn.aetheris.yuki.check.impl.misc.packet.PacketMitigation;
import cn.aetheris.yuki.check.impl.misc.spam.*;
import cn.aetheris.yuki.check.impl.misc.visual.*;
import cn.aetheris.yuki.check.impl.movement.elytra.*;
import cn.aetheris.yuki.check.impl.movement.groundspoof.*;
import cn.aetheris.yuki.check.impl.movement.noslow.*;
import cn.aetheris.yuki.check.impl.movement.movementvalidation.MovementValidation;
import cn.aetheris.yuki.check.impl.movement.sprint.SprintA;
import cn.aetheris.yuki.check.impl.movement.vehicle.fly.*;
import cn.aetheris.yuki.check.impl.movement.vehicle.nosaddle.*;
import cn.aetheris.yuki.check.impl.player.airplace.AirPlaceA;
import cn.aetheris.yuki.check.impl.player.autoclicker.*;
import cn.aetheris.yuki.check.impl.player.autoclicker.bad.*;
import cn.aetheris.yuki.check.impl.player.badpackets.*;
import cn.aetheris.yuki.check.impl.player.badpackets.bad.*;
import cn.aetheris.yuki.check.impl.player.badpackets.packetorder.*;
import cn.aetheris.yuki.check.impl.player.badpackets.packetorder.processor.PacketActionProcessor;
import cn.aetheris.yuki.check.impl.player.badpackets.switchitem.*;
import cn.aetheris.yuki.check.impl.player.baritone.*;
import cn.aetheris.yuki.check.impl.player.blink.BlinkA;
import cn.aetheris.yuki.check.impl.player.breaking.far.*;
import cn.aetheris.yuki.check.impl.player.breaking.fast.*;
import cn.aetheris.yuki.check.impl.player.breaking.invalid.*;
import cn.aetheris.yuki.check.impl.player.breaking.multi.*;
import cn.aetheris.yuki.check.impl.player.breaking.position.*;
import cn.aetheris.yuki.check.impl.player.breaking.wrong.*;
import cn.aetheris.yuki.check.impl.player.crash.*;
import cn.aetheris.yuki.check.impl.player.exploit.*;
import cn.aetheris.yuki.check.impl.player.fastplace.FastPlaceA;
import cn.aetheris.yuki.check.impl.player.impossible.*;
import cn.aetheris.yuki.check.impl.player.multiactions.*;
import cn.aetheris.yuki.check.impl.player.interact.*;
import cn.aetheris.yuki.check.impl.player.inventory.*;
import cn.aetheris.yuki.check.impl.player.inventory.prediction.*;
import cn.aetheris.yuki.check.impl.player.pingspoof.*;
import cn.aetheris.yuki.check.impl.player.post.PostCheck;
import cn.aetheris.yuki.check.impl.player.scaffold.*;
import cn.aetheris.yuki.check.impl.player.scaffold.invalid.*;
import cn.aetheris.yuki.check.impl.player.timer.*;
import cn.aetheris.yuki.check.impl.player.xray.XRayA;
import cn.aetheris.yuki.check.util.handler.*;
import cn.aetheris.yuki.check.util.processor.clickprocessor.ClickProcessor;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.Cinematic;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.GhostBlockDetector;
import cn.aetheris.yuki.predictionengine.SneakingEstimator;
import cn.aetheris.yuki.util.latency.*;
import cn.aetheris.yuki.util.team.TeamHandler;
import cn.aetheris.yuki.util.maps.ClassLoadingMap;
import cn.aetheris.yuki.functionality.*;
import cn.aetheris.yuki.listener.packets.*;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Central registry of all anti-cheat checks.
 * Determines the pipeline each check belongs to based on its superclass
 * and {@link CheckData#pipeline()} annotation.
 */
@SuppressWarnings("unchecked")
public final class CheckRegistry {

    private static final Class<?>[] PACKET_CHECK_CLASSES = {
        // Reach
        ReachA.class, ReachB.class, ReachC.class, ReachD.class,
        // Packet handlers
        PacketEntityReplication.class, PayloadHandler.class, PacketChangeGameState.class,
        CompensatedInventory.class, AbilitiesHandler.class, PacketWorldBorder.class,
        TeamHandler.class,
        // GroundSpoof
        GroundSpoofA.class,
        // FastBreak
        FastBreakB.class, FastBreakC.class,
        // Client
        ClientA.class,
        // Chat
        ChatA.class, ChatB.class, ChatC.class, ChatD.class,
        // Impossible
        ImpossibleB.class, ImpossibleC.class, ImpossibleD.class, ImpossibleE.class,
        ImpossibleF.class, ImpossibleG.class, ImpossibleH.class, ImpossibleI.class,
        ImpossibleJ.class, ImpossibleK.class, ImpossibleL.class,
        // Exploit
        ExploitA.class, ExploitB.class, ExploitC.class, ExploitD.class,
        ExploitE.class, ExploitF.class, ExploitG.class,
        // Crash
        CrashA.class, CrashB.class, CrashC.class, CrashD.class, CrashE.class,
        CrashF.class, CrashG.class, CrashH.class, CrashI.class, CrashJ.class,
        CrashK.class, CrashL.class, CrashM.class,
        // PingSpoof
        PingSpoofA.class, PingSpoofB.class, PingSpoofC.class,
        PingSpoofD.class, PingSpoofE.class, PingSpoofF.class,
        // Inventory
        InventoryH.class, InventoryE.class, InventoryF.class, InventoryG.class,
        InventoryI.class, InventoryJ.class, InventoryK.class, InventoryL.class,
        InventoryM.class, InventoryN.class, BadPacketsG.class,
        // AutoClicker
        AutoClickerS.class, AutoClickerT.class,
        AutoClickerA.class, AutoClickerB.class, AutoClickerC.class, AutoClickerD.class,
        AutoClickerE.class, AutoClickerF.class, AutoClickerG.class, AutoClickerH.class,
        AutoClickerI.class, AutoClickerJ.class, AutoClickerK.class, AutoClickerL.class,
        AutoClickerM.class, AutoClickerN.class, AutoClickerO.class, AutoClickerP.class,
        AutoClickerQ.class,
        // KillAura
        KillAuraA.class, KillAuraB.class, KillAuraC.class, KillAuraD.class,
        KillAuraE.class, KillAuraF.class, KillAuraG.class, KillAuraH.class,
        KillAuraI.class, KillAuraJ.class, KillAuraK.class, KillAuraL.class,
        KillAuraM.class, AnalysisG.class,
        // Vehicle
        NoSaddleA.class, VehicleFlyA.class, VehicleFlyB.class,
        // Misc
        XRayA.class, BlinkA.class, TimerC.class,
        // BadPackets
        BadPacketsA.class, BadPacketsAA.class, BadPacketsB.class,
        BadPacketsD.class, BadPacketsE.class, BadPacketsH.class,
        BadPacketsY.class,
        // Mitigation
        PacketMitigation.class, MetaDataHider.class, EquipmentHider.class,
        CancelHandler.class,
        // MultiActions
        MultiActionsA.class, MultiActionsB.class, MultiActionsC.class,
        MultiActionsD.class, MultiActionsE.class, MultiActionsF.class,
        MultiActionsG.class,
    };

    private static final Class<?>[] PRE_VIA_PACKET_CLASSES = {
        SpamA.class, SpamB.class,
        BadPacketsJ.class, BadPacketsK.class,
        BadPacketsN.class, BadPacketsQ.class,
        BadPacketsI.class,
    };

    private static final Class<?>[] PRE_PREDICTION_CLASSES = {
        ImpossibleA.class,
        TimerA.class, TimerAA.class, TimerB.class, TimerD.class,
        AutoBlockA.class, AutoBlockB.class, AutoBlockC.class, AutoBlockD.class,
        AutoBlockE.class, AutoBlockF.class, AutoBlockG.class,
        NoSlowB.class, NoSlowG.class,
        BaritoneB.class, BaritoneC.class, BaritoneD.class,
        BadPacketsO.class, BadPacketsW.class, BadPacketsL.class,
        BadPacketsM.class, BadPacketsR.class,
    };

    private static final Class<?>[] POSITION_CLASSES = {
        PredictionHandler.class,
        CompensatedCooldown.class,
        NoSlowG.class,
    };

    private static final Class<?>[] ROTATION_CLASSES = {
        RotateProcessor.class,
        Cinematic.class,
        RotationDebugHandler.class,
        AimA.class, AimC.class, AimD.class, AimE.class, AimF.class,
        AimG.class, AimH.class, AimI.class, AimJ.class, AimK.class,
        AimL.class, AimM.class, AimN.class, AimO.class, AimP.class,
        AimQ.class, AimR.class, AimS.class, AimT.class, AimU.class,
        AimV.class,
        AnalysisA.class, AnalysisB.class, AnalysisD.class, AnalysisE.class,
        AnalysisF.class, AnalysisC.class, AnalysisH.class,
        BaritoneA.class,
    };

    private static final Class<?>[] VEHICLE_CLASSES = {
        VehiclePredictionHandler.class,
    };

    private static final Class<?>[] PRE_VIA_POST_PREDICTION_CLASSES = {
        BadPacketsC.class, BadPacketsS.class, BadPacketsU.class, BadPacketsP.class,
        NoSaddleB.class,
    };

    private static final Class<?>[] POST_PREDICTION_CLASSES = {
        InventoryA.class, InventoryB.class,
        BadPacketsT.class, BadPacketsV.class,
        ElytraA.class, ElytraB.class, ElytraC.class, ElytraD.class, ElytraE.class,
        ElytraF.class, ElytraG.class, ElytraH.class, ElytraI.class, ElytraJ.class,
        ElytraK.class,
        NoSlowC.class, NoSlowD.class, NoSlowE.class, NoSlowF.class,
        VelocityB.class, VelocityA.class, VelocityC.class, VelocityD.class,
        VelocityE.class, VelocityF.class,
        AutoBlock1.class, AutoBlock2.class, AutoBlock3.class, AutoBlock4.class,
        BadPacketsF.class,
        PostCheck.class,
        GroundSpoofB.class, GroundSpoofC.class,
        MovementValidation.class,
        SprintA.class,
        NoSlowA.class,
        GhostBlockDetector.class,
        PredictionDebugHandler.class,
        DebugManager.class,
        SetbackTeleportUtil.class,
        CompensatedFireworks.class,
        SneakingEstimator.class,
        LastInstanceManager.class,
    };

    private static final Class<?>[] BLOCK_PLACE_CLASSES = {
        GhostBlockMitigation.class,
        InventoryD.class,
        ScaffoldH.class, ScaffoldK.class, ScaffoldJ.class, ScaffoldI.class,
        AirPlaceA.class,
        ScaffoldA.class, ScaffoldB.class, ScaffoldC.class, ScaffoldD.class,
        ScaffoldE.class, ScaffoldF.class, ScaffoldG.class,
        BadPacketsX.class,
        FastPlaceA.class,
    };

    private static final Class<?>[] BLOCK_BREAK_CLASSES = {
        FastBreakA.class,
        InvalidBreakA.class, PositionBreakA.class,
        FarBreakA.class,
        WrongBreakA.class, WrongBreakB.class, WrongBreakC.class,
        NoSwingBreakA.class, MultiBreakA.class,
        InteractA.class, InteractB.class,
        InventoryC.class,
    };

    private CheckRegistry() {}

    // --- Instantiation helpers ---

    private static <T extends AbstractCheck> void instantiateInto(ClassLoadingMap<T> map, Class<?>[] classes, PlayerData player) {
        for (Class<?> cls : classes) {
            try {
                Constructor<?> ctor = cls.getDeclaredConstructor(PlayerData.class);
                ctor.setAccessible(true);
                T instance = (T) ctor.newInstance(player);
                map.putAndMoveToLast((Class<T>) cls, instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate check: " + cls.getSimpleName(), e);
            }
        }
    }

    // --- Special entry points for manually-constructed objects ---

    private static void addSpecial(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(ClickProcessor.class, player.getClickProcessor());
        map.putAndMoveToLast(PacketActionProcessor.class, player.getPacketActionProcessor());
        map.putAndMoveToLast(GeyserManager.class, new GeyserManager(player));
    }

    private static void addSpecialPostPrediction(ClassLoadingMap<PostPredictionCheck> map, PlayerData player) {
        map.putAndMoveToLast(CompensatedFireworks.class, player.compensatedFireworks);
        map.putAndMoveToLast(LastInstanceManager.class, player.lastInstanceManager);
    }

    // --- Public API used by CheckManager ---

    public static void fillPacketChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        instantiateInto(map, PACKET_CHECK_CLASSES, player);
        addSpecial(map, player);
    }

    public static void fillPreViaPacketChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        instantiateInto(map, PRE_VIA_PACKET_CLASSES, player);
    }

    public static void fillPrePredictionChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        instantiateInto(map, PRE_PREDICTION_CLASSES, player);
    }

    public static void fillPositionChecks(ClassLoadingMap<PositionCheck> map, PlayerData player) {
        instantiateInto(map, POSITION_CLASSES, player);
    }

    public static void fillRotationChecks(ClassLoadingMap<RotationCheck> map, PlayerData player) {
        instantiateInto(map, ROTATION_CLASSES, player);
    }

    public static void fillVehicleChecks(ClassLoadingMap<VehicleCheck> map, PlayerData player) {
        instantiateInto(map, VEHICLE_CLASSES, player);
    }

    public static void fillPreViaPostPredictionChecks(ClassLoadingMap<PostPredictionCheck> map, PlayerData player) {
        instantiateInto(map, PRE_VIA_POST_PREDICTION_CLASSES, player);
    }

    public static void fillPostPredictionChecks(ClassLoadingMap<PostPredictionCheck> map, PlayerData player) {
        instantiateInto(map, POST_PREDICTION_CLASSES, player);
        addSpecialPostPrediction(map, player);
    }

    public static void fillBlockPlaceChecks(ClassLoadingMap<BlockPlaceCheck> map, PlayerData player) {
        instantiateInto(map, BLOCK_PLACE_CLASSES, player);
    }

    public static void fillBlockBreakChecks(ClassLoadingMap<BlockBreakCheck> map, PlayerData player) {
        instantiateInto(map, BLOCK_BREAK_CLASSES, player);
    }
}