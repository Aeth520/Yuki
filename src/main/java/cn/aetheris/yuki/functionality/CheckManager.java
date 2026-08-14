package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.type.*;
import cn.aetheris.yuki.check.impl.combat.velocity.*;
import cn.aetheris.yuki.check.impl.movement.noslow.NoSlowA;
import cn.aetheris.yuki.check.impl.movement.movementvalidation.MovementValidation;
import cn.aetheris.yuki.check.util.handler.*;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.maps.ClassLoadingMap;
import cn.aetheris.yuki.util.update.*;
import cn.aetheris.yuki.listener.packets.PacketEntityReplication;
import cn.aetheris.yuki.util.latency.CompensatedCooldown;
import cn.aetheris.yuki.util.latency.CompensatedInventory;
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
    public final ClassLoadingMap<AbstractCheck> allChecks;
    private boolean inited;
    private PacketEntityReplication packetEntityReplication;
    private CompensatedInventory inventory;

    public CheckManager(PlayerData player) {
        this.packetChecks = new ClassLoadingMap<>(null);
        this.preViaPacketChecks = new ClassLoadingMap<>(null);
        this.positionCheck = new ClassLoadingMap<>(null);
        this.rotationCheck = new ClassLoadingMap<>(null);
        this.vehicleCheck = new ClassLoadingMap<>(null);
        this.preViaPostPredictionChecks = new ClassLoadingMap<>(null);
        this.postPredictionCheck = new ClassLoadingMap<>(null);
        this.prePredictionChecks = new ClassLoadingMap<>(null);
        this.blockPlaceCheck = new ClassLoadingMap<>(null);
        this.blockBreakChecks = new ClassLoadingMap<>(null);

        CheckRegistry.fillPacketChecks(packetChecks, player);
        CheckRegistry.fillPreViaPacketChecks(preViaPacketChecks, player);
        CheckRegistry.fillPrePredictionChecks(prePredictionChecks, player);
        CheckRegistry.fillPositionChecks(positionCheck, player);
        CheckRegistry.fillRotationChecks(rotationCheck, player);
        CheckRegistry.fillVehicleChecks(vehicleCheck, player);
        CheckRegistry.fillPreViaPostPredictionChecks(preViaPostPredictionChecks, player);
        CheckRegistry.fillPostPredictionChecks(postPredictionCheck, player);
        CheckRegistry.fillBlockPlaceChecks(blockPlaceCheck, player);
        CheckRegistry.fillBlockBreakChecks(blockBreakChecks, player);

        this.allChecks = buildAllChecks();
        this.init();
    }

    private ClassLoadingMap<AbstractCheck> buildAllChecks() {
        ClassLoadingMap<AbstractCheck> all = new ClassLoadingMap<>(null);
        all.putAll(positionCheck);
        all.putAll(rotationCheck);
        all.putAll(vehicleCheck);
        all.putAll(postPredictionCheck);
        all.putAll(preViaPostPredictionChecks);
        all.putAll(prePredictionChecks);
        all.putAll(blockPlaceCheck);
        all.putAll(blockBreakChecks);
        all.putAll(packetChecks);
        all.putAll(preViaPacketChecks);
        return all;
    }

    // --- Type-safe accessors ---

    @SuppressWarnings("unchecked")
    public <T extends PositionCheck> T getPositionCheck(Class<T> check) {
        return (T) positionCheck.get(check);
    }

    @SuppressWarnings("unchecked")
    public <T extends RotationCheck> T getRotationCheck(Class<T> check) {
        return (T) rotationCheck.get(check);
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockBreakCheck> T getBlockBreakChecks(Class<T> check) {
        return (T) blockBreakChecks.get(check);
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

    @SuppressWarnings("unchecked")
    public <T extends PacketCheck> T getPacketCheck(Class<T> check) {
        return (T) packetChecks.get(check);
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockPlaceCheck> T getBlockPlaceCheck(Class<T> check) {
        return (T) blockPlaceCheck.get(check);
    }

    @SuppressWarnings("unchecked")
    public <T extends PacketCheck> T getPrePredictionCheck(Class<T> check) {
        return (T) prePredictionChecks.get(check);
    }

    @SuppressWarnings("unchecked")
    public <T extends PostPredictionCheck> T getPostPredictionCheck(Class<T> check) {
        return (T) postPredictionCheck.get(check);
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

    public VelocityB getExplosionHandler() {
        return getCheck(VelocityB.class);
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

    // --- Dispatch ---

    public void onPrePredictionReceivePacket(final PacketReceiveEvent packet) {
        Consumer<PacketCheck> consumer = i -> i.onPacketReceive(packet);
        prePredictionChecks.forEachValue(consumer);
        blockBreakChecks.forEachValue(consumer);
    }

    public void onPacketReceive(final PacketReceiveEvent packet) {
        Consumer<PacketCheck> consumer = i -> i.onPacketReceive(packet);
        packetChecks.forEachValue(consumer);
        preViaPacketChecks.forEachValue(consumer);
        postPredictionCheck.forEachValue(consumer);
        preViaPostPredictionChecks.forEachValue(consumer);
        blockPlaceCheck.forEachValue(consumer);
    }

    public void onPreViaPacketReceive(final PacketReceiveEvent packet) {
        Consumer<PacketCheck> consumer = i -> i.onPacketReceive(packet);
        preViaPacketChecks.forEachValue(consumer);
        preViaPostPredictionChecks.forEachValue(consumer);
        blockBreakChecks.forEachValue(consumer);
    }

    public void onPacketSend(final PacketSendEvent packet) {
        Consumer<PacketCheck> consumer = i -> i.onPacketSend(packet);
        prePredictionChecks.forEachValue(consumer);
        packetChecks.forEachValue(consumer);
        postPredictionCheck.forEachValue(consumer);
    }

    public void onPreViaPacketSend(final PacketSendEvent packet) {
        Consumer<PacketCheck> consumer = i -> i.onPacketSend(packet);
        preViaPacketChecks.forEachValue(consumer);
        preViaPostPredictionChecks.forEachValue(consumer);
        blockBreakChecks.forEachValue(consumer);
    }

    public void onPositionUpdate(final PositionUpdate position) {
        Consumer<PositionCheck> consumer = check -> check.onPositionUpdate(position);
        positionCheck.forEachValue(consumer);
    }

    public void onRotationUpdate(final RotationUpdate rotation) {
        Consumer<RotationCheck> consumer = check -> check.process(rotation);
        rotationCheck.forEachValue(consumer);
        blockPlaceCheck.forEachValue(consumer);
    }

    public void onVehiclePositionUpdate(final VehiclePositionUpdate update) {
        Consumer<VehicleCheck> consumer = check -> check.process(update);
        vehicleCheck.forEachValue(consumer);
    }

    public void onPredictionFinish(final PredictionComplete complete) {
        Consumer<PostPredictionCheck> consumer = check -> check.onPredictionComplete(complete);
        postPredictionCheck.forEachValue(consumer);
        blockPlaceCheck.forEachValue(consumer);
        preViaPostPredictionChecks.forEachValue(consumer);
    }

    public void onBlockPlace(final BlockPlace place) {
        Consumer<BlockPlaceCheck> consumer = check -> check.onBlockPlace(place);
        blockPlaceCheck.forEachValue(consumer);
    }

    public void onPostFlyingBlockPlace(final BlockPlace place) {
        Consumer<BlockPlaceCheck> consumer = check -> check.onPostFlyingBlockPlace(place);
        blockPlaceCheck.forEachValue(consumer);
    }

    public void onBlockBreak(final BlockBreak blockBreak) {
        Consumer<BlockBreakCheck> consumer = i -> i.onBlockBreak(blockBreak);
        blockBreakChecks.forEachValue(consumer);
    }

    public void onPostFlyingBlockBreak(final BlockBreak blockBreak) {
        Consumer<BlockBreakCheck> consumer = i -> i.onPostFlyingBlockBreak(blockBreak);
        blockBreakChecks.forEachValue(consumer);
    }

    // --- Init ---

    private void init() {
        if (inited) return;
        inited = true;
        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            Set<String> permissionNames = new LinkedHashSet<>();
            for (AbstractCheck check : allChecks.values()) {
                if (check.isUtilityClass()) continue;
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