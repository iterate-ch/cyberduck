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
    private final SizeFormatter sizeFormatter;

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
    private final PeriodFormatter periodFormatter = new RemainingPeriodFormatter();

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
     * @return Bytes transferred in period
     */
    protected Double getSpeed(final long transferred) {
        return this.getSpeed(transferred, true);
    }

    /**
     * @param transferred Bytes transferred
     * @param reset       Reset overall speed
     * @return Bytes transferred in period
     */
    protected Double getSpeed(final long transferred, final boolean reset) {
        return this.getSpeed(System.currentTimeMillis(), transferred, reset);
    }

    /**
     * @param time        Current timestamp
     * @param transferred Bytes transferred
     * @param reset       Reset overall speed
     * @return Bytes transferred in period
     */
    protected Double getSpeed(final long time, final long transferred, final boolean reset) {
        // Number of seconds data was actually transferred
        final long elapsed = time - timestamp;
        if(elapsed > 0) {
            final long differential = transferred - last;
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
     * @param running     Transfer is running. Show speed and progress in percent
     * @param size        Transfer length
     * @param transferred Current byte count
     * @return 500.0 KB (500,000 bytes) of 1.0 MB (50%, 500.0 KB/sec, 2 seconds remaining)
     */
    public String getProgress(final boolean running, final long size, final long transferred) {
        return this.getProgress(System.currentTimeMillis(), running, size, transferred);
    }

    /**
     * @param time        Timestamp
     * @param running     Transfer is running. Show speed and progress in percent
     * @param size        Transfer length
     * @param transferred Current byte count
     * @return 500.0 KB (500,000 bytes) of 1.0 MB (50%, 500.0 KB/sec, 2 seconds remaining)
     */
    public String getProgress(final long time, final boolean running, final long size, final long transferred) {
        return this.getProgress(running, size, transferred, running, this.getSpeed(time, transferred, true));
    }

    /**
     * @param running     Show speed and progress in percent
     * @param size        Transfer length
     * @param transferred Current
     * @param plain       Include transferred size in bytes
     * @param speed       Bytes transferred in period
     * @return 500.0 KB (500,000 bytes) of 1.0 MB (50%, 500.0 KB/sec, 2 seconds remaining)
     */
    public String getProgress(final boolean running, final long size, final long transferred, boolean plain, final Double speed) {
        final StringBuilder b = new StringBuilder(
            MessageFormat.format(LocaleFactory.localizedString("{0} of {1}"),
                sizeFormatter.format(transferred, plain),
                sizeFormatter.format(size))
        );
        if(running && transferred > 0) {
            if(size > 0) {
                b.append(" (");
                b.append((int) ((double) transferred / size * 100));
                b.append("%");
                b.append(", ");
                b.append(SizeFormatterFactory.get(true).format(
                    new BigDecimal(speed * 1000).setScale(0, RoundingMode.UP).longValue()));
                b.append("/sec");
                if(speed > 0) {
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

    public void reset(final Long timestamp, final Long transferred) {
        this.timestamp = timestamp;
        this.last = transferred;
    }
}
