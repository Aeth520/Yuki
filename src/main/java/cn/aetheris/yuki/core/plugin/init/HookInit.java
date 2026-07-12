package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.functionality.AbstractHook;
import cn.aetheris.yuki.core.plugin.hooks.*;
import cn.aetheris.yuki.core.plugin.interfaces.Hook;
import cn.aetheris.yuki.util.message.LogUtils;
import lombok.Getter;

import java.util.logging.Level;

public final class HookInit implements Hook {
    @Getter
    private static final PacketEventsHook packetEventsHook = new PacketEventsHook();
    @Getter
    private static final PlaceholderAPIHook placeholderAPIHook = new PlaceholderAPIHook();
    @Getter
    private static final PaperMCHook paperMCHook = new PaperMCHook();
    @Getter
    private static final RegistryHook registryHook = new RegistryHook();
    @Getter
    private static final TABHook tabHook = new TABHook();
    @Getter
    private static final FloodgateHook floodgateHook = new FloodgateHook();
    @Getter
    private static final ViaPluginHook viaPluginHook = new ViaPluginHook();
    @Getter
    private static final MythicMobsHook mythicMobsHook = new MythicMobsHook();
    @Getter
    private static final PayPluginHook payPluginHook = new PayPluginHook();

    private static final AbstractHook[] NECESSARY_HOOKS = {packetEventsHook, registryHook};
    private static final AbstractHook[] OPTIONAL_HOOKS = {
            placeholderAPIHook, paperMCHook, tabHook, mythicMobsHook,
            payPluginHook, floodgateHook, viaPluginHook
    };

    @Override
    public void hook() {
        for (AbstractHook h : NECESSARY_HOOKS) {
            try {
                h.hook();
            } catch (Exception ex) {
                LogUtils.console("&cYuki &8» &4FATAL: Necessary hook &c" + h.getClass().getSimpleName() + " &4failed, disabling plugin.");
                Yuki.getInstance().getLogger().log(Level.SEVERE, "Hook failure", ex);
                Yuki.getInstance().disablePlugin();
                return;
            }
        }
        for (AbstractHook h : OPTIONAL_HOOKS) {
            try {
                h.hook();
            } catch (Exception ex) {
                LogUtils.console("&cYuki &8» &eOptional hook &c" + h.getClass().getSimpleName() + " &efailed, skipping.");
                Yuki.getInstance().getLogger().log(Level.WARNING, "Optional hook failure", ex);
            }
        }
    }

    @Override
    public void unhook() {
        for (AbstractHook h : NECESSARY_HOOKS) {
            try {
                h.unhook();
            } catch (Exception ignored) {
            }
        }
        for (AbstractHook h : OPTIONAL_HOOKS) {
            try {
                h.unhook();
            } catch (Exception ignored) {
            }
        }
    }
}
