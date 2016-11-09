package org.devocative.adroit;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import org.devocative.adroit.vo.DateFieldVO;

import java.text.ParseException;
import java.util.Date;

public final class CalendarUtil {
	private static final ULocale fa = new ULocale("en_US@calendar=persian");

	// ------------------------------ DATE CONVERSION

	public static String toPersian(Date dt, String pattern) {
		return new SimpleDateFormat(pattern, fa).format(dt);
	}

	public static DateFieldVO toPersianDateField(Date dt) {
		return toPersianDateField(dt, java.util.TimeZone.getDefault());
	}

	public static DateFieldVO toPersianDateField(Date dt, java.util.TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone.getID()), fa);
		calendar.setTime(dt);

		return new DateFieldVO()
			.setYear(calendar.get(Calendar.YEAR))
			.setMonth(calendar.get(Calendar.MONTH) + 1)
			.setDay(calendar.get(Calendar.DAY_OF_MONTH))
			.setHour(calendar.get(Calendar.HOUR_OF_DAY))
			.setMinute(calendar.get(Calendar.MINUTE))
			.setSecond(calendar.get(Calendar.SECOND))
			;
	}

	public static Date toGregorian(String persianDate, String pattern) {
		try {
			return new SimpleDateFormat(pattern, fa).parse(persianDate);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static Date toGregorian(DateFieldVO dateField) {
		return toGregorian(dateField, java.util.TimeZone.getDefault());
	}

	public static Date toGregorian(DateFieldVO dateField, java.util.TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone.getID()), fa);
		calendar.set(Calendar.YEAR, dateField.getYear());
		calendar.set(Calendar.MONTH, dateField.getMonth() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, dateField.getDay());
		calendar.set(Calendar.HOUR_OF_DAY, dateField.getHour());
		calendar.set(Calendar.MINUTE, dateField.getMinute());
		calendar.set(Calendar.SECOND, dateField.getSecond());
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime();
	}

	public static DateFieldVO getDateField(Date dt) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dt);
		return new DateFieldVO()
			.setYear(calendar.get(Calendar.YEAR))
			.setMonth(calendar.get(Calendar.MONTH) + 1)
			.setDay(calendar.get(Calendar.DAY_OF_MONTH))
			.setHour(calendar.get(Calendar.HOUR_OF_DAY))
			.setMinute(calendar.get(Calendar.MINUTE))
			.setSecond(calendar.get(Calendar.SECOND))
			;
	}

	public static Date getDate(DateFieldVO dateField) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, dateField.getYear());
		calendar.set(Calendar.MONTH, dateField.getMonth() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, dateField.getDay());
		calendar.set(Calendar.HOUR_OF_DAY, dateField.getHour());
		calendar.set(Calendar.MINUTE, dateField.getMinute());
		calendar.set(Calendar.SECOND, dateField.getSecond());
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime();
	}

	// ------------------------------

	public static String formatDate(Date dt, String pattern) {
		return new SimpleDateFormat(pattern).format(dt);
	}

	public static Date parseDate(String dt, String pattern) {
		try {
			return new SimpleDateFormat(pattern).parse(dt);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static Date add(Date dt, int field, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.add(field, amount);
		return cal.getTime();
	}
}
