package ch.cyberduck.core.transfer;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.cyberduck.core.date.PeriodFormatter;
import ch.cyberduck.core.date.RemainingPeriodFormatter;
import ch.cyberduck.core.formatter.SizeFormatter;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.i18n.Locale;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class Speedometer {

    /**
     * Formatter for file size
     */
    private SizeFormatter sizeFormatter = SizeFormatterFactory.get();

    /**
     * The time to start counting bytes transferred
     */
    private long timestamp
            = System.currentTimeMillis();

    /**
     * Initial data already transferred
     */
    private long last = 0L;

    /**
     * Formatter for remaining time
     */
    private PeriodFormatter periodFormatter = new RemainingPeriodFormatter();

    protected double getSpeed(final long transferred) {
        // Number of seconds data was actually transferred
        final long elapsed = System.currentTimeMillis() - timestamp;
        if(elapsed > 0) {
            final long differential = transferred - last;
            // Remember for next iteration
            last = transferred;
            timestamp = System.currentTimeMillis();
            // The throughput is usually measured in bits per second
            return (double) differential / elapsed;
        }
        return 0L;
    }

    public String getProgress(final boolean running, final long size, final long transferred) {
        final StringBuilder b = new StringBuilder(
                MessageFormat.format(Locale.localizedString("{0} of {1}"),
                        sizeFormatter.format(transferred, running),
                        sizeFormatter.format(size))
        );
        if(running) {
            final double speed = this.getSpeed(transferred);
            if(size > 0 || speed > 0) {
                b.append(" (");
                if(size > 0) {
                    b.append((int) ((double) transferred / size * 100));
                    b.append("%");
                }
                if(speed > 0) {
                    if(size > 0) {
                        b.append(", ");
                    }
                    b.append(SizeFormatterFactory.get(true).format(
                            new BigDecimal(speed * 1000).setScale(0, RoundingMode.UP).longValue()));
                    b.append("/sec");
                    if(transferred < size) {
                        b.append(", ");
                        // Remaining time in milliseconds
                        long remaining = new BigDecimal((size - transferred) / speed).setScale(0, RoundingMode.UP).longValue();
                        // Display in seconds
                        b.append(periodFormatter.format(new BigDecimal(remaining).divide(new BigDecimal(1000L), RoundingMode.UP).longValue()));
                    }
                }
                b.append(")");
            }
        }
        return b.toString();
    }

    public void reset(long transferred) {
        timestamp = System.currentTimeMillis();
        last = transferred;
    }
}