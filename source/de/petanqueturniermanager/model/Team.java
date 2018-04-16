/**
* Erstellung : 04.09.2017 / Michael Massee
**/

package de.petanqueturniermanager.model;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.petanqueturniermanager.algorithmen.AlgorithmenException;

public class Team implements Comparable<Team> {
	private final int nr;
	private final ArrayList<Spieler> spielerList;

	public Team(int nr) {
		checkArgument(nr > 0, "Team Nr <1");
		this.nr = nr;
		this.spielerList = new ArrayList<>();
	}

	public int getNr() {
		return this.nr;
	}

	public Team addSpielerWennNichtVorhanden(List<Spieler> spielerlist) throws AlgorithmenException {
		checkNotNull(spielerlist, "spielerlist == null");
		checkArgument(!spielerlist.isEmpty(), "spielerlist ist leer");

		for (Spieler spielerAusList : spielerlist) {
			addSpielerWennNichtVorhanden(spielerAusList);
		}
		return this;
	}

	public Team addSpielerWennNichtVorhanden(Spieler spieler) throws AlgorithmenException {
		checkNotNull(spieler, "spieler == null");

		if (!this.spielerList.contains(spieler)) {
			this.spielerList.add(spieler);
			addMitspieler(spieler);
			spieler.setTeam(this);
		}
		return this;
	}

	private Team addMitspieler(Spieler spieler) {
		checkNotNull(spieler, "spieler == null");

		for (Spieler spielerausList : this.spielerList) {
			spielerausList.addWarImTeamMitWennNichtVorhanden(spieler);
			spieler.addWarImTeamMitWennNichtVorhanden(spielerausList);
		}
		return this;
	}

	public int size() {
		return this.spielerList.size();
	}

	public Team removeAlleSpieler() throws AlgorithmenException {
		List<Spieler> spielerListClone = spieler();
		for (Spieler spielerausList : spielerListClone) {
			removeSpieler(spielerausList);
		}
		return this;
	}

	public Team removeSpieler(Spieler spieler) throws AlgorithmenException {
		checkNotNull(spieler, "spieler == null");
		this.spielerList.remove(spieler);
		spieler.deleteTeam();
		for (Spieler spielerausList : this.spielerList) {
			spieler.deleteWarImTeam(spielerausList);
			spielerausList.deleteWarImTeam(spieler);
		}
		return this;
	}

	public List<Spieler> spieler() {
		return new ArrayList<>(this.spielerList);
	}

	public boolean istSpielerImTeam(Spieler spieler) {
		return this.spielerList.contains(spieler);
	}

	public Spieler findSpielerByNr(int nr) {
		Spieler spieler = null;
		for (Spieler spielerausList : this.spielerList) {
			if (spielerausList.getNr() == nr) {
				spieler = spielerausList;
				break;
			}
		}
		return spieler;
	}

	public List<Spieler> listeVonSpielerOhneSpieler(Spieler spieler) {
		checkNotNull(spieler, "spieler == null");

		List<Spieler> spielerteamOhneSpieler = new ArrayList<>(this.spielerList);
		spielerteamOhneSpieler.remove(spieler);
		return spielerteamOhneSpieler;
	}

	public boolean hatZusammenGespieltMit(Spieler spieler) {
		checkNotNull(spieler, "spieler == null");
		boolean hatzusammenGespielt = false;

		for (Spieler spielerAusTeam : this.spielerList) {
			if (!spielerAusTeam.equals(spieler)) {
				hatzusammenGespielt |= spielerAusTeam.warImTeamMit(spieler);
			}
		}
		return hatzusammenGespielt;
	}

	@Override
	public int compareTo(Team team) {
		if (team == null) {
			return 1;
		}
		if (team.getNr() < getNr()) {
			return 1;
		}
		if (team.getNr() > getNr()) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Team)) {
			return false;
		}
		return getNr() == ((Team) obj).getNr();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.nr);
	}

	@Override
	public String toString() {

		String spielerNr = "[";
		for (Spieler spielerAusTeam : this.spielerList) {
			if (spielerNr.length() > 1) {
				spielerNr += ",";
			}
			spielerNr += spielerAusTeam.getNr();
		}
		spielerNr += "]";

		// @formatter:off
		return MoreObjects.toStringHelper(this)
				.add("nr", this.nr)
				.add("Spieler", spielerNr)
				.toString();
		// @formatter:on

	}

}