package ch.cyberduck.core;

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
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.formatter.SizeFormatterFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class Speedometer {

    /**
     * The time to start counting bytes transferred
     */
    private long timestamp
            = System.currentTimeMillis();

    /**
     * Initial data already transferred
     */
    private long last = 0L;

    private boolean overall = false;

    private PeriodFormatter formatter
            = new RemainingPeriodFormatter();

    private Transfer transfer;

    public Speedometer(Transfer transfer) {
        this.transfer = transfer;
    }

    /**
     * Returns the data transfer rate. The rate should depend on the transfer
     * rate timestamp.
     *
     * @return The bytes being processed per millisecond
     */
    protected double getSpeed() {
        // Number of seconds data was actually transferred
        final long elapsed = System.currentTimeMillis() - timestamp;
        if(elapsed > 0) {
            final long differential = transfer.getTransferred() - last;
            // Remember for next iteration
            last = transfer.getTransferred();
            if(!overall) {
                timestamp = System.currentTimeMillis();
            }
            // The throughput is usually measured in bits per second
            return (double) differential / elapsed;
        }
        return 0L;
    }

    /**
     * @return Progress information string with bytes transferred
     *         including a percentage and estimated time remaining
     */
    public String getProgress() {
        final StringBuilder b = new StringBuilder(
                MessageFormat.format(Locale.localizedString("{0} of {1}"),
                        SizeFormatterFactory.instance().format(transfer.getTransferred(), !transfer.isComplete()),
                        SizeFormatterFactory.instance().format(transfer.getSize()))
        );
        if(transfer.isRunning()) {
            final double speed = this.getSpeed();
            if(transfer.getSize() > 0 || speed > 0) {
                b.append(" (");
                if(transfer.getSize() > 0) {
                    b.append(transfer.getSize() == 0 ? 0 : (int) ((double) transfer.getTransferred() / transfer.getSize() * 100));
                    b.append("%");
                }
                if(speed > 0) {
                    if(transfer.getSize() > 0) {
                        b.append(", ");
                    }
                    b.append(SizeFormatterFactory.instance(true).format(
                            new BigDecimal(speed * 1000).setScale(0, RoundingMode.UP).longValue()));
                    b.append("/sec");
                    if(transfer.getTransferred() < transfer.getSize()) {
                        b.append(", ");
                        // Remaining time in milliseconds
                        long remaining = new BigDecimal((transfer.getSize() - transfer.getTransferred()) / speed).setScale(0, RoundingMode.UP).longValue();
                        // Display in seconds
                        b.append(formatter.format(new BigDecimal(remaining).divide(new BigDecimal(1000L), RoundingMode.UP).longValue()));
                    }
                }
                b.append(")");
            }
        }
        return b.toString();
    }

    public void reset() {
        timestamp = System.currentTimeMillis();
        last = transfer.getTransferred();
    }
}