package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;

/**
 * The Status class is the model of a download's status.
 * The wrapper for any status informations of a transfer as the size and transferred
 * bytes.
 *
 * @version $Id$
 */
public class Status {
    private static Logger log = Logger.getLogger(Status.class);

    /**
     * Transfer is resumable
     */
    private boolean resume = false;

    /**
     * The number of transfered bytes. Must be less or equals size.
     */
    private long current = 0;

    /**
     * Indiciating wheter the transfer has been cancled by the user.
     */
    private boolean canceled;

    /**
     * Indicates that the last action has been completed.
     */
    private boolean complete = false;

    public static long KILO = -1;
    public static long MEGA = -1;
    public static long GIGA = -1;

    static {
        if(Preferences.instance().getBoolean("browser.filesize.decimal")) {
            KILO = 1000; //10^3
            MEGA = 1000000; // 10^6
            GIGA = 1000000000; // 10^9
        }
        else {
            // Default is binary sizes
            KILO = 1024; //2^10
            MEGA = 1048576; // 2^20
            GIGA = 1073741824; // 2^30
        }
    }

    /**
     * Rounding mode to round towards "nearest neighbor" unless both
     * neighbors are equidistant, in which case round up.
     *
     * @param size Number of bytes
     * @return The size of the file using BigDecimal.ROUND_HALF_UP rounding
     */
    public static String getSizeAsString(double size) {
        return getSizeAsString(size, false, true);
    }

    /**
     * @param size
     * @param plain Include plain format of bytes
     * @return Formatted size
     */
    public static String getSizeAsString(double size, boolean plain) {
        return getSizeAsString(size, plain, true);
    }

    /**
     * @param size
     * @param bytes  Report file size as bytes or bits.
     * @param plain Include plain format of bytes
     * @return Formatted size
     */
    public static String getSizeAsString(double size, boolean plain, boolean bytes) {
        if(-1 == size) {
            return Locale.localizedString("Unknown size");
        }
        if(size < KILO) {
            return (int) size + (bytes ? " B" : " bit");
        }
        StringBuilder formatted = new StringBuilder();
        if(size < MEGA) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(KILO),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " KB" : " kbit");
        }
        else if(size < GIGA) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(MEGA),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " MB" : " Mbit");
        }
        else {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(GIGA),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " GB" : " Gbit");
        }
        if(plain) {
            formatted.append(" (").append(NumberFormat.getInstance().format(size)).append(" bytes)");
        }
        return formatted.toString();
    }

    public static String getSpeedAsString(double size) {
        return getSizeAsString(size, false, true);
    }

    /**
     * @param remaining Seconds
     * @return Humean readable string for seconds in hours, minutes or seconds remaining
     */
    public static String getRemainingAsString(double remaining) {
        StringBuilder b = new StringBuilder();
        if(remaining < 0) {
            // File sizes larger than advertised
            return Locale.localizedString("Unknown");
        }
        if(remaining > 7200) { // More than two hours
            b.append(MessageFormat.format(Locale.localizedString("{0} hours remaining", "Status"),
                    new BigDecimal(remaining).divide(new BigDecimal(3600), 1, BigDecimal.ROUND_DOWN).toString())
            );
        }
        else if(remaining > 120) { // More than two minutes
            b.append(MessageFormat.format(Locale.localizedString("{0} minutes remaining", "Status"),
                    String.valueOf((int) (remaining / 60)))
            );
        }
        else {
            b.append(MessageFormat.format(Locale.localizedString("{0} seconds remaining", "Status"),
                    String.valueOf((int) remaining))
            );
        }
        return b.toString();
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
        if(log.isInfoEnabled()) {
            log.info("Complete:" + complete + "," + this.getCurrent());
        }
    }

    public boolean isComplete() {
        return this.complete;
    }

    /**
     * If this path is currently transferred, interrupt it as soon as possible
     */
    public void setCanceled() {
        canceled = true;
    }

    /**
     * @return True if marked for interrupt
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * @return Number of bytes transferred
     */
    public long getCurrent() {
        return this.current;
    }

    /**
     * @param current The already transfered bytes
     */
    public void setCurrent(long current) {
        this.current = current;
    }

    public void addCurrent(long transferred) {
        this.current += transferred;
    }

    /**
     * A state variable to mark this path if the path is explicitly selected
     * for inclusion in the transfer prompt
     */
    private boolean selected = true;

    public boolean isSelected() {
        return selected;
    }

    /**
     * Mark for inclusion from transfer prompt
     *
     * @param selected True if selected
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    /**
     * @return True if selected for inclusion in transfer prompt
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * Mark this path with an append flag when transfered
     *
     * @param resume If false, the current status is cleared
     * @see #setCurrent(long)
     */
    public void setResume(boolean resume) {
        if(!resume) {
            this.current = 0;
        }
        this.resume = resume;
    }

    public boolean isResume() {
        return this.resume;
    }

    /**
     * Reset completion status.
     */
    public void reset() {
        complete = false;
        canceled = false;
    }
}