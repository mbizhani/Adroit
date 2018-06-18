package org.devocative.adroit;

import com.ibm.icu.util.Calendar;
import org.devocative.adroit.date.*;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDate {

	@Test
	public void testUniDate() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		UniDate date = UniDate.of(EUniCalendar.Gregorian, 2018, 5, 29, 1, 2, 3, 456);
		assertEquals(2018, date.getYear());
		assertEquals(5, date.getMonth());
		assertEquals(29, date.getDay());
		assertEquals(gr(2018, 5, 29, 1, 2, 3, 456), date.toDate());
		assertEquals("2018-05-29 01:02:03.456 UTC", date.format("yyyy-MM-dd HH:mm:ss.SSS VV"));
		assertEquals("2018-05-29 01:02:03.456 UTC", date.toString());

		TimeFieldVO timeFields = date.getTimeFields();
		assertEquals(1, timeFields.getHour());
		assertEquals(2, timeFields.getMinute());
		assertEquals(3, timeFields.getSecond());
		assertEquals(456, timeFields.getMillisecond());

		DateTimeFieldVO dateTimeFields = date.getDateTimeFields();
		assertEquals(2018, dateTimeFields.getYear());
		assertEquals(5, dateTimeFields.getMonth());
		assertEquals(29, dateTimeFields.getDay());
		assertEquals(1, dateTimeFields.getHour());
		assertEquals(2, dateTimeFields.getMinute());
		assertEquals(3, dateTimeFields.getSecond());
		assertEquals(456, dateTimeFields.getMillisecond());

		/*
		y = 2010
		m = 15 -> y = 2011, m = 3
		d =  0 -> y = 2011, m = 2, d = 28 (last day of Feb)
		 */
		date = UniDate.of(EUniCalendar.Gregorian, 2010, 15, 0);
		assertEquals(2011, date.getYear());
		assertEquals(2, date.getMonth());
		assertEquals(28, date.getDay());
		assertEquals(gr(2010, 15, 0, 0, 0, 0), date.toDate());
		assertEquals("2011-02-28 00:00:00.000 UTC", date.format("yyyy-MM-dd HH:mm:ss.SSS VV"));

		/*
		y = 2010
		m = 0 -> y = 2009, m = 12
		d = 0 -> y = 2011, m = 11, d = 30 (last day of Nov)
		 */
		date = UniDate.of(EUniCalendar.Gregorian, 2010, 0, 0);
		assertEquals(2009, date.getYear());
		assertEquals(11, date.getMonth());
		assertEquals(30, date.getDay());

		date = UniDate.of(EUniCalendar.Persian, 1397, 3, 8);
		assertEquals(1397, date.getYear());
		assertEquals(3, date.getMonth());
		assertEquals(8, date.getDay());
		assertEquals(gr(2018, 5, 29, 0, 0, 0), date.toDate());
		assertEquals("1397-03-08 00:00:00", date.format("yyyy-MM-dd HH:mm:ss"));
		assertEquals("1397-03-08 00:00:00.000 UTC", date.toString());

		date = UniDate.of(EUniCalendar.Gregorian, "2018-01-10", "yyyy-MM-dd");
		assertEquals(2018, date.getYear());
		assertEquals(1, date.getMonth());
		assertEquals(10, date.getDay());
		assertEquals(gr(2018, 1, 10, 0, 0, 0), date.toDate());
		assertEquals("2018-01-10", date.format("yyyy-MM-dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Buddhist);
		System.out.println("Buddhist\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Chinese);
		System.out.println("Chinese\t\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Coptic);
		System.out.println("Coptic\t\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Dangi);
		System.out.println("Dangi\t\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Ethiopian);
		System.out.println("Ethiopian\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Gregorian);
		System.out.println("Gregorian\t\t= " + date.format("yyyy MMMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Hebrew);
		System.out.println("Hebrew\t\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Islamic);
		System.out.println("Islamic\t\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.IslamicCivil);
		System.out.println("IslamicCivil\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.IslamicRGSA);
		System.out.println("IslamicRGSA\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.IslamicTbla);
		System.out.println("IslamicTbla\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.IslamicUmalqura);
		System.out.println("IslamicUmalqura = " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Indian);
		System.out.println("Indian\t\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Persian);
		System.out.println("Persian\t\t\t= " + date.format("yyyy MMM dd"));

		date = UniDate.now().updateCalendar(EUniCalendar.Japanese);
		System.out.println("Japanese\t\t= " + date.format("yyyy MMM dd"));

		// Persian <-> Gregorian Conversion Asserts

		date = UniDate.of(EUniCalendar.Persian, "1397-03-17", "yyyy-MM-dd");
		assertEquals(1397, date.getYear());
		assertEquals(3, date.getMonth());
		assertEquals(17, date.getDay());
		assertEquals(gr(2018, 6, 7, 0, 0, 0), date.toDate());
		assertEquals("1397-03-17", date.format("yyyy-MM-dd"));

		date = UniDate.of(EUniCalendar.Gregorian, "2018-05-31", "yyyy-MM-dd")
			.updateCalendar(EUniCalendar.Persian);
		assertEquals(1397, date.getYear());
		assertEquals(3, date.getMonth());
		assertEquals(10, date.getDay());
		assertEquals(gr(2018, 5, 31, 0, 0, 0), date.toDate());
		assertEquals("1397-03-10", date.format("yyyy-MM-dd"));

		date = UniDate.of(EUniCalendar.Persian, "1396-12-29", "yyyy-MM-dd")
			.updateCalendar(EUniCalendar.Gregorian);
		assertEquals(2018, date.getYear());
		assertEquals(3, date.getMonth());
		assertEquals(20, date.getDay());
		assertEquals(gr(2018, 3, 20, 0, 0, 0), date.toDate());
		assertEquals("2018-03-20", date.format("yyyy-MM-dd"));

		date = UniDate.of(EUniCalendar.Persian, "1397-01-01", "yyyy-MM-dd")
			.updateCalendar(EUniCalendar.Persian);
		assertEquals(1397, date.getYear());
		assertEquals(1, date.getMonth());
		assertEquals(1, date.getDay());
		assertEquals(gr(2018, 3, 21, 0, 0, 0), date.toDate());
		assertEquals("1397-01-01", date.format("yyyy-MM-dd"));

		date = date
			.updateMonth(7)
			.updateDay(23)
			.updateYear(-1)
			.updateHour(2)
			.updateMinute(13)
			.updateSecond(70);
		assertEquals(1396, date.getYear());
		assertEquals(8, date.getMonth());
		assertEquals(24, date.getDay());
		assertEquals(2, date.getHour());
		assertEquals(14, date.getMinute());
		assertEquals(10, date.getSecond());
		assertEquals(gr(2017, 11, 15, 2, 14, 10), date.toDate());
		assertEquals("1396-08-24 02:14:10", date.format("yyyy-MM-dd HH:mm:ss"));

		Date now = new Date();
		date = UniDate.of(now);
		assertEquals(now, date.toDate());

		date = UniDate.of(now.getTime());
		assertEquals(now, date.toDate());
	}

	@Test
	public void testCalendar() {
		final Date date = EUniCalendar.Gregorian
			.convertToDate(
				new DateTimeFieldVO(2018, 2, 4, 6, 6, 6, 100),
				TimeZone.getDefault());

		assertEquals(gr(2018, 2, 4, 6, 6, 6, 100), date);
	}

	@Test
	public void testTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+4"));
		assertEquals("GMT+04:00", TimeZone.getDefault().getID());

		UniDate date01 = UniDate.of(EUniCalendar.Gregorian, TimeZone.getTimeZone("UTC"));
		UniDate date02 = date01.setTime(8, 0, 0);

		assertTrue(date01 != date02);
		assertEquals("UTC", date01.getTimeZone().getID());
		assertEquals("UTC", date02.getTimeZone().getID());
		assertEquals(8, date02.getHour());
		assertEquals(gr(null, null, null, 12, 0, 0).toString(), date02.toDate().toString());
		assertEquals("08:00:00 GMT", date02.format("HH:mm:ss v"));
		assertEquals("13:00:00 GMT+5", date02.format("HH:mm:ss v", TimeZone.getTimeZone("GMT+05:00")));
		assertTrue(date02.toDate().toString().contains("12:00:00 GMT+04:00"));

		UniDate date = UniDate.of(EUniCalendar.Gregorian, 2018, 1, 1)
			.setTime(8, 18, 28)
			.updateTimeZone(TimeZone.getTimeZone("UTC"));
		assertEquals("2018-01-01 08:18:28.000 UTC", date.toString());
		assertEquals("Mon Jan 01 12:18:28 GMT+04:00 2018", date.toDate().toString());

		System.out.println("date = " + date.toDate());
		// ---------------

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		assertEquals("UTC", TimeZone.getDefault().getID());

		date = UniDate.now()
			.setTime(8, 18, 28);
		assertEquals(8, date.getHour());
		assertEquals(18, date.getMinute());
		assertEquals(28, date.getSecond());
		assertEquals("UTC", date.getTimeZone().getID());
		assertEquals("13:18:28 GMT+5", date.format("HH:mm:ss v", TimeZone.getTimeZone("GMT+5")));

		/*final String tz = "Asia/Tehran";
		System.out.println("   v " + date.format("v", TimeZone.getTimeZone(tz)));
		System.out.println("  vv " + date.format("vv", TimeZone.getTimeZone(tz)));
		System.out.println(" vvv " + date.format("vvv", TimeZone.getTimeZone(tz)));
		System.out.println("vvvv " + date.format("vvvv", TimeZone.getTimeZone(tz)));
		System.out.println("   V " + date.format("V", TimeZone.getTimeZone(tz)));
		System.out.println("  VV " + date.format("VV", TimeZone.getTimeZone(tz)));
		System.out.println(" VVV " + date.format("VVV", TimeZone.getTimeZone(tz)));
		System.out.println("VVVV " + date.format("VVVV", TimeZone.getTimeZone(tz)));
		System.out.println("   z " + date.format("z", TimeZone.getTimeZone(tz)));
		System.out.println("  zz " + date.format("zz", TimeZone.getTimeZone(tz)));
		System.out.println(" zzz " + date.format("zzz", TimeZone.getTimeZone(tz)));
		System.out.println("zzzz " + date.format("zzzz", TimeZone.getTimeZone(tz)));
		System.out.println("   Z " + date.format("Z", TimeZone.getTimeZone(tz)));
		System.out.println("  ZZ " + date.format("ZZ", TimeZone.getTimeZone(tz)));
		System.out.println(" ZZZ " + date.format("ZZZ", TimeZone.getTimeZone(tz)));
		System.out.println("ZZZZ " + date.format("ZZZZ", TimeZone.getTimeZone(tz)));*/

		/*
		   v IR
		  vv null
		 vvv null
		vvvv IR
		   V irthr
		  VV Asia/Tehran
		 VVV Tehran
		VVVV IR
		   z GMT+4:30
		  zz GMT+4:30
		 zzz GMT+4:30
		zzzz GMT+04:30
		   Z +0430
		  ZZ +0430
		 ZZZ +0430
		ZZZZ GMT+04:30
		*/
	}

	@Test
	public void testPeriod() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		UniDate start = UniDate.of(EUniCalendar.Gregorian, 2018, 1, 1, 1, 0, 0);
		UniDate end = start.setTime(9, 59, 30);

		assertTrue(start != end);
		assertEquals(1, start.getHour());
		assertEquals(9, end.getHour());

		UniPeriod period = UniPeriod.of(end.toTimeInMillis(), start.toTimeInMillis());
		assertEquals(539, period.getTotalMinutes());
		assertEquals(8, period.getTotalHours());
		assertEquals(0, period.getTotalDays());
		assertEquals("539 (30)", period.format("M (S)"));
		assertEquals("08-59-30", period.format("H-M-S"));
		assertEquals("0 08:59:30", period.format("D H:M:S"));
		assertEquals("00 08:59:30", period.format("DD H:M:S"));

		UniDate end01 = start.setMonth(5).setDay(5);
		UniDate end02 = start.setDate(2018, 5, 5);

		assertTrue(end01 != end02);
		assertTrue(end01.equals(end02));
		assertEquals(5, end01.getMonth());
		assertEquals(5, end02.getMonth());

		period = UniPeriod.of(end01.toTimeInMillis(), start.toTimeInMillis());
		assertEquals("2976:00:00", period.format("H:M:S"));
		assertEquals("124 00:00:00", period.format("D H:M:S"));
		assertEquals("124 00:00:00", period.format("DD H:M:S"));
		assertEquals("124 00:00:00", period.format("DDD H:M:S"));
		assertEquals("0124 00:00:00", period.format("DDDD H:M:S"));
		assertEquals("00124 00:00:00", period.format("DDDDD H:M:S"));

		period = start.diff(end02);
		assertEquals(178560, period.getTotalMinutes());
		assertEquals(2976, period.getTotalHours());
		assertEquals("178560:00", period.format("M:S"));
		assertEquals("2976:00:00", period.format("H:M:S"));
		assertEquals("124 00:00:00", period.format("D H:M:S"));

		period = end02.diff(start);
		assertEquals(178560, period.getTotalMinutes());
		assertEquals(2976, period.getTotalHours());
		assertEquals("178560:00", period.format("M:S"));
		assertEquals("2976:00:00", period.format("H:M:S"));
		assertEquals("124 00:00:00", period.format("D H:M:S"));

		period = UniPeriod.of(end01.toDate(), end02.toDate());
		assertEquals("0 00:00:00", period.format("D H:M:S"));
		assertEquals(0, period.getTotalMinutes());

		period = end02.diff(end02);
		assertEquals("0 00:00:00", period.format("D H:M:S"));
		assertEquals(0, period.getTotalMinutes());

		try {
			start.sub(end);
			assertTrue(false);
		} catch (Exception e) {
			assertEquals("Invalid start and end: start is greater than end!", e.getMessage());
		}

		period = end.sub(start);
		assertEquals("08 59 30", period.format("H M S"));
	}

	// ------------------------------

	private Date gr(Integer y, Integer mo, Integer d, Integer h, Integer mi, Integer s) {
		return gr(y, mo, d, h, mi, s, 0);
	}

	private Date gr(Integer y, Integer mo, Integer d, Integer h, Integer mi, Integer s, Integer ms) {
		Calendar c = Calendar.getInstance(
			com.ibm.icu.util.TimeZone.getTimeZone(java.util.TimeZone.getDefault().getID()),
			EUniCalendar.Gregorian.getLocale());

		if (y != null) {
			c.set(Calendar.YEAR, y);
		}
		if (mo != null) {
			c.set(Calendar.MONTH, mo - 1);
		}
		if (d != null) {
			c.set(Calendar.DAY_OF_MONTH, d);
		}
		if (h != null) {
			c.set(Calendar.HOUR_OF_DAY, h);
		}
		if (mi != null) {
			c.set(Calendar.MINUTE, mi);
		}
		if (s != null) {
			c.set(Calendar.SECOND, s);
		}

		if (ms != null) {
			c.set(Calendar.MILLISECOND, ms);
		}

		return c.getTime();
	}
}
