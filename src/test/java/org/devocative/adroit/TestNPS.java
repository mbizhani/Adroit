package org.devocative.adroit;

import org.devocative.adroit.date.EUniCalendar;
import org.devocative.adroit.date.UniDate;
import org.devocative.adroit.obuilder.ObjectBuilder;
import org.devocative.adroit.sql.InitDB;
import org.devocative.adroit.sql.NamedParameterStatement;
import org.devocative.adroit.sql.filter.FilterValue;
import org.devocative.adroit.sql.plugin.FilterPlugin;
import org.devocative.adroit.sql.plugin.ObjectNavigationPlugin;
import org.devocative.adroit.sql.plugin.PaginationPlugin;
import org.devocative.adroit.sql.plugin.SchemaPlugin;
import org.devocative.adroit.sql.result.EColumnNameCase;
import org.devocative.adroit.sql.result.QueryVO;
import org.devocative.adroit.sql.result.ResultSetProcessor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.devocative.adroit.obuilder.ObjectBuilder.list;
import static org.devocative.adroit.obuilder.ObjectBuilder.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestNPS {
	private static Connection connection;

	@BeforeClass
	public static void init() {
		ConfigUtil.load(TestAdroit.class.getResourceAsStream("/config_test.properties"));

		InitDB initDB = new InitDB();
		initDB
			.setDriver(ConfigUtil.getString(true, "db.driver"))
			.setUrl(ConfigUtil.getString(true, "db.url"))
			.setUsername(ConfigUtil.getString(true, "db.username"))
			.setPassword(ConfigUtil.getString("db.password", ""))
			.addScript("src/test/resources/init_hsql.sql");

		initDB.build();

		connection = initDB.getConnection();
	}

	@Test
	public void testNPS() throws Exception {
		NamedParameterStatement nps =
			new NamedParameterStatement(connection)
				.setQuery("select -- test :this \n /* test :that from comment */ * from t_person where (f_education in (:edu) or f_education in (:edu)) and c_name like :name")
				//.setSchema(ConfigUtil.getString(true, "db.schema"))
				.setParameter("edu", Arrays.asList(1, 2, 3))
				.setParameter("name", "Jo%");

		nps
			.addPlugin(PaginationPlugin.of(connection, 1L, null))
			.addPlugin(new SchemaPlugin(ConfigUtil.getString(true, "db.schema")))
		;

		int no = 0;
		ResultSet rs = nps.executeQuery();
		while (rs.next()) {
			System.out.println(rs.getString("c_name"));
			no++;
		}

		System.out.println(nps.getFinalIndexedQuery());
		System.out.println(nps.getFinalParams());

		//MYSQL Assert.assertEquals("select -- test :this \n /* test :that from comment */ * from adroit.t_person where (f_education in ( ?1 , ?2 , ?3 ) or f_education in ( ?4 , ?5 , ?6 )) and c_name like  ?7  limit  ?8 , ?9 ", nps.getFinalIndexedQuery());
		Assert.assertEquals(
			"select * from (select a.*, rownum rnum_pg from ( " +
				"select -- test :this \n /* test :that from comment */ * from public.t_person where (f_education in ( ?1 , ?2 , ?3 ) or f_education in ( ?4 , ?5 , ?6 )) and c_name like  ?7 " +
				" ) a) where rnum_pg >=  ?8 ", nps.getFinalIndexedQuery());
		Assert.assertEquals("Jo%", nps.getFinalParams().get(7));

		Assert.assertEquals(2, no);

		nps = new NamedParameterStatement(connection)
			.setQuery("select * from t_person where (f_education in (:edu) or f_education in (:edu)) and c_name like :name")
			//.setParameter("edu", Arrays.asList(1, 2, 3))
			//.setParameter("name", "Jo%")
			.setIgnoreMissedParam(true)
		;
		nps.addPlugin(PaginationPlugin.of(connection, null, 10L));

		no = 0;
		rs = nps.executeQuery();
		while (rs.next()) {
			System.out.println(rs.getString("c_name"));
			no++;
		}

		Map<Integer, Object> finalParams = nps.getFinalParams();
		Assert.assertEquals(0, no);
		Assert.assertEquals(4, finalParams.size());
		Assert.assertNull(finalParams.get(1));
		Assert.assertNull(finalParams.get(2));
		Assert.assertNull(finalParams.get(3));
	}

	@Test
	public void testObjectNavPlugin() throws SQLException {
		final Map<Object, Object> curUser = map()
			.put("name", "Joe")
			.put("other", map()
				.put("name", "Jack")
				.put("edu", Arrays.asList(1, 2, 3))
				.get())
			.put("list", list()
				.add(map().put("id", 1).get())
				.add(map().put("id", 2).get())
				.get())
			.get();

		NamedParameterStatement nps = new NamedParameterStatement(connection)
			.setQuery("select * from t_person where " +
				"c_name = :$$curUser$name or " +
				"c_name = :$$curUser$other$name or " +
				"f_education in (:$$curUser$other$edu) or " +
				"f_education in (:$$curUser$list$id) or " +
				"c_name = :$$curUser$unknown")
			.setParameter("$$curUser", curUser)
			.setIgnoreExtraPassedParam(true);
		nps.addPlugin(new ObjectNavigationPlugin());

		final List<String> assertNames = Arrays.asList("Jack", "Joe", "John");
		int no = 0;
		ResultSet rs = nps.executeQuery();
		while (rs.next()) {
			Assert.assertTrue(assertNames.contains(rs.getString("c_name")));
			no++;
		}
		Assert.assertEquals(assertNames.size(), no);

		Map<String, Object> params = nps.getOldParams();
		Assert.assertEquals("Joe", params.get("$$curUser$name"));
		Assert.assertEquals("Jack", params.get("$$curUser$other$name"));
		Assert.assertEquals(Arrays.asList(1, 2, 3), params.get("$$curUser$other$edu"));
		Assert.assertEquals(Arrays.asList(1, 2), params.get("$$curUser$list$id"));
		Assert.assertTrue(params.containsKey("$$curUser$unknown"));
		Assert.assertNull(params.get("$$curUser$unknown"));
	}

	@Test
	public void testParam() {
		List<String> paramsInQuery = NamedParameterStatement.findParamsInQuery("select 'is :nok' -- ignore :this and\n" +
				" /* and ignore :this too */ from dual where 1=:One and 2<>:two or :One=:two and :$that=1 or :$$p$t=2",
			false);
		Assert.assertEquals(4, paramsInQuery.size());
		Assert.assertNotEquals("one", paramsInQuery.get(0));
		Assert.assertEquals("One", paramsInQuery.get(0));
		Assert.assertEquals("two", paramsInQuery.get(1));
		Assert.assertEquals("$that", paramsInQuery.get(2));
		Assert.assertEquals("$$p$t", paramsInQuery.get(3));
	}

	@Test
	public void testSchema() {
		String q =
			"select \n" +
				"--comment to test from schema \n" +  //NOTE: no schema addition
				"/*comment to test from schema*/ \n" + //NOTE: no schema addition
				"eq.code equipment_code,\n" +
				"'a from and join in string constant' as \"from and join and into id\"\n" + //NOTE: no schema addition
				"from\n" +
				"(select \n" +
				"flr.equipment equipment_id ,\n" +

				//NOTE: no schema addition /!\
				"ROUND((extract(DAY FROM MAX(NVL(flr.endDateTime,SYSDATE))- MIN(flr.startDateTime)) - SUM(extract(DAY FROM NVL(flr.endDateTime,SYSDATE) - flr.startDateTime))) / (COUNT(*) - 1 ),2) mtbf\n" +

				//NOTE: schema addition to "Failure" but not to "Attempt" /!\
				"from Failure flr, Attempt atm \n" +
				"where flr.equipment = :eq_id and flr.startDateTime >= :start_date and flr.endDateTime <= :end_date and flr.id = atm.id\n" +
				"group by flr.equipment) T1\n" +
				"join Equipment eq on T1.equipment_id = eq.id\n" + //NOTE: schema addition
				"join Asset ast on eq.vrAsset = ast.id\n" +        //NOTE: schema addition
				"join Item item on ast.vrPhysicalItem = item.id";  //NOTE: schema addition

		String expected =
			"select \n" +
				"--comment to test from schema \n" +
				"/*comment to test from schema*/ \n" +
				"eq.code equipment_code,\n" +
				"'a from and join in string constant' as \"from and join and into id\"\n" +
				"from\n" +
				"(select \n" +
				"flr.equipment equipment_id ,\n" +
				"ROUND((extract(DAY FROM MAX(NVL(flr.endDateTime,SYSDATE))- MIN(flr.startDateTime)) - SUM(extract(DAY FROM NVL(flr.endDateTime,SYSDATE) - flr.startDateTime))) / (COUNT(*) - 1 ),2) mtbf\n" +
				"from qaz.Failure flr, Attempt atm \n" +
				"where flr.equipment = :eq_id and flr.startDateTime >= :start_date and flr.endDateTime <= :end_date and flr.id = atm.id\n" +
				"group by flr.equipment) T1\n" +
				"join qaz.Equipment eq on T1.equipment_id = eq.id\n" +
				"join qaz.Asset ast on eq.vrAsset = ast.id\n" +
				"join qaz.Item item on ast.vrPhysicalItem = item.id";

		String result = SchemaPlugin.applySchema("qaz", q);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void testFilter() throws SQLException {
		String process = new FilterPlugin().addAll(
			ObjectBuilder.<String, Object>map()
				.put("col1", "A")
				.put("col2", 123)
				.put("col3", new Date())
				.get())
			.process("", ObjectBuilder.<String, Object>map().get());
		assertTrue(process.contains("and col1 like :col1"));
		assertTrue(process.contains("and col2 = :col2"));
		assertTrue(process.contains("and col3 = :col3"));

		process = new FilterPlugin()
			.add("cl1", FilterValue.equal(1))
			.add("cl2", FilterValue.equal(Arrays.asList(1, 3, 5)))
			.add("cl3", FilterValue.range(new Date(), null))
			.add("cl4", FilterValue.between(1, 2))
			.add("cl5", FilterValue.between(1.1, 2.2).setSqlFunc("trunc"))
			.add("cl6", FilterValue.between(null, 2.2).setSqlFunc("trunc"))
			.add("cl7", FilterValue.contain("cl7").setSqlFunc("lower"))
			.process("", ObjectBuilder.<String, Object>map().get());
		assertTrue(process.contains("and cl1 = :cl1"));
		assertTrue(process.contains("and cl2 in (:cl2)"));
		assertTrue(process.contains("and cl3 >= :cl3_l"));
		assertTrue(process.contains("and trunc(cl5) >= trunc(:cl5_l)\n\tand trunc(cl5) <= trunc(:cl5_u)"));
		assertTrue(process.contains("and trunc(cl6) <= trunc(:cl6_u)"));
		assertTrue(process.contains("and lower(cl7) like lower(:cl7)"));

		process = new FilterPlugin()
			.add("c1", FilterValue.equal(1))
			.process("select * from t1 where 1=1" + FilterPlugin.EMBED_FILTER_EXPRESSION,
				ObjectBuilder.<String, Object>map().get());
		assertEquals("select * from t1 where 1=1\tand c1 = :c1\n", process);

		// --------------- CONTAINS NO CASE

		ResultSet rs =
			new NamedParameterStatement(connection, "select * from t_person")
				.addPlugin(
					new FilterPlugin()
						.add("C_NAME", FilterValue.contain("ja%").setSqlFunc("lower"))
				).executeQuery();
		List<Object> rows = new ArrayList<>();
		ResultSetProcessor.processRowAsList(rs, rows::add);
		Assert.assertEquals(2, rows.size());

		// --------------- CONTAINS CASE
		rs =
			new NamedParameterStatement(connection, "select * from t_person")
				.addPlugin(
					new FilterPlugin()
						.add("C_Name", FilterValue.contain("%o%"))
				).executeQuery();
		rows = new ArrayList<>();
		ResultSetProcessor.processRowAsList(rs, rows::add);
		Assert.assertEquals(2, rows.size());

		// --------------- RANGE
		rs =
			new NamedParameterStatement(connection, "select * from t_person")
				.addPlugin(
					new FilterPlugin()
						.add("d_birth_date", FilterValue.range(
							UniDate.of(EUniCalendar.Gregorian, 2008, 1, 1).toDate(),
							UniDate.of(EUniCalendar.Gregorian, 2009, 1, 1).toDate()))
				).executeQuery();
		rows = new ArrayList<>();
		ResultSetProcessor.processRowAsList(rs, rows::add);
		Assert.assertEquals(1, rows.size());

		// --------------- LIST
		rs =
			new NamedParameterStatement(connection, "select * from t_person")
				.addPlugin(
					new FilterPlugin()
						.add("f_education", FilterValue.equal(Arrays.asList(2, 4, 6, 8)))
				).executeQuery();
		rows = new ArrayList<>();
		ResultSetProcessor.processRowAsList(rs, rows::add);
		Assert.assertEquals(2, rows.size());

		// --------------- EMBEDDED FILTER
		rs =
			new NamedParameterStatement(connection, "select * from t_person where 1=1 %FILTER% order by c_name")
				.addPlugin(
					new FilterPlugin()
						.add("f_education", FilterValue.equal(Arrays.asList(2, 4, 6, 8)))
				).executeQuery();
		rows = new ArrayList<>();
		ResultSetProcessor.processRowAsList(rs, rows::add);
		Assert.assertEquals(2, rows.size());
	}

	// ---------------

	@Test
	public void testRSProcessorList() throws SQLException {
		NamedParameterStatement nps = new NamedParameterStatement(connection, "select * from t_person");

		if (nps.execute()) {
			ResultSet rs = nps.getResultSet();
			List<List<Object>> rows = new ArrayList<>();
			ResultSetProcessor.processRowAsList(rs, rows::add);
			Assert.assertEquals(4, rows.size());
			System.out.println("rows = " + rows);
		}
	}

	@Test
	public void testRSProcessorMap() throws SQLException {
		NamedParameterStatement nps = new NamedParameterStatement(connection, "select * from t_person");

		if (nps.execute()) {
			ResultSet rs = nps.getResultSet();
			List<Map<String, Object>> rows = new ArrayList<>();
			ResultSetProcessor.processRowAsMap(rs, rows::add, EColumnNameCase.LOWER);
			Assert.assertEquals(4, rows.size());
			System.out.println("rows = " + rows);
		}
	}

	@Test
	public void testRSProcessor() throws SQLException {
		NamedParameterStatement nps = new NamedParameterStatement(connection, "select * from t_person");

		if (nps.execute()) {
			ResultSet rs = nps.getResultSet();
			QueryVO rvo = ResultSetProcessor.process(rs, EColumnNameCase.LOWER);
			Assert.assertEquals(4, rvo.getHeader().size());
			Assert.assertEquals(4, rvo.getRows().size());
		}
	}
}
