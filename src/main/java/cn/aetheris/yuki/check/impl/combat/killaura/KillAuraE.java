package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.Bukkit;

@CheckData(name = "KillAuraE (Swing)", type = CheckType.KILLAURA, configName = "KillAuraE", description = "check for no swing", decay = 0.85, experimental = true)
public final class KillAuraE extends Check implements PacketCheck {

    private final boolean isViaRewind = Bukkit.getPluginManager().getPlugin("ViaRewind") != null;
    private boolean swung;
    private int attacks;
    private int swings;

    public KillAuraE(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) && !isViaRewind) {
            if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
                if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    if (!swung) {
                        if (flagAndAlert("(1.8)")) {
                            event.setCancelled(true);
                            player.mitigateDamage();
                            player.onPacketCancel();
                        }
                    }
                }
            } else if (isFlying(event.getPacketType())) {
                swung = false;
            } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
                swung = true;
            }
        } else {
            if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
                if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    attacks++;
                }
            } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
                swings++;
            } else if (isFlying(event.getPacketType())) {
                
                
                
                if (attacks > swings) {
                    if (flagAndAlert("(1.9+)\na= " + attacks + "\ns= " + swings)) {
                        event.setCancelled(true);
                        player.mitigateDamage();
                        player.onPacketCancel();
                    }
                }
                attacks = 0;
                swings = 0;
            }
        }
    }
}