package org.devocative.adroit.date;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

import java.util.Date;

/*
List of calendars at
com.ibm.icu.util.Calendar.CalType
 */
public enum EUniCalendar {
	Gregorian(new ULocale("en_US@calendar=gregorian")),

	/*
	check at:
	https://calendar.zoznam.sk/persian_calendar-en.php
	 */
	Persian(new ULocale("@calendar=persian")),

	/*
	check at:
	http://www.utbf.org/en/resources/calendar/
	 */
	Buddhist(new ULocale("en_US@calendar=buddhist")),

	/*
	?
	 */
	Chinese(new ULocale("@calendar=chinese")),

	/*
	check at:
	https://calendar.zoznam.sk/coptic_calendar-en.php
	 */
	Coptic(new ULocale("@calendar=coptic")),

	/*
	?
	 */
	Dangi(new ULocale("@calendar=dangi")),

	/*
	check at:
	https://calendar.zoznam.sk/ethiopian_calendar-en.php
	 */
	Ethiopian(new ULocale("@calendar=ethiopic")),

	/*
	check at:
	http://www.hebcal.com/converter/
	 */
	Hebrew(new ULocale("@calendar=hebrew")),

	/*
	The Indian Calendar also know as the Saka Calendar or the Hindu Calendar forms the basis in determining
	Hindu religious festivals. The months of the Indian Hindu Calendar starts with Chaitra and ends with Phalguna.

	check at:
	http://www.probharat.com/indian-calendars/indian-calendar.php
	 */
	Indian(new ULocale("@calendar=indian")),

	/*
	check at:
	https://calendar.zoznam.sk/islamic_calendar-en.php

	Islamic Calendar Types:
	http://cldr.unicode.org/development/development-process/design-proposals/islamic-calendar-types
	 */
	Islamic(new ULocale("@calendar=islamic")),
	IslamicCivil(new ULocale("@calendar=islamic-civil")),
	IslamicRGSA(new ULocale("@calendar=islamic-rgsa")),
	IslamicTbla(new ULocale("@calendar=islamic-tbla")),
	IslamicUmalqura(new ULocale("@calendar=islamic-umalqura")),

	/*
	check at:
	http://www.ewc.co.jp/Pages/Information/CalendarEN.aspx
	 */
	Japanese(new ULocale("en_US@calendar=japanese")),;

	// ------------------------------

	private ULocale locale;

	// ------------------------------

	EUniCalendar(ULocale uLocale) {
		this.locale = uLocale;
	}

	// ------------------------------

	public ULocale getLocale() {
		return locale;
	}


	// ------------------------------

	public String convertToString(Date dt, String pattern, java.util.TimeZone timeZone) {
		final SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
		format.setTimeZone(TimeZone.getTimeZone(timeZone.getID()));
		return format.format(dt);
	}

	public Date convertToDate(DateTimeFieldVO dateTimeFieldVO, java.util.TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone.getID()), locale);
		calendar.set(Calendar.YEAR, dateTimeFieldVO.getYear());
		calendar.set(Calendar.MONTH, dateTimeFieldVO.getMonth() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, dateTimeFieldVO.getDay());
		calendar.set(Calendar.HOUR_OF_DAY, dateTimeFieldVO.getHour());
		calendar.set(Calendar.MINUTE, dateTimeFieldVO.getMinute());
		calendar.set(Calendar.SECOND, dateTimeFieldVO.getSecond());
		calendar.set(Calendar.MILLISECOND, dateTimeFieldVO.getMillisecond());
		return calendar.getTime();
	}

	public DateTimeFieldVO convertToFields(Date dt, java.util.TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone.getID()), locale);
		calendar.setTime(dt);

		return new DateTimeFieldVO()
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
