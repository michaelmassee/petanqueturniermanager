/**
* Erstellung : 30.04.2018 / Michael Massee
**/

package de.petanqueturniermanager.meldeliste;

import com.sun.star.uno.XComponentContext;

import de.petanqueturniermanager.exception.GenerateException;

public class MeldeListeSheet_Update extends AbstractMeldeListeSheet {

	public MeldeListeSheet_Update(XComponentContext xContext) {
		super(xContext);
	}

	@Override
	protected void doRun() throws GenerateException {
		upDateSheet();
	}

}
