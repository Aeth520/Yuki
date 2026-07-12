package cn.aetheris.yuki.menu;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.config.file.YamlConfiguration;
import cn.aetheris.yuki.util.message.ColorUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

public class SettingsMenu extends FastInv {
    public SettingsMenu() {
        super(27, ColorUtils.color("&3Yuki &7- &eSettings"));
        int[] whiteFrames = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 18, 19, 20, 21, 22, 23, 24, 25};
        for (int slot : whiteFrames) {
            setItem(slot, MenuUtil.getBlueFrame());
        }
        setItem(9, MenuUtil.getBlueFrame());
        setItem(17, MenuUtil.getBlueFrame());
        setItem(26, MenuUtil.getBack(), e -> new Menu((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));
        YamlConfiguration config = YamlConfiguration.loadConfiguration(PluginLoader.INSTANCE.getConfigManager().getSettingsFile());
        boolean enabled = config.getBoolean("function.experimental");
        ItemStack icon = new ItemBuilder(enabled ? Material.ENCHANTED_BOOK : Material.BOOK)
                .name(ColorUtils.color("&bExperimental"))
                .lore(new String[]{ColorUtils.color("&7&m&l------------------------"),
                        ColorUtils.color("&7Enabled: " + (enabled ? "&a✔" : "&c✘")),
                        ColorUtils.color("&7&m&l------------------------")})
                .build();
        setItem(10, icon, e -> {
            config.set("function.experimental", !enabled);
            try {
                config.save(PluginLoader.INSTANCE.getConfigManager().getSettingsFile());
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
            List<String> lore = e.getCurrentItem().getItemMeta().getLore();
            assert (lore != null);
            lore.set(1, ColorUtils.color("&7Enabled: " + (e.getCurrentItem().getType() == Material.BOOK ? "&a✔" : "&c✘")));
            ItemBuilder builder = new ItemBuilder(e.getCurrentItem()).type(e.getCurrentItem().getType() == Material.ENCHANTED_BOOK ? Material.BOOK : Material.ENCHANTED_BOOK).name(e.getCurrentItem().getItemMeta().getDisplayName()).lore(lore);
            setItem(10, builder.build());
        });
    }

    @Override
    protected void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        event.setCursor(null);
    }
}
