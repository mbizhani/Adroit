package org.devocative.adroit;

import org.devocative.adroit.cache.LRUCache;
import org.devocative.adroit.obuilder.ObjectBuilder;
import org.devocative.adroit.sql.NamedParameterStatement;
import org.devocative.adroit.vo.DateFieldVO;
import org.devocative.adroit.vo.KeyValueVO;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;

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
		Class.forName(ConfigUtil.getString(true, "db.driver"));

		Connection sa = DriverManager.getConnection(
			ConfigUtil.getString(true, "db.url"),
			ConfigUtil.getString(true, "db.username"),
			ConfigUtil.getString("db.password", ""));

		NamedParameterStatement nps =
			new NamedParameterStatement(sa)
				.setQuery("select -- test :this \n /* test :that from comment */ * from t_person where (f_education in (:edu) or f_education in (:edu)) and c_name like :name")
				.setSchema(ConfigUtil.getString(true, "db.schema"))
				.setParameter("edu", Arrays.asList(1, 2, 3))
				.setParameter("name", "Jo%")
				.setFirstResult(1L)
			//.setMaxResults(10L)
			;

		int no = 0;
		ResultSet rs = nps.executeQuery();
		while (rs.next()) {
			System.out.println(rs.getString("c_name"));
			no++;
		}

		System.out.println(nps.getFinalIndexedQuery());
		System.out.println(nps.getFinalParams());

		Assert.assertEquals("select -- test :this \n /* test :that from comment */ * from adroit.t_person where (f_education in ( ?1 , ?2 , ?3 ) or f_education in ( ?4 , ?5 , ?6 )) and c_name like  ?7  limit  ?8 , ?9 ", nps.getFinalIndexedQuery());
		Assert.assertEquals("Jo%", nps.getFinalParams().get(7));

		Assert.assertEquals(2, no);

		nps = new NamedParameterStatement(sa)
			.setQuery("select * from t_person where (f_education in (:edu) or f_education in (:edu)) and c_name like :name")
				//.setParameter("edu", Arrays.asList(1, 2, 3))
				//.setParameter("name", "Jo%")
			.setIgnoreMissedParam(true)
//			.setFirstResult(1L)
			.setMaxResults(10L)
		;

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
	public void testParamOfNPS() {
		List<String> paramsInQuery = NamedParameterStatement.findParamsInQuery("select 'is :nok' -- ignore :this \n" +
			" /* and ignore :this too */ from dual where 1=:One and 2<>:two or :One=:two", false);
		Assert.assertEquals(2, paramsInQuery.size());
		Assert.assertNotEquals("one", paramsInQuery.get(0));
		Assert.assertEquals("One", paramsInQuery.get(0));
		Assert.assertEquals("two", paramsInQuery.get(1));
	}

	@Test
	public void testSchemaOfNPS() {
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

		String result = NamedParameterStatement.applySchema("qaz", q);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void testConfigUtil() {
		Assert.assertEquals(123L, ConfigUtil.getInteger(true, "int.key").longValue());

		Assert.assertTrue(123 == ConfigUtil.getInteger(TestConfigKey.OK));

		Assert.assertTrue(ConfigUtil.getBoolean(true, "bool.key"));

		Assert.assertEquals(987654321L, ConfigUtil.getLong(true, "long.key").longValue());

		Assert.assertEquals("Hello", ConfigUtil.getString(true, "string.key"));

		Assert.assertEquals("salam", ConfigUtil.getString(true, "encrypted.key"));

		Assert.assertEquals(3, ConfigUtil.getList(true, "list.key").size());

		Assert.assertEquals(0, ConfigUtil.getList(false, "empty.list.key ").size());

		try {
			ConfigUtil.getString(true, "empty.string.key");
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(true);
		}

		try {
			ConfigUtil.getString(TestConfigKey.NOK);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertEquals("Invalid value for key=[test.choices], possibles=[ok, nok]", e.getMessage());
		}

		Assert.assertTrue(ConfigUtil.hasKey(TestConfigKey.NOK));
		Assert.assertTrue(ConfigUtil.hasKey("empty.list.key"));
		Assert.assertFalse(ConfigUtil.hasKey("my.key"));
	}

	@Test
	public void testStringEncryptorUtil() {
		System.out.println(StringEncryptorUtil.hash("Hello World!"));

		System.out.println(StringEncryptorUtil.hash(""));

		try {
			System.out.println(StringEncryptorUtil.hash(null));
			Assert.assertTrue(false);
		} catch (NullPointerException e) {
			Assert.assertTrue(true);
		}

		Assert.assertEquals(StringEncryptorUtil.encodeBase64(new byte[]{1, 2, 3, 4}), "AQIDBA==");

		//TODO test enc/dec
	}

	@Test
	public void testObjectBuilder() {
		Collection<String> list1 = ObjectBuilder
			.<String>createDefaultList()
			.add("A")
			.add("B")
			.add("C")
			.get();

		Collection<String> list2 = ObjectBuilder
			.createCollection(new LinkedList<String>())
			.add("A")
			.add("B")
			.add("D")
			.get();

		Assert.assertNotEquals(list1, list2);

		Map<String, Integer> map1 = ObjectBuilder
			.<String, Integer>createDefaultMap()
			.put("A", 1)
			.put("B", 2)
			.put("C", 33)
			.get();

		Map<String, Integer> map2 = ObjectBuilder
			.createMap(new LinkedHashMap<String, Integer>())
			.put("C", 33)
			.put("A", 1)
			.put("B", 2)
			.get();

		Assert.assertEquals(map1, map2);

		Map<String, Integer> map3 = new HashMap<>();
		map3.put("B", 2);
		map3.put("C", 33);
		map3.put("A", 1);

		Assert.assertEquals(map1, map3);
	}

	@Test
	public void testCalendar() {
		List<Thread> threads = new ArrayList<>();

		for (int i = 0; i < 100; i++) {

			Thread t = new Thread() {
				@Override
				public void run() {
					testCalendarInThread();
				}
			};
			threads.add(t);
			t.start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Test
	public void testCalendarInThread() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2016);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		System.out.println("cal.getTime() = " + cal.getTime());
		//Jan 01 2016

		Date jan_01_2016 = CalendarUtil.getDate(new DateFieldVO(2016, 1, 1));
		System.out.println("jan_01_2016 = " + jan_01_2016);

		Assert.assertEquals("2016-01-01", CalendarUtil.formatDate(jan_01_2016, "yyyy-MM-dd"));

		Assert.assertEquals(
			CalendarUtil.formatDate(jan_01_2016, "yyyy-MM-dd"),
			CalendarUtil.formatDate(cal.getTime(), "yyyy-MM-dd")
		);

		Assert.assertEquals(
			jan_01_2016,
			CalendarUtil.parseDate("2016-01-01", "yyyy-MM-dd")
		);


		System.out.println(CalendarUtil.toPersian(cal.getTime(), "yyyy-MM-dd"));
		Assert.assertEquals("1394-10-11", CalendarUtil.toPersian(cal.getTime(), "yyyy-MM-dd"));

		Date esf_30_1391 = CalendarUtil.toGregorian("1391-12-30", "yyyy-MM-dd");
		System.out.println("esf_30_1391 = " + esf_30_1391);
		Assert.assertEquals("2013-03-20", CalendarUtil.formatDate(esf_30_1391, "yyyy-MM-dd"));

		Date aba_15_1394 = CalendarUtil.toGregorian(new DateFieldVO(1394, 8, 15));
		System.out.println("aba_15_1394 = " + aba_15_1394);
		Assert.assertEquals("2015-11-06", CalendarUtil.formatDate(aba_15_1394, "yyyy-MM-dd"));
	}

	@Test
	public void testObjectUtil() {
		KeyValueVO<String, Integer> k1 = new KeyValueVO<>("A", 1);
		System.out.println("k1 = " + ObjectUtil.toString(k1));

		KeyValueVO<String, Integer> k2 = new KeyValueVO<>("B", null);
		System.out.println("k2 = " + ObjectUtil.toString(k2));

		Assert.assertNotEquals(k2.getKey(), k1.getKey());
		Assert.assertNotEquals(k2.getValue(), k1.getValue());

		ObjectUtil.merge(k2, k1, false);

		System.out.println("k2 (merged) = " + ObjectUtil.toString(k2));

		Assert.assertNotEquals(k2.getKey(), k1.getKey());
		Assert.assertEquals(k2.getValue(), k1.getValue());

		ListHolder l1 = new ListHolder(
			ObjectUtil.asList(
				new KeyValueVO<>("A", (Integer) null),
				new KeyValueVO<>("B", 1),
				new KeyValueVO<>("D", 2)
			));

		ListHolder l2 = new ListHolder(
			ObjectUtil.asList(
				new KeyValueVO<>("A", 11),
				new KeyValueVO<>("B", (Integer) null),
				new KeyValueVO<>("C", 13)
			));

		ObjectUtil.merge(l2, l1, false);

		Assert.assertEquals(l2.getList().size(), 4);

		Assert.assertEquals(((KeyValueVO) l2.getList().get(0)).getKey(), "A");
		Assert.assertEquals(((KeyValueVO) l2.getList().get(0)).getValue(), 11);

		Assert.assertEquals(((KeyValueVO) l2.getList().get(1)).getKey(), "B");
		Assert.assertEquals(((KeyValueVO) l2.getList().get(1)).getValue(), 1);

		Assert.assertEquals(((KeyValueVO) l2.getList().get(2)).getKey(), "C");

		Assert.assertEquals(((KeyValueVO) l2.getList().get(3)).getKey(), "D");

		Assert.assertNull(ObjectUtil.getPropertyValue(k2, "test", true));

		Assert.assertFalse(ObjectUtil.hasIt(""));
		Assert.assertFalse(ObjectUtil.hasIt("  "));
		Assert.assertTrue(ObjectUtil.hasIt(" a  "));

		Assert.assertTrue(ObjectUtil.isFalse(null));
		Assert.assertTrue(ObjectUtil.isFalse(false));
		Assert.assertFalse(ObjectUtil.isTrue(null));
	}

	@Test
	public void testLRUCache() {
		LRUCache<String, Integer> cache = new LRUCache<>(3);

		Assert.assertEquals(0, cache.getSize());

		cache.put("A", 1); // A
		cache.put("B", 2); // A B
		cache.put("C", 3); // A B C
		cache.put("D", 4); // B C D

		Assert.assertEquals(3, cache.getSize());
		Assert.assertFalse(cache.containsKey("A"));
		Assert.assertEquals(1, cache.getMissHitCount());

		cache.put("B", 5); // C D B
		cache.put("E", 6); // D B E

		Assert.assertEquals(5, (int) cache.get("B")); // D E B
		Assert.assertFalse(cache.containsKey("C"));
		Assert.assertEquals(2, cache.getMissHitCount());

		Assert.assertEquals(4, (int) cache.get("D")); // E B D
		cache.put("F", 7); // B D F
		Assert.assertFalse(cache.containsKey("E"));

		cache.get("B"); // D F B
		cache.put("G", 8); // F B G
		Assert.assertFalse(cache.containsKey("D"));

		cache.remove("B");
		Assert.assertEquals(2, cache.getSize());

		cache.setCapacity(4);

		cache.put("Z", 11);
		Assert.assertEquals(3, cache.getSize());

		cache.put("Y", 12);
		Assert.assertEquals(4, cache.getSize());

		cache.clear();
		Assert.assertEquals(0, cache.getSize());
	}

	// ------------------------------

	public static class ListHolder {
		private List list;

		public ListHolder(List list) {
			this.list = list;
		}

		public List getList() {
			return list;
		}

		public void setList(List list) {
			this.list = list;
		}
	}
}
