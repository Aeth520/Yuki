package cn.aetheris.yuki.api.enums;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@Getter
public enum CheckType {

    ELYTRA(Material.FEATHER),
    GROUNDSPOOF(Material.DIRT),
    NOSLOW(Material.DIAMOND_SWORD),
    MOVEMENT_VALIDATION(Material.STICK),
    SPRINT(Material.LEATHER_BOOTS),
    VEHICLE(Material.MINECART),

    AIM(Material.BOW),
    ANALYSIS(Material.BOOK),
    AUTOBLOCK(Material.IRON_SWORD),
    KILLAURA(Material.REDSTONE),
    REACH(Material.STONE_SWORD),
    VELOCITY(resolve("SNOW_BALL", "LEGACY_SNOW_BALL")),
    CLIENT(Material.COMPASS),
    SPAM(Material.PAPER),
    AIRPLACE(Material.COBBLESTONE),
    AUTOCLICKER(Material.BLAZE_ROD),
    AUTOTOTEM(resolve("GOLDEN_APPLE", "totem_of_undying")),
    BADPACKETS(Material.BOOK),
    BARITONE(resolve("RECORD_3", "MUSIC_DISC_BLOCKS")),
    BLINK(Material.REDSTONE),
    BREAK(Material.STONE_PICKAXE),
    CRASH(Material.TNT),
    ENTITY(Material.ROTTEN_FLESH),
    EXPLOIT(Material.FURNACE),
    IMPOSSIBLE(Material.BEDROCK),
    INTERACT(Material.IRON_AXE),
    FASTPLACE(resolve("WOOL", "WHITE_WOOL")),
    INVENTORY(Material.CHEST),
    PINGSPOOF(Material.TRIPWIRE_HOOK),
    POST(Material.PAPER),
    SCAFFOLD(Material.LADDER),
    TIMER(resolve("WATCH", "CLOCK")),
    XRAY(Material.GLASS),
    MULTIACTIONS(Material.DIAMOND_AXE),
    NONE(Material.BARRIER),
    CHAT(Material.WRITABLE_BOOK),
    BEDROCK(Material.BEDROCK);

    public static final CheckType[] MOVEMENT = {
            ELYTRA, GROUNDSPOOF, NOSLOW, MOVEMENT_VALIDATION, SPRINT, VEHICLE
    };

    public static final CheckType[] COMBAT = {
            AIM, ANALYSIS, AUTOBLOCK, KILLAURA, REACH, VELOCITY
    };

    public static final CheckType[] MISC = {
            CLIENT, SPAM, CHAT
    };

    public static final CheckType[] PLAYER = {
            AIRPLACE, AUTOCLICKER, AUTOTOTEM, BADPACKETS, BARITONE, BLINK, BREAK,
            CRASH, ENTITY, EXPLOIT, IMPOSSIBLE, INTERACT, FASTPLACE, INVENTORY,
            PINGSPOOF, POST, SCAFFOLD, TIMER, XRAY, MULTIACTIONS
    };

    final ItemStack[] items;

    CheckType(ItemStack... items) {
        this.items = items;
    }

    CheckType(Material... items) {
        ItemStack[] itemStacks = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            itemStacks[i] = new ItemStack(items[i]);
        }
        this.items = itemStacks;
    }

    private static Material resolve(String... names) {
        for (String name : names) {
            try {
                Material m = Material.matchMaterial(name);
                if (m != null) return m;
            } catch (Exception ignored) {
            }
        }
        throw new IllegalStateException("No compatible Material for " + Arrays.toString(names));
    }
}
