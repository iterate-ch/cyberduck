package ch.cyberduck.core.udt;

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

import ch.cyberduck.core.threading.AutoReleaseNamedThreadFactory;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.net.NetSocketUDT;

public class UDTSocket extends NetSocketUDT {
    private static final Logger log = Logger.getLogger(UDTSocket.class);

    private final ThreadFactory threadFactory
            = new AutoReleaseNamedThreadFactory("connect");

    private IOException exception;

    public UDTSocket() throws ExceptionUDT {
        super();
    }

    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        final CountDownLatch signal = new CountDownLatch(1);
        final Thread t = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    connect(endpoint);
                }
                catch(IOException e) {
                    exception = e;
                }
                finally {
                    signal.countDown();
                }
            }
        });
        t.start();
        try {
            // Wait for #run to finish
            if(!signal.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new SocketTimeoutException();
            }
        }
        catch(InterruptedException e) {
            final SocketTimeoutException s = new SocketTimeoutException(e.getMessage());
            s.initCause(e);
            throw s;
        }
        if(exception != null) {
            throw exception;
        }
    }

    @Override
    public void setSoLinger(final boolean on, final int linger) throws SocketException {
        if(linger <= 0) {
            log.warn("Ignore SO_LINGER");
        }
        else {
            super.setSoLinger(on, linger);
        }
    }
}
