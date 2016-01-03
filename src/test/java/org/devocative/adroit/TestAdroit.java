package org.devocative.adroit;

import org.devocative.adroit.sql.NamedParameterStatement;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

	@Test
	public void testNPS() throws Exception {
		Class.forName("org.h2.Driver");

		Connection sa = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/adroit", "sa", "");
		NamedParameterStatement nps =
			new NamedParameterStatement(sa)
				.setQuery("select * from t_person where (f_education in (:edu) or f_education in (:edu)) and c_name like :name")
				.setParameter("edu", Arrays.asList(1, 2, 3))
				.setParameter("name", "Jo%");
		int no = 0;
		ResultSet rs = nps.executeQuery();
		while (rs.next()) {
			System.out.println(rs.getString("c_name"));
			no++;
		}

		Assert.assertEquals(no, 2);
	}
}
