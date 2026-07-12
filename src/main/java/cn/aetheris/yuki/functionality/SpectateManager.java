package cn.aetheris.yuki.functionality;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SpectateManager implements Init {

    public final Map<UUID, PreviousState> spectatingPlayers = new ConcurrentHashMap<>();
    private final Set<UUID> hiddenPlayers = ConcurrentHashMap.newKeySet();
    private final Set<String> allowedWorlds = new HashSet<>();
    private boolean checkWorld = false;

    @Override
    public void init() {
        allowedWorlds.addAll(PluginLoader.INSTANCE.getConfigManager().getConfig().getStringList("output.spectators.allowed-world"));
        checkWorld = !allowedWorlds.isEmpty();
    }

    
    public boolean shouldHidePlayer(PlayerData receiver, WrapperPlayServerPlayerInfo.PlayerData playerData) {
        UUID uuid = playerData.getUser() != null ? playerData.getUser().getUUID() : null;
        return uuid != null && shouldHidePlayer(receiver, uuid);
    }

    
    public boolean shouldHidePlayer(PlayerData receiver, UUID uuid) {
        if (uuid.equals(receiver.uuid)) return false;

        boolean isSpectatorOrHidden = spectatingPlayers.containsKey(uuid) || hiddenPlayers.contains(uuid);
        boolean receiverIsSpectator = receiver.uuid != null &&
                (spectatingPlayers.containsKey(receiver.uuid) || hiddenPlayers.contains(receiver.uuid));
        boolean notInAllowedWorld = checkWorld && receiver.bukkitPlayer != null &&
                !allowedWorlds.contains(receiver.bukkitPlayer.getWorld().getName());

        return isSpectatorOrHidden && !receiverIsSpectator && !notInAllowedWorld;
    }

    

    public boolean enable(Player player) {
        return spectatingPlayers.putIfAbsent(player.getUniqueId(),
                new PreviousState(player.getGameMode(), player.getLocation())) == null;
    }

    

    public void onLogin(Player player) {
        hiddenPlayers.add(player.getUniqueId());
    }

    

    public void onQuit(UUID uuid) {
        hiddenPlayers.remove(uuid);
        disable(uuid, true);
    }


    

    public void disable(UUID uuid, boolean teleportBack) {
        PreviousState previousState = spectatingPlayers.remove(uuid);
        if (previousState == null || !teleportBack) return;
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        MHDFScheduler.getRegionScheduler().runTask(Yuki.getInstance(), previousState.location, () -> {
            PaperUtils.teleport(player, previousState.location);
            player.setGameMode(previousState.gameMode);
        });
    }

    @ToString
    private static final class PreviousState {
        private final GameMode gameMode;
        private final Location location;

        private PreviousState(GameMode gameMode, Location location) {
            this.gameMode = gameMode;
            this.location = location;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PreviousState) obj;
            return Objects.equals(this.gameMode, that.gameMode) &&
                    Objects.equals(this.location, that.location);
        }
    }
}
