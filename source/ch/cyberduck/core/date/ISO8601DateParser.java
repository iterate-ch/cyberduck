package ch.cyberduck.core.date;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Date parser for ISO 8601 format
 * http://www.w3.org/TR/1998/NOTE-datetime-19980827
 *
 * @author Benoï¿½t Mahï¿½ (bmahe@w3.org)
 * @author Yves Lafon (ylafon@w3.org)
 */
public final class ISO8601DateParser {

    public ISO8601DateParser() {
        //
    }

    private boolean check(final StringTokenizer st, final String token)
            throws InvalidDateException {
        try {
            if(st.nextToken().equals(token)) {
                return true;
            }
            else {
                throw new InvalidDateException(String.format("Missing [%s]", token));
            }
        }
        catch(NoSuchElementException ex) {
            return false;
        }
    }

    private Calendar getCalendar(final String isodate) throws InvalidDateException {
        // YYYY-MM-DDThh:mm:ss.sTZD
        StringTokenizer st = new StringTokenizer(isodate, "-T:.+Z", true);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        try {
            // Year
            if(st.hasMoreTokens()) {
                int year = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.YEAR, year);
            }
            else {
                return calendar;
            }
            // Month
            if(check(st, "-") && (st.hasMoreTokens())) {
                int month = Integer.parseInt(st.nextToken()) - 1;
                calendar.set(Calendar.MONTH, month);
            }
            else {
                return calendar;
            }
            // Day
            if(check(st, "-") && (st.hasMoreTokens())) {
                int day = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.DAY_OF_MONTH, day);
            }
            else {
                return calendar;
            }
            // Hour
            if(check(st, "T") && (st.hasMoreTokens())) {
                int hour = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
            }
            else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }
            // Minutes
            if(check(st, ":") && (st.hasMoreTokens())) {
                int minutes = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.MINUTE, minutes);
            }
            else {
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }

            //
            // Not mandatory now
            //

            // Secondes
            if(!st.hasMoreTokens()) {
                return calendar;
            }
            String tok = st.nextToken();
            if(tok.equals(":")) { // secondes
                if(st.hasMoreTokens()) {
                    int secondes = Integer.parseInt(st.nextToken());
                    calendar.set(Calendar.SECOND, secondes);
                    if(!st.hasMoreTokens()) {
                        return calendar;
                    }
                    // frac sec
                    tok = st.nextToken();
                    if(tok.equals(".")) {
                        // bug fixed, thx to Martin Bottcher
                        String nt = st.nextToken();
                        while(nt.length() < 3) {
                            nt += "0";
                        }
                        nt = nt.substring(0, 3); //Cut trailing chars..
                        int millisec = Integer.parseInt(nt);
                        //int millisec = Integer.parseInt(st.nextToken()) * 10;
                        calendar.set(Calendar.MILLISECOND, millisec);
                        if(!st.hasMoreTokens()) {
                            return calendar;
                        }
                        tok = st.nextToken();
                    }
                    else {
                        calendar.set(Calendar.MILLISECOND, 0);
                    }
                }
                else {
                    throw new InvalidDateException("No seconds specified");
                }
            }
            else {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }
            // Timezone
            if(!tok.equals("Z")) { // UTC
                if(!(tok.equals("+") || tok.equals("-"))) {
                    throw new InvalidDateException("only Z, + or - allowed");
                }
                boolean plus = tok.equals("+");
                if(!st.hasMoreTokens()) {
                    throw new InvalidDateException("Missing hour field");
                }
                int tzhour = Integer.parseInt(st.nextToken());
                int tzmin = 0;
                if(check(st, ":") && (st.hasMoreTokens())) {
                    tzmin = Integer.parseInt(st.nextToken());
                }
                else {
                    throw new InvalidDateException("Missing minute field");
                }
                if(plus) {
                    calendar.add(Calendar.HOUR, -tzhour);
                    calendar.add(Calendar.MINUTE, -tzmin);
                }
                else {
                    calendar.add(Calendar.HOUR, tzhour);
                    calendar.add(Calendar.MINUTE, tzmin);
                }
            }
        }
        catch(NumberFormatException ex) {
            throw new InvalidDateException(String.format("[%s] is not an integer", ex.getMessage()), ex);
        }
        return calendar;
    }

    /**
     * Parse the given string in ISO 8601 format and build a Date object.
     *
     * @param iso the date in ISO 8601 format
     * @return a Date instance
     * @throws InvalidDateException if the date is not valid
     */
    public Date parse(final String iso) throws InvalidDateException {
        return this.getCalendar(iso).getTime();
    }
}
