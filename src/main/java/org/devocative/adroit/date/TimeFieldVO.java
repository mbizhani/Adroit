package org.devocative.adroit.date;

import java.io.Serializable;

public class TimeFieldVO implements Serializable {
	private static final long serialVersionUID = -627507217921706174L;

	private int hour;
	private int minute;
	private int second;
	private int millisecond;

	// ------------------------------


	public TimeFieldVO() {
		this(0, 0, 0, 0);
	}

	public TimeFieldVO(int hour, int minute, int second) {
		this(hour, minute, second, 0);
	}

	//Main Constructor
	public TimeFieldVO(int hour, int minute, int second, int millisecond) {
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.millisecond = millisecond;
	}

	// ------------------------------

	public int getHour() {
		return hour;
	}

	public TimeFieldVO setHour(int hour) {
		this.hour = hour;
		return this;
	}

	public int getMinute() {
		return minute;
	}

	public TimeFieldVO setMinute(int minute) {
		this.minute = minute;
		return this;
	}

	public int getSecond() {
		return second;
	}

	public TimeFieldVO setSecond(int second) {
		this.second = second;
		return this;
	}

	public int getMillisecond() {
		return millisecond;
	}

	public TimeFieldVO setMillisecond(int millisecond) {
		this.millisecond = millisecond;
		return this;
	}

	// ---------------

	public TimeFieldVO setTime(int hour, int minute, int second) {
		setTime(hour, minute, second, 0);

		return this;
	}

	public TimeFieldVO setTime(int hour, int minute, int second, int millisecond) {
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.millisecond = millisecond;

		return this;
	}

	public TimeFieldVO setTime(TimeFieldVO other) {
		this.hour = other.getHour();
		this.minute = other.getMinute();
		this.second = other.getSecond();
		this.millisecond = other.getMillisecond();

		return this;
	}
}
