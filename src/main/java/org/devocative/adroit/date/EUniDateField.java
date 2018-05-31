package org.devocative.adroit.date;

import com.ibm.icu.util.Calendar;

public enum EUniDateField {
	ERA(Calendar.ERA),
	YEAR(Calendar.YEAR),
	MONTH(Calendar.MONTH),
	WEEK_OF_YEAR(Calendar.WEEK_OF_YEAR),
	WEEK_OF_MONTH(Calendar.WEEK_OF_MONTH),
	DATE(Calendar.DATE),
	DAY_OF_MONTH(Calendar.DAY_OF_MONTH),
	DAY_OF_YEAR(Calendar.DAY_OF_YEAR),
	DAY_OF_WEEK(Calendar.DAY_OF_WEEK),
	DAY_OF_WEEK_IN_MONTH(Calendar.DAY_OF_WEEK_IN_MONTH),
	AM_PM(Calendar.AM_PM),
	HOUR(Calendar.HOUR),
	HOUR_OF_DAY(Calendar.HOUR_OF_DAY),
	MINUTE(Calendar.MINUTE),
	SECOND(Calendar.SECOND),
	MILLISECOND(Calendar.MILLISECOND),
	ZONE_OFFSET(Calendar.ZONE_OFFSET),
	DST_OFFSET(Calendar.DST_OFFSET),
	YEAR_WOY(Calendar.YEAR_WOY),
	DOW_LOCAL(Calendar.DOW_LOCAL),
	EXTENDED_YEAR(Calendar.EXTENDED_YEAR),
	JULIAN_DAY(Calendar.JULIAN_DAY),
	MILLISECONDS_IN_DAY(Calendar.MILLISECONDS_IN_DAY),
	IS_LEAP_MONTH(Calendar.IS_LEAP_MONTH);
	private int value;

	EUniDateField(int value) {
		this.value = value;
	}

	int getValue() {
		return value;
	}
}
