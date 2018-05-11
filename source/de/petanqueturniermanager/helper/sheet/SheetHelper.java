/**
* Erstellung : 10.03.2018 / Michael Massee
**/

package de.petanqueturniermanager.helper.sheet;

import static com.google.common.base.Preconditions.*;
import static de.petanqueturniermanager.helper.cellvalue.CellProperties.*;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.util.Arrays;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sheet.*;
import com.sun.star.table.CellAddress;
import com.sun.star.table.CellHoriJustify;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XColumnRowRange;
import com.sun.star.table.XTableColumns;
import com.sun.star.table.XTableRows;
import com.sun.star.text.XText;
import com.sun.star.uno.Any;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XMergeable;

import de.petanqueturniermanager.helper.cellvalue.AbstractCellValue;
import de.petanqueturniermanager.helper.cellvalue.CellProperties;
import de.petanqueturniermanager.helper.cellvalue.NumberCellValue;
import de.petanqueturniermanager.helper.cellvalue.StringCellValue;
import de.petanqueturniermanager.helper.position.FillAutoPosition;
import de.petanqueturniermanager.helper.position.Position;
import de.petanqueturniermanager.helper.position.RangePosition;

// welche service ???
// https://www.openoffice.org/api/docs/common/ref/com/sun/star/lang/XComponent-xref.html

public class SheetHelper {

	private static final Logger logger = LogManager.getLogger(SheetHelper.class);

	private final XComponentContext xContext;
	private final HashMap<String, XSpreadsheet> sheetCache = new HashMap<>(); // not static!

	public SheetHelper(XComponentContext xContext) {
		checkNotNull(xContext);
		this.xContext = xContext;
	}

	public XSpreadsheet findByName(String sheetName) {
		checkNotNull(sheetName);

		if (this.sheetCache.get(sheetName) != null) {
			return this.sheetCache.get(sheetName);
		}

		XSpreadsheet foundSpreadsheet = null;
		XSpreadsheets sheets = getSheets();

		if (sheets != null) {
			try {
				if (sheets.hasByName(sheetName)) {
					Any currentDoc = (Any) sheets.getByName(sheetName);
					foundSpreadsheet = (XSpreadsheet) currentDoc.getObject();
				}
			} catch (NoSuchElementException | WrappedTargetException e) {
				// ignore
			}
		}

		this.sheetCache.put(sheetName, foundSpreadsheet);
		return foundSpreadsheet;
	}

	public void removeSheet(String sheetName) {
		checkNotNull(sheetName);
		this.sheetCache.remove(sheetName);
		XSpreadsheets sheets = getSheets();
		try {
			sheets.removeByName(sheetName);
		} catch (NoSuchElementException | WrappedTargetException e) {
			// ignore
		}
	}

	public XSpreadsheet newIfNotExist(String sheetName, short pos) {
		return newIfNotExist(sheetName, pos, null);
	}

	public XSpreadsheet newIfNotExist(String sheetName, short pos, String tabColor) {
		checkNotNull(sheetName);

		if (this.sheetCache.get(sheetName) != null) {
			return this.sheetCache.get(sheetName);
		}

		XSpreadsheet currSpreadsheet = null;
		XSpreadsheets sheets = getSheets();

		if (sheets != null) {
			if (!sheets.hasByName(sheetName)) {
				sheets.insertNewByName(sheetName, pos);

				if (tabColor != null) {
					currSpreadsheet = this.findByName(sheetName);
					setTabColor(currSpreadsheet, tabColor);
				}
			}
			currSpreadsheet = this.findByName(sheetName);
		}
		return currSpreadsheet;
	}

	/**
	 * @return null wenn kein getCurrentSpreadsheetDocument
	 */

	public XSpreadsheets getSheets() {
		XSpreadsheetDocument spreadsheetDoc = DocumentHelper.getCurrentSpreadsheetDocument(this.xContext);
		if (spreadsheetDoc != null) {
			return spreadsheetDoc.getSheets();
		}
		return null;
	}

	@Deprecated
	public XCell setValInCell(XSpreadsheet sheet, int spalte, int zeile, double val) {
		return setValInCell(sheet, Position.from(spalte, zeile), val);
	}

	public XCell setValInCell(NumberCellValue numberCellValue) {
		checkNotNull(numberCellValue);
		XCell xCell = setValInCell(numberCellValue.getSheet(), numberCellValue.getPos(), numberCellValue.getValue());
		handleAbstractCellValue(numberCellValue, xCell);
		return xCell;
	}

	public XCell setValInCell(XSpreadsheet sheet, Position pos, int val) {
		return setValInCell(sheet, pos, (double) val);
	}

	public XCell setValInCell(XSpreadsheet sheet, Position pos, double val) {
		checkNotNull(sheet);
		checkNotNull(pos);
		XCell xCell = null;
		try {
			xCell = sheet.getCellByPosition(pos.getSpalte(), pos.getZeile());
			xCell.setValue(val);
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
		return xCell;
	}

	public XCell setValInCell(XSpreadsheet sheet, Position pos, NumberCellValue cellValue) {
		checkNotNull(sheet);
		checkNotNull(pos);
		XCell xCell = setValInCell(sheet, pos, cellValue.getValue());
		return xCell;
	}

	@Deprecated
	public XCell setFormulaInCell(XSpreadsheet sheet, int spalte, int zeile, String formula) {
		return setFormulaInCell(sheet, Position.from(spalte, zeile), formula);
	}

	public XCell setFormulaInCell(StringCellValue stringVal) {
		checkNotNull(stringVal);

		XCell xCell = null;
		xCell = setFormulaInCell(stringVal.getSheet(), stringVal.getPos(), stringVal.getValue());

		handleAbstractCellValue(stringVal, xCell);
		return xCell;
	}

	public XCell setFormulaInCell(XSpreadsheet sheet, Position pos, String formula) {
		checkNotNull(sheet);
		checkNotNull(pos);
		checkNotNull(formula);

		XCell xCell = null;
		try {
			xCell = sheet.getCellByPosition(pos.getSpalte(), pos.getZeile());
			xCell.setFormula(StringUtils.prependIfMissing(formula, "="));
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
		return xCell;
	}

	@Deprecated
	public XCell setTextInCell(XSpreadsheet sheet, int spalte, int zeile, String val) {
		return setTextInCell(sheet, Position.from(spalte, zeile), val);
	}

	// StringCellValue
	public XCell setTextInCell(StringCellValue stringVal) {
		checkNotNull(stringVal);
		XCell xCell = null;
		xCell = setTextInCell(stringVal.getSheet(), stringVal.getPos(), stringVal.getValue());

		handleAbstractCellValue(stringVal, xCell);

		return xCell;
	}

	private void handleAbstractCellValue(AbstractCellValue<?, ?> cellVal, XCell xCell) {
		checkNotNull(cellVal);
		checkNotNull(xCell);

		// zellen merge ?
		if (cellVal.getPos() != null && cellVal.getEndPosMerge() != null) {
			mergeRange(cellVal.getSheet(), RangePosition.from(cellVal.getPos(), cellVal.getEndPosMerge()));
		}

		// kommentar ?
		if (cellVal.getPos() != null && cellVal.hasComment()) {
			setCommentInCell(cellVal.getSheet(), xCell, cellVal.getComment());
		}

		XPropertySet xPropertySetColumn = null;
		// spalte breite
		if (cellVal.getPos() != null && cellVal.getSetColumnWidth() > 0) {
			xPropertySetColumn = setColumnWidth(cellVal.getSheet(), cellVal.getPos(), cellVal.getSetColumnWidth());
		}

		// spalte ausrichten
		if (cellVal.getSpalteHoriJustify() != null) {
			if (xPropertySetColumn != null) {
				setProperty(xPropertySetColumn, HORI_JUSTIFY, cellVal.getSpalteHoriJustify());
			} else {
				setColumnCellHoriJustify(cellVal.getSheet(), cellVal.getPos(), cellVal.getSpalteHoriJustify());
			}
		}

		// fill
		if (cellVal.getFillAuto() != null) {
			FillAutoPosition fillAuto = cellVal.getFillAuto();
			fillAuto(cellVal.getSheet(), RangePosition.from(cellVal.getPos(), cellVal.getFillAuto()),
					fillAuto.getFillDirection());
		}

		// Zellen Properties ?
		if (!cellVal.getCellProperties().isEmpty()) {
			XPropertySet xPropSetCell = getCellPropertySet(xCell);
			setProperties(xPropSetCell, cellVal.getCellProperties());
		}
	}

	public void fillAuto(XSpreadsheet sheet, RangePosition rangePos, FillDirection fillDirection) {
		try {
			XCellRange xCellRange = sheet.getCellRangeByPosition(rangePos.getStartSpalte(), rangePos.getStartZeile(),
					rangePos.getEndeSpalte(), rangePos.getEndeZeile());
			XCellSeries xCellSeries = UnoRuntime.queryInterface(XCellSeries.class, xCellRange);
			xCellSeries.fillAuto(fillDirection, 1);
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param sheet
	 * @param spalte,column, 0 = erste spalte = A
	 * @param zeile,row, 0 = erste zeile
	 * @return XCell
	 */

	public XCell setTextInCell(XSpreadsheet sheet, Position pos, String val) {
		checkNotNull(sheet);
		checkNotNull(pos);
		checkNotNull(val);

		XCell xCell = null;
		try {
			// --- Get cell B3 by position - (column, row) ---
			// xCell = xSheet.getCellByPosition(1, 2);
			xCell = sheet.getCellByPosition(pos.getSpalte(), pos.getZeile());
			XText xText = UnoRuntime.queryInterface(XText.class, xCell);
			xText.setString(val);
		} catch (IndexOutOfBoundsException | IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		}
		return xCell;
	}

	@Deprecated
	public Integer getIntFromCell(XSpreadsheet sheet, int spalte, int zeile) {
		return getIntFromCell(sheet, new Position(spalte, zeile));
	}

	/**
	 * @param sheet
	 * @param spalte,column, 0 = erste spalte = A
	 * @param zeile,row, 0 = erste zeile
	 * @return -1 when not found
	 */
	public Integer getIntFromCell(XSpreadsheet sheet, Position pos) {
		checkNotNull(sheet);
		checkNotNull(pos);

		return NumberUtils.toInt(getTextFromCell(sheet, pos), -1);
	}

	/**
	 * @param sheet
	 * @param spalte,column, 0 = erste spalte = A
	 * @param zeile,row, 0 = erste zeile
	 * @return timed textval, null when not found
	 */
	public String getTextFromCell(XSpreadsheet sheet, Position pos) {
		checkNotNull(sheet);
		checkNotNull(pos);

		XText xText = getXTextFromCell(sheet, pos);
		if (xText != null && xText.getString() != null) {
			return xText.getString().trim();
		}
		return null;
	}

	/**
	 * use getXTextFromCell(XSpreadsheet sheet, Position pos)
	 */
	@Deprecated
	public XText getXTextFromCell(XSpreadsheet sheet, int spalte, int zeile) {
		return getXTextFromCell(sheet, new Position(spalte, zeile));
	}

	public XText getXTextFromCell(XSpreadsheet sheet, Position pos) {
		checkNotNull(sheet);
		checkNotNull(pos);

		XText xText = null;
		try {
			XCell xCell = sheet.getCellByPosition(pos.getSpalte(), pos.getZeile());
			xText = UnoRuntime.queryInterface(XText.class, xCell);
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
		return xText;
	}

	public String getAddressFromCellAsString(XCell xCell) {
		checkNotNull(xCell);

		XCellAddressable xCellAddr = UnoRuntime.queryInterface(XCellAddressable.class, xCell);
		CellAddress aAddress = xCellAddr.getCellAddress();
		return getAddressFromColumnRow(new Position(aAddress.Column, aAddress.Row));
	}

	@Deprecated
	public String getAddressFromColumnRow(int column, int row) {
		return getAddressFromColumnRow(new Position(column, row));
	}

	/**
	 * @param column = spalte, erste spalte = 0
	 * @param row = zeile, erste zeile = 0
	 * @return "A2"
	 */
	public String getAddressFromColumnRow(Position pos) {
		checkNotNull(pos);
		try {
			Object aFuncInst = this.xContext.getServiceManager()
					.createInstanceWithContext("com.sun.star.sheet.FunctionAccess", this.xContext);
			XFunctionAccess xFuncAcc = UnoRuntime.queryInterface(XFunctionAccess.class, aFuncInst);
			// https://wiki.openoffice.org/wiki/Documentation/How_Tos/Calc:_ADDRESS_function
			// put the data in a array
			Object[] data = { pos.getRow() + 1, pos.getColumn() + 1, 4, 1 };
			Object addressString = xFuncAcc.callFunction("ADDRESS", data);
			if (addressString != null) {
				return addressString.toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * @param zeileNr = 0 = erste Zeile
	 * @param searchStr
	 * @return spalte 0 = erste Spalte, -1 when not found
	 */
	public int findSpalteInZeileNachString(XSpreadsheet sheet, int zeileNr, String searchStr) {
		checkNotNull(sheet);
		checkNotNull(searchStr);

		int spalteNr = -1;

		// Primitiv search
		for (int spalteCntr = 0; spalteCntr < 999; spalteCntr++) {
			String text = getTextFromCell(sheet, Position.from(spalteCntr, zeileNr));
			if (text.equals(searchStr)) {
				spalteNr = spalteCntr;
				break;
			}
		}
		return spalteNr;
	}

	public void setActiveSheet(XSpreadsheet spreadsheet) {
		XSpreadsheetView spreadsheetView = DocumentHelper.getCurrentSpreadsheetView(this.xContext);
		if (spreadsheetView != null) {
			spreadsheetView.setActiveSheet(spreadsheet);
		}
	}

	/**
	 * 1. in google nach begriff "color chooser" suchen. -> Color chooser verwenden, hex code ohne #<br>
	 * 2. Color chooser in Zelle verwenden-> hex code kopieren <br>
	 *
	 * @param xSheet
	 * @param hex, 6 stellige farbcode, ohne # oder sonstige vorzeichen !
	 */
	public void setTabColor(XSpreadsheet xSheet, String hex) {
		setTabColor(xSheet, Integer.parseInt(hex, 16));
	}

	/**
	 * 1. in google nach begriff "color chooser" suchen. -> Color chooser verwenden, hex code ohne #<br>
	 * 2. Color chooser in Zelle verwenden-> hex code kopieren und <br>
	 * setTabColor(XSpreadsheet xSheet, String hex) verwenden <br>
	 * <br>
	 * Property TabColor in Sheet <br>
	 * list of properties <br>
	 * https://api.libreoffice.org/docs/idl/ref/servicecom_1_1sun_1_1star_1_1sheet_1_1Spreadsheet.html#details
	 *
	 * @param xSheet
	 * @param color int val. convert from hex z.b. Integer.valueOf(0x003399), Integer.parseInt("003399", 16)
	 */
	public void setTabColor(XSpreadsheet xSheet, int color) {
		checkNotNull(xSheet);
		XPropertySet xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, xSheet);
		try {
			xPropSet.setPropertyValue("TabColor", new Integer(color));
		} catch (IllegalArgumentException | UnknownPropertyException | PropertyVetoException
				| WrappedTargetException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public XCellRange mergeRange(XSpreadsheet sheet, RangePosition rangePosition) {
		XCellRange xCellRange = null;
		try {
			xCellRange = sheet.getCellRangeByPosition(rangePosition.getStartSpalte(), rangePosition.getStartZeile(),
					rangePosition.getEndeSpalte(), rangePosition.getEndeZeile());
			XMergeable xMerge = UnoRuntime.queryInterface(com.sun.star.util.XMergeable.class, xCellRange);
			xMerge.merge(true);
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
		return xCellRange;
	}

	// zeile
	public Object getRow(XSpreadsheet sheet, int zeile) {
		checkNotNull(sheet);
		Object aColumnObj = null;
		try {
			// spalte, zeile
			XCellRange xCellRange = sheet.getCellRangeByPosition(0, zeile, 1, zeile);
			XColumnRowRange xColRowRange = UnoRuntime.queryInterface(XColumnRowRange.class, xCellRange);
			XTableRows rows = xColRowRange.getRows();
			aColumnObj = rows.getByIndex(0);
		} catch (IndexOutOfBoundsException | WrappedTargetException | IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		}
		return aColumnObj;
	}

	// spalte
	public Object getColumn(XSpreadsheet sheet, int spalte) {
		checkNotNull(sheet);
		Object aColumnObj = null;
		try {
			// spalte, zeile
			XCellRange xCellRange = sheet.getCellRangeByPosition(spalte, 0, spalte, 1);
			XColumnRowRange xColRowRange = UnoRuntime.queryInterface(XColumnRowRange.class, xCellRange);
			XTableColumns columns = xColRowRange.getColumns();
			aColumnObj = columns.getByIndex(0);
		} catch (IndexOutOfBoundsException | WrappedTargetException | IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		}
		return aColumnObj;
	}

	// zeile
	public XPropertySet setRowProperty(XSpreadsheet sheet, int zeile, String key, Object val) {
		XPropertySet xPropSet = getRowPropertySet(sheet, zeile);
		if (xPropSet != null) {
			setProperty(xPropSet, key, val);
		}
		return xPropSet;
	}

	// zeile
	public XPropertySet getRowPropertySet(XSpreadsheet sheet, int zeile) {
		checkNotNull(sheet);
		Object aRowObj = getRow(sheet, zeile);
		XPropertySet xPropSet = null;
		if (aRowObj != null) {
			xPropSet = UnoRuntime.queryInterface(XPropertySet.class, aRowObj);
		}
		return xPropSet;
	}

	public XPropertySet setColumnProperties(XSpreadsheet sheet, int spalte, CellProperties properties) {
		checkNotNull(sheet);
		checkNotNull(properties);
		XPropertySet xPropSet = getColumnPropertySet(sheet, spalte);
		setProperties(xPropSet, properties);
		return xPropSet;
	}

	// spalte
	public XPropertySet setColumnProperty(XSpreadsheet sheet, int spalte, String key, Object val) {
		checkNotNull(sheet);
		XPropertySet xPropSet = getColumnPropertySet(sheet, spalte);
		if (xPropSet != null) {
			setProperty(xPropSet, key, val);
		}
		return xPropSet;
	}

	// spalte
	public XPropertySet getColumnPropertySet(XSpreadsheet sheet, int spalte) {
		checkNotNull(sheet);
		Object aColumnObj = getColumn(sheet, spalte);
		XPropertySet xPropSet = null;
		if (aColumnObj != null) {
			xPropSet = UnoRuntime.queryInterface(XPropertySet.class, aColumnObj);
		}
		return xPropSet;
	}

	public void inpectPropertySet(XPropertySet xPropSet) {
		XPropertySetInfo propertySetInfo = xPropSet.getPropertySetInfo();
		Property[] properties = propertySetInfo.getProperties();
		Arrays.asList(properties).forEach((property) -> {
			System.out.println(((Property) property).Name);
			System.out.println(((Property) property).Type);
		});
	}

	public void setProperties(XPropertySet xPropSet, CellProperties properties) {
		properties.forEach((key, value) -> {
			setProperty(xPropSet, key, value);
		});
	}

	public void setProperties(XPropertySet xPropSet, HashMap<String, Object> properties) {
		properties.forEach((key, value) -> {
			setProperty(xPropSet, key, value);
		});
	}

	public void setProperty(XPropertySet xPropSet, String key, Object val) {
		checkNotNull(key);
		checkNotNull(val);
		checkNotNull(xPropSet);

		try {
			xPropSet.setPropertyValue(key, val);
		} catch (IllegalArgumentException | UnknownPropertyException | PropertyVetoException
				| WrappedTargetException e) {
			logger.error("Property '" + key + "' = '" + val + "'\r" + e.getMessage(), e);
		}
	}

	/**
	 * example properties <br>
	 *
	 * @see #setPropertyInCell(XSpreadsheet, Position, String, Object)
	 * @param sheet
	 * @param pos
	 * @param name
	 * @param val
	 * @return xcellrange, null when error
	 */

	public XCellRange setPropertyInRange(XSpreadsheet sheet, RangePosition pos, String key, Object val) {
		checkNotNull(sheet);
		checkNotNull(pos);
		checkNotNull(key);
		checkNotNull(val);
		CellProperties properties = new CellProperties();
		properties.put(key, val);
		return setPropertiesInRange(sheet, pos, properties);
	}

	public XCellRange setPropertiesInRange(XSpreadsheet sheet, RangePosition pos, CellProperties properties) {
		checkNotNull(sheet);
		checkNotNull(pos);
		checkNotNull(properties);

		XCellRange xCellRange = null;
		try {
			// // spalte, zeile,spalte, zeile
			xCellRange = sheet.getCellRangeByPosition(pos.getStartSpalte(), pos.getStartZeile(), pos.getEndeSpalte(),
					pos.getEndeZeile());

			XPropertySet xPropSet = UnoRuntime.queryInterface(XPropertySet.class, xCellRange);
			setProperties(xPropSet, properties);
		} catch (IndexOutOfBoundsException | IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		}
		return xCellRange;
	}

	/**
	 * setPropertyInCell(sheet, pos,"CharColor", Integer.valueOf(0x003399));<br>
	 * setPropertyInCell(sheet, pos,"CharHeight", new Float(20.0));<br>
	 * // from styles.ParagraphProperties<br>
	 * setPropertyInCell(sheet, pos,"ParaLeftMargin", Integer.valueOf(500));<br>
	 * // from table.CellProperties<br>
	 * setPropertyInCell(sheet, pos,"IsCellBackgroundTransparent", Boolean.FALSE);<br>
	 * setPropertyInCell(sheet, pos,"CellBackColor", Integer.valueOf(0x99CCFF));
	 *
	 * @param sheet
	 * @param pos
	 * @param Name
	 * @param val
	 * @return xcell, null whenn error or not found
	 */

	public XCell setPropertyInCell(XSpreadsheet sheet, Position pos, String Name, Object val) {
		checkNotNull(sheet);
		checkNotNull(pos);
		checkNotNull(Name);
		checkNotNull(val);

		XCell xCell = null;
		try {
			xCell = sheet.getCellByPosition(pos.getSpalte(), pos.getZeile());
			XPropertySet xPropSet = UnoRuntime.queryInterface(XPropertySet.class, xCell);
			xPropSet.setPropertyValue(Name, val);
		} catch (IndexOutOfBoundsException | IllegalArgumentException | UnknownPropertyException | PropertyVetoException
				| WrappedTargetException e) {
			logger.error("\n***** Fehler beim Property in Zelle:" + Name + "=" + val + " *****\n" + e.getMessage(), e);
		}
		return xCell;
	}

	/**
	 * Horizontal zentrieren und breite, optional ein überschrift
	 *
	 * @param sheet
	 * @param pos
	 * @param header = optional, wenn vorhanden dann die wird die zeile in pos verwendet
	 */
	public void setColumnWidthAndHoriJustifyCenter(XSpreadsheet sheet, Position pos, int width, String header) {
		checkNotNull(sheet);

		if (header != null) {
			setTextInCell(sheet, pos, header);
		}
		setColumnCellHoriJustify(sheet, pos, CellHoriJustify.CENTER);
		setColumnWidth(sheet, pos, width);
	}

	public XPropertySet setColumnCellHoriJustify(XSpreadsheet sheet, Position pos, CellHoriJustify cellHoriJustify) {
		return setColumnCellHoriJustify(sheet, pos.getSpalte(), cellHoriJustify);
	}

	// CellVertJustify2
	// CellHoriJustify
	// https://api.libreoffice.org/docs/idl/ref/servicecom_1_1sun_1_1star_1_1table_1_1CellProperties.html#ac4ecfad4d3b8fcf60e5205465fb254dd
	public XPropertySet setColumnCellHoriJustify(XSpreadsheet sheet, int spalte, CellHoriJustify cellHoriJustify) {
		checkNotNull(sheet);
		// HoriJustify ,VertJustify ,Orientation
		return setColumnProperty(sheet, spalte, HORI_JUSTIFY, cellHoriJustify);
	}

	public XPropertySet setColumnWidth(XSpreadsheet sheet, Position pos, int width) {
		checkNotNull(sheet);
		return setColumnWidth(sheet, pos.getSpalte(), width);
	}

	public XPropertySet setColumnWidth(XSpreadsheet sheet, int spalte, int width) {
		checkNotNull(sheet);
		return setColumnProperty(sheet, spalte, "Width", new Integer(width));
	}

	public XCell getCell(XSpreadsheet xSheet, Position pos) {
		checkNotNull(xSheet);
		checkNotNull(pos);
		XCell xCell = null;
		try {
			xCell = xSheet.getCellByPosition(pos.getColumn(), pos.getRow());
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}
		return xCell;
	}

	/**
	 *
	 * @param xCell
	 * @param key
	 * @return null when not found
	 */

	public Object getCellProperty(XSpreadsheet xSheet, Position pos, String key) {
		checkNotNull(xSheet);
		checkNotNull(pos);
		checkNotNull(key);
		Object val = null;
		XCell cell = getCell(xSheet, pos);

		if (cell != null) {
			val = getCellProperty(cell, key);
		}

		return val;
	}

	/**
	 *
	 * @param xCell
	 * @param key
	 * @return null when not found
	 */
	public Object getCellProperty(XCell xCell, String key) {
		checkNotNull(xCell);
		checkNotNull(key);
		Object val = null;
		XPropertySet cellPropertySet = getCellPropertySet(xCell);
		try {
			val = cellPropertySet.getPropertyValue(key);
		} catch (UnknownPropertyException | WrappedTargetException e) {
			logger.error(e.getMessage(), e);
		}
		return val;
	}

	public XPropertySet getCellPropertySet(XCell xCell) {
		checkNotNull(xCell);
		return UnoRuntime.queryInterface(XPropertySet.class, xCell);
	}

	public void setCommentInCell(XSpreadsheet xSheet, Position pos, String text) {
		XCell xCell = getCell(xSheet, pos);
		if (xCell != null) {
			setCommentInCell(xSheet, xCell, text);
		}
	}

	public void setCommentInCell(XSpreadsheet xSheet, XCell xCell, String text) {
		checkNotNull(xSheet);
		checkNotNull(text);
		checkNotNull(xCell);

		// XCell xCell = getCell( xSheet,pos);

		// create the CellAddress struct
		XCellAddressable xCellAddr = UnoRuntime.queryInterface(XCellAddressable.class, xCell);
		CellAddress aAddress = xCellAddr.getCellAddress();

		// insert an annotation
		XSheetAnnotationsSupplier xAnnotationsSupp = UnoRuntime.queryInterface(XSheetAnnotationsSupplier.class, xSheet);
		XSheetAnnotations xAnnotations = xAnnotationsSupp.getAnnotations();
		xAnnotations.insertNew(aAddress, text);
		// make the annotation visible
		// XSheetAnnotationAnchor xAnnotAnchor = UnoRuntime.queryInterface(XSheetAnnotationAnchor.class, xCell);
		// XSheetAnnotation xAnnotation = xAnnotAnchor.getAnnotation();
		// xAnnotation.setIsVisible(true);
	}

	public XCellRange clearRange(XSpreadsheet xSheet, RangePosition rangePos) {
		checkNotNull(xSheet);
		checkNotNull(rangePos);

		XCellRange xRangetoClear;
		xRangetoClear = getCellRange(xSheet, rangePos);
		if (xRangetoClear != null) {
			// --- Sheet operation. ---
			XSheetOperation xSheetOp = UnoRuntime.queryInterface(com.sun.star.sheet.XSheetOperation.class,
					xRangetoClear);
			xSheetOp.clearContents(CellFlags.ANNOTATION | CellFlags.DATETIME | CellFlags.EDITATTR | CellFlags.FORMATTED
					| CellFlags.FORMULA | CellFlags.HARDATTR | CellFlags.OBJECTS | CellFlags.STRING | CellFlags.STYLES
					| CellFlags.VALUE);
		}
		return xRangetoClear;
	}

	public XCellRange getCellRange(XSpreadsheet xSheet, RangePosition rangePos) {
		checkNotNull(xSheet);
		checkNotNull(rangePos);

		XCellRange xCellRange = null;

		try {
			xCellRange = xSheet.getCellRangeByPosition(rangePos.getStartSpalte(), rangePos.getStartZeile(),
					rangePos.getEndeSpalte(), rangePos.getEndeZeile());
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage(), e);
		}

		return xCellRange;
	}

	public XCellRangesQuery getCellRangesQuery(XSpreadsheet xSheet, RangePosition rangePos) {
		checkNotNull(xSheet);
		checkNotNull(rangePos);

		XCellRange xCellRange = null;
		XCellRangesQuery xCellRangesQuery = null;

		xCellRange = getCellRange(xSheet, rangePos);
		if (xCellRange != null) {
			xCellRangesQuery = UnoRuntime.queryInterface(com.sun.star.sheet.XCellRangesQuery.class, xCellRange);
		}
		return xCellRangesQuery;
	}

}
