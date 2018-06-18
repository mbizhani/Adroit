package org.devocative.adroit.date;

import java.io.Serializable;

public class DateTimeFieldVO implements Serializable {
	private static final long serialVersionUID = -3421038624924188837L;

	private int year;
	private int month;
	private int day;

	private final TimeFieldVO time;

	// ------------------------------

	public DateTimeFieldVO() {
		this(0, 0, 0, 0, 0, 0, 0);
	}

	public DateTimeFieldVO(int year, int month, int day) {
		this(year, month, day, 0, 0, 0, 0);
	}

	public DateTimeFieldVO(int year, int month, int day, int hour, int minute, int second) {
		this(year, month, day, hour, minute, second, 0);
	}

	// Main Constructor
	public DateTimeFieldVO(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.time = new TimeFieldVO(hour, minute, second, millisecond);
	}

	// ------------------------------

	public int getYear() {
		return year;
	}

	public DateTimeFieldVO setYear(int year) {
		this.year = year;
		return this;
	}

	public int getMonth() {
		return month;
	}

	public DateTimeFieldVO setMonth(int month) {
		this.month = month;
		return this;
	}

	public int getDay() {
		return day;
	}

	public DateTimeFieldVO setDay(int day) {
		this.day = day;
		return this;
	}

	public int getHour() {
		return time.getHour();
	}

	public DateTimeFieldVO setHour(int hour) {
		this.time.setHour(hour);
		return this;
	}

	public int getMinute() {
		return time.getMinute();
	}

	public DateTimeFieldVO setMinute(int minute) {
		this.time.setMinute(minute);
		return this;
	}

	public int getSecond() {
		return time.getSecond();
	}

	public DateTimeFieldVO setSecond(int second) {
		this.time.setSecond(second);
		return this;
	}

	public int getMillisecond() {
		return time.getMillisecond();
	}

	public DateTimeFieldVO setMillisecond(int millisecond) {
		this.time.setMillisecond(millisecond);
		return this;
	}

	// ---------------

	public DateTimeFieldVO setDate(int year, int month, int day) {
		setYear(year);
		setMonth(month);
		setDay(day);

		return this;
	}

	public DateTimeFieldVO setTime(int hour, int minute, int second) {
		return setTime(hour, minute, second, 0);
	}

	public DateTimeFieldVO setTime(int hour, int minute, int second, int millisecond) {
		time.setTime(hour, minute, second, millisecond);

		return this;
	}

	public DateTimeFieldVO setTime(TimeFieldVO other) {
		time.setTime(other);

		return this;
	}

	public TimeFieldVO getTime() {
		return time;
	}

	// ---------------

	public String getId() {
		return toString();
	}

	@Override
	public String toString() {
		return String.format("%04d/%02d/%02d %02d:%02d:%02d.%03d",
			getYear(), getMonth(), getDay(), getHour(), getMinute(), getSecond(), getMillisecond());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DateTimeFieldVO)) return false;

		DateTimeFieldVO that = (DateTimeFieldVO) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}
}
