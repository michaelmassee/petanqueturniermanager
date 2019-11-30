/**
* Erstellung : 24.03.2018 / Michael Massee
**/

package de.petanqueturniermanager.liga.meldeliste;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.star.sheet.XSpreadsheet;

import de.petanqueturniermanager.basesheet.konfiguration.IKonfigurationKonstanten;
import de.petanqueturniermanager.basesheet.meldeliste.MeldeListeKonstanten;
import de.petanqueturniermanager.comp.WorkingSpreadsheet;
import de.petanqueturniermanager.exception.GenerateException;
import de.petanqueturniermanager.helper.position.Position;
import de.petanqueturniermanager.helper.sheet.RangeHelper;
import de.petanqueturniermanager.helper.sheet.TurnierSheet;
import de.petanqueturniermanager.helper.sheet.rangedata.RangeData;
import de.petanqueturniermanager.helper.sheet.rangedata.RowData;
import de.petanqueturniermanager.liga.konfiguration.LigaSheet;
import de.petanqueturniermanager.supermelee.SupermeleeTeamPaarungenSheet;

public class LigaMeldeListeSheet_TestDaten extends LigaSheet {

	private static final Logger logger = LogManager.getLogger(LigaMeldeListeSheet_TestDaten.class);

	private final LigaMeldeListeSheet_New meldeListe;

	public LigaMeldeListeSheet_TestDaten(WorkingSpreadsheet workingSpreadsheet) {
		super(workingSpreadsheet);
		meldeListe = new LigaMeldeListeSheet_New(workingSpreadsheet);
	}

	@Override
	public Logger getLogger() {
		return LigaMeldeListeSheet_TestDaten.logger;
	}

	@Override
	protected void doRun() throws GenerateException {
		// clean up first
		getSheetHelper().removeAllSheetsExclude(new String[] { IKonfigurationKonstanten.SHEETNAME, SupermeleeTeamPaarungenSheet.SHEETNAME });
		testNamenEinfuegen();
	}

	/**
	 * @throws GenerateException
	 */
	public void testNamenEinfuegen() throws GenerateException {
		XSpreadsheet meldelisteSheet = meldeListe.getXSpreadSheet();
		TurnierSheet.from(meldelisteSheet, getWorkingSpreadsheet()).setActiv();

		List<String> testNamen = listeMitTestNamen();
		Collections.shuffle(testNamen);

		RangeData data = new RangeData();

		int cntr = 0;
		for (String name : testNamen) {
			RowData newTeam = data.newRow();
			newTeam.newEmpty();
			newTeam.newString(name);
			if (cntr++ > 5) {
				// 6 Teams
				break;
			}
		}
		Position posSpielerNr = Position.from(MeldeListeKonstanten.SPIELER_NR_SPALTE, MeldeListeKonstanten.ERSTE_DATEN_ZEILE - 1);
		RangeHelper.from(meldelisteSheet, data.getRangePosition(posSpielerNr)).setDataInRange(data);
		meldeListe.upDateSheet();
	}

	// Testdaten Generator
	// http://migano.de/testdaten.php

	List<String> listeMitTestNamen() {
		List<String> testNamen = new ArrayList<>();
		testNamen.add("BC-Linden 1");
		testNamen.add("Boule Biebertal");
		testNamen.add("Boule-Freunde Fernwald");
		testNamen.add("PC Petterweil");
		testNamen.add("PSG Ehringshausen 1");
		testNamen.add("DFG Wettenberg 1");
		testNamen.add("Boulefreunde Marburg");
		testNamen.add("Boulodromedare Fulda 2");
		testNamen.add("VNH Hain-Gründau 1");
		return testNamen;
	}

}
