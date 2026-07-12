package cn.aetheris.yuki.util.team;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;

public final class EntityPredicates {

    public static boolean canBePushedBy(EntityTeam entityTeam, EntityTeam playersTeam) {
        WrapperPlayServerTeams.CollisionRule collisionRule = entityTeam == null ? WrapperPlayServerTeams.CollisionRule.ALWAYS : entityTeam.getCollisionRule();
        if (collisionRule == WrapperPlayServerTeams.CollisionRule.NEVER) return false;
        WrapperPlayServerTeams.CollisionRule collisionRule2 = playersTeam == null ? WrapperPlayServerTeams.CollisionRule.ALWAYS : playersTeam.getCollisionRule();
        if (collisionRule2 == WrapperPlayServerTeams.CollisionRule.NEVER) {
            return false;
        } else {
            final boolean bl = entityTeam != null && entityTeam.equals(playersTeam);
            if ((collisionRule == WrapperPlayServerTeams.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == WrapperPlayServerTeams.CollisionRule.PUSH_OWN_TEAM) && bl) {
                return false;

            } else {
                return collisionRule != WrapperPlayServerTeams.CollisionRule.PUSH_OTHER_TEAMS && collisionRule2 != WrapperPlayServerTeams.CollisionRule.PUSH_OTHER_TEAMS || bl;
            }
        }
    }
}
