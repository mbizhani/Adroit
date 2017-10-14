package org.devocative.adroit;

import java.io.*;
import java.util.*;

public final class ConfigUtil {
	private static final String ENC_SUFFIX = "~ENC";
	private static final String ENC_PREFIX = "ENC~";
	private static final Properties PROPERTIES = new Properties();
	private static final List<IConfigKey> CONFIG_KEYS = new ArrayList<>();

	static {
		load(ConfigUtil.class.getResourceAsStream("/config.properties"));
	}

	public static void load(InputStream stream) {
		PROPERTIES.clear();

		try {
			PROPERTIES.load(stream);
			stream.close();

			if (PROPERTIES.size() == 0) {
				throw new RuntimeException("Empty [config.properties]!");
			}
		} catch (Exception e) {
			throw new RuntimeException("Can't load [config.properties]!", e);
		}
	}

	// ------------------------------ String

	public static String getString(boolean validate, String key) {
		String value = PROPERTIES.getProperty(key);

		if (value == null && PROPERTIES.containsKey(key + ENC_SUFFIX)) {
			value = PROPERTIES.getProperty(key + ENC_SUFFIX);
			if (value != null) {
				value = StringEncryptorUtil.decrypt(value);
			}
		} else if (value != null && value.trim().startsWith(ENC_PREFIX)) {
			value = value.substring(4);
			value = StringEncryptorUtil.decrypt(value);
		}

		if (validate) {
			if (value == null) {
				throw new RuntimeException("Key not found: " + key);
			} else if (value.trim().length() == 0) {
				throw new RuntimeException("Key without value: " + key);
			}
		}

		return value;
	}

	public static String getString(String key, String defaultValue) {
		String value = getString(false, key);
		return value != null ? value : defaultValue;
	}

	public static String getString(IConfigKey configKey) {
		String val = configKey.getValidate() ?
			getString(true, configKey.getKey()) :
			getString(configKey.getKey(), (String) configKey.getDefaultValue());

		checkValueInPossibilities(val, configKey);

		return val;
	}

	// ------------------------------ Boolean

	public static Boolean getBoolean(boolean validate, String key) {
		return Boolean.valueOf(getString(validate, key));
	}

	public static Boolean getBoolean(String key, boolean defaultValue) {
		try {
			return getString(false, key) != null ? Boolean.valueOf(getString(false, key)) : defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static Boolean getBoolean(IConfigKey configKey) {
		Boolean val = configKey.getValidate() ?
			getBoolean(true, configKey.getKey()) :
			getBoolean(configKey.getKey(), (Boolean) configKey.getDefaultValue());

		checkValueInPossibilities(val, configKey);

		return val;
	}

	// ------------------------------ Integer

	public static Integer getInteger(boolean validate, String key) {
		return Integer.valueOf(getString(validate, key));
	}

	public static Integer getInteger(String key, int defaultValue) {
		try {
			return getString(false, key) != null ? Integer.valueOf(getString(false, key)) : defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static Integer getInteger(IConfigKey configKey) {
		Integer val = configKey.getValidate() ?
			getInteger(true, configKey.getKey()) :
			getInteger(configKey.getKey(), (Integer) configKey.getDefaultValue());

		checkValueInPossibilities(val, configKey);

		return val;
	}

	// ------------------------------ Long

	public static Long getLong(boolean validate, String key) {
		return Long.valueOf(getString(validate, key));
	}

	public static Long getLong(String key, long defaultValue) {
		try {
			return getString(false, key) != null ? Long.valueOf(getString(false, key)) : defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static Long getLong(IConfigKey configKey) {
		Long val = configKey.getValidate() ?
			getLong(true, configKey.getKey()) :
			getLong(configKey.getKey(), (Long) configKey.getDefaultValue());

		checkValueInPossibilities(val, configKey);

		return val;
	}

	// ------------------------------ List<String>

	public static List<String> getList(boolean validate, String key) {
		String value = getString(validate, key);

		List<String> result = new ArrayList<>();
		if (value != null) {
			String[] parts = value.split("[,]");
			for (String part : parts) {
				if (part.trim().length() > 0) {
					result.add(part.trim());
				}
			}
		}
		return result;
	}

	public static List<String> getList(String key, List<String> defaultValue) {
		String value = getString(false, key);

		if (value != null) {
			List<String> result = new ArrayList<>();
			String[] parts = value.split("[,]");
			for (String part : parts) {
				if (part.trim().length() > 0) {
					result.add(part.trim());
				}
			}
			return result;
		}
		return defaultValue;
	}

	public static List<String> getList(IConfigKey configKey) {
		return configKey.getValidate() ?
			getList(true, configKey.getKey()) :
			getList(configKey.getKey(), (List<String>) configKey.getDefaultValue());
	}

	// ------------------------------ Other Methods

	public static void updateKey(String key, String value) {
		if (value != null) {
			if (PROPERTIES.containsKey(key + ENC_SUFFIX)) {
				PROPERTIES.setProperty(key + ENC_SUFFIX, StringEncryptorUtil.encrypt(value));
			} else {
				PROPERTIES.setProperty(key, value);
			}
		}
	}

	public static void addKey(String key, String value, boolean doEncrypt) {
		if (doEncrypt) {
			if (PROPERTIES.containsKey(key + ENC_SUFFIX)) {
				throw new RuntimeException("Property already exists: " + key);
			}
			PROPERTIES.setProperty(key + ENC_SUFFIX, StringEncryptorUtil.encrypt(value));
		} else {
			if (PROPERTIES.containsKey(key)) {
				throw new RuntimeException("Property already exists: " + key);
			}
			PROPERTIES.setProperty(key, value);
		}
	}

	public static void removeKey(String key) {
		PROPERTIES.remove(key);
	}

	public static boolean hasKey(String key) {
		return PROPERTIES.containsKey(key);
	}

	public static boolean hasKey(IConfigKey configKey) {
		return PROPERTIES.containsKey(configKey.getKey());
	}

	public static boolean keyHasValue(IConfigKey configKey) {
		return PROPERTIES.containsKey(configKey.getKey()) || configKey.getDefaultValue() != null;
	}

	public static void write() {
		try {
			write(new File(ConfigUtil.class.getResource("/config.properties").toURI()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void write(File file) {
		String line;
		List<String> lines = new ArrayList<>();
		Set<String> notVisitedKeys = new HashSet<>(PROPERTIES.stringPropertyNames());
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					lines.add(line);
				} else if (line.contains("=")) {
					String[] parts = line.split("=");
					if (PROPERTIES.getProperty(parts[0]) != null) {
						lines.add(String.format("%s=%s", parts[0], PROPERTIES.getProperty(parts[0])));
						notVisitedKeys.remove(parts[0]);
					} /*else {
						lines.add(line);
					}*/
				} else {
					lines.add(line);
				}
			}
			reader.close();

			FileWriter writer = new FileWriter(file);
			writer.write(String.format("# Write Method: %s\n\n", CalendarUtil.toPersian(new Date(), "yyyy/MM/dd HH:mm:ss")));
			for (String l : lines) {
				writer.write(l);
				writer.write("\n");
			}
			if (notVisitedKeys.size() > 0) {
				writer.write("\n# NEW KEYS\n");
				for (String key : notVisitedKeys) {
					writer.write(String.format("%s=%s\n", key, PROPERTIES.getProperty(key)));
				}
			}
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void add(IConfigKey key) {
		CONFIG_KEYS.add(key);
	}

	public static List<IConfigKey> getConfigKeys() {
		return CONFIG_KEYS;
	}

	public static boolean isEncrypted(String key) {
		return PROPERTIES.containsKey(key + ENC_SUFFIX);
	}

	// ------------------------------ PRIVATE

	private static void checkValueInPossibilities(Object val, IConfigKey configKey) {
		if (val != null && configKey.getPossibleValues() != null && !configKey.getPossibleValues().contains(val)) {
			throw new RuntimeException(String.format("Invalid value for key=[%s], possibles=%s",
				configKey.getKey(), configKey.getPossibleValues()));
		}
	}
}
