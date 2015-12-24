package org.devocative.adroit;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TestAdroit {

	@Test
	public void testExcelExporter() throws IOException {
		ExcelExporter exporter = new ExcelExporter("Sheet");

		exporter.setColumnsHeader(Arrays.asList("A", "B", "C"));

		for (int i = 0; i < 200; i++) {
			exporter.addRowData(Arrays.asList(i, "B" + i, "C" + i));
		}

		FileOutputStream stream = new FileOutputStream("test.xlsx");
		exporter.generate(stream);
		stream.close();

		File testFile = new File("test.xlsx");
		Assert.assertTrue(testFile.exists());
	}
}
