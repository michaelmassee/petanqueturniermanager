/**
 * Erstellung 17.01.2020 / Michael Massee
 */
package de.petanqueturniermanager.comp.turnierevent;

/**
 * @author Michael Massee
 *
 */
public interface ITurnierEventListener {

	/**
	 * @param eventObj
	 */
	default void onPropertiesChanged(ITurnierEvent eventObj) {
	}

}
