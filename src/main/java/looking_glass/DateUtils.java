/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

// This file is a modified version of DateUtils from Apache HttpComponents Client.
// See the original license above. The file is located at:
// https://github.com/apache/httpcomponents-client/blob/master/httpclient5/src/main/java/org/apache/hc/client5/http/utils/DateUtils.java
// I removed some imports and deprecated methods.

package looking_glass;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;

/**
 * A utility class for parsing and formatting HTTP dates as used in cookies and
 * other headers.
 *
 * @since 4.3
 */
public final class DateUtils {

    /**
     * @deprecated use {@link #INTERNET_MESSAGE_FORMAT}
     */
    @Deprecated
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String INTERNET_MESSAGE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Date formatter used to parse HTTP date headers in the Internet Message Format
     * specified by the HTTP protocol.
     *
     * @since 5.2
     */
    public static final DateTimeFormatter FORMATTER_RFC1123 = new DateTimeFormatterBuilder()
            .parseLenient()
            .parseCaseInsensitive()
            .appendPattern(INTERNET_MESSAGE_FORMAT)
            .toFormatter(Locale.ENGLISH);

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    public static final String PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz";

    /**
     * Date formatter used to parse HTTP date headers in RFC 1036 format.
     *
     * @since 5.2
     */
    public static final DateTimeFormatter FORMATTER_RFC1036 = new DateTimeFormatterBuilder()
            .parseLenient()
            .parseCaseInsensitive()
            .appendPattern(PATTERN_RFC1036)
            .toFormatter(Locale.ENGLISH);

    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * {@code asctime()} format.
     */
    public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

    /**
     * Date formatter used to parse HTTP date headers in in ANSI C {@code asctime()}
     * format.
     *
     * @since 5.2
     */
    public static final DateTimeFormatter FORMATTER_ASCTIME = new DateTimeFormatterBuilder()
            .parseLenient()
            .parseCaseInsensitive()
            .appendPattern(PATTERN_ASCTIME)
            .toFormatter(Locale.ENGLISH);

    /**
     * Standard date formatters: {@link #FORMATTER_RFC1123},
     * {@link #FORMATTER_RFC1036}, {@link #FORMATTER_ASCTIME}.
     *
     * @since 5.2
     */
    public static final DateTimeFormatter[] STANDARD_PATTERNS = new DateTimeFormatter[] {
            FORMATTER_RFC1123,
            FORMATTER_RFC1036,
            FORMATTER_ASCTIME
    };

    static final ZoneId GMT_ID = ZoneId.of("GMT");

    /**
     * @since 5.2
     */
    public static Date toDate(final Instant instant) {
        return instant != null ? new Date(instant.toEpochMilli()) : null;
    }

    /**
     * @since 5.2
     */
    public static Instant toInstant(final Date date) {
        return date != null ? Instant.ofEpochMilli(date.getTime()) : null;
    }

    /**
     * @since 5.2
     */
    public static LocalDateTime toUTC(final Instant instant) {
        return instant != null ? instant.atZone(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    /**
     * @since 5.2
     */
    public static LocalDateTime toUTC(final Date date) {
        return toUTC(toInstant(date));
    }

    /**
     * Parses the date value using the given date/time formats.
     * <p>
     * This method can handle strings without time-zone information by failing
     * gracefully, in which case
     * it returns {@code null}.
     * </p>
     *
     * @param dateValue      the instant value to parse
     * @param dateFormatters the date/time formats to use
     *
     * @return the parsed instant or null if input could not be parsed
     *
     * @since 5.2
     */
    public static Instant parseDate(final String dateValue, final DateTimeFormatter... dateFormatters) {
        // Args.notNull(dateValue, "Date value");
        String v = dateValue;
        // trim single quotes around date if present
        // see issue #5279
        if (v.length() > 1 && v.startsWith("'") && v.endsWith("'")) {
            v = v.substring(1, v.length() - 1);
        }

        for (final DateTimeFormatter dateFormatter : dateFormatters) {
            try {
                return Instant.from(dateFormatter.parse(v));
            } catch (final DateTimeException ignore) {
            }
        }
        return null;
    }

    /**
     * Parses the instant value using the standard date/time formats
     * ({@link #PATTERN_RFC1123},
     * {@link #PATTERN_RFC1036}, {@link #PATTERN_ASCTIME}).
     *
     * @param dateValue the instant value to parse
     *
     * @return the parsed instant or null if input could not be parsed
     *
     * @since 5.2
     */
    public static Instant parseStandardDate(final String dateValue) {
        return parseDate(dateValue, STANDARD_PATTERNS);
    }

    /**
     * Formats the given instant according to the RFC 1123 pattern.
     *
     * @param instant Instant to format.
     * @return An RFC 1123 formatted instant string.
     *
     * @see #PATTERN_RFC1123
     *
     * @since 5.2
     */
    public static String formatStandardDate(final Instant instant) {
        return formatDate(instant, FORMATTER_RFC1123);
    }

    /**
     * Formats the given date according to the specified pattern.
     *
     * @param instant           Instant to format.
     * @param dateTimeFormatter The pattern to use for formatting the instant.
     * @return A formatted instant string.
     *
     * @throws IllegalArgumentException If the given date pattern is invalid.
     *
     * @since 5.2
     */
    public static String formatDate(final Instant instant, final DateTimeFormatter dateTimeFormatter) {
        // Args.notNull(instant, "Instant");
        // Args.notNull(dateTimeFormatter, "DateTimeFormatter");
        return dateTimeFormatter.format(instant.atZone(GMT_ID));
    }

    /** This class should not be instantiated. */
    private DateUtils() {
    }

}