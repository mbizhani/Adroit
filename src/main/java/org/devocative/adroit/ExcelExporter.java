package org.devocative.adroit;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ExcelExporter {
	private final SXSSFWorkbook workbook;
	private final SXSSFSheet sheet;
	private final XSSFCellStyle headerStyle, cellStyle;

	private int rowNo = 1;
	private int noOfCols = -1;
	private boolean autoSizeColumns = true;

	// ------------------------------

	public ExcelExporter(String sheetName) {
		workbook = new SXSSFWorkbook();
		sheet = workbook.createSheet(sheetName);

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontName("Arial");
		headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(new XSSFColor(new Color(200, 200, 200)));
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		Font cellFont = workbook.createFont();
		cellFont.setFontName("Arial");
		cellStyle = (XSSFCellStyle) workbook.createCellStyle();
		cellStyle.setFont(cellFont);
	}

	// ------------------------------

	public ExcelExporter setColumnsHeader(List<String> columnsHeader) {
		noOfCols = columnsHeader.size();

		sheet.createFreezePane(0, 1);
		SXSSFRow headerRow = sheet.createRow(0);

		for (int i = 0; i < columnsHeader.size(); i++) {
			String cellHeader = columnsHeader.get(i);
			writeCell(headerRow, i, cellHeader, headerStyle);
		}
		return this;
	}

	public ExcelExporter setRtl(boolean value) {
		sheet.setRightToLeft(value);
		return this;
	}

	public ExcelExporter setAutoSizeColumns(boolean autoSizeColumns) {
		this.autoSizeColumns = autoSizeColumns;
		return this;
	}

	public ExcelExporter addRowData(List<?> cellsData) {
		noOfCols = Math.max(noOfCols, cellsData.size());

		SXSSFRow row = sheet.createRow(rowNo++);

		for (int i = 0; i < cellsData.size(); i++) {
			Object cell = cellsData.get(i);
			writeCell(row, i, cell, cellStyle);
		}
		return this;
	}

	public void generate(OutputStream outputStream) throws IOException {
		if (autoSizeColumns) {
			for (int c = noOfCols; c > 0; c--) {
				sheet.trackColumnForAutoSizing(c);
				sheet.autoSizeColumn(c);
			}
		}
		workbook.write(outputStream);
		workbook.close();
	}

	// ------------------------------

	private void writeCell(Row row, int col, Object value, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(value != null ? value.toString() : "");
		cell.setCellStyle(style);
	}
}
