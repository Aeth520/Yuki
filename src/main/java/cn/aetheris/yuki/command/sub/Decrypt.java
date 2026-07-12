package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.util.encrypt.AESUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class Decrypt extends AbstractCommand {

    public Decrypt() {
        super(
                "Decrypt code",
                "yuki.commands.decrypt",
                false
        );
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.punish.decrypt"));
            return;
        }

        String cipherText = String.join(" ", args).trim();

        try {
            String decrypted = AESUtil.decrypt(cipherText);
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.punish.message")
                    .replace("%message%", decrypted));
        } catch (Exception e) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.punish.decrypt")
                    .replace("%message%", "Failed to decrypt: " + e.getMessage()));
        }
    }
}
