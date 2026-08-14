package cn.aetheris.yuki.listener.bukkit;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.impl.player.autototem.AutoTotemA;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInventoryListener extends AbstractListener {

    private final Map<UUID, PlayerTotemState> playerStates = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onResurrectComplete(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.isCancelled()) return;
        PlayerTotemState state = getOrCreateState(player);
        EntityEquipment equipment = player.getEquipment();

        Bukkit.getScheduler().runTaskLater(Yuki.getInstance(), () ->
                checkTotemConsumption(player, state, equipment), 3L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerStates.remove(event.getPlayer().getUniqueId());
    }

    private void checkTotemConsumption(Player player, PlayerTotemState state, EntityEquipment equipment) {
        ItemStack currentOffhand = equipment.getItemInOffHand();
        ItemStack currentMainhand = equipment.getItemInMainHand();

        if (state.preResurrectOffhand != null
                && state.preResurrectOffhand.getType() == Material.TOTEM_OF_UNDYING
                && currentOffhand.getType() == Material.TOTEM_OF_UNDYING
                && state.preResurrectOffhand.isSimilar(currentOffhand)) {

            triggerDetection(player);
        }

        if (state.preResurrectMainhand != null
                && state.preResurrectMainhand.getType() == Material.TOTEM_OF_UNDYING
                && currentMainhand.getType() == Material.TOTEM_OF_UNDYING
                && state.preResurrectMainhand.isSimilar(currentMainhand)) {

            triggerDetection(player);
        }
    }

    private void triggerDetection(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            PlayerData data = getData(player);
            if (data == null) return;

            AutoTotemA check = data.getCheckManager().getCheck(AutoTotemA.class);
            if (check == null) return;


            check.flagAndAlert("5L");
        });

    }

    private PlayerTotemState getOrCreateState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), k -> new PlayerTotemState());
    }

    private static class PlayerTotemState {
        ItemStack preResurrectOffhand = null;
        ItemStack preResurrectMainhand = null;
    }

}
