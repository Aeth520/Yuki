package cn.aetheris.yuki.command.sub;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.combat.killaura.KillAuraG;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.protocol.nms.NMSUtils;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class Mitigate extends AbstractCommand {

    public static final Set<Entity> teleport_entity = new HashSet<>();
    private static final Map<String, MitigationType> TYPES;
    public static Set<Entity> violationMap = new HashSet<>();

    static {
        Map<String, MitigationType> types = new LinkedHashMap<>();

        types.put("damage", (data, sender, target, args) -> {
            data.mitigateDamage();
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.mitigate.types.damage.message"));
        });

        types.put("resetitem", (data, sender, target, args) -> {
            NMSUtils.resetItemUsage(target);
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.mitigate.types.reset-item.message"));
        });

        MitigationType knockbackAction = (data, sender, target, args) -> {
            double x = PluginLoader.INSTANCE.getConfigManager().getConfig().getDoubleElse("knock-back.x", 0.75);
            double y = PluginLoader.INSTANCE.getConfigManager().getConfig().getDoubleElse("knock-back.y", 0.5);
            double z = PluginLoader.INSTANCE.getConfigManager().getConfig().getDoubleElse("knock-back.z", 0.75);
            target.setVelocity(new Vector(x, y, z));
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.mitigate.types.knock-back.message"));
        };
        types.put("knockback", knockbackAction);
        types.put("kb", knockbackAction);

        types.put("rotate", (data, sender, target, args) -> {
            data.randomiseAim(target, target.getLocation());
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.mitigate.types.rotate.message"));
        });

        types.put("shuffle", (data, sender, target, args) -> {
            int randomSlot = ThreadLocalRandom.current().nextInt(9);

            WrapperPlayServerHeldItemChange slotPacket = new WrapperPlayServerHeldItemChange(randomSlot);
            data.user.writePacket(slotPacket);

            data.packetStateData.lastSlotSelected = randomSlot;
            data.getInventory().inventory.selected = randomSlot;
            data.getInventory().requiresRefresh = true;
            data.packetStateData.setSlowedByUsingItem(false);

            if (data.bukkitPlayer != null) {
                data.bukkitPlayer.updateInventory();
                data.bukkitPlayer.getInventory().setHeldItemSlot(randomSlot); 
            }

            NMSUtils.resetItemUsage(target);
            data.user.flushPackets();

            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.mitigate.types.shuffle.message").replace("%slot%", String.valueOf(randomSlot)));
        });

        types.put("food", (data, sender, target, args) -> {
            int original = target.getFoodLevel();
            target.setFoodLevel(0);
            MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {
                if (target.isOnline()) {
                    target.setFoodLevel(original);
                }
            }, 20L);
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.mitigate.types.food.message"));
        });

        types.put("resync", (data, sender, target, args) -> {
            data.resyncPose();
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.mitigate.types.resync.message"));
        });
        types.put("killaura", (data, sender, target, args) -> {
            if (data.target == null) return;

            PacketEntity targetEntity = data.getTarget();
            if (targetEntity == null || targetEntity.isDead || targetEntity.getType() != EntityTypes.PLAYER) {
                sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.mitigate.types.killaura.not-target"));
                return;
            }

            final Player targetPlayer = Bukkit.getPlayer(targetEntity.getUuid());
            Location originalLoc;
            if (targetPlayer != null) {
                originalLoc = targetPlayer.getLocation().clone();
            } else {
                originalLoc = null;
            }

            Vector direction = target.getLocation().getDirection().normalize(); 
            Location behindLoc = target.getLocation().add(direction.multiply(-1.5)); 

            behindLoc.setY(target.getLocation().getY() + 0.85);
            behindLoc.setYaw(target.getLocation().getYaw());

            PaperUtils.teleport(targetPlayer, behindLoc);
            teleport_entity.add(targetPlayer);
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(targetPlayer, "commands.mitigate.types.killaura.message").replace("%target%", target.getName()));

            MHDFScheduler.getGlobalRegionScheduler().runTaskLater(Yuki.getInstance(), () -> {
                if (teleport_entity.contains(targetPlayer)) {
                    PaperUtils.teleport(targetPlayer, originalLoc);
                    sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(targetPlayer, "commands.mitigate.types.killaura.pull-back").replace("%target%", target.getName()));
                    teleport_entity.remove(targetPlayer);
                    if (violationMap.contains(targetPlayer)) {
                        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
                            if (data.getCheckManager().getCheck(KillAuraG.class) != null) {
                                data.getCheckManager().getCheck(KillAuraG.class).flagAndAlert("impossible attack?");
                                violationMap.remove(targetPlayer);
                                sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(targetPlayer, "commands.mitigate.types.killaura.attacked").replace("%target%", target.getName()));
                            }
                        });
                    }
                }
            }, PluginLoader.INSTANCE.getConfigManager().getConfig().getIntElse("commands.mitigate.types.killaura.tick", 10));
        });
        TYPES = Collections.unmodifiableMap(types);
    }

    public Mitigate() {
        super("mitigate", "yuki.commands.mitigate", false);
    }

    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> list = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String name = player.getName();
                if (name.toLowerCase().startsWith(partial)) {
                    list.add(name);
                }
            }
            return list;
        } else if (args.length == 2) {
            String partial = args[1].toLowerCase();
            List<String> list = new ArrayList<>();
            for (String type : TYPES.keySet()) {
                if (type.startsWith(partial)) {
                    if (sender.hasPermission("yuki.commands.mitigate." + type)) {
                        list.add(type);
                    }
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.mitigate.usage"));
            return;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-found").replace("%player%", playerName));
            return;
        }

        if (target == sender) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-my-self"));
            return;
        }

        PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(target);
        if (data == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "not-data-user"));
            return;
        }

        String typeName = (args.length >= 2) ? args[1].toLowerCase() : PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("commands.mitigate.default-type", "damage");

        MitigationType mitigation = TYPES.get(typeName);
        if (mitigation == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.mitigate.invalid-type").replace("%type%", typeName));
            return;
        }

        if (!sender.hasPermission("yuki.commands.mitigate." + typeName)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("no-permission"));
            return;
        }

        mitigation.execute(data, sender, target, args);
    }

    @FunctionalInterface
    private interface MitigationType {
        void execute(PlayerData data, CommandSender sender, Player target, String[] args);
    }
}
