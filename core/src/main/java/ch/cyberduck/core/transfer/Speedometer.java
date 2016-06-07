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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.date.PeriodFormatter;
import ch.cyberduck.core.date.RemainingPeriodFormatter;
import ch.cyberduck.core.formatter.SizeFormatter;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;

public class Speedometer {

    /**
     * Formatter for file size
     */
    private SizeFormatter sizeFormatter = SizeFormatterFactory.get();

    /**
     * The time to start counting bytes transferred
     */
    private long timestamp;

    /**
     * Initial data already transferred
     */
    private long last = 0L;

    /**
     * Formatter for remaining time
     */
    private PeriodFormatter periodFormatter = new RemainingPeriodFormatter();

    public Speedometer() {
        this(System.currentTimeMillis());
    }

    public Speedometer(final long timestamp) {
        this(timestamp, PreferencesFactory.get().getBoolean("browser.filesize.decimal"));
        this.timestamp = timestamp;
    }

    public Speedometer(final long timestamp, final boolean decimal) {
        this.timestamp = timestamp;
        this.sizeFormatter = SizeFormatterFactory.get(decimal);
    }


    /**
     * @param transferred Bytes transferred
     * @return Differential by time
     */
    protected Double getSpeed(final Long transferred) {
        return this.getSpeed(System.currentTimeMillis(), transferred, true);
    }

    protected Double getSpeed(final Long transferred, final boolean reset) {
        return this.getSpeed(System.currentTimeMillis(), transferred, reset);
    }

    protected Double getSpeed(final Long time, final Long transferred, final boolean reset) {
        // Number of seconds data was actually transferred
        final Long elapsed = time - timestamp;
        if(elapsed > 0) {
            final Long differential = transferred - last;
            // No reset for overall speed
            if(reset) {
                this.reset(time, transferred);
            }
            // The throughput is usually measured in bits per second
            return (double) differential / elapsed;
        }
        return 0d;
    }

    /**
     * @param running     Show speed and progress in percent
     * @param size        Transfer length
     * @param transferred Current
     * @return 500.0 KB (500,000 bytes) of 1.0 MB (50%, 500.0 KB/sec, 2 seconds remaining)
     */
    public String getProgress(final Boolean running, final Long size, final Long transferred) {
        return this.getProgress(System.currentTimeMillis(), running, size, transferred);
    }

    public String getProgress(final Long time, final Boolean running,
                              final Long size, final Long transferred) {
        return this.getProgress(running, size, transferred, this.getSpeed(time, transferred, true));
    }

    public String getProgress(final Boolean running,
                              final Long size, final Long transferred, final Double speed) {
        final StringBuilder b = new StringBuilder(
                MessageFormat.format(LocaleFactory.localizedString("{0} of {1}"),
                        sizeFormatter.format(transferred, running),
                        sizeFormatter.format(size))
        );
        if(running && transferred > 0) {
            if(size > 0) {
                b.append(" (");
                if(size > 0) {
                    b.append((int) ((double) transferred / size * 100));
                    b.append("%");
                }
                if(size > 0) {
                    b.append(", ");
                }
                b.append(SizeFormatterFactory.get(true).format(
                        new BigDecimal(speed * 1000).setScale(0, RoundingMode.UP).longValue()));
                b.append("/sec");
                if(speed > 0) {
                    if(transferred < size) {
                        b.append(", ");
                        // Remaining time in milliseconds
                        Long remaining = new BigDecimal((size - transferred) / speed).setScale(0, RoundingMode.UP).longValue();
                        // Display in seconds
                        b.append(periodFormatter.format(new BigDecimal(remaining).divide(new BigDecimal(1000L), RoundingMode.UP).longValue()));
                    }
                }
                b.append(")");
            }
        }
        return b.toString();
    }

    public void reset(final Long timestamp, final Long transferred) {
        this.timestamp = timestamp;
        this.last = transferred;
    }
}