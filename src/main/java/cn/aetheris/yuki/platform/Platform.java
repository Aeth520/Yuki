package cn.aetheris.yuki.platform;

import org.bukkit.plugin.Plugin;

/**
 * Abstraction over the hosting platform (Bukkit, Folia, Velocity, ...).
 * <p>
 * Keeps platform-specific lifecycle, logging and player management
 * out of the core plugin classes so the engine can be ported to other
 * platforms without touching the business logic.
 */
public interface Platform {

    /**
     * @return the backing {@link Plugin} instance.
     */
    Plugin getPlugin();

    /**
     * Send a colour-coded message to the console.
     *
     * @param message message with {@code &} colour codes
     */
    void console(String message);

    /**
     * Disable the anti-cheat: kick all online players and shut the plugin down.
     */
    void disablePlugin();

    /**
     * Register a Bukkit listener with the platform.
     *
     * @param listener the listener to register
     */
    void registerListener(org.bukkit.event.Listener listener);

    /**
     * Check that the runtime Java version meets the minimum requirement.
     *
     * @return {@code true} if the version is supported, {@code false} if the
     *         plugin should abort startup.
     */
    boolean checkJavaVersion();

    /**
     * @return the main server thread captured at construction time.
     */
    Thread getMainThread();
}
