package org.devocative.adroit.date;

import java.io.Serializable;

public class DateFieldVO implements Serializable {
	private static final long serialVersionUID = -3421038624924188837L;

	private int year;
	private int month;
	private int day;

	private int hour;
	private int minute;
	private int second;
	private int millisecond;

	// ------------------------------

	public DateFieldVO() {
	}

	public DateFieldVO(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public DateFieldVO(int year, int month, int day, int hour, int minute, int second) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	// ------------------------------

	public int getYear() {
		return year;
	}

	public DateFieldVO setYear(int year) {
		this.year = year;
		return this;
	}

	public int getMonth() {
		return month;
	}

	public DateFieldVO setMonth(int month) {
		this.month = month;
		return this;
	}

	public int getDay() {
		return day;
	}

	public DateFieldVO setDay(int day) {
		this.day = day;
		return this;
	}

	public int getHour() {
		return hour;
	}

	public DateFieldVO setHour(int hour) {
		this.hour = hour;
		return this;
	}

	public int getMinute() {
		return minute;
	}

	public DateFieldVO setMinute(int minute) {
		this.minute = minute;
		return this;
	}

	public int getSecond() {
		return second;
	}

	public DateFieldVO setSecond(int second) {
		this.second = second;
		return this;
	}

	public int getMillisecond() {
		return millisecond;
	}

	public DateFieldVO setMillisecond(int millisecond) {
		this.millisecond = millisecond;
		return this;
	}

	// ------------------------------

	public String getId() {
		return toString();
	}

	@Override
	public String toString() {
		return String.format("%04d/%02d/%02d %02d:%02d:%02d.%03d",
			year, month, day, hour, minute, second, millisecond);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DateFieldVO)) return false;

		DateFieldVO that = (DateFieldVO) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}
}
