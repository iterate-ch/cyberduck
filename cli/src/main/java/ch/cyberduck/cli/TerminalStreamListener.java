package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferSpeedometer;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class TerminalStreamListener implements StreamListener {

    private TransferSpeedometer meter;

    private Console console = new Console();

    private AtomicLong timestamp = new AtomicLong();

    private static final int DEFAULT_WIDTH = 30;

    /**
     * Progress bar fixed width in characters
     */
    private int width;

    private final Semaphore lock
            = new Semaphore(1);

    public TerminalStreamListener(final TransferSpeedometer meter) {
        this(meter, DEFAULT_WIDTH);
    }

    public TerminalStreamListener(final TransferSpeedometer meter, final int width) {
        this.meter = meter;
        this.width = width;
    }

    private void increment() {
        final TransferProgress progress = meter.getStatus();
        if(System.currentTimeMillis() - timestamp.get() < 100L) {
            if(!progress.isComplete()) {
                return;
            }
        }
        try {
            lock.acquire();
            final BigDecimal fraction;
            if(progress.getTransferred() == 0L) {
                fraction = BigDecimal.ZERO;
            }
            else {
                fraction = new BigDecimal(progress.getTransferred())
                        .divide(new BigDecimal(progress.getSize()), 1, RoundingMode.DOWN);
            }
            console.printf("\r%s[", Ansi.ansi()
                    .saveCursorPosition()
                    .eraseLine(Ansi.Erase.ALL)
                    .restoreCursorPosition());
            int i = 0;
            for(; i <= (int) (fraction.doubleValue() * width); i++) {
                console.printf("\u25AE");
            }
            for(; i < width; i++) {
                console.printf(StringUtils.SPACE);
            }
            console.printf("] %s%s", progress.getProgress(), Ansi.ansi().reset());
            timestamp.set(System.currentTimeMillis());
        }
        catch(InterruptedException e) {
            //
        }
        finally {
            lock.release();
        }
    }

    @Override
    public void recv(final long bytes) {
        this.increment();
    }

    @Override
    public void sent(final long bytes) {
        this.increment();
    }
}
