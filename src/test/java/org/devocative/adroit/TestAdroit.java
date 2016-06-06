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
				.setQuery("select * from t_person where (f_education in (:edu) or f_education in (:edu)) and c_name like :name")
				.setParameter("edu", Arrays.asList(1, 2, 3))
				.setParameter("name", "Jo%")
				.setPageIndex(1L)
				.setPageSize(10L);

		int no = 0;
		ResultSet rs = nps.executeQuery();
		while (rs.next()) {
			System.out.println(rs.getString("c_name"));
			no++;
		}

		Assert.assertEquals(2, no);
	}

	@Test
	public void testConfigUtil() {
		Assert.assertEquals(123L, ConfigUtil.getInteger(true, "int.key").longValue());

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

		cache.put("A", 1); // A
		cache.put("B", 2); // A B
		cache.put("C", 3); // A B C
		cache.put("D", 4); // B C D

		Assert.assertTrue(cache.size() == 3);
		Assert.assertFalse(cache.containsKey("A"));

		cache.put("B", 5); // C D B
		cache.put("E", 6); // D B E

		Assert.assertTrue(cache.get("B") == 5); // D E B
		Assert.assertFalse(cache.containsKey("C"));

		Assert.assertTrue(cache.get("D") == 4); // E B D
		cache.put("F", 7); // B D F
		Assert.assertFalse(cache.containsKey("E"));

		cache.get("B"); // D F B
		cache.put("G", 8); // F B G
		Assert.assertFalse(cache.containsKey("D"));

		cache.remove("B");
		Assert.assertTrue(cache.size() == 2);
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
