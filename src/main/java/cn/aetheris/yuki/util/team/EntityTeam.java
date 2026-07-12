package cn.aetheris.yuki.util.team;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class EntityTeam {

    @Getter
    private final String name;
    @Getter
    private final Set<String> entries = new HashSet<>();
    private final PlayerData player;
    @Getter
    private WrapperPlayServerTeams.CollisionRule collisionRule;

    public EntityTeam(PlayerData player, String name) {
        this.player = player;
        this.name = name;
    }

    public void update(WrapperPlayServerTeams teams) {
        teams.getTeamInfo().ifPresent(info -> this.collisionRule = info.getCollisionRule());

        final TeamHandler teamHandler = player.checkManager.getCheck(TeamHandler.class);
        final WrapperPlayServerTeams.TeamMode mode = teams.getTeamMode();
        if (mode == WrapperPlayServerTeams.TeamMode.ADD_ENTITIES || mode == WrapperPlayServerTeams.TeamMode.CREATE) {
            label:
            for (String teamPlayer : teams.getPlayers()) {
                if (teamPlayer.equals(player.user.getName())) {
                    teamHandler.setPlayerTeam(this);
                    continue;
                }

                for (UserProfile profile : player.compensatedEntities.profiles.values()) {
                    if (profile.getName() != null && profile.getName().equals(teamPlayer)) {
                        teamHandler.addEntityToTeam(profile.getUUID().toString(), this);
                        continue label;
                    }
                }
                teamHandler.addEntityToTeam(teamPlayer, this);

            }
        } else if (mode == WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES) {
            label:
            for (String teamPlayer : teams.getPlayers()) {
                if (teamPlayer.equals(player.user.getName())) {
                    
                    teamHandler.setPlayerTeam(null);
                    continue;
                }
                for (UserProfile profile : player.compensatedEntities.profiles.values()) {
                    if (profile.getName() != null && profile.getName().equals(teamPlayer)) {
                        String uuid = profile.getUUID().toString();
                        entries.remove(uuid);
                        teamHandler.removeEntityFromTeam(uuid);
                        continue label;
                    }
                }
                
                teamHandler.removeEntityFromTeam(teamPlayer);
                entries.remove(teamPlayer);
            }
        } else if (mode == WrapperPlayServerTeams.TeamMode.REMOVE) {
            EntityTeam playersTeam = teamHandler.getPlayerTeam();
            
            if (playersTeam != null && playersTeam.name.equals(name)) {
                teamHandler.setPlayerTeam(null);
            }
            
            for (String entry : entries) {
                teamHandler.removeEntityFromTeam(entry);
            }
            entries.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityTeam)) return false;
        return Objects.equals(getName(), ((EntityTeam) o).getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}