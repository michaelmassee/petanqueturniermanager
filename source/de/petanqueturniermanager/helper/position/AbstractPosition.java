/**
* Erstellung : 26.03.2018 / Michael Massee
**/

package de.petanqueturniermanager.helper.position;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.MoreObjects;

public abstract class AbstractPosition<T> {
	private int zeile;
	private int spalte;

	public AbstractPosition() {
	}

	/**
	 *
	 * @param spalte (column)
	 * @param zeile (row)
	 */

	public AbstractPosition(int spalte, int zeile) {
		checkArgument(spalte > -1, "spalte (column) ungueltige wert %s. <0 ", spalte);
		checkArgument(zeile > -1, "zeile (row) ungueltige wert %s. <0 ", zeile);
		this.setZeile(zeile);
		this.setSpalte(spalte);
	}

	/**
	 * @return row
	 */
	public int getZeile() {
		return this.zeile;
	}

	/**
	 * @return zeile
	 */
	public int getRow() {
		return getZeile();
	}

	/**
	 * @return Column
	 */
	public int getSpalte() {
		return this.spalte;
	}

	/**
	 * @return spalte
	 */
	public int getColumn() {
		return getSpalte();
	}

	@SuppressWarnings("unchecked")
	public T zeilePlusEins() {
		this.setZeile(this.getZeile() + 1);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T zeilePlus(int anz) {
		this.setZeile(this.getZeile() + anz);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T zeile(int val) {
		this.setZeile(val);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T spaltePlusEins() {
		this.setSpalte(this.getSpalte() + 1);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T spaltePlus(int anz) {
		this.setSpalte(this.getSpalte() + anz);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T spalte(int val) {
		this.setSpalte(val);
		return (T) this;
	}

	private void setZeile(int zeile) {
		checkArgument(zeile > -1, "zeile (row) ungueltige wert %d. <0 ", zeile);
		this.zeile = zeile;
	}

	private void setSpalte(int spalte) {
		checkArgument(spalte > -1, "spalte (column) ungueltige wert %d. <0 ", spalte);
		this.spalte = spalte;
	}

	public String getAddress() {
		String aStr = "";
		if (getColumn() > 25)
			aStr += (char) ('A' + getColumn() / 26 - 1);
		aStr += (char) ('A' + getColumn() % 26);
		aStr += (getRow() + 1);
		return aStr;
	}

	@Override
	public String toString() {
		// @formatter:off
		return MoreObjects.toStringHelper(this)
				.add("Zeile (row)", this.getZeile())
				.add("Spalte (column)", this.getSpalte())
				.add("Adress", this.getAddress())
				.toString();
		// @formatter:on
	}
}