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

import ch.cyberduck.core.i18n.Locale;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class Speedometer {

    /**
     * The time to start counting bytes transfered
     */
    private long timestamp;

    /**
     * Initial data already transfered
     */
    private double initialBytesTransferred;

    /**
     * Actual bytes transferred
     */
    private double bytesTransferred;

    private Transfer transfer;

    public Speedometer(Transfer transfer) {
        this.transfer = transfer;
        this.reset();
    }

    /**
     * Returns the data transfer rate. The rate should depend on the transfer
     * rate timestamp.
     *
     * @return The bytes being processed per second
     */
    public float getSpeed() {
        bytesTransferred = transfer.getTransferred();
        if(bytesTransferred > initialBytesTransferred) {
            if(0 == initialBytesTransferred) {
                initialBytesTransferred = bytesTransferred;
                return -1;
            }
            // number of seconds data was actually transferred
            double elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
            if(elapsedSeconds > 1) {
                // bytes per second
                return (float) ((bytesTransferred - initialBytesTransferred) / (elapsedSeconds));
            }
        }
        return -1;
    }

    /**
     * @return Progress information string with bytes transfered
     *         including a percentage and estimated time remaining
     */
    public String getProgress() {
        StringBuilder b = new StringBuilder();
        final double size = transfer.getSize();
        final double transferred = transfer.getTransferred();
        b.append(MessageFormat.format(Locale.localizedString("{0} of {1}"),
                Status.getSizeAsString(transferred), Status.getSizeAsString(size)));
        final float speed = this.getSpeed();
        if(transfer.isRunning()) {
            if(size > -1 || speed > 0) {
                b.append(" (");
                if(size > -1) {
                    if(0 == size) {
                        b.append(0);
                    }
                    else {
                        b.append((int) (transferred / size * 100));
                    }
                    b.append("%");
                }
                if(speed > 0) {
                    if(size > -1) {
                        b.append(", ");
                    }
                    b.append(Status.getSizeAsString(speed));
                    b.append("/sec");
                    if(size > 0) {
                        b.append(", ");
                        // remaining time in seconds
                        double remaining = ((size - this.getBytesTransfered()) / speed);
                        b.append(Status.getRemainingAsString(remaining));
                    }
                }
                b.append(")");
            }
        }
        return b.toString();
    }

    public double getBytesTransfered() {
        return bytesTransferred;
    }

    /**
     * Reset this meter
     */
    public void reset() {
        this.timestamp = System.currentTimeMillis();
        this.initialBytesTransferred = transfer.getTransferred();
        this.bytesTransferred = 0;
    }
}
