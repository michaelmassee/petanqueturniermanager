/**
 * Erstellung 24.05.2019 / Michael Massee
 */
package de.petanqueturniermanager.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.google.common.base.MoreObjects;

/**
 * @author Michael Massee
 */
public class TeamPaarung implements Cloneable {

	private Team a;
	private Optional<Team> b;

	/**
	 * wenn b = null dann freilos
	 *
	 * @param a
	 * @param b
	 */
	public TeamPaarung(Team a, Team b) {
		this(checkNotNull(a), (b == null) ? Optional.empty() : Optional.of(b));
	}

	public TeamPaarung(Team a) {
		this(checkNotNull(a), Optional.empty());
	}

	public TeamPaarung(Team a, Optional<Team> b) {
		checkNotNull(a);
		checkNotNull(b);
		if (b.isPresent()) {
			checkArgument(!a.equals(b.get()), "Team A == Team B");
		}
		this.a = a;
		this.b = b;
	}

	/**
	 * nur wenn B vorhanden dann A<->B Tauschen
	 */
	public void flipTeams() {
		if (b.isPresent()) {
			Team oldA = a;
			a = b.get();
			setB(oldA);
		}
	}

	public Team getA() {
		return a;
	}

	public Team getB() {
		return getOptionalB().orElse(null);
	}

	public Optional<Team> getOptionalB() {
		return b;
	}

	/**
	 * @param b
	 */
	public void setB(Team b) {
		setB(Optional.of(b));
	}

	public void setB(Optional<Team> b) {
		checkNotNull(b);
		this.b = b;
	}

	@Override
	public int hashCode() {
		return a.hashCode() + (b.isPresent() ? b.get().hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TeamPaarung)) {
			return false;
		}
		TeamPaarung teamPaarung = (TeamPaarung) obj;
		return getA().equals(teamPaarung.getA()) && bEquals(teamPaarung);
	}

	private boolean bEquals(TeamPaarung teamPaarung) {
		if (b.isPresent() && teamPaarung.getOptionalB().isPresent()) {
			return b.get().equals(teamPaarung.getOptionalB().get());
		}

		if (!b.isPresent() && !teamPaarung.getOptionalB().isPresent()) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {

		String teamsStr = "[";
		teamsStr += a.toString();
		teamsStr += ",";
		teamsStr += (b.isPresent()) ? b.get().toString() : null;
		teamsStr += "]";

		// @formatter:off
		return MoreObjects.toStringHelper(this)
				.add("Teams", teamsStr)
				.toString();
		// @formatter:on
	}

	@Override
	public Object clone() {
		if (b.isPresent()) {
			return new TeamPaarung(Team.from(a.nr), Team.from(b.get().nr));
		}
		return new TeamPaarung(Team.from(a.nr));
	}

}