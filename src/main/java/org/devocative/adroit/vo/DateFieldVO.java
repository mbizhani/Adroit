package org.devocative.adroit.vo;

import java.io.Serializable;

public class DateFieldVO implements Serializable {
	private int year;
	private int month;
	private int day;

	private int hour;
	private int minute;
	private int second;

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

	@Override
	public String toString() {
		return String.format("%04d/%02d/%02d %02d:%02d:%02d",
			year, month, day, hour, minute, second);
	}
}
