package org.devocative.adroit;

import com.ibm.icu.util.Calendar;
import org.devocative.adroit.date.EUniCalendar;
import org.devocative.adroit.date.UniDate;
import org.devocative.adroit.date.UniPeriod;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDate {
	@Test
	public void testUniDate() {
		UniDate date = UniDate.of(EUniCalendar.Gregorian, 2018, 5, 29);
		assertEquals(2018, date.getYear());
		assertEquals(5, date.getMonth());
		assertEquals(29, date.getDay());
		assertEquals(gr(2018, 5, 29, 0, 0, 0), date.toDate());
		assertEquals("2018-05-29", date.format("yyyy-MM-dd"));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("01 ===================================");

		date = UniDate.of(EUniCalendar.Persian, 1397, 3, 8);
		assertEquals(1397, date.getYear());
		assertEquals(3, date.getMonth());
		assertEquals(8, date.getDay());
		assertEquals(gr(2018, 5, 29, 0, 0, 0), date.toDate());
		assertEquals("1397-03-08", date.format("yyyy-MM-dd"));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("02 ===================================");

		date = UniDate.of(EUniCalendar.Gregorian, "2018-01-10", "yyyy-MM-dd");
		assertEquals(2018, date.getYear());
		assertEquals(1, date.getMonth());
		assertEquals(10, date.getDay());
		assertEquals(gr(2018, 1, 10, 0, 0, 0), date.toDate());
		assertEquals("2018-01-10", date.format("yyyy-MM-dd"));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("03 ===================================");

		date = UniDate.of(EUniCalendar.Persian, "1397-03-17", "yyyy-MM-dd");
		assertEquals(1397, date.getYear());
		assertEquals(3, date.getMonth());
		assertEquals(17, date.getDay());
		assertEquals(gr(2018, 6, 7, 0, 0, 0), date.toDate());
		assertEquals("1397-03-17", date.format("yyyy-MM-dd"));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("04 ===================================");

		date = UniDate.of(EUniCalendar.Gregorian, "2018-05-31", "yyyy-MM-dd")
				.updateCalendar(EUniCalendar.Persian);
		assertEquals(1397, date.getYear());
		assertEquals(3, date.getMonth());
		assertEquals(10, date.getDay());
		assertEquals(gr(2018, 5, 31, 0, 0, 0), date.toDate());
		assertEquals("1397-03-10", date.format("yyyy-MM-dd"));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("05 ===================================");

		date = UniDate.of(EUniCalendar.Persian, "1396-12-29", "yyyy-MM-dd")
				.updateCalendar(EUniCalendar.Gregorian);
		assertEquals(2018, date.getYear());
		assertEquals(3, date.getMonth());
		assertEquals(20, date.getDay());
		assertEquals(gr(2018, 3, 20, 0, 0, 0), date.toDate());
		assertEquals("2018-03-20", date.format("yyyy-MM-dd"));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("06 ===================================");

		date = UniDate.of(EUniCalendar.Persian, "1397-01-01", "yyyy-MM-dd")
				.updateCalendar(EUniCalendar.Persian);
		assertEquals(1397, date.getYear());
		assertEquals(1, date.getMonth());
		assertEquals(1, date.getDay());
		assertEquals(gr(2018, 3, 21, 0, 0, 0), date.toDate());
		assertEquals("1397-01-01", date.format("yyyy-MM-dd"));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("07 ===================================");

		date = date
				.updateMonth(7)
				.updateDay(23)
				.updateYear(-1);
		assertEquals(1396, date.getYear());
		assertEquals(8, date.getMonth());
		assertEquals(24, date.getDay());
		assertEquals(gr(2017, 11, 15, 0, 0, 0), date.toDate());
		assertEquals("1396-08-24", date.format("yyyy-MM-dd"));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("08 ===================================");
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
		System.out.println("date02.toDate() = " + date02.toDate());
		System.out.println("09 ===================================");

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		assertEquals("UTC", TimeZone.getDefault().getID());

		UniDate date = UniDate.now()
				.setTime(8, 0, 0);
		assertEquals(8, date.getHour());
		assertEquals("UTC", date.getTimeZone().getID());
		assertEquals("13:00:00 GMT+5", date.format("HH:mm:ss v", TimeZone.getTimeZone("GMT+5")));
		System.out.println("date.toDate() = " + date.toDate());
		System.out.println("10 ===================================");
	}

	@Test
	public void testPeriod() {
		UniDate start = UniDate.of(EUniCalendar.Gregorian, 2018, 1, 1, 1, 0, 0);
		UniDate end = start.setTime(9, 59, 30);

		UniPeriod period = UniPeriod.of(end.toTimeInMillis(), start.toTimeInMillis());
		System.out.println(period.format("D H:M:S"));

		end = start.setDate(2018, 5, 5);

		period = UniPeriod.of(end.toTimeInMillis(), start.toTimeInMillis());
		System.out.println(period.format("DDDD H:M:S"));
	}

	private Date gr(Integer y, Integer mo, Integer d, Integer h, Integer mi, Integer s) {
		Calendar c = Calendar.getInstance(EUniCalendar.Gregorian.getLocale());
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
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
}
