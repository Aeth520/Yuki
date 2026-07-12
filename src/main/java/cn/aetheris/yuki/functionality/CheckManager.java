package cn.aetheris.yuki.functionality;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.impl.combat.aim.*;
import cn.aetheris.yuki.check.impl.combat.analysis.*;
import cn.aetheris.yuki.check.impl.combat.autoblock.*;
import cn.aetheris.yuki.check.impl.combat.autoblock.prediction.AutoBlock1;
import cn.aetheris.yuki.check.impl.combat.autoblock.prediction.AutoBlock2;
import cn.aetheris.yuki.check.impl.combat.autoblock.prediction.AutoBlock3;
import cn.aetheris.yuki.check.impl.combat.autoblock.prediction.AutoBlock4;
import cn.aetheris.yuki.check.impl.combat.killaura.*;
import cn.aetheris.yuki.check.impl.combat.reach.ReachA;
import cn.aetheris.yuki.check.impl.combat.reach.ReachB;
import cn.aetheris.yuki.check.impl.combat.reach.ReachC;
import cn.aetheris.yuki.check.impl.combat.reach.ReachD;
import cn.aetheris.yuki.check.impl.combat.velocity.*;
import cn.aetheris.yuki.check.impl.misc.chat.*;
import cn.aetheris.yuki.check.impl.misc.client.ClientA;
import cn.aetheris.yuki.check.impl.misc.ghostblock.GhostBlockMitigation;
import cn.aetheris.yuki.check.impl.misc.packet.PacketMitigation;
import cn.aetheris.yuki.check.impl.misc.spam.SpamA;
import cn.aetheris.yuki.check.impl.misc.spam.SpamB;
import cn.aetheris.yuki.check.impl.misc.visual.EquipmentHider;
import cn.aetheris.yuki.check.impl.misc.visual.MetaDataHider;
import cn.aetheris.yuki.check.impl.movement.elytra.*;
import cn.aetheris.yuki.check.impl.movement.groundspoof.GroundSpoofA;
import cn.aetheris.yuki.check.impl.movement.groundspoof.GroundSpoofB;
import cn.aetheris.yuki.check.impl.movement.groundspoof.GroundSpoofC;
import cn.aetheris.yuki.check.impl.movement.noslow.*;
import cn.aetheris.yuki.check.impl.movement.movementvalidation.MovementValidation;
import cn.aetheris.yuki.check.impl.movement.sprint.SprintA;
import cn.aetheris.yuki.check.impl.movement.vehicle.fly.VehicleFlyA;
import cn.aetheris.yuki.check.impl.movement.vehicle.fly.VehicleFlyB;
import cn.aetheris.yuki.check.impl.movement.vehicle.nosaddle.NoSaddleA;
import cn.aetheris.yuki.check.impl.player.airplace.AirPlaceA;
import cn.aetheris.yuki.check.impl.player.autoclicker.*;
import cn.aetheris.yuki.check.impl.player.autoclicker.bad.AutoClickerS;
import cn.aetheris.yuki.check.impl.player.autoclicker.bad.AutoClickerT;
import cn.aetheris.yuki.check.impl.player.badpackets.*;
import cn.aetheris.yuki.check.impl.player.badpackets.bad.BadPacketsX;
import cn.aetheris.yuki.check.impl.player.badpackets.bad.BadPacketsY;
import cn.aetheris.yuki.check.impl.player.badpackets.packetorder.*;
import cn.aetheris.yuki.check.impl.player.badpackets.packetorder.processor.PacketActionProcessor;
import cn.aetheris.yuki.check.impl.player.badpackets.switchitem.BadPacketsA;
import cn.aetheris.yuki.check.impl.player.badpackets.switchitem.BadPacketsAA;
import cn.aetheris.yuki.check.impl.player.badpackets.switchitem.BadPacketsI;
import cn.aetheris.yuki.check.impl.player.badpackets.switchitem.BadPacketsV;
import cn.aetheris.yuki.check.impl.player.baritone.BaritoneA;
import cn.aetheris.yuki.check.impl.player.baritone.BaritoneB;
import cn.aetheris.yuki.check.impl.player.baritone.BaritoneC;
import cn.aetheris.yuki.check.impl.player.baritone.BaritoneD;
import cn.aetheris.yuki.check.impl.player.blink.BlinkA;
import cn.aetheris.yuki.check.impl.player.breaking.far.FarBreakA;
import cn.aetheris.yuki.check.impl.player.breaking.fast.FastBreakA;
import cn.aetheris.yuki.check.impl.player.breaking.fast.FastBreakB;
import cn.aetheris.yuki.check.impl.player.breaking.fast.FastBreakC;
import cn.aetheris.yuki.check.impl.player.breaking.invalid.InvalidBreakA;
import cn.aetheris.yuki.check.impl.player.breaking.multi.MultiBreakA;
import cn.aetheris.yuki.check.impl.player.breaking.position.PositionBreakA;
import cn.aetheris.yuki.check.impl.player.breaking.wrong.NoSwingBreakA;
import cn.aetheris.yuki.check.impl.player.breaking.wrong.WrongBreakA;
import cn.aetheris.yuki.check.impl.player.breaking.wrong.WrongBreakB;
import cn.aetheris.yuki.check.impl.player.breaking.wrong.WrongBreakC;
import cn.aetheris.yuki.check.impl.player.crash.*;
import cn.aetheris.yuki.check.impl.movement.vehicle.nosaddle.NoSaddleB;
import cn.aetheris.yuki.check.impl.player.exploit.*;
import cn.aetheris.yuki.check.impl.player.fastplace.FastPlaceA;
import cn.aetheris.yuki.check.impl.player.impossible.*;
import cn.aetheris.yuki.check.impl.player.multiactions.*;
import cn.aetheris.yuki.check.impl.player.interact.InteractA;
import cn.aetheris.yuki.check.impl.player.interact.InteractB;
import cn.aetheris.yuki.check.impl.player.inventory.*;
import cn.aetheris.yuki.check.impl.player.inventory.prediction.InventoryA;
import cn.aetheris.yuki.check.impl.player.inventory.prediction.InventoryB;
import cn.aetheris.yuki.check.impl.player.pingspoof.*;
import cn.aetheris.yuki.check.impl.player.post.PostCheck;
import cn.aetheris.yuki.check.impl.player.scaffold.*;
import cn.aetheris.yuki.check.impl.player.scaffold.invalid.ScaffoldH;
import cn.aetheris.yuki.check.impl.player.scaffold.invalid.ScaffoldI;
import cn.aetheris.yuki.check.impl.player.scaffold.invalid.ScaffoldJ;
import cn.aetheris.yuki.check.impl.player.scaffold.invalid.ScaffoldK;
import cn.aetheris.yuki.check.impl.player.timer.*;
import cn.aetheris.yuki.check.impl.player.xray.XRayA;
import cn.aetheris.yuki.check.type.*;
import cn.aetheris.yuki.check.util.handler.*;
import cn.aetheris.yuki.check.util.processor.clickprocessor.ClickProcessor;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.Cinematic;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.listener.packets.PacketChangeGameState;
import cn.aetheris.yuki.listener.packets.PacketEntityReplication;
import cn.aetheris.yuki.listener.packets.PacketWorldBorder;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.GhostBlockDetector;
import cn.aetheris.yuki.predictionengine.SneakingEstimator;
import cn.aetheris.yuki.util.latency.CompensatedCooldown;
import cn.aetheris.yuki.util.latency.CompensatedFireworks;
import cn.aetheris.yuki.util.latency.CompensatedInventory;
import cn.aetheris.yuki.util.maps.ClassLoadingMap;
import cn.aetheris.yuki.util.team.TeamHandler;
import cn.aetheris.yuki.util.update.*;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.function.Consumer;

public final class CheckManager {

    private final ClassLoadingMap<PostPredictionCheck> preViaPostPredictionChecks;
    private final ClassLoadingMap<PacketCheck> preViaPacketChecks;
    private final ClassLoadingMap<PacketCheck> packetChecks;
    private final ClassLoadingMap<PositionCheck> positionCheck;
    private final ClassLoadingMap<RotationCheck> rotationCheck;
    private final ClassLoadingMap<VehicleCheck> vehicleCheck;
    private final ClassLoadingMap<PacketCheck> prePredictionChecks;
    private final ClassLoadingMap<BlockBreakCheck> blockBreakChecks;
    private final ClassLoadingMap<BlockPlaceCheck> blockPlaceCheck;
    private final ClassLoadingMap<PostPredictionCheck> postPredictionCheck;
    public ClassLoadingMap<AbstractCheck> allChecks;
    private boolean inited;
    private PacketEntityReplication packetEntityReplication;
    private CompensatedInventory inventory;

    
    public CheckManager(PlayerData player) {
        this.packetChecks = initializePacketChecks(player);
        this.preViaPacketChecks = initializePreViaPacketChecks(player);
        this.positionCheck = initializePositionCheck(player);
        this.rotationCheck = initializeRotationCheck(player);
        this.vehicleCheck = initializeVehicleCheck(player);
        this.preViaPostPredictionChecks = initializePreViaPostPredictionChecks(player);
        this.postPredictionCheck = initializePostPredictionCheck(player);
        this.prePredictionChecks = initializePredictionChecks(player);
        this.blockPlaceCheck = initializeBlockPlaceCheck(player);
        this.blockBreakChecks = initializeBlockBreakChecks(player);

        this.allChecks = buildAllChecks();

        this.init();
    }


    
    private ClassLoadingMap<PacketCheck> initializePredictionChecks(PlayerData player) {
        ClassLoadingMap<PacketCheck> map = new ClassLoadingMap<>(null);
        map.putAndMoveToLast(ImpossibleA.class, new ImpossibleA(player));
        map.putAndMoveToLast(TimerA.class, new TimerA(player));
        map.putAndMoveToLast(TimerAA.class, new TimerAA(player));
        map.putAndMoveToLast(TimerB.class, new TimerB(player));
        map.putAndMoveToLast(TimerD.class, new TimerD(player));

        
        map.putAndMoveToLast(AutoBlockA.class, new AutoBlockA(player));
        map.putAndMoveToLast(AutoBlockB.class, new AutoBlockB(player));
        map.putAndMoveToLast(AutoBlockC.class, new AutoBlockC(player));
        map.putAndMoveToLast(AutoBlockD.class, new AutoBlockD(player));
        map.putAndMoveToLast(AutoBlockE.class, new AutoBlockE(player));
        map.putAndMoveToLast(AutoBlockF.class, new AutoBlockF(player));
        map.putAndMoveToLast(AutoBlockG.class, new AutoBlockG(player));

        map.putAndMoveToLast(NoSlowB.class, new NoSlowB(player));
        map.putAndMoveToLast(NoSlowG.class, new NoSlowG(player));

        map.putAndMoveToLast(BaritoneB.class, new BaritoneB(player));
        map.putAndMoveToLast(BaritoneC.class, new BaritoneC(player));
        map.putAndMoveToLast(BaritoneD.class, new BaritoneD(player));


        map.putAndMoveToLast(BadPacketsO.class, new BadPacketsO(player));
        map.putAndMoveToLast(BadPacketsW.class, new BadPacketsW(player));
        map.putAndMoveToLast(BadPacketsL.class, new BadPacketsL(player));
        map.putAndMoveToLast(BadPacketsM.class, new BadPacketsM(player));
        map.putAndMoveToLast(BadPacketsR.class, new BadPacketsR(player));
        return map;
    }


    private ClassLoadingMap<PacketCheck> initializePreViaPacketChecks(PlayerData player) {
        ClassLoadingMap<PacketCheck> map = new ClassLoadingMap<>(null);
        map.putAndMoveToLast(SpamA.class, new SpamA(player));
        map.putAndMoveToLast(SpamB.class, new SpamB(player));

        map.putAndMoveToLast(BadPacketsJ.class, new BadPacketsJ(player));
        map.putAndMoveToLast(BadPacketsK.class, new BadPacketsK(player));

        map.putAndMoveToLast(BadPacketsN.class, new BadPacketsN(player));
        map.putAndMoveToLast(BadPacketsQ.class, new BadPacketsQ(player));
        map.putAndMoveToLast(BadPacketsI.class, new BadPacketsI(player));

        return map;
    }


    private ClassLoadingMap<PacketCheck> initializePacketChecks(PlayerData player) {
        ClassLoadingMap<PacketCheck> map = new ClassLoadingMap<>(null);
        registerReachChecks(map, player);
        registerPacketHandlers(map, player);
        registerGroundSpoofChecks(map, player);
        registerFastBreakChecks(map, player);
        registerClientChecks(map, player);
        registerChatChecks(map, player);
        registerImpossibleChecks(map, player);
        registerExploitChecks(map, player);
        registerCrashChecks(map, player);
        registerPingSpoofChecks(map, player);
        registerInventoryChecks(map, player);
        registerAutoClickerChecks(map, player);
        registerKillAuraChecks(map, player);
        registerVehicleChecks(map, player);
        registerMiscPacketChecks(map, player);
        registerBadPacketsChecks(map, player);
        registerMitigationChecks(map, player);
        registerMultiActionsChecks(map, player);
        return map;
    }

    private void registerReachChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(ReachA.class, new ReachA(player));
        map.putAndMoveToLast(ReachB.class, new ReachB(player));
        map.putAndMoveToLast(ReachC.class, new ReachC(player));
        map.putAndMoveToLast(ReachD.class, new ReachD(player));
    }

    private void registerPacketHandlers(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(PacketEntityReplication.class, new PacketEntityReplication(player));
        map.putAndMoveToLast(PayloadHandler.class, new PayloadHandler(player));
        map.putAndMoveToLast(PacketChangeGameState.class, new PacketChangeGameState(player));
        map.putAndMoveToLast(CompensatedInventory.class, new CompensatedInventory(player));
        map.putAndMoveToLast(AbilitiesHandler.class, new AbilitiesHandler(player));
        map.putAndMoveToLast(PacketWorldBorder.class, new PacketWorldBorder(player));
        map.putAndMoveToLast(TeamHandler.class, new TeamHandler(player));
        map.putAndMoveToLast(ClickProcessor.class, player.getClickProcessor());
        map.putAndMoveToLast(PacketActionProcessor.class, player.getPacketActionProcessor());
    }

    private void registerGroundSpoofChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(GroundSpoofA.class, new GroundSpoofA(player));
    }

    private void registerFastBreakChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(FastBreakB.class, new FastBreakB(player));
        map.putAndMoveToLast(FastBreakC.class, new FastBreakC(player));
    }

    private void registerClientChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(ClientA.class, new ClientA(player));
    }

    private void registerChatChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(ChatA.class, new ChatA(player));
        map.putAndMoveToLast(ChatB.class, new ChatB(player));
        map.putAndMoveToLast(ChatC.class, new ChatC(player));
        map.putAndMoveToLast(ChatD.class, new ChatD(player));
    }

    private void registerImpossibleChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(ImpossibleB.class, new ImpossibleB(player));
        map.putAndMoveToLast(ImpossibleC.class, new ImpossibleC(player));
        map.putAndMoveToLast(ImpossibleD.class, new ImpossibleD(player));
        map.putAndMoveToLast(ImpossibleE.class, new ImpossibleE(player));
        map.putAndMoveToLast(ImpossibleF.class, new ImpossibleF(player));
        map.putAndMoveToLast(ImpossibleG.class, new ImpossibleG(player));
        map.putAndMoveToLast(ImpossibleH.class, new ImpossibleH(player));
        map.putAndMoveToLast(ImpossibleI.class, new ImpossibleI(player));
        map.putAndMoveToLast(ImpossibleJ.class, new ImpossibleJ(player));
        map.putAndMoveToLast(ImpossibleK.class, new ImpossibleK(player));
        map.putAndMoveToLast(ImpossibleL.class, new ImpossibleL(player));
    }

    private void registerExploitChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(ExploitA.class, new ExploitA(player));
        map.putAndMoveToLast(ExploitB.class, new ExploitB(player));
        map.putAndMoveToLast(ExploitC.class, new ExploitC(player));
        map.putAndMoveToLast(ExploitD.class, new ExploitD(player));
        map.putAndMoveToLast(ExploitE.class, new ExploitE(player));
        map.putAndMoveToLast(ExploitF.class, new ExploitF(player));
        map.putAndMoveToLast(ExploitG.class, new ExploitG(player));
    }

    private void registerCrashChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(CrashA.class, new CrashA(player));
        map.putAndMoveToLast(CrashB.class, new CrashB(player));
        map.putAndMoveToLast(CrashC.class, new CrashC(player));
        map.putAndMoveToLast(CrashD.class, new CrashD(player));
        map.putAndMoveToLast(CrashE.class, new CrashE(player));
        map.putAndMoveToLast(CrashF.class, new CrashF(player));
        map.putAndMoveToLast(CrashG.class, new CrashG(player));
        map.putAndMoveToLast(CrashH.class, new CrashH(player));
        map.putAndMoveToLast(CrashI.class, new CrashI(player));
        map.putAndMoveToLast(CrashJ.class, new CrashJ(player));
        map.putAndMoveToLast(CrashK.class, new CrashK(player));
        map.putAndMoveToLast(CrashL.class, new CrashL(player));
        map.putAndMoveToLast(CrashM.class, new CrashM(player));
    }

    private void registerPingSpoofChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(PingSpoofA.class, new PingSpoofA(player));
        map.putAndMoveToLast(PingSpoofB.class, new PingSpoofB(player));
        map.putAndMoveToLast(PingSpoofC.class, new PingSpoofC(player));
        map.putAndMoveToLast(PingSpoofD.class, new PingSpoofD(player));
        map.putAndMoveToLast(PingSpoofE.class, new PingSpoofE(player));
        map.putAndMoveToLast(PingSpoofF.class, new PingSpoofF(player));
    }

    private void registerInventoryChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(InventoryH.class, new InventoryH(player));
        map.putAndMoveToLast(InventoryE.class, new InventoryE(player));
        map.putAndMoveToLast(InventoryF.class, new InventoryF(player));
        map.putAndMoveToLast(InventoryG.class, new InventoryG(player));
        map.putAndMoveToLast(InventoryI.class, new InventoryI(player));
        map.putAndMoveToLast(InventoryJ.class, new InventoryJ(player));
        map.putAndMoveToLast(InventoryK.class, new InventoryK(player));
        map.putAndMoveToLast(InventoryL.class, new InventoryL(player));
        map.putAndMoveToLast(InventoryM.class, new InventoryM(player));
        map.putAndMoveToLast(InventoryN.class, new InventoryN(player));
        map.putAndMoveToLast(BadPacketsG.class, new BadPacketsG(player));
    }

    private void registerAutoClickerChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(AutoClickerS.class, new AutoClickerS(player));
        map.putAndMoveToLast(AutoClickerT.class, new AutoClickerT(player));
        map.putAndMoveToLast(AutoClickerA.class, new AutoClickerA(player));
        map.putAndMoveToLast(AutoClickerB.class, new AutoClickerB(player));
        map.putAndMoveToLast(AutoClickerC.class, new AutoClickerC(player));
        map.putAndMoveToLast(AutoClickerD.class, new AutoClickerD(player));
        map.putAndMoveToLast(AutoClickerE.class, new AutoClickerE(player));
        map.putAndMoveToLast(AutoClickerF.class, new AutoClickerF(player));
        map.putAndMoveToLast(AutoClickerG.class, new AutoClickerG(player));
        map.putAndMoveToLast(AutoClickerH.class, new AutoClickerH(player));
        map.putAndMoveToLast(AutoClickerI.class, new AutoClickerI(player));
        map.putAndMoveToLast(AutoClickerJ.class, new AutoClickerJ(player));
        map.putAndMoveToLast(AutoClickerK.class, new AutoClickerK(player));
        map.putAndMoveToLast(AutoClickerL.class, new AutoClickerL(player));
        map.putAndMoveToLast(AutoClickerM.class, new AutoClickerM(player));
        map.putAndMoveToLast(AutoClickerN.class, new AutoClickerN(player));
        map.putAndMoveToLast(AutoClickerO.class, new AutoClickerO(player));
        map.putAndMoveToLast(AutoClickerP.class, new AutoClickerP(player));
        map.putAndMoveToLast(AutoClickerQ.class, new AutoClickerQ(player));
    }

    private void registerKillAuraChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(KillAuraA.class, new KillAuraA(player));
        map.putAndMoveToLast(KillAuraB.class, new KillAuraB(player));
        map.putAndMoveToLast(KillAuraC.class, new KillAuraC(player));
        map.putAndMoveToLast(KillAuraD.class, new KillAuraD(player));
        map.putAndMoveToLast(KillAuraE.class, new KillAuraE(player));
        map.putAndMoveToLast(KillAuraF.class, new KillAuraF(player));
        map.putAndMoveToLast(KillAuraG.class, new KillAuraG(player));
        map.putAndMoveToLast(KillAuraH.class, new KillAuraH(player));
        map.putAndMoveToLast(KillAuraI.class, new KillAuraI(player));
        map.putAndMoveToLast(KillAuraJ.class, new KillAuraJ(player));
        map.putAndMoveToLast(KillAuraK.class, new KillAuraK(player));
        map.putAndMoveToLast(KillAuraL.class, new KillAuraL(player));
        map.putAndMoveToLast(KillAuraM.class, new KillAuraM(player));
        map.putAndMoveToLast(AnalysisG.class, new AnalysisG(player));
    }

    private void registerVehicleChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(NoSaddleA.class, new NoSaddleA(player));
        map.putAndMoveToLast(VehicleFlyA.class, new VehicleFlyA(player));
        map.putAndMoveToLast(VehicleFlyB.class, new VehicleFlyB(player));
    }

    private void registerMiscPacketChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(XRayA.class, new XRayA(player));
        map.putAndMoveToLast(BlinkA.class, new BlinkA(player));
        map.putAndMoveToLast(TimerC.class, new TimerC(player));
        map.putAndMoveToLast(GeyserManager.class, new GeyserManager(player));
    }

    private void registerBadPacketsChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(BadPacketsA.class, new BadPacketsA(player));
        map.putAndMoveToLast(BadPacketsAA.class, new BadPacketsAA(player));
        map.putAndMoveToLast(BadPacketsB.class, new BadPacketsB(player));
        map.putAndMoveToLast(BadPacketsD.class, new BadPacketsD(player));
        map.putAndMoveToLast(BadPacketsE.class, new BadPacketsE(player));
        map.putAndMoveToLast(BadPacketsH.class, new BadPacketsH(player));
        map.putAndMoveToLast(BadPacketsY.class, new BadPacketsY(player));
    }

    private void registerMitigationChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(PacketMitigation.class, new PacketMitigation(player));
        map.putAndMoveToLast(MetaDataHider.class, new MetaDataHider(player));
        map.putAndMoveToLast(EquipmentHider.class, new EquipmentHider(player));
        map.putAndMoveToLast(CancelHandler.class, new CancelHandler(player));
    }

    private void registerMultiActionsChecks(ClassLoadingMap<PacketCheck> map, PlayerData player) {
        map.putAndMoveToLast(MultiActionsA.class, new MultiActionsA(player));
        map.putAndMoveToLast(MultiActionsB.class, new MultiActionsB(player));
        map.putAndMoveToLast(MultiActionsC.class, new MultiActionsC(player));
        map.putAndMoveToLast(MultiActionsD.class, new MultiActionsD(player));
        map.putAndMoveToLast(MultiActionsE.class, new MultiActionsE(player));
        map.putAndMoveToLast(MultiActionsF.class, new MultiActionsF(player));
        map.putAndMoveToLast(MultiActionsG.class, new MultiActionsG(player));
    }

    

    private ClassLoadingMap<PositionCheck> initializePositionCheck(PlayerData player) {
        ClassLoadingMap<PositionCheck> map = new ClassLoadingMap<>(null);

        map.putAndMoveToLast(PredictionHandler.class, new PredictionHandler(player));

        map.putAndMoveToLast(CompensatedCooldown.class, new CompensatedCooldown(player));

        map.putAndMoveToLast(NoSlowG.class, new NoSlowG(player));

        return map;
    }

    

    private ClassLoadingMap<RotationCheck> initializeRotationCheck(PlayerData player) {
        ClassLoadingMap<RotationCheck> map = new ClassLoadingMap<>(null);

        map.putAndMoveToLast(RotateProcessor.class, new RotateProcessor(player));
        map.putAndMoveToLast(Cinematic.class, new Cinematic(player));
        map.putAndMoveToLast(RotationDebugHandler.class, new RotationDebugHandler(player));

        map.putAndMoveToLast(AimA.class, new AimA(player));
        
        map.putAndMoveToLast(AimC.class, new AimC(player));
        map.putAndMoveToLast(AimD.class, new AimD(player));
        map.putAndMoveToLast(AimE.class, new AimE(player));
        map.putAndMoveToLast(AimF.class, new AimF(player));
        map.putAndMoveToLast(AimG.class, new AimG(player));
        map.putAndMoveToLast(AimH.class, new AimH(player));
        map.putAndMoveToLast(AimI.class, new AimI(player));
        map.putAndMoveToLast(AimJ.class, new AimJ(player));
        map.putAndMoveToLast(AimK.class, new AimK(player));
        map.putAndMoveToLast(AimL.class, new AimL(player));
        map.putAndMoveToLast(AimM.class, new AimM(player));
        map.putAndMoveToLast(AimN.class, new AimN(player));
        map.putAndMoveToLast(AimO.class, new AimO(player));
        map.putAndMoveToLast(AimP.class, new AimP(player));
        map.putAndMoveToLast(AimQ.class, new AimQ(player));
        map.putAndMoveToLast(AimR.class, new AimR(player));
        map.putAndMoveToLast(AimS.class, new AimS(player));
        map.putAndMoveToLast(AimT.class, new AimT(player));
        map.putAndMoveToLast(AimU.class, new AimU(player));
        map.putAndMoveToLast(AimV.class, new AimV(player));

        
        map.putAndMoveToLast(AnalysisA.class, new AnalysisA(player));
        map.putAndMoveToLast(AnalysisB.class, new AnalysisB(player));
        map.putAndMoveToLast(AnalysisD.class, new AnalysisD(player));
        map.putAndMoveToLast(AnalysisE.class, new AnalysisE(player));
        map.putAndMoveToLast(AnalysisF.class, new AnalysisF(player));
        map.putAndMoveToLast(AnalysisC.class, new AnalysisC(player));
        map.putAndMoveToLast(AnalysisH.class, new AnalysisH(player));

        
        map.putAndMoveToLast(BaritoneA.class, new BaritoneA(player));

        return map;
    }

    

    private ClassLoadingMap<VehicleCheck> initializeVehicleCheck(PlayerData player) {
        ClassLoadingMap<VehicleCheck> map = new ClassLoadingMap<>(null);

        map.putAndMoveToLast(VehiclePredictionHandler.class, new VehiclePredictionHandler(player));

        return map;
    }

    
    private ClassLoadingMap<PostPredictionCheck> initializePreViaPostPredictionChecks(PlayerData player) {
        ClassLoadingMap<PostPredictionCheck> map = new ClassLoadingMap<>(null);

        map.putAndMoveToLast(BadPacketsC.class, new BadPacketsC(player));
        map.putAndMoveToLast(BadPacketsS.class, new BadPacketsS(player));
        map.putAndMoveToLast(BadPacketsU.class, new BadPacketsU(player));
        map.putAndMoveToLast(BadPacketsP.class, new BadPacketsP(player));

        map.putAndMoveToLast(NoSaddleB.class, new NoSaddleB(player));
        return map;
    }

    

    private ClassLoadingMap<PostPredictionCheck> initializePostPredictionCheck(PlayerData player) {
        ClassLoadingMap<PostPredictionCheck> map = new ClassLoadingMap<>(null);

        map.putAndMoveToLast(InventoryA.class, new InventoryA(player));
        map.putAndMoveToLast(InventoryB.class, new InventoryB(player));

        map.putAndMoveToLast(BadPacketsT.class, new BadPacketsT(player));
        map.putAndMoveToLast(BadPacketsV.class, new BadPacketsV(player));

        map.putAndMoveToLast(ElytraA.class, new ElytraA(player));
        map.putAndMoveToLast(ElytraB.class, new ElytraB(player));
        map.putAndMoveToLast(ElytraC.class, new ElytraC(player));
        map.putAndMoveToLast(ElytraD.class, new ElytraD(player));
        map.putAndMoveToLast(ElytraE.class, new ElytraE(player));
        map.putAndMoveToLast(ElytraF.class, new ElytraF(player));
        map.putAndMoveToLast(ElytraG.class, new ElytraG(player));
        map.putAndMoveToLast(ElytraH.class, new ElytraH(player));
        map.putAndMoveToLast(ElytraI.class, new ElytraI(player));
        map.putAndMoveToLast(ElytraJ.class, new ElytraJ(player));
        map.putAndMoveToLast(ElytraK.class, new ElytraK(player));

        map.putAndMoveToLast(NoSlowC.class, new NoSlowC(player));
        map.putAndMoveToLast(NoSlowD.class, new NoSlowD(player));
        map.putAndMoveToLast(NoSlowE.class, new NoSlowE(player));
        map.putAndMoveToLast(NoSlowF.class, new NoSlowF(player));

        map.putAndMoveToLast(VelocityB.class, new VelocityB(player));
        map.putAndMoveToLast(VelocityA.class, new VelocityA(player));
        map.putAndMoveToLast(VelocityC.class, new VelocityC(player));
        map.putAndMoveToLast(VelocityD.class, new VelocityD(player));
        map.putAndMoveToLast(VelocityE.class, new VelocityE(player));
        map.putAndMoveToLast(VelocityF.class, new VelocityF(player));

        map.putAndMoveToLast(AutoBlock1.class, new AutoBlock1(player));
        map.putAndMoveToLast(AutoBlock2.class, new AutoBlock2(player));
        map.putAndMoveToLast(AutoBlock3.class, new AutoBlock3(player));
        map.putAndMoveToLast(AutoBlock4.class, new AutoBlock4(player));

        map.putAndMoveToLast(BadPacketsF.class, new BadPacketsF(player));

        map.putAndMoveToLast(PostCheck.class, new PostCheck(player));

        map.putAndMoveToLast(GroundSpoofB.class, new GroundSpoofB(player));
        map.putAndMoveToLast(GroundSpoofC.class, new GroundSpoofC(player));

        map.putAndMoveToLast(MovementValidation.class, new MovementValidation(player));

        map.putAndMoveToLast(SprintA.class, new SprintA(player));

        map.putAndMoveToLast(NoSlowA.class, new NoSlowA(player));

        map.putAndMoveToLast(GhostBlockDetector.class, new GhostBlockDetector(player));
        map.putAndMoveToLast(PredictionDebugHandler.class, new PredictionDebugHandler(player));
        map.putAndMoveToLast(DebugManager.class, new DebugManager(player));

        map.putAndMoveToLast(SetbackTeleportUtil.class, new SetbackTeleportUtil(player));
        map.putAndMoveToLast(CompensatedFireworks.class, player.compensatedFireworks);
        map.putAndMoveToLast(SneakingEstimator.class, new SneakingEstimator(player));
        map.putAndMoveToLast(LastInstanceManager.class, player.lastInstanceManager);

        return map;
    }

    

    private ClassLoadingMap<BlockPlaceCheck> initializeBlockPlaceCheck(PlayerData player) {
        ClassLoadingMap<BlockPlaceCheck> map = new ClassLoadingMap<>(null);

        map.putAndMoveToLast(GhostBlockMitigation.class, new GhostBlockMitigation(player));

        map.putAndMoveToLast(InventoryD.class, new InventoryD(player));

        map.putAndMoveToLast(ScaffoldH.class, new ScaffoldH(player));
        map.putAndMoveToLast(ScaffoldK.class, new ScaffoldK(player));
        map.putAndMoveToLast(ScaffoldJ.class, new ScaffoldJ(player));
        map.putAndMoveToLast(ScaffoldI.class, new ScaffoldI(player));
        map.putAndMoveToLast(AirPlaceA.class, new AirPlaceA(player));
        map.putAndMoveToLast(ScaffoldA.class, new ScaffoldA(player));
        map.putAndMoveToLast(ScaffoldB.class, new ScaffoldB(player));
        map.putAndMoveToLast(ScaffoldC.class, new ScaffoldC(player));
        map.putAndMoveToLast(ScaffoldD.class, new ScaffoldD(player));
        map.putAndMoveToLast(ScaffoldE.class, new ScaffoldE(player));
        map.putAndMoveToLast(ScaffoldF.class, new ScaffoldF(player));
        map.putAndMoveToLast(ScaffoldG.class, new ScaffoldG(player));

        map.putAndMoveToLast(BadPacketsX.class, new BadPacketsX(player));

        map.putAndMoveToLast(FastPlaceA.class, new FastPlaceA(player));

        return map;
    }

    

    private ClassLoadingMap<BlockBreakCheck> initializeBlockBreakChecks(PlayerData player) {
        ClassLoadingMap<BlockBreakCheck> map = new ClassLoadingMap<>(null);

        map.putAndMoveToLast(FastBreakA.class, new FastBreakA(player));

        map.putAndMoveToLast(InvalidBreakA.class, new InvalidBreakA(player));
        map.putAndMoveToLast(PositionBreakA.class, new PositionBreakA(player));
        
        map.putAndMoveToLast(FarBreakA.class, new FarBreakA(player));
        map.putAndMoveToLast(WrongBreakA.class, new WrongBreakA(player));
        map.putAndMoveToLast(WrongBreakB.class, new WrongBreakB(player));
        map.putAndMoveToLast(WrongBreakC.class, new WrongBreakC(player));

        map.putAndMoveToLast(NoSwingBreakA.class, new NoSwingBreakA(player));
        map.putAndMoveToLast(MultiBreakA.class, new MultiBreakA(player));

        map.putAndMoveToLast(InteractA.class, new InteractA(player));
        map.putAndMoveToLast(InteractB.class, new InteractB(player));

        map.putAndMoveToLast(InventoryC.class, new InventoryC(player));

        return map;
    }

    
    private ClassLoadingMap<AbstractCheck> buildAllChecks() {
        ClassLoadingMap<AbstractCheck> allChecks = new ClassLoadingMap<>(null);

        allChecks.putAll(positionCheck);
        allChecks.putAll(rotationCheck);
        allChecks.putAll(vehicleCheck);
        allChecks.putAll(postPredictionCheck);
        allChecks.putAll(preViaPostPredictionChecks);
        allChecks.putAll(prePredictionChecks);
        allChecks.putAll(blockPlaceCheck);
        allChecks.putAll(blockBreakChecks);
        allChecks.putAll(packetChecks);
        allChecks.putAll(preViaPacketChecks);

        return allChecks;
    }


    
    public <T extends PositionCheck> T getPositionCheck(Class<T> check) {
        return (T) positionCheck.get(check);
    }

    
    public <T extends RotationCheck> T getRotationCheck(Class<T> check) {
        return (T) rotationCheck.get(check);
    }

    
    public <T extends BlockBreakCheck> T getBlockBreakChecks(Class<T> check) {
        return (T) blockBreakChecks.get(check);
    }

    
    public void onPrePredictionReceivePacket(final PacketReceiveEvent packet) {
        Consumer<PacketCheck> prePredictionConsumer = i -> i.onPacketReceive(packet);
        prePredictionChecks.forEachValue(prePredictionConsumer);
        blockBreakChecks.forEachValue(prePredictionConsumer);

    }

    
    public void onBlockBreak(final BlockBreak blockBreak) {
        Consumer<BlockBreakCheck> consumer = i -> i.onBlockBreak(blockBreak);
        blockBreakChecks.forEachValue(consumer);
    }

    
    public void onPostFlyingBlockBreak(final BlockBreak blockBreak) {
        Consumer<BlockBreakCheck> consumer = i -> i.onPostFlyingBlockBreak(blockBreak);
        blockBreakChecks.forEachValue(consumer);
    }

    
    public void onPacketReceive(final PacketReceiveEvent packet) {
        Consumer<PacketCheck> packetCheckConsumer = i -> i.onPacketReceive(packet);
        packetChecks.forEachValue(packetCheckConsumer);
        preViaPacketChecks.forEachValue(packetCheckConsumer);
        postPredictionCheck.forEachValue(packetCheckConsumer);
        preViaPostPredictionChecks.forEachValue(packetCheckConsumer);
        blockPlaceCheck.forEachValue(packetCheckConsumer);
    }

    public void onPreViaPacketReceive(final PacketReceiveEvent packet) {
        Consumer<PacketCheck> packetCheckConsumer = i -> i.onPacketReceive(packet);
        preViaPacketChecks.forEachValue(packetCheckConsumer);
        preViaPostPredictionChecks.forEachValue(packetCheckConsumer);
        blockBreakChecks.forEachValue(packetCheckConsumer);
    }

    
    public void onPacketSend(final PacketSendEvent packet) {
        Consumer<PacketCheck> packetCheckConsumer = i -> i.onPacketSend(packet);
        prePredictionChecks.forEachValue(packetCheckConsumer);
        packetChecks.forEachValue(packetCheckConsumer);
        postPredictionCheck.forEachValue(packetCheckConsumer);
    }

    
    public void onPreViaPacketSend(final PacketSendEvent packet) {
        Consumer<PacketCheck> packetCheckConsumer = i -> i.onPacketSend(packet);
        preViaPacketChecks.forEachValue(packetCheckConsumer);
        preViaPostPredictionChecks.forEachValue(packetCheckConsumer);
        blockBreakChecks.forEachValue(packetCheckConsumer);
    }

    
    public void onPositionUpdate(final PositionUpdate position) {
        Consumer<PositionCheck> consumer = check -> check.onPositionUpdate(position);
        positionCheck.forEachValue(consumer);
    }

    
    public void onRotationUpdate(final RotationUpdate rotation) {
        Consumer<RotationCheck> rotationConsumer = check -> check.process(rotation);
        rotationCheck.forEachValue(rotationConsumer);
        blockPlaceCheck.forEachValue(rotationConsumer);
    }

    
    public void onVehiclePositionUpdate(final VehiclePositionUpdate update) {
        Consumer<VehicleCheck> consumer = check -> check.process(update);
        vehicleCheck.forEachValue(consumer);
    }

    
    public void onPredictionFinish(final PredictionComplete complete) {
        Consumer<PostPredictionCheck> postConsumer = check -> check.onPredictionComplete(complete);
        postPredictionCheck.forEachValue(postConsumer);
        blockPlaceCheck.forEachValue(postConsumer);
        preViaPostPredictionChecks.forEachValue(postConsumer);
    }

    
    public void onBlockPlace(final BlockPlace place) {
        Consumer<BlockPlaceCheck> consumer = check -> check.onBlockPlace(place);
        blockPlaceCheck.forEachValue(consumer);
    }

    
    public void onPostFlyingBlockPlace(final BlockPlace place) {
        Consumer<BlockPlaceCheck> consumer = check -> check.onPostFlyingBlockPlace(place);
        blockPlaceCheck.forEachValue(consumer);
    }


    @SuppressWarnings("unchecked")
    public <T extends AbstractCheck> T getCheck(Class<T> check) {
        return (T) allChecks.get(check);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractCheck> Collection<T> getChecks(CheckType type) {
        List<AbstractCheck> list = new ArrayList<>();
        for (AbstractCheck check : allChecks.values()) {
            if (check.getCheckType() == type) {
                list.add(check);
            }
        }
        return (Collection<T>) list;
    }

    
    public VelocityB getExplosionHandler() {
        return getCheck(VelocityB.class);
    }

    
    public <T extends PacketCheck> T getPacketCheck(Class<T> check) {
        return (T) packetChecks.get(check);
    }

    
    public <T extends BlockPlaceCheck> T getBlockPlaceCheck(Class<T> check) {
        return (T) blockPlaceCheck.get(check);
    }

    
    public <T extends PacketCheck> T getPrePredictionCheck(Class<T> check) {
        return (T) prePredictionChecks.get(check);
    }

    
    public PacketEntityReplication getEntityReplication() {
        if (packetEntityReplication == null) {
            packetEntityReplication = getCheck(PacketEntityReplication.class);
        }
        return packetEntityReplication;
    }

    
    public CompensatedInventory getInventory() {
        if (inventory == null) {
            inventory = getCheck(CompensatedInventory.class);
        }
        return inventory;
    }

    
    public VelocityA getKnockbackHandler() {
        return getCheck(VelocityA.class);
    }

    
    public CompensatedCooldown getCompensatedCooldown() {
        return getPositionCheck(CompensatedCooldown.class);
    }

    
    public NoSlowA getNoSlow() {
        return getCheck(NoSlowA.class);
    }

    
    public SetbackTeleportUtil getSetbackUtil() {
        return getCheck(SetbackTeleportUtil.class);
    }

    
    public PredictionDebugHandler getMotionDebugHandler() {
        return getCheck(PredictionDebugHandler.class);
    }

    
    public RotationDebugHandler getRotationDebugHandler() {
        return getRotationCheck(RotationDebugHandler.class);
    }

    
    public MovementValidation getMovementValidation() {
        return getCheck(MovementValidation.class);
    }

    
    public <T extends PostPredictionCheck> T getPostPredictionCheck(Class<T> check) {
        return (T) postPredictionCheck.get(check);
    }

    

    private void init() {
        if (inited) {
            return;
        }
        inited = true;
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            Set<String> permissionNames = new HashSet<>();
            for (AbstractCheck check : allChecks.values()) {
                String configName = check.getConfigName();
                if (configName != null && !configName.isEmpty()) {
                    permissionNames.add("yuki.exempt." + configName.toLowerCase());
                }
            }


            PluginManager pluginManager = Bukkit.getPluginManager();
            for (String permissionName : permissionNames) {
                try {
                    Permission permission = pluginManager.getPermission(permissionName);
                    if (permission == null) {
                        pluginManager.addPermission(new Permission(permissionName, PermissionDefault.FALSE));
                    } else {
                        permission.setDefault(PermissionDefault.FALSE);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        });
    }
}
