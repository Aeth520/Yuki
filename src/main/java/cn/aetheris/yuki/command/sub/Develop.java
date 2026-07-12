package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.config.file.YamlConfiguration;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Develop extends AbstractCommand {

    public Develop() {
        super(
                "Enable develop mode",
                "yuki.commands.develop",
                false
        );
    }

    @SneakyThrows
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        final File settingsFile = new File(Yuki.getInstance().getDataFolder(), "settings.yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(settingsFile);

        if (!config.getBoolean("function.develop.enable")) {
            config.set("function.develop.enable", true);
            config.save(settingsFile);
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.develop.enable"));
            PluginLoader.INSTANCE.getConfigManager().reload();
        } else {
            config.set("function.develop.enable", false);
            config.save(settingsFile);
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.develop.disable"));
            PluginLoader.INSTANCE.getConfigManager().reload();
        }
    }
}
