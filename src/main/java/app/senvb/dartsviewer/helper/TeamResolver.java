package app.senvb.dartsviewer.helper;

import java.util.Optional;

import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.Team;

public class TeamResolver {

    public static String resolveTeamName(int id, LeagueData leagueData) {
        Optional<Team> team = leagueData.getTeamByNumber(id);
        if (team.isPresent()) {
            return team.get().getName();
        }
        return "";
    }
}
