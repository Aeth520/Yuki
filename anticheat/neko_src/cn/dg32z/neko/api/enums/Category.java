/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 */
package cn.dg32z.neko.api.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Category {
    ELYTRA(Parent.MOVEMENT, Material.FEATHER),
    GROUNDSPOOF(Parent.MOVEMENT, Material.DIRT),
    NOSLOW(Parent.MOVEMENT, Material.DIAMOND_SWORD),
    NOWEB(Parent.MOVEMENT, Category.resolve("COBWEB", "WEB")),
    JUMP(Parent.MOVEMENT, Material.DIAMOND_BOOTS),
    SLIMULATION(Parent.MOVEMENT, Material.STICK),
    JESUS(Parent.MOVEMENT, Category.resolve("LILY_PAD", "WATER_LILY")),
    VEHICLE(Parent.MOVEMENT, Material.MINECART),
    PHASE(Parent.MOVEMENT, Material.VINE),
    SPEED(Parent.MOVEMENT, Material.POTION),
    AIM(Parent.COMBAT, Material.BOW),
    ANALYSIS(Parent.COMBAT, Material.BOOK),
    HITSELECT(Parent.COMBAT, Category.resolve("GOLDEN_SWORD", "GOLD_SWORD")),
    AUTOBLOCK(Parent.COMBAT, Material.IRON_SWORD),
    KILLAURA(Parent.COMBAT, Material.REDSTONE),
    REACH(Parent.COMBAT, Material.STONE_SWORD),
    VELOCITY(Parent.COMBAT, Category.resolve("SNOWBALL", "SNOW_BALL")),
    CLIENT(Parent.MISC, Material.COMPASS),
    SPAM(Parent.MISC, Material.PAPER),
    NONE(Parent.MISC, Material.BARRIER),
    BEDROCK(Parent.MISC, Material.BEDROCK),
    INTERACT(Parent.PLAYER, Material.STONE),
    AUTOCLICKER(Parent.PLAYER, Material.BLAZE_ROD),
    AUTOTOTEM(Parent.PLAYER, Category.resolve("GOLDEN_APPLE", "totem_of_undying")),
    BADPACKETS(Parent.PLAYER, Material.BOOK),
    CRYSTAL(Parent.PLAYER, Category.resolve("END_CRYSTAL", "ENDER_PEARL")),
    ANCHOR(Parent.PLAYER, Material.GLOWSTONE),
    BARITONE(Parent.PLAYER, Category.resolve("MUSIC_DISC_BLOCKS", "RECORD_3")),
    BLINK(Parent.PLAYER, Material.REDSTONE),
    BREAK(Parent.PLAYER, Material.STONE_PICKAXE),
    CRASH(Parent.PLAYER, Material.TNT),
    ENTITY(Parent.PLAYER, Material.ROTTEN_FLESH),
    EXPLOIT(Parent.PLAYER, Material.FURNACE),
    IMPOSSIBLE(Parent.PLAYER, Material.BEDROCK),
    FASTPLACE(Parent.PLAYER, Category.resolve("WOOL", "WHITE_WOOL")),
    INVENTORY(Parent.PLAYER, Material.CHEST),
    PINGSPOOF(Parent.PLAYER, Material.TRIPWIRE_HOOK),
    LAGASSIST(Parent.PLAYER, Material.FISHING_ROD),
    POST(Parent.PLAYER, Material.PAPER),
    SCAFFOLD(Parent.PLAYER, Material.LADDER),
    TIMER(Parent.PLAYER, Category.resolve("WATCH", "CLOCK")),
    XRAY(Parent.PLAYER, Material.GLASS);

    final ItemStack[] items;
    final Parent parent;

    private Category(Parent parent, ItemStack ... items) {
        this.items = items;
        this.parent = parent;
        parent.addCategory(this);
    }

    private Category(Parent parent, Material ... items) {
        ItemStack[] itemStacks = new ItemStack[items.length];
        for (int i = 0; i < items.length; ++i) {
            itemStacks[i] = new ItemStack(items[i]);
        }
        this.parent = parent;
        this.items = itemStacks;
        parent.addCategory(this);
    }

    private static Material resolve(String ... names) {
        for (String name : names) {
            try {
                return Material.valueOf((String)name);
            }
            catch (IllegalArgumentException illegalArgumentException) {
            }
        }
        return Material.BARRIER;
    }

    @Generated
    public ItemStack[] getItems() {
        return this.items;
    }

    @Generated
    public Parent getParent() {
        return this.parent;
    }

    public static enum Parent {
        MOVEMENT("Movement"),
        COMBAT("Combat"),
        MISC("Misc"),
        PLAYER("Player");

        final String name;
        final List<Category> categories = new ArrayList<Category>();
        Category[] categoriesArray;

        public void forEach(Consumer<Category> categoryConsumer) {
            this.categories.forEach(categoryConsumer);
        }

        public void addCategory(Category category) {
            this.categories.add(category);
            this.categoriesArray = this.categories.toArray(new Category[0]);
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public List<Category> getCategories() {
            return this.categories;
        }

        @Generated
        public Category[] getCategoriesArray() {
            return this.categoriesArray;
        }

        @Generated
        private Parent(String name) {
            this.name = name;
        }
    }
}
