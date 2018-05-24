/**
* Erstellung : 09.04.2018 / Michael Massee
**/

package de.petanqueturniermanager.helper.sheet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class CloseConnections {
	private static final Logger logger = LogManager.getLogger(CloseConnections.class);

	/**
	 * Close alles !
	 */
	public static void closeOfficeConnection(XComponentContext xContext) {

		try {
			XModel xModel = UnoRuntime.queryInterface(XModel.class, DocumentHelper.getCurrentComponent(xContext));
			com.sun.star.lang.XComponent xDisposeable = UnoRuntime.queryInterface(com.sun.star.lang.XComponent.class, xModel);

			// Schließt Document !!!
			// xDisposeable.dispose();
		} catch (Throwable e) {
			logger.error("Exception disposing office process connection bridge:");
			logger.error(e.getMessage(), e);
		}
	}

}
