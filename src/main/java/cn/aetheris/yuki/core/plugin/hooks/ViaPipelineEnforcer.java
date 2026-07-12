package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.PacketEvents;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class ViaPipelineEnforcer {

    private static final String VIA_DECODER = "via-decoder";
    private static final String VIA_ENCODER = "via-encoder";
    private static final int MAX_RETRIES = 5;

    public static void enforce(Player player) {
        enforce(player, false);
    }

    public static void enforce(Player player, boolean silent) {
        if (!HookInit.getViaPluginHook().isEnabled()) return;
        if (player == null || !player.isOnline()) return;

        Object channelObj = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (!(channelObj instanceof Channel channel)) return;

        channel.eventLoop().execute(() -> enforce0(channel, player.getName(), 0, silent));
    }

    public static void enforceAll() {
        if (!HookInit.getViaPluginHook().isEnabled()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            enforce(player, true);
        }
        LogUtils.console("&3Yuki &8» &aEnforced ViaVersion pipeline order for all online players");
    }

    public static void scheduleEnforce(Player player, long delayTicks) {
        if (!HookInit.getViaPluginHook().isEnabled()) return;
        if (player == null || !player.isOnline()) return;

        MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> enforce(player), delayTicks);
    }

    private static void enforce0(Channel channel, String playerName, int attempt, boolean silent) {
        try {
            if (!channel.isActive() || !channel.isRegistered()) return;

            ChannelPipeline pipeline = channel.pipeline();
            List<String> names = pipeline.names();

            boolean relocated = false;
            relocated |= ensureDecoderOrder(pipeline, names, playerName, silent);
            relocated |= ensureEncoderOrder(pipeline, names, playerName, silent);

            if (relocated) {
                if (!silent) {
                    LogUtils.console("&3Yuki &8» &aViaVersion pipeline order corrected for &b" + playerName);
                }
            }
        } catch (Exception e) {
            if (!silent) {
                LogUtils.console("&3Yuki &8» &cFailed to verify ViaVersion pipeline order for &b" + playerName + "&c: " + e.getMessage());
            }
        }

        if (attempt < MAX_RETRIES && !isOrderCorrect(channel)) {
            MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {
                if (channel.isActive()) {
                    enforce0(channel, playerName, attempt + 1, silent);
                }
            }, 5L + attempt * 5L);
        }
    }

    private static boolean isOrderCorrect(Channel channel) {
        try {
            ChannelPipeline pipeline = channel.pipeline();
            List<String> names = pipeline.names();

            boolean decoderCorrect = true;
            boolean encoderCorrect = true;

            if (names.contains(VIA_DECODER) && names.contains(PacketEvents.DECODER_NAME)) {
                decoderCorrect = names.indexOf(PacketEvents.DECODER_NAME) <= names.indexOf(VIA_DECODER);
            }

            if (names.contains(VIA_ENCODER) && names.contains(PacketEvents.ENCODER_NAME)) {
                encoderCorrect = names.indexOf(PacketEvents.ENCODER_NAME) <= names.indexOf(VIA_ENCODER);
            }

            return decoderCorrect && encoderCorrect;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean ensureDecoderOrder(ChannelPipeline pipeline, List<String> names, String playerName, boolean silent) {
        if (!names.contains(VIA_DECODER) || !names.contains(PacketEvents.DECODER_NAME)) {
            return false;
        }

        int viaIndex = names.indexOf(VIA_DECODER);
        int peIndex = names.indexOf(PacketEvents.DECODER_NAME);
        if (peIndex <= viaIndex) {
            return false;
        }

        if (!silent) {
            LogUtils.console("&3Yuki &8» &eViaVersion decoder is ahead of PacketEvents decoder for &b" + playerName + "&e, relocating...");
        }
        ChannelHandler handler = pipeline.get(PacketEvents.DECODER_NAME);
        if (handler == null) return false;

        pipeline.remove(PacketEvents.DECODER_NAME);
        pipeline.addBefore(VIA_DECODER, PacketEvents.DECODER_NAME, handler);
        return true;
    }

    private static boolean ensureEncoderOrder(ChannelPipeline pipeline, List<String> names, String playerName, boolean silent) {
        if (!names.contains(VIA_ENCODER) || !names.contains(PacketEvents.ENCODER_NAME)) {
            return false;
        }

        int viaIndex = names.indexOf(VIA_ENCODER);
        int peIndex = names.indexOf(PacketEvents.ENCODER_NAME);
        if (peIndex <= viaIndex) {
            return false;
        }

        if (!silent) {
            LogUtils.console("&3Yuki &8» &eViaVersion encoder is ahead of PacketEvents encoder for &b" + playerName + "&e, relocating...");
        }
        ChannelHandler handler = pipeline.get(PacketEvents.ENCODER_NAME);
        if (handler == null) return false;

        pipeline.remove(PacketEvents.ENCODER_NAME);
        pipeline.addBefore(VIA_ENCODER, PacketEvents.ENCODER_NAME, handler);
        return true;
    }
}
