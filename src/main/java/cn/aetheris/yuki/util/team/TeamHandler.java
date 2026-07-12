package cn.aetheris.yuki.util.team;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public final class TeamHandler extends Check implements PacketCheck {

    private final Map<String, EntityTeam> entityTeams = new Object2ObjectOpenHashMap<>();
    private final Map<String, EntityTeam> entityToTeam = new Object2ObjectOpenHashMap<>();
    private @Getter
    @Setter
    @Nullable EntityTeam playerTeam = null;

    public TeamHandler(PlayerData player) {
        super(player);
    }

    public void addEntityToTeam(String entityTeamRepresentation, EntityTeam team) {
        entityToTeam.put(entityTeamRepresentation, team);
    }

    public void removeEntityFromTeam(String entityTeamRepresentation) {
        entityToTeam.remove(entityTeamRepresentation);
    }

    public EntityTeam getEntityTeam(PacketEntity entity) {
        final UUID uuid = entity.getUuid();
        return uuid == null ? null : entityToTeam.get(uuid.toString());
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.TEAMS) {
            WrapperPlayServerTeams teams = new WrapperPlayServerTeams(event);
            final String teamName = teams.getTeamName();
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                EntityTeam entityTeam;
                switch (teams.getTeamMode()) {
                    case CREATE -> {
                        var newTeam = new EntityTeam(player, teamName);
                        entityTeams.put(teamName, newTeam);
                        entityTeam = newTeam;
                    }
                    case REMOVE -> entityTeam = entityTeams.remove(teamName);
                    default -> entityTeam = entityTeams.get(teamName);
                }

                if (entityTeam != null) {
                    entityTeam.update(teams);
                }
            });
        }
    }
}