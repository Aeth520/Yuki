package cn.aetheris.yuki.util.latency;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PositionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.player.CooldownData;
import cn.aetheris.yuki.util.update.PositionUpdate;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemUseCooldown;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.resources.ResourceLocation;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;






@CheckData(utilityClass = true)
public final class CompensatedCooldown extends Check implements PositionCheck {
    private final ConcurrentHashMap<ResourceLocation, CooldownData> itemCooldownMap = new ConcurrentHashMap<>();

    public CompensatedCooldown(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPositionUpdate(final PositionUpdate positionUpdate) {
        for (Iterator<Map.Entry<ResourceLocation, CooldownData>> it = itemCooldownMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<ResourceLocation, CooldownData> entry = it.next();
            
            if (entry.getValue().getTransaction() < player.lastTransactionReceived.get()) {
                entry.getValue().tick();
            }

            
            if (entry.getValue().getTicksRemaining() <= 0) it.remove();
        }
    }

    public boolean hasItem(ItemStack item) {
        
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
            ItemUseCooldown cooldown = item.getComponentOr(ComponentTypes.USE_COOLDOWN, null);
            if (cooldown != null) {
                final Optional<ResourceLocation> cooldownGroup = cooldown.getCooldownGroup();
                
                
                if (cooldownGroup.isPresent()) {
                    return itemCooldownMap.containsKey(cooldownGroup.get());
                }
            }
        }

        return itemCooldownMap.containsKey(item.getType().getName());
    }

    
    public void addCooldown(ResourceLocation location, int cooldown, int transaction) {
        if (cooldown == 0) {
            removeCooldown(location);
            return;
        }

        itemCooldownMap.put(location, new CooldownData(cooldown, transaction));
    }

    public void removeCooldown(ResourceLocation location) {
        itemCooldownMap.remove(location);
    }
}
