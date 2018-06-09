package org.devocative.adroit.date;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

public class UniDate implements Serializable {
	private static final long serialVersionUID = 2794409518338560515L;

	// ------------------------------

	private final EUniCalendar calendar;
	private final Calendar mainCal;

	// ------------------------------

	private UniDate(EUniCalendar calendar, Calendar mainCal) {
		this.calendar = calendar;
		this.mainCal = mainCal;
	}

	private UniDate(EUniCalendar calendar, int year, int month, int day) {
		this.calendar = calendar;

		mainCal = createCalendar(calendar);
		mainCal.set(Calendar.YEAR, year);
		mainCal.set(Calendar.MONTH, month - 1);
		mainCal.set(Calendar.DAY_OF_MONTH, day);
	}

	private UniDate(EUniCalendar calendar, int year, int month, int day, int hour, int minute, int second, int millisecond) {
		this(calendar, year, month, day);

		mainCal.set(Calendar.HOUR_OF_DAY, hour);
		mainCal.set(Calendar.MINUTE, minute);
		mainCal.set(Calendar.SECOND, second);
		mainCal.set(Calendar.MILLISECOND, millisecond);
	}

	private UniDate(Date date) {
		this.calendar = EUniCalendar.Gregorian;

		mainCal = createCalendar(this.calendar);
		mainCal.setTime(date);
	}

	private UniDate(long timeInMillis) {
		this(new Date(timeInMillis));
	}

	private UniDate(UniDate old) {
		this.calendar = old.calendar;
		this.mainCal = (Calendar) old.mainCal.clone();
	}

	// ------------------------------

	public static UniDate of(EUniCalendar calendar, java.util.TimeZone timeZone) {
		return new UniDate(calendar, createCalendar(calendar, TimeZone.getTimeZone(timeZone.getID()), null));
	}

	public static UniDate of(EUniCalendar calendar, int year, int month, int day) {
		return new UniDate(calendar, year, month, day, 0, 0, 0, 0);
	}

	public static UniDate of(EUniCalendar calendar, int year, int month, int day, int hour, int minute, int second) {
		return of(calendar, year, month, day, hour, minute, second, 0);
	}

	public static UniDate of(EUniCalendar calendar, int year, int month, int day, int hour, int minute, int second, int millisecond) {
		return new UniDate(calendar, year, month, day, hour, minute, second, millisecond);
	}

	public static UniDate of(EUniCalendar calendar, String dateStr, String pattern) {
		try {
			final Date parse = createDateFormat(pattern, calendar).parse(dateStr);
			return new UniDate(calendar, createCalendar(calendar, parse));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static UniDate of(Date date) {
		return new UniDate(date);
	}

	public static UniDate of(long timeInMillis) {
		return new UniDate(timeInMillis);
	}

	public static UniDate now() {
		return new UniDate(new Date());
	}

	// ------------------------------

	public EUniCalendar getCalendar() {
		return calendar;
	}

	public int getYear() {
		return mainCal.get(Calendar.YEAR);
	}

	public int getMonth() {
		return mainCal.get(Calendar.MONTH) + 1;
	}

	public int getDay() {
		return mainCal.get(Calendar.DAY_OF_MONTH);
	}

	public int getHour() {
		return mainCal.get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute() {
		return mainCal.get(Calendar.MINUTE);
	}

	public int getSecond() {
		return mainCal.get(Calendar.SECOND);
	}

	public int get(EUniDateField field) {
		return mainCal.get(field.getValue());
	}

	public java.util.TimeZone getTimeZone() {
		return java.util.TimeZone.getTimeZone(mainCal.getTimeZone().getID());
	}

	// ---------------

	public Date toDate() {
		return mainCal.getTime();
	}

	public long toTimeInMillis() {
		return mainCal.getTimeInMillis();
	}

	public String format(String pattern) {
		return createDateFormat(pattern, calendar, mainCal.getTimeZone())
			.format(mainCal.getTime());
	}

	public String format(String pattern, java.util.TimeZone timeZone) {
		return createDateFormat(pattern, calendar, TimeZone.getTimeZone(timeZone.getID()))
			.format(mainCal.getTime());
	}

	public UniPeriod diff(UniDate date) {
		final long ths = toTimeInMillis();
		final long tht = date.toTimeInMillis();
		if (ths > tht)
			return UniPeriod.of(ths, tht);
		return UniPeriod.of(tht, ths);
	}

	public UniPeriod sub(UniDate operand2) {
		return UniPeriod.of(this.toTimeInMillis(), operand2.toTimeInMillis());
	}

	// ---------------

	public UniDate updateCalendar(EUniCalendar calendar) {
		final Date date = mainCal.getTime();
		return new UniDate(calendar, createCalendar(calendar, date));
	}

	public UniDate updateTimeZone(java.util.TimeZone timeZone) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.setTimeZone(TimeZone.getTimeZone(timeZone.getID()));
		return cloned;
	}

	public UniDate updateYear(int diff) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.add(Calendar.YEAR, diff);
		return cloned;
	}

	public UniDate updateMonth(int diff) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.add(Calendar.MONTH, diff);
		return cloned;
	}

	public UniDate updateDay(int diff) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.add(Calendar.DAY_OF_MONTH, diff);
		return cloned;
	}

	public UniDate updateHour(int diff) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.add(Calendar.HOUR_OF_DAY, diff);
		return cloned;
	}

	public UniDate updateMinute(int diff) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.add(Calendar.MINUTE, diff);
		return cloned;
	}

	public UniDate updateSecond(int diff) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.add(Calendar.SECOND, diff);
		return cloned;
	}

	public UniDate update(EUniDateField field, int diff) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.add(field.getValue(), diff);
		return cloned;
	}

	// ---------------

	public UniDate setYear(int year) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.set(Calendar.YEAR, year);
		return cloned;
	}

	public UniDate setMonth(int month) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.set(Calendar.MONTH, month - 1);
		return cloned;
	}

	public UniDate setDay(int day) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.set(Calendar.DAY_OF_MONTH, day);
		return cloned;
	}

	public UniDate setHour(int hour) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.set(Calendar.HOUR_OF_DAY, hour);
		return cloned;
	}

	public UniDate setMinute(int minute) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.set(Calendar.MINUTE, minute);
		return cloned;
	}

	public UniDate setSecond(int second) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.set(Calendar.SECOND, second);
		return cloned;
	}

	public UniDate setDate(int year, int month, int day) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.set(year, month - 1, day);
		return cloned;
	}

	public UniDate setTime(int hour, int minute, int second) {
		return setTime(hour, minute, second, 0);
	}

	public UniDate setTime(int hour, int minute, int second, int millisecond) {
		UniDate cloned = new UniDate(this);
		cloned.mainCal.set(Calendar.HOUR_OF_DAY, hour);
		cloned.mainCal.set(Calendar.MINUTE, minute);
		cloned.mainCal.set(Calendar.SECOND, second);
		cloned.mainCal.set(Calendar.MILLISECOND, millisecond);
		return cloned;
	}

	public UniDate set(EUniDateField field, int value) {
		UniDate cloned = new UniDate(this);
		if (field == EUniDateField.MONTH) {
			cloned.mainCal.set(field.getValue(), value - 1);
		} else {
			cloned.mainCal.set(field.getValue(), value);
		}
		return cloned;
	}

	// ---------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UniDate)) return false;

		UniDate uniDate = (UniDate) o;

		if (calendar != uniDate.calendar) return false;
		return mainCal != null ? mainCal.equals(uniDate.mainCal) : uniDate.mainCal == null;
	}

	@Override
	public int hashCode() {
		int result = calendar != null ? calendar.hashCode() : 0;
		result = 31 * result + (mainCal != null ? mainCal.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return format("yyyy-MM-dd HH:mm:ss.SSS VV");
	}

	// ------------------------------

	private static SimpleDateFormat createDateFormat(String pattern, EUniCalendar calendar) {
		return createDateFormat(pattern, calendar, getDefault());
	}

	private static SimpleDateFormat createDateFormat(String pattern, EUniCalendar calendar, TimeZone timeZone) {
		SimpleDateFormat format = new SimpleDateFormat(pattern, calendar.getLocale());
		format.setTimeZone(timeZone);
		return format;
	}

	private static Calendar createCalendar(EUniCalendar calendar) {
		return createCalendar(calendar, getDefault(), null);
	}

	private static Calendar createCalendar(EUniCalendar calendar, Date date) {
		return createCalendar(calendar, getDefault(), date);
	}

	private static Calendar createCalendar(EUniCalendar calendar, TimeZone tz, Date date) {
		Calendar c = Calendar.getInstance(tz, calendar.getLocale());
		if (date != null) {
			c.setTime(date);
		}
		return c;
	}

	private static TimeZone getDefault() {
		return TimeZone.getTimeZone(java.util.TimeZone.getDefault().getID());
	}
}
