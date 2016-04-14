package org.devocative.adroit;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.util.*;

public class ObjectUtil {

	// --------------------- BEAN REFLECTION

	static {
		PropertyUtils.addBeanIntrospector(new FluentPropertyBeanIntrospector());
	}

	public static void merge(Object destBean, Object srcBean, boolean force) {
		merge(destBean, srcBean, force, null);
	}

	public static void merge(Object destBean, Object srcBean, boolean force, String filterByPackage) {
		PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(srcBean, force);

		for (PropertyDescriptor pd : propertyDescriptors) {
			String prop = pd.getName();

			if ("class".equals(prop)) {
				continue;
			}

			Object destValue = getPropertyValue(destBean, prop, force);
			Object srcValue = getPropertyValue(srcBean, prop, force);

			if (srcValue != null) {
				Class propType = srcValue.getClass();

				if (destValue == null) {
					setPropertyValue(destBean, prop, srcValue, force);
				} else {
					if (srcValue instanceof Collection) {
						Collection srcCol = (Collection) srcValue;
						Collection destCol = (Collection) destValue;
						for (Object srcObjOfCol : srcCol) {
							Object destObjOfCol = findFirstIn(srcObjOfCol, destCol);
							if (destObjOfCol == null) {
								destCol.add(srcObjOfCol);
							} else {
								merge(destObjOfCol, srcObjOfCol, force);
							}
						}
					} else if (propType.isArray()) {
						Object[] srcArr = (Object[]) srcValue;
						Object[] destArr = (Object[]) destValue;
						for (Object srcObjOfArr : srcArr) {
							Object destObjOfArr = findFirstIn(srcObjOfArr, destArr);
							if (destObjOfArr == null) {
								append(srcObjOfArr, destArr);
							} else {
								merge(destObjOfArr, srcObjOfArr, force);
							}
						}
					} else if (srcValue instanceof Map) {
						Map srcMap = (Map) srcValue;
						Map destMap = (Map) destValue;
						for (Object srcKey : srcMap.keySet()) {
							if (destMap.containsKey(srcKey)) {
								merge(destMap.get(srcKey), srcMap.get(srcKey), force);
							} else {
								destMap.put(srcKey, srcMap.get(srcKey));
							}
						}
					} else if (
						!propType.getName().startsWith("java.lang.") &&
							!propType.isPrimitive() &&
							(filterByPackage == null ||
								propType.getName().startsWith(filterByPackage))) {
						merge(destValue, srcValue, force);
					}
				}
			}
		}
	}

	public static PropertyDescriptor[] getPropertyDescriptors(Object bean, boolean force) {
		PropertyDescriptor[] result;
		try {
			result = PropertyUtils.getPropertyDescriptors(bean);
		} catch (Exception e) {
			if (force) {
				result = new PropertyDescriptor[0];
			} else {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static PropertyDescriptor[] getPropertyDescriptors(Class cls, boolean force) {
		PropertyDescriptor[] result;
		try {
			result = PropertyUtils.getPropertyDescriptors(cls);
		} catch (Exception e) {
			if (force) {
				result = new PropertyDescriptor[0];
			} else {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static PropertyDescriptor getPropertyDescriptor(Object bean, String property, boolean force) {
		PropertyDescriptor result;
		try {
			result = PropertyUtils.getPropertyDescriptor(bean, property);
		} catch (Exception e) {
			if (force) {
				result = null;
			} else {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static PropertyDescriptor getPropertyDescriptor(Class cls, String property, boolean force) {
		PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(cls, force);
		for (PropertyDescriptor descriptor : propertyDescriptors) {
			if (property.equals(descriptor.getName())) {
				return descriptor;
			}
		}
		return null;
	}

	public static Object getPropertyValue(Object bean, String property, boolean force) {
		Object result;
		try {
			result = PropertyUtils.getProperty(bean, property);
		} catch (Exception e) {
			if (force) {
				result = null;
			} else {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static void setPropertyValue(Object bean, String property, Object value, boolean force) {
		try {
			PropertyUtils.setProperty(bean, property, value);
		} catch (Exception e) {
			if (!force) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Object findFirstIn(Object obj, Collection col) {
		for (Object o : col) {
			if (obj.equals(o)) {
				return o;
			}
		}
		return null;
	}

	public static String toString(Object bean) {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		try {
			Map<String, String> describe = BeanUtils.describe(bean);
			describe.remove("class");

			for (Map.Entry<String, String> entry : describe.entrySet()) {
				builder
					.append("\t")
					.append(entry.getKey())
					.append(": ")
					.append(entry.getValue())
					.append("\n");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		builder.append("}");
		return builder.toString();
	}

	// --------------------- LIST/ARRAY UTILITIES

	public static Object findFirstIn(Object obj, Object[] arr) {
		for (Object o : arr) {
			if (obj.equals(o)) {
				return o;
			}
		}
		return null;
	}

	public static Object[] append(Object obj, Object[] arr) {
		Object[] result = new Object[arr.length + 1];
		int i;
		for (i = 0; i < arr.length; i++) {
			result[i] = arr[i];
		}
		result[i] = obj;

		return result;
	}

	@SafeVarargs
	public static <T> List<T> asList(T... objects) {
		List<T> list = new ArrayList<>();
		Collections.addAll(list, objects);
		return list;
	}

	// --------------------- PRIMITIVE UTILITIES

	public static boolean hasIt(String str) {
		return str != null && str.trim().length() > 0;
	}

	public static boolean hasIt(Collection col) {
		return col != null && col.size() > 0;
	}

	public static boolean isTrue(Boolean b) {
		return b != null && b;
	}

	public static boolean isFalse(Boolean b) {
		return b == null || !b;
	}
}
