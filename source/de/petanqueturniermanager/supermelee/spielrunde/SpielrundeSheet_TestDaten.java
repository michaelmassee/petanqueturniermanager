/**
* Erstellung : 26.03.2018 / Michael Massee
**/
package de.petanqueturniermanager.supermelee.spielrunde;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.uno.XComponentContext;

import de.petanqueturniermanager.exception.GenerateException;
import de.petanqueturniermanager.helper.cellvalue.NumberCellValue;
import de.petanqueturniermanager.helper.position.Position;
import de.petanqueturniermanager.model.Meldungen;
import de.petanqueturniermanager.supermelee.meldeliste.MeldeListeSheet_TestDaten;
import de.petanqueturniermanager.supermelee.spieltagrangliste.SpieltagRanglisteSheet;

public class SpielrundeSheet_TestDaten extends AbstractSpielrundeSheet {
	private static final Logger logger = LogManager.getLogger(SpielrundeSheet_TestDaten.class);

	private final SpielrundeSheet_Naechste naechsteSpielrundeSheet;
	private final MeldeListeSheet_TestDaten meldeListeTestDatenGenerator;
	private final SpieltagRanglisteSheet spieltagRanglisteSheet;

	public SpielrundeSheet_TestDaten(XComponentContext xContext) {
		super(xContext);
		this.naechsteSpielrundeSheet = new SpielrundeSheet_Naechste(xContext);
		this.meldeListeTestDatenGenerator = new MeldeListeSheet_TestDaten(xContext);
		this.spieltagRanglisteSheet = new SpieltagRanglisteSheet(xContext);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected void doRun() throws GenerateException {
		generate();
		this.spieltagRanglisteSheet.generate();
		if (this.spieltagRanglisteSheet.isErrorInSheet()) {
			return;
		}
	}

	/**
	 * 5 spielrunden testdaten generieren
	 */
	private void generate() {

		this.meldeListeTestDatenGenerator.generateTestDaten();

		for (int spielrunde = 1; spielrunde < 5; spielrunde++) {
			this.getSheetHelper().removeSheet(this.getSheetName(spielrunde));
		}

		this.getKonfigurationSheet().setAktuelleSpielRunde(1);
		for (int spielrundeNr = 1; spielrundeNr < 5; spielrundeNr++) {

			if (spielrundeNr > 1) {
				this.meldeListeTestDatenGenerator.spielerAufAktivInaktivMischen();
			}

			Meldungen meldungen = this.getMeldeListe().getAktiveMeldungenAktuellenSpielTag();
			this.naechsteSpielrundeSheet.gespieltenRundenEinlesen(meldungen, spielrundeNr,
					getKonfigurationSheet().getSpielRundeNeuAuslosenAb());
			neueSpielrunde(meldungen, spielrundeNr, true);

			// ------------------------------------
			// spiel test ergebnisse einfuegen
			// ------------------------------------
			XSpreadsheet sheet = getSpielRundeSheet(spielrundeNr);
			Position letztePos = letzteZeile(spielrundeNr);

			if (letztePos != null && sheet != null) {
				for (int zeileCntr = ERSTE_DATEN_ZEILE; zeileCntr <= letztePos.getZeile(); zeileCntr++) {
					Position pos = Position.from(ERSTE_SPALTE_ERGEBNISSE, zeileCntr);

					int welchenTeamHatGewonnen = ThreadLocalRandom.current().nextInt(0, 2); // 0,1
					int verliererPunkte = ThreadLocalRandom.current().nextInt(0, 13); // 0 - 12
					// gewinner kann auch weniger als 13 punkte
					int gewinnerPunkte = ThreadLocalRandom.current().nextInt(verliererPunkte + 1, 14); // random rest
																										// bis 13
					int valA = (welchenTeamHatGewonnen == 0 ? verliererPunkte : gewinnerPunkte);
					int valB = (welchenTeamHatGewonnen == 0 ? gewinnerPunkte : verliererPunkte);

					NumberCellValue numberCellValue = NumberCellValue.from(sheet, pos, valA);
					getSheetHelper().setValInCell(numberCellValue);
					getSheetHelper().setValInCell(numberCellValue.spaltePlusEins().setValue((double) valB));
				}
			}
		}

	}
}