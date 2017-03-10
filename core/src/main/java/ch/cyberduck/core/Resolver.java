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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ResolveCanceledException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.threading.AutoReleaseNamedThreadFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class Resolver {
    private static final Logger log = Logger.getLogger(Resolver.class);

    private final ThreadFactory threadFactory
            = new AutoReleaseNamedThreadFactory("resolver");

    /**
     * This method is blocking until the hostname has been resolved or the lookup
     * has been canceled using #cancel
     *
     * @return The resolved IP address for this hostname
     * @throws ResolveFailedException   If the hostname cannot be resolved
     * @throws ResolveCanceledException If the lookup has been interrupted
     */
    public InetAddress resolve(final String hostname, final CancelCallback callback) throws ResolveFailedException, ResolveCanceledException {
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicReference<InetAddress> resolved = new AtomicReference<>();
        final AtomicReference<UnknownHostException> failure = new AtomicReference<>();
        final Thread resolver = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final InetAddress address = InetAddress.getByName(hostname);
                    resolved.set(address);
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Resolved %s to %s", hostname, address.getHostAddress()));
                    }
                }
                catch(UnknownHostException e) {
                    log.warn(String.format("Failed resolving %s", hostname));
                    failure.set(e);
                }
                finally {
                    signal.countDown();
                }
            }
        });
        resolver.start();
        log.debug(String.format("Waiting for resolving of %s", hostname));
        // Wait for #run to finish
        try {
            while(!signal.await(500, TimeUnit.MILLISECONDS)) {
                try {
                    callback.verify();
                }
                catch(ConnectionCanceledException c) {
                    throw new ResolveCanceledException(c);
                }
            }
        }
        catch(InterruptedException e) {
            log.error(String.format("Waiting for resolving of %s", hostname), e);
            throw new ResolveCanceledException(e);
        }
        try {
            callback.verify();
        }
        catch(ConnectionCanceledException c) {
            throw new ResolveCanceledException(c);
        }
        if(null == resolved.get()) {
            if(null == failure.get()) {
                log.warn(String.format("Canceled resolving %s", hostname));
                throw new ResolveCanceledException();
            }
            throw new ResolveFailedException(
                    MessageFormat.format(LocaleFactory.localizedString("DNS lookup for {0} failed", "Error"), hostname), failure.get());
        }
        return resolved.get();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Resolver{");
        sb.append('}');
        return sb.toString();
    }
}