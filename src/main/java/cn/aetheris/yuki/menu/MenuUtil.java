package cn.aetheris.yuki.menu;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.functionality.CheckManager;
import cn.aetheris.yuki.functionality.ConfigManager;
import cn.aetheris.yuki.util.message.ColorUtils;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

public class MenuUtil {

    public static boolean isNew = Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_12_2);

    private static Material getCheckMaterial(CheckType type) {
        return switch (type) {
            case ELYTRA -> Material.FEATHER;
            case GROUNDSPOOF -> Material.DIRT;
            case NOSLOW -> Material.DIAMOND_SWORD;
            case MOVEMENT_VALIDATION -> Material.STICK;
            case SPRINT -> Material.LEATHER_BOOTS;
            case VEHICLE -> Material.MINECART;
            case AIM -> Material.BOW;
            case ANALYSIS -> Material.BOOK;
            case AUTOBLOCK -> Material.IRON_SWORD;
            case KILLAURA -> Material.REDSTONE;
            case REACH -> Material.STONE_SWORD;
            case VELOCITY -> resolveMaterial("SNOW_BALL", "LEGACY_SNOW_BALL");
            case CLIENT -> Material.COMPASS;
            case SPAM -> Material.PAPER;
            case AIRPLACE -> Material.COBBLESTONE;
            case AUTOCLICKER -> Material.BLAZE_ROD;
            case AUTOTOTEM -> resolveMaterial("GOLDEN_APPLE", "totem_of_undying");
            case BADPACKETS -> Material.BOOK;
            case BARITONE -> resolveMaterial("RECORD_3", "MUSIC_DISC_BLOCKS");
            case BLINK -> Material.REDSTONE;
            case BREAK -> Material.STONE_PICKAXE;
            case CRASH -> Material.TNT;
            case ENTITY -> Material.ROTTEN_FLESH;
            case EXPLOIT -> Material.FURNACE;
            case IMPOSSIBLE -> Material.BEDROCK;
            case INTERACT -> Material.IRON_AXE;
            case FASTPLACE -> resolveMaterial("WOOL", "WHITE_WOOL");
            case INVENTORY -> Material.CHEST;
            case PINGSPOOF -> Material.TRIPWIRE_HOOK;
            case POST -> Material.PAPER;
            case SCAFFOLD -> Material.LADDER;
            case TIMER -> resolveMaterial("WATCH", "CLOCK");
            case XRAY -> Material.GLASS;
            case MULTIACTIONS -> Material.DIAMOND_AXE;
            case NONE -> Material.BARRIER;
            case CHAT -> Material.WRITABLE_BOOK;
            case BEDROCK -> Material.BEDROCK;
        };
    }

    private static Material resolveMaterial(String... names) {
        for (String name : names) {
            try {
                Material m = Material.matchMaterial(name);
                if (m != null) return m;
            } catch (Exception ignored) {
            }
        }
        throw new IllegalStateException("No compatible Material for " + Arrays.toString(names));
    }

    public static void setCheckItems(FastInv inventory, CheckType[] types) {
        List<Integer> glassSlots = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43);
        for (int i : glassSlots) {
            inventory.setItem(i, getBlueFrame());
        }
        for (int i = 0; i < types.length; ++i) {
            if (glassSlots.contains(i)) continue;
            int finalI = i;

            ItemBuilder builder = new ItemBuilder(getCheckMaterial(types[i])).name(ColorUtils.color("&b" + types[i].name()));
            inventory.setItem(i, builder.build(), e -> {
                e.setCancelled(true);
                FastInv fi = new FastInv(fastInv -> {
                    Inventory inv = Bukkit.createInventory(fastInv, 36, ColorUtils.color("&3Yuki &7- &b" + types[finalI].name()));
                    CheckManager cm = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId())).getCheckManager();
                    Collection<Check> checks = cm.getChecks(types[finalI]);
                    checks.forEach(check -> {
                        ItemStack icon = new ItemBuilder(check.isEnabled() ? Material.ENCHANTED_BOOK : Material.BOOK)
                                .name(ColorUtils.color("&b" + check.getCheckName()))
                                .lore(new String[]{ColorUtils.color("&7&m&l------------------------"),
                                        ColorUtils.color("&7Enabled: " + (check.isEnabled() ? "&a✔" : "&c✘")),
                                        ColorUtils.color("&7Violations: " + check.getMaxVL()),
                                        ColorUtils.color(""),
                                        ColorUtils.color("&7Description: &e" + check.getDescription()),
                                        ColorUtils.color("&7&m&l------------------------")})
                                .build();
                        inv.addItem(icon);
                    });
                    return inv;
                }) {
                    protected void onClick(InventoryClickEvent event) {
                        event.setCancelled(true);
                        ItemStack item = event.getCurrentItem();
                        if (item == null) return;
                        if (item.getType() == Material.ENCHANTED_BOOK || item.getType() == Material.BOOK) {
                            event.setCursor(null);
                            List<String> lore = item.getItemMeta().getLore();
                            assert (lore != null);
                            lore.set(1, ColorUtils.color("&7Enabled: " + (item.getType() == Material.BOOK ? "&a✔" : "&c✘")));
                            ItemBuilder builder = new ItemBuilder(item).type(item.getType() == Material.ENCHANTED_BOOK ? Material.BOOK : Material.ENCHANTED_BOOK).name(item.getItemMeta().getDisplayName()).lore(lore);
                            this.setItem(event.getSlot(), builder.build());
                            String configName = ColorUtils.stripColor(item.getItemMeta().getDisplayName()).split(" ")[0];
                            Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> MenuUtil.changeCheck(configName));
                        }
                    }
                };
                fi.setItem(35, MenuUtil.getBack(), event -> {
                    event.setCancelled(true);
                    inventory.open((Player) event.getWhoClicked());
                });
                fi.open((Player) e.getWhoClicked());
            });
        }
    }

    private static void changeCheck(String name) {
        ConfigManager configManager = PluginLoader.INSTANCE.getConfigManager();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configManager.getPunishFile());
        for (String s : Objects.requireNonNull(config.getConfigurationSection("Punishments")).getKeys(false)) {
            List<String> checks = config.getStringList("Punishments." + s + ".checks");
            if (!checks.contains(name) && !checks.contains("!" + name)) continue;
            ArrayList<String> result = new ArrayList<>();
            for (String check : checks) {
                if (check.equals(name)) {
                    result.add("!" + check);
                    continue;
                }
                if (check.equals("!" + name)) {
                    result.add(check.substring(1));
                    continue;
                }
                result.add(check);
            }
            config.set("Punishments." + s + ".checks", result);
            try {
                config.save(configManager.getPunishFile());
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
            break;
        }
    }


    public static ItemStack getBlueFrame() {
        if (isNew) {
            return new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(ColorUtils.color("&3Yuki")).build();
        }
        return new ItemBuilder(new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 3)).name(ColorUtils.color("&f边框")).build();
    }

    public static ItemStack getWhiteFrame() {
        if (isNew) {
            return new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name(ColorUtils.color("&3Yuki")).build();
        }
        return new ItemBuilder(new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 0)).name(ColorUtils.color("&f边框")).build();
    }

    public static ItemStack getBack() {
        return new ItemBuilder(Material.EMERALD).name(ColorUtils.color("&b返回")).lore(new String[]{ColorUtils.color("&7&m&l------------------------"), ColorUtils.color("&e返回上一个菜单"), ColorUtils.color("&7&m&l------------------------")}).build();
    }
}
