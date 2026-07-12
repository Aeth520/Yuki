package cn.aetheris.yuki.check.impl.misc.client;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

import java.util.Map;
import java.util.StringJoiner;

@CheckData(name = "ClientA (Payload)", configName = "ClientA", type = CheckType.CLIENT, description = "Sent Invalid Payload While Player Joined")
public final class ClientA extends Check implements PacketCheck {

    private static final Map<String, String> BRAND_MAP = Map.ofEntries(
            Map.entry("sb123", "XinXin"),
            Map.entry("LMC", "LabyMod"),
            Map.entry("labymod3:main", "OldLabyMod"),
            Map.entry("Lunar-Client", "Lunar"),
            Map.entry("lunarclient:pelBWJH", "Lunar"),
            Map.entry("CB", "CheatBreaker"),
            Map.entry("PLC18", "PvPLounge"),
            Map.entry("Cracked Vape", "Cracked Vape"),
            Map.entry("fuck", "FuckClient"),
            Map.entry("Misplace", "MisPlacement"),
            Map.entry("Reach Mod", "Reach-Mod"),
            Map.entry("Synergy", "Synergy"),
            Map.entry("\u0007Synergy", "Invalid-Synergy"),
            Map.entry("utvIx", "ETB1"),
            Map.entry("NNfMC", "ETB2"),
            Map.entry("enjoytheban", "ETB3")
    );
    private final StringJoiner joiner = new StringJoiner(" ");
    private boolean shouldCheckMeteor;

    public ClientA(PlayerData player) {
        super(player);
    }

    @Override

    public void onPacketReceive(PacketReceiveEvent event) {
        String brand = player.getBrand();

        if (BRAND_MAP.containsKey(brand)) {
            joiner.add(BRAND_MAP.get(brand));
        }

        if ("geyser".equals(brand)) {
            joiner.add("SpoofedGeyser");
        } else if (brand.contains("lunarclient:aV4exCC")) {
            joiner.add("LunarClient");
        }

        if ("原版".equals(brand)) {
            if (shouldCheckMeteor) {
                joiner.add("Meteor");
            } else {
                alert("Meteor?");
            }
        }

        if (joiner.length() > 0 && flagAndAlert("Type= " + joiner)) {
            event.setCancelled(true);
            kickPlayer();
        }
    }

    @Override
    public void reload() {
        super.reload();
        shouldCheckMeteor = getConfig().getBooleanElse(getConfigName() + ".check-for-meteor", false);
    }
}

