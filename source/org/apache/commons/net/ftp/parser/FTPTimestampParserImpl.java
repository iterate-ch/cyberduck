/*
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp.parser;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Default implementation of the {@link  FTPTimestampParser  FTPTimestampParser}
 * interface to allow the parsing to be configured from the outside.
 *
 * @since 1.4
 */
public class FTPTimestampParserImpl implements FTPTimestampParser {

    private SimpleDateFormat defaultDateFormat;
    private SimpleDateFormat recentDateFormat;
    private boolean lenientFutureDates = false;

    /**
     * The only constructor for this class.
     */
    public FTPTimestampParserImpl() {
        setDefaultDateFormat(DEFAULT_SDF);
        setRecentDateFormat(DEFAULT_RECENT_SDF);
    }

    /**
     * Implements the one {@link  FTPTimestampParser#parseTimestamp(String)  method}
     * in the {@link  FTPTimestampParser  FTPTimestampParser} interface
     * according to this algorithm:
     * <p/>
     * If the recentDateFormat member has been defined, try to parse the
     * supplied string with that.  If that parse fails, or if the recentDateFormat
     * member has not been defined, attempt to parse with the defaultDateFormat
     * member.  If that fails, throw a ParseException.
     *
     * @see FTPTimestampParser#parseTimestamp(String)
     */
    /* (non-Javadoc)
      *
      */
    public Calendar parseTimestamp(String timestampStr) throws ParseException {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(this.getServerTimeZone());

        Calendar working = Calendar.getInstance();
        working.setTimeZone(this.getServerTimeZone());
        ParsePosition pp = new ParsePosition(0);

        Date parsed = null;
        if (this.recentDateFormat != null) {
            parsed = recentDateFormat.parse(timestampStr, pp);
        }
        if (parsed != null && pp.getIndex() == timestampStr.length()) {
            working.setTime(parsed);
            working.set(Calendar.YEAR, now.get(Calendar.YEAR));

            if (this.lenientFutureDates) {
                // add a day to "now" so that "slop" doesn't cause a date
                // slightly in the future to roll back a full year.  (Bug 35181)
                now.add(Calendar.DATE, 1);
            }
            if (working.after(now)) {
                working.add(Calendar.YEAR, -1);
            }
        }
        else {
            pp = new ParsePosition(0);
            parsed = defaultDateFormat.parse(timestampStr, pp);
            // note, length checks are mandatory for us since
            // SimpleDateFormat methods will succeed if less than
            // full string is matched.  They will also accept,
            // despite "leniency" setting, a two-digit number as
            // a valid year (e.g. 22:04 will parse as 22 A.D.)
            // so could mistakenly confuse an hour with a year,
            // if we don't insist on full length parsing.
            if (parsed != null && pp.getIndex() == timestampStr.length()) {
                working.setTime(parsed);
            }
            else {
                throw new ParseException(
                        "Timestamp could not be parsed with older or recent DateFormat",
                        pp.getIndex());
            }
        }
        return working;
    }

    /**
     * @return Returns the defaultDateFormat.
     */
    public SimpleDateFormat getDefaultDateFormat() {
        return defaultDateFormat;
    }

    /**
     * @return Returns the defaultDateFormat pattern string.
     */
    public String getDefaultDateFormatString() {
        return defaultDateFormat.toPattern();
    }

    /**
     * @param format The defaultDateFormat to be set.
     */
    private void setDefaultDateFormat(String format) {
        if (format != null) {
            this.defaultDateFormat = new SimpleDateFormat(format);
            this.defaultDateFormat.setLenient(false);
        }
    }

    /**
     * @return Returns the recentDateFormat.
     */
    public SimpleDateFormat getRecentDateFormat() {
        return recentDateFormat;
    }

    /**
     * @return Returns the recentDateFormat.
     */
    public String getRecentDateFormatString() {
        return recentDateFormat.toPattern();
    }

    /**
     * @param format The recentDateFormat to set.
     */
    private void setRecentDateFormat(String format) {
        if (format != null) {
            this.recentDateFormat = new SimpleDateFormat(format);
            this.recentDateFormat.setLenient(false);
        }
    }

    /**
     * @return returns an array of 12 strings representing the short
     *         month names used by this parse.
     */
    public String[] getShortMonths() {
        return defaultDateFormat.getDateFormatSymbols().getShortMonths();
    }


    /**
     * @return Returns the serverTimeZone used by this parser.
     */
    public TimeZone getServerTimeZone() {
        return this.defaultDateFormat.getTimeZone();
    }

    /**
     * sets a TimeZone represented by the supplied ID string into all
     * of the parsers used by this server.
     *
     * @param serverTimeZoneId Time Id java.util.TimeZone id used by
     *                         the ftp server.  If null the client's local time zone is assumed.
     */
    private void setServerTimeZone(String serverTimeZoneId) {
        TimeZone serverTimeZone = TimeZone.getDefault();
        if (serverTimeZoneId != null) {
            serverTimeZone = TimeZone.getTimeZone(serverTimeZoneId);
        }
        this.defaultDateFormat.setTimeZone(serverTimeZone);
        if (this.recentDateFormat != null) {
            this.recentDateFormat.setTimeZone(serverTimeZone);
        }
    }


    /**
     * @return Returns the lenientFutureDates.
     */
    boolean isLenientFutureDates() {
        return lenientFutureDates;
    }

    /**
     * @param lenientFutureDates The lenientFutureDates to set.
     */
    void setLenientFutureDates(boolean lenientFutureDates) {
        this.lenientFutureDates = lenientFutureDates;
    }
}
