package org.devocative.adroit;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import org.devocative.adroit.vo.DateFieldVO;

import java.text.ParseException;
import java.util.Date;

public class CalendarUtil {
	private static final ULocale fa = new ULocale("en_US@calendar=persian");

	public static String toPersian(Date dt, String pattern) {
		return new SimpleDateFormat(pattern, fa).format(dt);
	}

	public static DateFieldVO toPersianDateField(Date dt) {
		Calendar calendar = Calendar.getInstance(fa);
		calendar.setTime(dt);
		return new DateFieldVO()
			.setYear(calendar.get(Calendar.YEAR))
			.setMonth(calendar.get(Calendar.MONTH) + 1)
			.setDay(calendar.get(Calendar.DAY_OF_MONTH))
			.setHour(calendar.get(Calendar.HOUR))
			.setMinute(calendar.get(Calendar.MINUTE))
			.setSecond(calendar.get(Calendar.SECOND))
			;
	}

	public static Date toGregorian(String persianDate, String pattern) throws ParseException {
		return new SimpleDateFormat(pattern, fa).parse(persianDate);
	}

	public static Date toGregorian(DateFieldVO dateField) {
		Calendar calendar = Calendar.getInstance(fa);
		calendar.set(Calendar.YEAR, dateField.getYear());
		calendar.set(Calendar.MONTH, dateField.getMonth() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, dateField.getDay());
		calendar.set(Calendar.HOUR, dateField.getHour());
		calendar.set(Calendar.MINUTE, dateField.getMinute());
		calendar.set(Calendar.SECOND, dateField.getSecond());
		return calendar.getTime();
	}

}
