package org.devocative.adroit.date;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

import java.util.Date;

public enum EUniCalendar {
	Gregorian(new ULocale("en_US")),
	Persian(new ULocale("en_US@calendar=persian"));

	// ------------------------------

	private ULocale locale;

	// ------------------------------

	EUniCalendar(ULocale uLocale) {
		this.locale = uLocale;
	}

	// ------------------------------

	ULocale getLocale() {
		return locale;
	}


	// ------------------------------

	public String convertToString(Date dt, String pattern, java.util.TimeZone timeZone) {
		final SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
		format.setTimeZone(TimeZone.getTimeZone(timeZone.getID()));
		return format.format(dt);
	}

	public Date convertToDate(DateFieldVO dateFieldVO, java.util.TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone.getID()), locale);
		calendar.set(Calendar.YEAR, dateFieldVO.getYear());
		calendar.set(Calendar.MONTH, dateFieldVO.getMonth() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, dateFieldVO.getDay());
		calendar.set(Calendar.HOUR_OF_DAY, dateFieldVO.getHour());
		calendar.set(Calendar.MINUTE, dateFieldVO.getMinute());
		calendar.set(Calendar.SECOND, dateFieldVO.getSecond());
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public DateFieldVO convertToFields(Date dt, java.util.TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone.getID()), locale);
		calendar.setTime(dt);

		return new DateFieldVO()
			.setYear(calendar.get(Calendar.YEAR))
			.setMonth(calendar.get(Calendar.MONTH) + 1)
			.setDay(calendar.get(Calendar.DAY_OF_MONTH))
			.setHour(calendar.get(Calendar.HOUR_OF_DAY))
			.setMinute(calendar.get(Calendar.MINUTE))
			.setSecond(calendar.get(Calendar.SECOND))
			.setMillisecond(calendar.get(Calendar.MILLISECOND))
			;
	}
}
