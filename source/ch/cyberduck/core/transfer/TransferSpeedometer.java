package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

/**
 * @version $Id:$
 */
public class TransferSpeedometer extends Speedometer {

    private Transfer transfer;

    public TransferSpeedometer(final Transfer transfer) {
        this.transfer = transfer;
    }

    /**
     * Returns the data transfer rate. The rate should depend on the transfer
     * rate timestamp.
     *
     * @return The bytes being processed per millisecond
     */
    protected double getSpeed() {
        return this.getSpeed(transfer.getTransferred());
    }

    /**
     * @return Progress information string with bytes transferred
     *         including a percentage and estimated time remaining
     */
    public String getProgress() {
        return this.getProgress(transfer.isRunning(), transfer.getSize(), transfer.getTransferred());
    }

    public void reset() {
        this.reset(transfer.getTransferred());
    }
}