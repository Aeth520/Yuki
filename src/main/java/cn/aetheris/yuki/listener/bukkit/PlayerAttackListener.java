package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.combat.killaura.KillAuraD;
import cn.aetheris.yuki.check.impl.combat.reach.ReachE;
import cn.aetheris.yuki.command.sub.Mitigate;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.attribute.ValuedAttribute;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.ray.RayTraceUtil;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerAttackListener extends AbstractListener {

    public static final Set<String> user = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity target = event.getEntity();

        if (damagerEntity instanceof Player damager) {
            PlayerData data = getData(damager);

            wallHack(damager, target);

            postCheck(data);

            reduceDamage(damager, data, event);
        }

        if (Mitigate.teleport_entity.contains(target) && event.getDamager() instanceof Player) {
            Mitigate.violationMap.add(target);
            event.setCancelled(true);
        }
    }

    private void wallHack(Player damager, Entity target) {
        MHDFScheduler.getRegionScheduler().runTask(Yuki.getInstance(), damager.getWorld(), damager.getChunk().getX(), damager.getChunk().getZ(), () -> {
            boolean isVisible = RayTraceUtil.isEntityVisible(damager.getWorld(),
                    damager.getEyeLocation(),
                    target.getBoundingBox(),
                    parseMaterials(
                            PluginLoader.INSTANCE.getConfigManager()
                                    .getConfig()
                                    .getStringList("Reach.ray-trace-blacklist")
                    )
            );

            if (!isVisible) {
                MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
                    PlayerData data = getData(damager);
                    ReachE check = data.getCheckManager().getCheck(ReachE.class);
                    if (check != null) {
                        if (check.flagAndAlert("target= " + target.getName() + "\nloc= " + damager.getEyeLocation().clone())) {
                            data.mitigateDamage();
                            
                            MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), data::mitigateDamage, 40L);
                        }
                    }
                });
            }
        });
    }

    private void postCheck(PlayerData data) {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            if (data != null) {
                KillAuraD check2 = data.getCheckManager().getCheck(KillAuraD.class);
                if (check2 != null) {
                    double diff = Math.abs(System.currentTimeMillis() - data.getLastFlying());
                    
                    
                    if (data.getClientVersion().isOlderThan(ClientVersion.V_1_9)
                            && diff <= 3 && (System.currentTimeMillis() - data.getTranDelay()) < 60
                            && data.getTransactionPing() < 300) {
                        if (check2.buffer++ > 6) {
                            if (check2.flagAndAlert("(Order)\ndiff= " + diff)) {
                                if (check2.getViolations() > 4) {
                                    data.mitigateDamage();
                                }
                            }
                        } else {
                            check2.rewardBufferAndVL();
                        }
                    }
                }
            }
        });
    }

    private void reduceDamage(Player damager, PlayerData data, EntityDamageByEntityEvent event) {
        if (user.contains(damager.getName()) && !event.isCancelled()) {
            double originalDamage = event.getDamage();
            double newDamage = calculateDamage(originalDamage, data);
            event.setDamage(newDamage);

            LogUtils.mitigate("&b" + damager.getName() + " &7has been reduced damage " +
                    String.format("%.2f", originalDamage) + " -> " +
                    String.format("%.2f", newDamage) + " &7(&b" +
                    event.getEntity().getName() + "&7)");

            user.remove(damager.getName());
        }

        if (event.isCancelled()) {
            if (data != null) {
                data.lastAttack = System.currentTimeMillis();
                if (data.isAttacking) {
                    data.isAttacking = false;
                }
            }
        }
    }

    private double calculateDamage(double gotDamage, PlayerData data) {
        String type = PluginLoader.INSTANCE.getConfigManager().getConfig()
                .getStringElse("mitigates.attack.reduce-type", "Dynamic").toLowerCase();

        return switch (type) {
            case "dynamic" -> mathDamage(gotDamage, data);
            case "fixed" -> calculateFixedDamage(gotDamage);
            case "attackspeed" -> calculateSpeedBasedDamage(gotDamage, data);
            case "random" -> calculateRandomDamage(gotDamage);
            case "strict" -> calculatePolarDamage(gotDamage);
            default -> gotDamage;
        };
    }

    private double calculatePolarDamage(double gotDamage) {
        return gotDamage * 0.2;
    }

    private double calculateSpeedBasedDamage(double damage, PlayerData data) {
        ValuedAttribute attackSpeed = data.getCompensatedEntities().getSelf()
                .getAttribute(Attributes.MOVEMENT_SPEED).orElse(null);
        if (attackSpeed == null) return damage;

        double value = attackSpeed.get();
        double mitigationFactor = Math.min((value - 4) * 0.1, 0.3);
        return Math.max(damage - (damage * mitigationFactor), 0.5);
    }

    private double calculateRandomDamage(double damage) {
        double randomFactor = Math.random() * 0.4 + 0.2;
        return Math.max(damage * randomFactor, 0.5);
    }

    private double mathDamage(double damage, PlayerData player) {
        double needDamage = switch ((int) damage) {
            case 11 -> 10;
            case 10 -> 8.5;
            case 9 -> 6.5;
            case 8 -> 5.5;
            case 7 -> 4.5;
            case 6 -> 3.5;
            case 5 -> 2.5;
            case 4 -> 1.5;
            case 3 -> 1.0;
            case 2 -> 0.5;
            default -> damage >= 12 ? 10 : 0.3;
        };

        needDamage *= (Math.random() * 0.3 + 0.2);
        needDamage = Math.max(needDamage, 0.5);

        if ((player.firstBreadKB != null && player.firstBreadKB.isSetback) ||
                (player.firstBreadExplosion != null && player.firstBreadExplosion.isSetback)) {
            needDamage = Math.max(damage - 1.5, 0.5);
        }

        return Math.max(damage - needDamage, 0.5);
    }

    private double calculateFixedDamage(double damage) {
        double mitigationPercentage = PluginLoader.INSTANCE.getConfigManager().getConfig()
                .getDoubleElse("mitigates.attack.damage", 50);
        mitigationPercentage = Math.max(1, Math.min(99, mitigationPercentage));

        return Math.max(damage * (mitigationPercentage / 100.0), 0.5);
    }

    private Set<Material> parseMaterials(List<String> names) {
        Set<Material> set = new HashSet<>();
        for (String name : names) {
            Material material = parseEnum(Material.class, name, "material");
            if (material != null) {
                set.add(material);
            }
        }
        return set;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String name, String typeName) {
        try {
            return Enum.valueOf(enumClass, name.toUpperCase());
        } catch (IllegalArgumentException e) {
            LogUtils.console("&cInvalid " + typeName + " | " + name);
            return null;
        }
    }
}
