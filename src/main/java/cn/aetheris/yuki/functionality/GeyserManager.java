package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.util.GeyserUtil;

public final class GeyserManager extends Check implements PacketCheck {

    private boolean sent;

    public GeyserManager(PlayerData player) {
        super(player);
        this.sent = false;
    }


    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!PluginLoader.INSTANCE.getConfigManager().isHookGeyser() || player.isBypass()) {
            return;
        }

        final User user = event.getUser();

        if (shouldExemptUser(user)) {
            addExemptUser(user);
        } else {
            sent = false;
        }
    }

    

    private boolean shouldExemptUser(User user) {
        final var config = PluginLoader.INSTANCE.getConfigManager();
        boolean isGeyserBrand = player.getBrand().equals("Geyser") && config.isHookGeyserBrand();
        boolean isFloodgateOrGeyserAPIPlayer = (GeyserUtil.isGeyserPlayer(user.getUUID())
                || HookInit.getFloodgateHook().isFloodgateUser(user.getUUID())
                && config.isHookGeyserAPI());
        boolean isSpecialUUID = (user.getUUID().toString().startsWith("00000000000-0000-0009") || user.getUUID().toString().contains("0000000")) && config.isHookGeyserUUID();

        return isGeyserBrand || isFloodgateOrGeyserAPIPlayer || isSpecialUUID;
    }

    public void addExemptUser(User user) {
        var playerDataManager = PluginLoader.INSTANCE.getPlayerDataManager();
        if (!playerDataManager.exemptUsers.contains(user) && !sent) {
            player.bypass = true;
            player.isBedrockPlayer = true;
            playerDataManager.exemptUsers.add(user);
            playerDataManager.remove(user);
            playerDataManager.onDisconnect(user);
            LogUtils.console("&b[GeyserManager] &b" + user.getName() +
                    "&f is a Bedrock Edition player! (" + player.getKeepAlivePing() + ")");
            sent = true;
        }
    }
}
