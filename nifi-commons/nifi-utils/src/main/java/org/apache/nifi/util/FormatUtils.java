/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.util;

import org.apache.nifi.processor.DataUnit;
import org.apache.nifi.time.DurationFormat;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class FormatUtils {

    // 'public static final' members defined for backward compatibility, since they were moved to TimeFormat.
    public static final String TIME_DURATION_REGEX = DurationFormat.TIME_DURATION_REGEX;
    public static final Pattern TIME_DURATION_PATTERN = DurationFormat.TIME_DURATION_PATTERN;

    private static final int NANOS_PER_MILLI = 1_000_000;

    // Date defaults as local runtime constants
    private static final int EPOCH_YEAR = LocalDate.EPOCH.getYear();
    private static final int FIRST_MONTH_OF_YEAR = LocalDate.EPOCH.getMonthValue();
    private static final int FIRST_DAY_OF_MONTH = LocalDate.EPOCH.getDayOfMonth();

    // Time defaults as local runtime constants
    private static final int FIRST_HOUR_OF_DAY = LocalTime.MIDNIGHT.getHour();
    private static final int FIRST_MINUTE_OF_DAY = LocalTime.MIDNIGHT.getMinute();
    private static final int FIRST_SECOND_OF_DAY = LocalTime.MIDNIGHT.getSecond();
    private static final int FIRST_NANO_OF_DAY = LocalTime.MIDNIGHT.getNano();

    /**
     * Formats the specified count by adding commas.
     *
     * @param count the value to add commas to
     * @return the string representation of the given value with commas included
     */
    public static String formatCount(final long count) {
        return NumberFormat.getIntegerInstance().format(count);
    }

    /**
     * Formats the specified duration in 'mm:ss.SSS' format.
     *
     * @param sourceDuration the duration to format
     * @param sourceUnit     the unit to interpret the duration
     * @return representation of the given time data in minutes/seconds
     * @deprecated Use {@link #formatMinutesSeconds(long, ChronoUnit)} instead
     */
    @Deprecated
    public static String formatMinutesSeconds(final long sourceDuration, final TimeUnit sourceUnit) {
        final long millis = TimeUnit.MILLISECONDS.convert(sourceDuration, sourceUnit);

        final long millisInMinute = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
        final int minutes = (int) (millis / millisInMinute);
        final long secondsMillisLeft = millis - minutes * millisInMinute;

        final long millisInSecond = TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);
        final int seconds = (int) (secondsMillisLeft / millisInSecond);
        final long millisLeft = secondsMillisLeft - seconds * millisInSecond;

        return pad2Places(minutes) + ":" + pad2Places(seconds) + "." + pad3Places(millisLeft);
    }

    /**
     * Formats the specified duration in 'mm:ss.SSS' format.
     *
     * @param sourceDuration the duration to format
     * @param sourceUnit     the unit to interpret the duration
     * @return representation of the given time data in minutes/seconds
     */
    public static String formatMinutesSeconds(final long sourceDuration, final ChronoUnit sourceUnit) {
        final Duration duration = Duration.ZERO.plus(sourceDuration, sourceUnit);
        return String.format("%02d:%02d.%03d", duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());
    }

    /**
     * Formats the specified duration in 'HH:mm:ss.SSS' format.
     *
     * @param sourceDuration the duration to format
     * @param sourceUnit     the unit to interpret the duration
     * @return representation of the given time data in hours/minutes/seconds
     * @deprecated Use {@link #formatMinutesSeconds(long, ChronoUnit)} instead
     */
    @Deprecated
    public static String formatHoursMinutesSeconds(final long sourceDuration, final TimeUnit sourceUnit) {
        final long millis = TimeUnit.MILLISECONDS.convert(sourceDuration, sourceUnit);

        final long millisInHour = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
        final int hours = (int) (millis / millisInHour);
        final long minutesSecondsMillisLeft = millis - hours * millisInHour;

        return pad2Places(hours) + ":" + formatMinutesSeconds(minutesSecondsMillisLeft, TimeUnit.MILLISECONDS);
    }

    /**
     * Formats the specified duration in 'HH:mm:ss.SSS' format.
     *
     * @param sourceDuration the duration to format
     * @param sourceUnit     the unit to interpret the duration
     * @return representation of the given time data in hours/minutes/seconds
     */
    public static String formatHoursMinutesSeconds(final long sourceDuration, final ChronoUnit sourceUnit) {
        final Duration duration = Duration.ZERO.plus(sourceDuration, sourceUnit);
        return String.format("%02d:%02d:%02d.%03d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
    }

    private static String pad2Places(final long val) {
        return (val < 10) ? "0" + val : String.valueOf(val);
    }

    private static String pad3Places(final long val) {
        return (val < 100) ? "0" + pad2Places(val) : String.valueOf(val);
    }

    /**
     * Formats the specified data size in human readable format.
     *
     * @param dataSize Data size in bytes
     * @return Human readable format
     */
    public static String formatDataSize(final double dataSize) {
        // initialize the formatter
        final NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);

        // check terabytes
        double dataSizeToFormat = DataUnit.B.toTB(dataSize);
        if (dataSizeToFormat > 1) {
            return format.format(dataSizeToFormat) + " TB";
        }

        // check gigabytes
        dataSizeToFormat = DataUnit.B.toGB(dataSize);
        if (dataSizeToFormat > 1) {
            return format.format(dataSizeToFormat) + " GB";
        }

        // check megabytes
        dataSizeToFormat = DataUnit.B.toMB(dataSize);
        if (dataSizeToFormat > 1) {
            return format.format(dataSizeToFormat) + " MB";
        }

        // check kilobytes
        dataSizeToFormat = DataUnit.B.toKB(dataSize);
        if (dataSizeToFormat > 1) {
            return format.format(dataSizeToFormat) + " KB";
        }

        // default to bytes
        return format.format(dataSize) + " bytes";
    }

    /**
     * Returns a time duration in the requested {@link TimeUnit} after parsing the {@code String}
     * input. If the resulting value is a decimal (i.e.
     * {@code 25 hours -> TimeUnit.DAYS = 1.04}), the value is rounded.
     * Use {@link #getPreciseTimeDuration(String, TimeUnit)} if fractional values are desirable
     *
     * @param value the raw String input (i.e. "28 minutes")
     * @param desiredUnit the requested output {@link TimeUnit}
     * @return the whole number value of this duration in the requested units
     * @see #getPreciseTimeDuration(String, TimeUnit)
     */
    public static long getTimeDuration(final String value, final TimeUnit desiredUnit) {
        return DurationFormat.getTimeDuration(value, desiredUnit);
    }

    /**
     * Returns the parsed and converted input in the requested units.
     * <p>
     * If the value is {@code 0 <= x < 1} in the provided units, the units will first be converted to a smaller unit to get a value >= 1 (i.e. 0.5 seconds -> 500 milliseconds).
     * This is because the underlying unit conversion cannot handle decimal values.
     * <p>
     * If the value is {@code x >= 1} but x is not a whole number, the units will first be converted to a smaller unit to attempt to get a whole number value (i.e. 1.5 seconds -> 1500 milliseconds).
     * <p>
     * If the value is {@code x < 1000} and the units are {@code TimeUnit.NANOSECONDS}, the result will be a whole number of nanoseconds, rounded (i.e. 123.4 ns -> 123 ns).
     * <p>
     * This method handles decimal values over {@code 1 ns}, but {@code < 1 ns} will return {@code 0} in any other unit.
     * <p>
     * Examples:
     * {@snippet lang="text" :
     *     "10 seconds", TimeUnit.MILLISECONDS -> 10_000.0
     *     "0.010 s", TimeUnit.MILLISECONDS -> 10.0
     *     "0.010 s", TimeUnit.SECONDS -> 0.010
     *     "0.010 ns", TimeUnit.NANOSECONDS -> 1
     *     "0.010 ns", TimeUnit.MICROSECONDS -> 0
     * }
     * @param value       the {@code String} input
     * @param desiredUnit the desired output {@link TimeUnit}
     * @return the parsed and converted amount (without a unit)
     */
    public static double getPreciseTimeDuration(final String value, final TimeUnit desiredUnit) {
        return DurationFormat.getPreciseTimeDuration(value, desiredUnit);
    }

    public static String formatUtilization(final double utilization) {
        return utilization + "%";
    }

    public static DateTimeFormatter prepareLenientCaseInsensitiveDateTimeFormatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseLenient()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.US);
    }

    /**
     * Formats nanoseconds in the format:
     * 3 seconds, 8 millis, 3 nanos - if includeTotalNanos = false,
     * 3 seconds, 8 millis, 3 nanos (3008000003 nanos) - if includeTotalNanos = true
     *
     * @param nanos             the number of nanoseconds to format
     * @param includeTotalNanos whether or not to include the total number of nanoseconds in parentheses in the returned value
     * @return a human-readable String that is a formatted representation of the given number of nanoseconds.
     */
    public static String formatNanos(final long nanos, final boolean includeTotalNanos) {
        Duration duration = Duration.ofNanos(nanos);

        long seconds = duration.toSeconds();
        long millis = duration.toMillisPart();
        long nanosLeft = duration.toNanosPart() % NANOS_PER_MILLI;

        final List<String> parts = new ArrayList<>();
        if (seconds > 0) {
            parts.add(seconds + " seconds");
        }
        if (millis > 0 || !parts.isEmpty()) {
            parts.add(millis + " millis");
        }
        parts.add(nanosLeft + " nanos");

        final String output = String.join(", ", parts);
        return includeTotalNanos ? String.format("%s (%d nanos)", output, nanos) : output;
    }

    /**
     * Parse text and build Instant based on chronological fields found
     *
     * @param formatter DateTimeFormatter responsible for parsing
     * @param text      text which will be parsed
     * @return parsed Instant
     */
    public static Instant parseToInstant(final DateTimeFormatter formatter, final String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }

        final TemporalAccessor parsed = formatter.parse(text);

        // Attempt to get the Date and Time values directly or fallback to constructing them
        LocalDate localDate = parsed.query(TemporalQueries.localDate());
        if (localDate == null) {
            localDate = LocalDate.of(
                    parsed.isSupported(ChronoField.YEAR) ? parsed.get(ChronoField.YEAR) : EPOCH_YEAR,
                    parsed.isSupported(ChronoField.MONTH_OF_YEAR) ? parsed.get(ChronoField.MONTH_OF_YEAR) : FIRST_MONTH_OF_YEAR,
                    parsed.isSupported(ChronoField.DAY_OF_MONTH) ? parsed.get(ChronoField.DAY_OF_MONTH) : FIRST_DAY_OF_MONTH
            );
        }

        LocalTime localTime = parsed.query(TemporalQueries.localTime());
        if (localTime == null) {
            localTime = LocalTime.of(
                    parsed.isSupported(ChronoField.HOUR_OF_DAY) ? parsed.get(ChronoField.HOUR_OF_DAY) : FIRST_HOUR_OF_DAY,
                    parsed.isSupported(ChronoField.MINUTE_OF_HOUR) ? parsed.get(ChronoField.MINUTE_OF_HOUR) : FIRST_MINUTE_OF_DAY,
                    parsed.isSupported(ChronoField.SECOND_OF_MINUTE) ? parsed.get(ChronoField.SECOND_OF_MINUTE) : FIRST_SECOND_OF_DAY,
                    parsed.isSupported(ChronoField.NANO_OF_SECOND) ? parsed.get(ChronoField.NANO_OF_SECOND) : FIRST_NANO_OF_DAY
            );
        }

        final LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        ZoneId zoneId = parsed.query(TemporalQueries.zoneId());
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }

        final ZoneOffset zoneOffset;
        if (parsed.isSupported(ChronoField.OFFSET_SECONDS)) {
            final int offsetSeconds = parsed.get(ChronoField.OFFSET_SECONDS);
            zoneOffset = ZoneOffset.ofTotalSeconds(offsetSeconds);
        } else {
            // Get Zone Offset from provided Zone ID and parsed Local Date Time
            zoneOffset = zoneId.getRules().getOffset(localDateTime);
        }

        final OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, zoneOffset);
        return offsetDateTime.toInstant();
    }
}
