package org.devocative.adroit;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.devocative.adroit.cache.LRUCache;
import org.devocative.adroit.obuilder.ObjectBuilder;
import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.adroit.xml.AdroitXStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class TestAdroit {
	private String keyStorePass = "adroitPassWord";
	private String entryName = "adroit";
	private String entryProtectionParam = "WiPHF7JjfuKHJz7jFI18";

	@BeforeClass
	public static void init() {
		ConfigUtil.load(TestAdroit.class.getResourceAsStream("/config_test.properties"));
	}

	// ---------------

	@Test
	public void testExcelExporter() throws IOException {
		File testFile = new File("test.xlsx");
		if (testFile.isFile() && testFile.exists()) {
			testFile.delete();
		}

		ExcelExporter exporter = new ExcelExporter("Sheet")
			.setRtl(true);

		exporter.setColumnsHeader(Arrays.asList("A A A A", "B-B-B-B-B-B", "C"));

		for (int i = 0; i < 200; i++) {
			exporter.addRowData(Arrays.asList(i, i + " - " + UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		}

		FileOutputStream stream = new FileOutputStream("test.xlsx");
		exporter.generate(stream);
		stream.close();

		Assert.assertTrue(testFile.exists());
	}

	// ---------------

	@Test
	public void testConfigUtil() {
		Assert.assertEquals(123L, ConfigUtil.getInteger(true, "int.key").longValue());

		Assert.assertTrue(123 == ConfigUtil.getInteger(TestConfigKey.OK));

		Assert.assertNull(ConfigUtil.getInteger(TestConfigKey.IntDefaultNull));
		Assert.assertNull(ConfigUtil.getInteger(TestConfigKey.LongDefaultNull));
		Assert.assertNull(ConfigUtil.getInteger(TestConfigKey.BooleanDefaultNull));

		Assert.assertTrue(ConfigUtil.getBoolean(true, "bool.key"));

		Assert.assertEquals(987654321L, ConfigUtil.getLong(true, "long.key").longValue());

		Assert.assertEquals("Hello", ConfigUtil.getString(true, "string.key"));

		Assert.assertEquals(3, ConfigUtil.getList(true, "list.key").size());

		Assert.assertEquals(0, ConfigUtil.getList(false, "empty.list.key ").size());

		Assert.assertNotNull(ConfigUtil.getList("dummy.key", null));

		try {
			ConfigUtil.getString(true, "empty.string.key");
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(true);
		}

		Assert.assertTrue(ConfigUtil.keyHasValue(TestConfigKey.NOK));
		assertFalse(ConfigUtil.keyHasValidValue(TestConfigKey.NOK));

		try {
			ConfigUtil.getString(TestConfigKey.NOK);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertEquals("Invalid value for key=[test.choices], possibles=[ok, nok]", e.getMessage());
		}

		Assert.assertTrue(ConfigUtil.hasKey(TestConfigKey.NOK));
		Assert.assertTrue(ConfigUtil.hasKey("empty.list.key"));
		assertFalse(ConfigUtil.hasKey("my.key"));

		StringEncryptorUtil.init(TestAdroit.class.getResourceAsStream("/adroit.ks"), keyStorePass, entryName, entryProtectionParam);
		Assert.assertEquals("The Config Value!", ConfigUtil.getString(true, "encrypted.key.suffix"));
		Assert.assertEquals("The Config Value!", ConfigUtil.getString(true, "encrypted.key.prefix"));
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

		File keyStoreFile = new File("adroit.ks");
		keyStoreFile.delete();
		assertFalse(keyStoreFile.exists());

		KeyTool.generatedKeyStoreWithSecureKey(keyStoreFile, keyStorePass, "p6oGS8f8vK7V5wRir9EQ", entryName, entryProtectionParam, KeyTool.EKeyLength.L128);

		Assert.assertTrue(keyStoreFile.exists());

		try {
			StringEncryptorUtil.init(new FileInputStream(keyStoreFile), keyStorePass, entryName, entryProtectionParam);
			String main = "Hello World! Spec Char: ";

			String encrypt = StringEncryptorUtil.encrypt(main);
			System.out.println("encrypt = " + encrypt);

			String decrypt = StringEncryptorUtil.decrypt(encrypt);
			System.out.println("decrypt = " + decrypt);
			Assert.assertEquals(main, decrypt);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testObjectBuilder() {
		Collection<String> list1 = ObjectBuilder
			.<String>list()
			.add("A")
			.add("B")
			.add("C")
			.get();

		Collection<String> list2 = ObjectBuilder
			.collection(new LinkedList<String>())
			.add("A")
			.add("B")
			.add("D")
			.get();

		Assert.assertNotEquals(list1, list2);

		Map<String, Integer> map1 = ObjectBuilder
			.<String, Integer>map()
			.put("A", 1)
			.put("B", 2)
			.put("C", 33)
			.get();

		Map<String, Integer> map2 = ObjectBuilder
			.map(new LinkedHashMap<String, Integer>())
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

	// ---------------

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

		assertFalse(ObjectUtil.hasIt(""));
		assertFalse(ObjectUtil.hasIt("  "));
		Assert.assertTrue(ObjectUtil.hasIt(" a  "));

		Assert.assertTrue(ObjectUtil.isFalse(null));
		Assert.assertTrue(ObjectUtil.isFalse(false));
		assertFalse(ObjectUtil.isTrue(null));
	}

	@Test
	public void testLRUCache() {
		Map<String, Integer> data = ObjectBuilder.<String, Integer>map()
			.put("A", 1)
			.put("B", 2)
			.put("C", 3)
			.put("D", 4)
			.put("E", 5)
			.put("F", 6)
			.put("G", 7)
			.put("H", 8)
			.get();

		LRUCache<String, Integer> cache = new LRUCache<>(3, data::get);

		assertEquals(0, cache.getSize());

		assertEquals(1, cache.get("A").intValue());
		assertEquals("[A]", cache.getKeys().toString());

		assertEquals(2, cache.get("B").intValue());
		assertEquals("[A, B]", cache.getKeys().toString());

		assertEquals(3, cache.get("C").intValue());
		assertEquals("[A, B, C]", cache.getKeys().toString());

		assertEquals(4, cache.get("D").intValue());
		assertEquals("[B, C, D]", cache.getKeys().toString());

		assertEquals(3, cache.getSize());
		assertFalse(cache.containsKey("A"));
		assertEquals(1, cache.getMissHitCount());

		cache.get("B");
		assertEquals("[C, D, B]", cache.getKeys().toString());

		assertEquals(5, cache.get("E").intValue());
		assertEquals("[D, B, E]", cache.getKeys().toString());

		assertFalse(cache.containsKey("C"));
		assertEquals(2, cache.getMissHitCount());

		assertTrue(cache.containsKey("D"));
		assertEquals("[D, B, E]", cache.getKeys().toString());

		cache.setCapacity(4);
		assertEquals("[D, B, E]", cache.getKeys().toString());

		cache.remove("E");
		assertEquals("[D, B]", cache.getKeys().toString());

		assertEquals(6, cache.get("F").intValue());
		assertEquals("[D, B, F]", cache.getKeys().toString());
		assertEquals(2, cache.getMissHitCount());

		assertEquals(7, cache.get("G").intValue());
		assertEquals("[D, B, F, G]", cache.getKeys().toString());
		assertEquals(2, cache.getMissHitCount());

		assertEquals(8, cache.get("H").intValue());
		assertEquals("[B, F, G, H]", cache.getKeys().toString());
		assertEquals(3, cache.getMissHitCount());

		cache.put("I", 9);
		assertEquals(9, cache.get("I").intValue());
		assertEquals("[F, G, H, I]", cache.getKeys().toString());
		assertEquals(4, cache.getMissHitCount());

		try {
			cache.put("I", 10);

			fail("Duplicate Key Accepted!");
		} catch (Exception e) {
			assertEquals("Invalid Put Action, Key Already Exists: I", e.getMessage());
		}

		assertNull(cache.remove("Y"));
		assertEquals("[F, G, H, I]", cache.getKeys().toString());
		assertEquals(4, cache.getMissHitCount());

		assertNull(cache.get("Z"));
		assertEquals("[F, G, H, I]", cache.getKeys().toString());
		assertEquals(4, cache.getMissHitCount());

		assertEquals(4, cache.getValues().size());
		assertEquals("[F, G, H, I]", cache.getKeys().toString());
		assertEquals(4, cache.getMissHitCount());

		final Set<Map.Entry<String, Integer>> entries = cache.getEntries();
		for (Map.Entry<String, Integer> entry : entries) {
			assertEquals("F", entry.getKey());
			break;
		}
		assertEquals("[F, G, H, I]", cache.getKeys().toString());
		assertEquals(4, cache.getMissHitCount());


		cache.clear();
		assertEquals(0, cache.getMissHitCount());
		assertEquals(0, cache.getSize());
		assertEquals(4, cache.getCapacity());
		assertEquals("[]", cache.getKeys().toString());
	}

	@Test
	public void testAdroitList() {
		AdroitList<String> strings = new AdroitList<>(String.CASE_INSENSITIVE_ORDER);
		strings.add("ali");
		strings.add("ALI");

		Assert.assertEquals(2, strings.size());

		Assert.assertTrue(strings.contains("Ali"));
		Assert.assertTrue(strings.contains("ALi"));
		Assert.assertTrue(strings.contains("aLi"));
		Assert.assertTrue(strings.contains("alI"));
		Assert.assertEquals(0, strings.indexOf("ALI"));

		Assert.assertEquals("ali", strings.get(0));
		Assert.assertNotEquals("ALI", strings.get(0));

		Assert.assertEquals("ALI", strings.get(1));
		Assert.assertNotEquals("ali", strings.get(1));


		strings = new AdroitList<>();
		strings.add("ali");
		strings.add("ALI");

		assertFalse(strings.contains("Ali"));
		assertFalse(strings.contains("ALi"));
		assertFalse(strings.contains("aLi"));
		assertFalse(strings.contains("alI"));

		Assert.assertEquals("ali", strings.get(0));
		Assert.assertNotEquals("ALI", strings.get(0));

		Assert.assertEquals("ALI", strings.get(1));
		Assert.assertNotEquals("ali", strings.get(1));
	}

	@Test
	public void testXML() {
		BeanAndXml bax = new BeanAndXml()
			.setAttr("ATTR")
			.setText("a < b && c >= 45 %% @")
			.setBody("Hello\n\tHow are you?\n~!@#$%^&*()_+}{][\\//><");


		XStream xStream = new AdroitXStream();
		xStream.processAnnotations(BeanAndXml.class);
		String xml = xStream.toXML(bax);
		System.out.println(xml);
		Assert.assertEquals("<bean attr=\"ATTR\" writeTrue=\"true\">\n" +
			"\t<text><![CDATA[a < b && c >= 45 %% @]]></text>\n" +
			"\t<body><![CDATA[Hello\n" +
			"\tHow are you?\n" +
			"~!@#$%^&*()_+}{][\\//><]]></body>\n" +
			"</bean>", xml);

		BeanAndXml bax2 = (BeanAndXml) xStream.fromXML(xml);
		Assert.assertTrue(bax2.equals(bax));

		xStream = new AdroitXStream(true);
		xStream.processAnnotations(BeanAndXml.class);
		xml = xStream.toXML(bax);
		System.out.println(xml);
		Assert.assertEquals("<bean attr=\"ATTR\" writeTrue=\"true\"><text><![CDATA[a < b && c >= 45 %% @]]></text><body><![CDATA[Hello\n" +
			"\tHow are you?\n" +
			"~!@#$%^&*()_+}{][\\//><]]></body></bean>", xml);

		bax2 = (BeanAndXml) xStream.fromXML(xml);
		Assert.assertTrue(bax2.equals(bax));
	}

	// ---------------

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

	@XStreamAlias("bean")
	public static class BeanAndXml {
		@XStreamAsAttribute
		private String attr;

		private String text;

		private String body;

		@XStreamAsAttribute
		private boolean writeFalse = false;

		@XStreamAsAttribute
		private boolean writeTrue = true;

		// ------------------------------

		public String getAttr() {
			return attr;
		}

		public BeanAndXml setAttr(String attr) {
			this.attr = attr;
			return this;
		}

		public String getText() {
			return text;
		}

		public BeanAndXml setText(String text) {
			this.text = text;
			return this;
		}

		public String getBody() {
			return body;
		}

		public BeanAndXml setBody(String body) {
			this.body = body;
			return this;
		}

		public boolean isWriteFalse() {
			return writeFalse;
		}

		public BeanAndXml setWriteFalse(boolean writeFalse) {
			this.writeFalse = writeFalse;
			return this;
		}

		public boolean isWriteTrue() {
			return writeTrue;
		}

		public BeanAndXml setWriteTrue(boolean writeTrue) {
			this.writeTrue = writeTrue;
			return this;
		}

		// ------------------------------

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof BeanAndXml)) return false;

			BeanAndXml that = (BeanAndXml) o;

			if (isWriteFalse() != that.isWriteFalse()) return false;
			if (isWriteTrue() != that.isWriteTrue()) return false;
			if (getAttr() != null ? !getAttr().equals(that.getAttr()) : that.getAttr() != null) return false;
			if (getText() != null ? !getText().equals(that.getText()) : that.getText() != null) return false;
			return !(getBody() != null ? !getBody().equals(that.getBody()) : that.getBody() != null);

		}

		@Override
		public int hashCode() {
			int result = getAttr() != null ? getAttr().hashCode() : 0;
			result = 31 * result + (getText() != null ? getText().hashCode() : 0);
			result = 31 * result + (getBody() != null ? getBody().hashCode() : 0);
			result = 31 * result + (isWriteFalse() ? 1 : 0);
			result = 31 * result + (isWriteTrue() ? 1 : 0);
			return result;
		}
	}
}
