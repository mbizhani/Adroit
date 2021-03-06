package org.devocative.adroit.date;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniPeriod implements Serializable {
	private static final long serialVersionUID = -7977197650701429649L;

	private static final long MINUTE_IN_SECONDS = 60;
	private static final long HOUR_IN_SECONDS = 60 * MINUTE_IN_SECONDS;
	private static final long DAY_IN_SECONDS = 24 * HOUR_IN_SECONDS;

	private static final Pattern DAYS = Pattern.compile("(D+)");

	private final long diffInSeconds;

	// ------------------------------

	private UniPeriod(Date end, Date start) {
		this(end.getTime(), start.getTime());
	}

	private UniPeriod(long end, long start) {
		if (end >= start) {
			diffInSeconds = (end - start) / 1000;
		} else {
			throw new RuntimeException("Invalid start and end: start is greater than end!");
		}
	}

	// ------------------------------

	public static UniPeriod of(long end, long start) {
		return new UniPeriod(end, start);
	}

	public static UniPeriod of(Date end, Date start) {
		return new UniPeriod(end, start);
	}

	// ------------------------------

	public long getTotalMinutes() {
		return diffInSeconds / MINUTE_IN_SECONDS;
	}

	public long getTotalHours() {
		return diffInSeconds / HOUR_IN_SECONDS;
	}

	public long getTotalDays() {
		return diffInSeconds / DAY_IN_SECONDS;
	}

	/*
	D: days
	H: hours
	M: minutes
	S: seconds
	 */
	public String format(String pattern) {
		long remain = diffInSeconds;

		final Matcher daysMatcher = DAYS.matcher(pattern);
		if (daysMatcher.find()) {
			final String daysFound = daysMatcher.group(1);
			final int d = (int) (remain / DAY_IN_SECONDS);
			remain = remain - d * DAY_IN_SECONDS;
			final String f = String.format("%%0%dd", daysFound.length());
			pattern = pattern.replaceAll(daysFound, String.format(f, d));
		}

		if (pattern.contains("H")) {
			final int h = (int) (remain / HOUR_IN_SECONDS);
			remain = remain - h * HOUR_IN_SECONDS;
			pattern = pattern.replaceAll("H", String.format("%02d", h));
		}

		if (pattern.contains("M")) {
			final int m = (int) (remain / MINUTE_IN_SECONDS);
			remain = remain - m * MINUTE_IN_SECONDS;
			pattern = pattern.replaceAll("M", String.format("%02d", m));
		}

		if (pattern.contains("S")) {
			pattern = pattern.replaceAll("S", String.format("%02d", remain));
		}

		return pattern;
	}

	// ---------------

	@Override
	public String toString() {
		return format("H:M:S");
	}
}
