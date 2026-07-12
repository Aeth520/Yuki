package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.player.inventory.InventoryH;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketPlayerUseTotemListener extends AbstractPacketListener {

    private final Map<UUID, Long> lastPickupMap = new ConcurrentHashMap<>();

    public PacketPlayerUseTotemListener() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            if (!PluginLoader.INSTANCE.getConfigManager().isMitigateAutoTotem()) {
                return;
            }

            final WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
            final String playerName = event.getUser().getName();
            final Player player = Bukkit.getPlayer(playerName);

            if (player == null) {
                return;
            }

            if (packet.getWindowId() != 0 || packet.getSlot() != 45) {
                return;
            }

            final UUID playerId = player.getUniqueId();

            switch (packet.getWindowClickType()) {
                case SWAP:
                    handleHotbarSwap(packet, player, playerId, event);
                    break;
                case PICKUP_ALL:
                    handlePickupAll(player, playerId, packet);
                    break;
                case PICKUP:
                    handlePlaceAll(event, playerId, packet);
                    break;
            }
        }
    }


    
    private void handleHotbarSwap(WrapperPlayClientClickWindow packet, Player player, UUID playerId, PacketReceiveEvent event) {
        int hotbarSlot = 36 + packet.getButton();
        ItemStack hotbarItem = getInventoryItem(player, hotbarSlot);

        if (isTotem(hotbarItem)) {
            checkAndCancel(event, playerId, player.getName());
        }
    }

    
    private void handlePickupAll(Player player, UUID playerId, WrapperPlayClientClickWindow packet) {
        ItemStack currentItem = getInventoryItem(player, packet.getSlot());
        if (isTotem(currentItem)) {
            lastPickupMap.put(playerId, System.currentTimeMillis());
        }
    }

    
    private void handlePlaceAll(PacketReceiveEvent event, UUID playerId, WrapperPlayClientClickWindow packet) {
        if (isTotem(packet.getCarriedItemStack())) {
            checkAndCancel(event, playerId, event.getUser().getName());
        }
    }

    
    private void checkAndCancel(PacketReceiveEvent event, UUID playerId, String name) {
        long lastPickup = lastPickupMap.getOrDefault(playerId, 0L);
        long delay = System.currentTimeMillis() - lastPickup;

        if (delay < 150) {
            event.setCancelled(true);
            LogUtils.mitigate("&b" + name + "&7 Mitigate for using auto totem? (&b" + delay + "&7ms)");
            final PlayerData data = getData(event.getUser());
            if (data == null) {
                return;
            }
            final InventoryH check = data.getCheckManager().getCheck(InventoryH.class);
            if (check != null) {
                check.setBuffer(check.getBuffer() + 1);
                if (check.getBuffer() > 3.5) {
                    check.flagAndAlert("(Totem)\nlastTime= " + delay);
                }
            }
        }
    }

    
    private ItemStack getInventoryItem(Player player, int slot) {
        org.bukkit.inventory.ItemStack bukkitItem = player.getInventory().getItem(slot);
        if (bukkitItem == null) {
            return ItemStack.EMPTY;
        }
        return ItemStack.builder()
                .type(Objects.requireNonNull(ItemTypes.getByName(bukkitItem.getType().name())))
                .amount(bukkitItem.getAmount())
                .build();
    }


    
    private boolean isTotem(ItemStack item) {
        return item != null && !item.isEmpty() && item.getType().getName().getKey().contains("TOTEM");
    }
}