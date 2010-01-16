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

/**
 * @version $Id$
 */
public class Speedometer {
    //the time to start counting bytes transfered
    private long timestamp;
    //initial data already transfered
    private double initialBytesTransfered;
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
        if(bytesTransferred > initialBytesTransfered) {
            if(0 == initialBytesTransfered) {
                initialBytesTransfered = bytesTransferred;
                return -1;
            }
            // number of seconds data was actually transferred
            double elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
            if(elapsedSeconds > 1) {
                // bytes per second
                return (float) ((bytesTransferred - initialBytesTransfered) / (elapsedSeconds));
            }
        }
        return -1;
    }

    public String getProgress() {
        StringBuilder b = new StringBuilder();
        b.append(Status.getSizeAsString(transfer.getTransferred()));
        b.append(" ");
        b.append(Locale.localizedString("of", "1.2MB of 3.4MB"));
        b.append(" ");
        b.append(Status.getSizeAsString(transfer.getSize()));
        if(transfer.isRunning()) {
            b.append(" (");
            if(0 == transfer.getSize()) {
                b.append(0);
            }
            else {
                b.append((int) (transfer.getTransferred() / transfer.getSize() * 100));
            }
            b.append("%");
            float speed = this.getSpeed();
            if(speed > 0) {
                b.append(", ");
                b.append(Status.getSizeAsString(speed));
                b.append("/sec");
                if(transfer.getSize() > 0) {
                    b.append(", ");
                    // remaining time in seconds
                    double remaining = ((transfer.getSize() - this.getBytesTransfered()) / speed);
                    b.append(Status.getRemainingAsString(remaining));
                }
            }
            b.append(")");
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
        this.initialBytesTransfered = transfer.getTransferred();
        this.bytesTransferred = 0;
    }
}
