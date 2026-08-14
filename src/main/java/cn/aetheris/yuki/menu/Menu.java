package cn.aetheris.yuki.menu;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.functionality.CheckManager;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.ColorUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Menu extends FastInv {
    public Menu(Player player) {
        super(27, ColorUtils.color("&3Yuki &7- &eMenu"));
        for (int slot : new int[]{0, 1, 3, 4, 5, 7, 8, 9, 17, 18, 19, 21, 22, 23, 25, 26}) {
            this.setItem(slot, MenuUtil.getBlueFrame());
        }
        for (int slot : new int[]{2, 6, 10, 12, 14, 16, 20, 24}) {
            this.setItem(slot, MenuUtil.getWhiteFrame());
        }
        this.setItem(11, this.getCheck(player), e -> new ChecksMenu().open((Player) e.getWhoClicked()));
        this.setItem(13, this.getInfo());
        this.setItem(15, new ItemBuilder(MenuUtil.isNew ? Material.WRITABLE_BOOK : Material.getMaterial("BOOK_AND_QUILL")).name(ColorUtils.color("&bSettings")).build(), e -> new SettingsMenu().open((Player) e.getWhoClicked()));
    }

    private ItemStack getCheck(Player player) {
        int checkCount = 0;
        PlayerData playerData = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(player);
        if (playerData != null) {
            checkCount = new CheckManager(playerData).allChecks.values().size();
        }
        return new ItemBuilder(Material.BEACON)
                .name(ColorUtils.color("&bChecks"))
                .lore(new String[]{ColorUtils.color("&7&m&l------------------------"),
                        ColorUtils.color("&7Manage checks stats"),
                        ColorUtils.color("&7Available Checks: " + checkCount),
                        ColorUtils.color("&7&m&l------------------------")})
                .build();
    }

    private ItemStack getInfo() {
        return new ItemBuilder(Material.NETHER_STAR)
                .name(ColorUtils.color("&bInfo"))
                .lore(new String[]{ColorUtils.color("&7&m&l------------------------"),
                        ColorUtils.color("&7Version: " + Yuki.getInstance().getDescription().getVersion()),
                        ColorUtils.color("&7Server Version: " + Yuki.getInstance().getServer().getVersion()),
                        ColorUtils.color("&7Description: " + Yuki.getInstance().getDescription().getDescription()),
                        ColorUtils.color("&7Authors: " + String.join(", ", Yuki.getInstance().getDescription().getAuthors())),
                        ColorUtils.color("&7&m&l------------------------")})
                .build();
    }

    protected void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
