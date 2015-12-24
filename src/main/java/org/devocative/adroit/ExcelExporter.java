package org.devocative.adroit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ExcelExporter {
	private SXSSFWorkbook workbook;
	private SXSSFSheet sheet;

	private int rowNo = 1;

	public ExcelExporter(String sheetName) {
		workbook = new SXSSFWorkbook();
		sheet = workbook.createSheet(sheetName);
	}

	public ExcelExporter setColumnsHeader(List<String> columnsHeader) {
		sheet.createFreezePane(0, 1);
		SXSSFRow headerRow = sheet.createRow(0);

		Font boldFont = workbook.createFont();
		boldFont.setBold(true);

		for (int i = 0; i < columnsHeader.size(); i++) {
			String cellHeader = columnsHeader.get(i);
			writeCell(headerRow, i, cellHeader, boldFont);
		}
		return this;
	}

	public ExcelExporter addRowData(List<?> cellsData) {
		SXSSFRow row = sheet.createRow(rowNo++);

		for (int i = 0; i < cellsData.size(); i++) {
			Object cell = cellsData.get(i);
			writeCell(row, i, cell, null);
		}
		return this;
	}

	public void generate(OutputStream outputStream) throws IOException {
		workbook.write(outputStream);
		workbook.close();
	}

	private void writeCell(Row row, int col, Object value, Font font) {
		Cell cell = row.createCell(col);

		if (value != null) {
			cell.setCellValue(value.toString());
		}

		if (font != null) {
			CellStyle style = workbook.createCellStyle();
			style.setFont(font);
			cell.setCellStyle(style);
		}
	}
}
