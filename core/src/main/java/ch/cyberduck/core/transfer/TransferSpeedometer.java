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

import org.apache.log4j.Logger;

public class TransferSpeedometer extends Speedometer {
    private static final Logger log = Logger.getLogger(TransferSpeedometer.class);

    private final Transfer transfer;

    public TransferSpeedometer(final Transfer transfer) {
        this.transfer = transfer;
    }

    public TransferProgress getStatus() {
        final Long transferred = transfer.getTransferred();
        final Long size = transfer.getSize();
        final Double speed = this.getSpeed(transferred, false);
        return new TransferProgress(size, transferred,
                this.getProgress(transfer.isRunning(), size, transferred, speed), speed);
    }

    public void reset() {
        final long timestamp = System.currentTimeMillis();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reset with timestamp %d", timestamp));
        }
        this.reset(timestamp, transfer.getTransferred());
    }
}