package de.petanqueturniermanager.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TeamRanglisteTest {

	@Test
	public void testAddTeams() throws Exception {

		TeamRangliste liste = new TeamRangliste();

		liste.add(Team.from(1));
		assertThat(liste.getTeamListe()).hasSize(1);
		liste.add(Team.from(2));
		assertThat(liste.getTeamListe()).hasSize(2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddDoubleTeams() throws Exception {
		TeamRangliste liste = new TeamRangliste();
		liste.add(Team.from(1));
		liste.add(Team.from(1));
	}

}
