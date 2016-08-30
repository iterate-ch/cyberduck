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

import ch.cyberduck.core.exception.ResolveCanceledException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

public class Resolver {
    private static final Logger log = Logger.getLogger(Resolver.class);

    private final ThreadFactory threadFactory
            = new NamedThreadFactory("resolver");

    private CountDownLatch signal = new CountDownLatch(1);

    /**
     * The IP address resolved for this hostname
     */
    private InetAddress resolved;

    /**
     * @return True if hostname is resolved to IP address
     */
    public boolean isResolved() {
        return resolved != null;
    }

    private UnknownHostException exception;

    /**
     * @return True if the lookup has failed and the host is unkown
     */
    public boolean hasFailed() {
        return exception != null;
    }

    /**
     * This method is blocking until the hostname has been resolved or the lookup
     * has been canceled using #cancel
     *
     * @return The resolved IP address for this hostname
     * @throws ResolveFailedException                               If the hostname cannot be resolved
     * @throws ch.cyberduck.core.exception.ResolveCanceledException If the lookup has been interrupted
     * @see #cancel
     */
    public InetAddress resolve(final String hostname) throws ResolveFailedException, ResolveCanceledException {
        final Thread t = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    resolved = InetAddress.getByName(hostname);
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Resolved %s to %s", hostname, resolved.getHostAddress()));
                    }
                }
                catch(UnknownHostException e) {
                    log.warn(String.format("Failed resolving %s", hostname));
                    exception = e;
                }
                finally {
                    signal.countDown();
                }
            }
        });
        t.start();
        if(!this.isResolved() && !this.hasFailed()) {
            // The lookup has not finished yet
            try {
                log.debug(String.format("Waiting for resolving of %s", hostname));
                // Wait for #run to finish
                signal.await();
            }
            catch(InterruptedException e) {
                log.error(String.format("Waiting for resolving of %s", hostname), e);
                throw new ResolveCanceledException(e);
            }
        }
        if(!this.isResolved()) {
            if(this.hasFailed()) {
                throw new ResolveFailedException(
                        MessageFormat.format(LocaleFactory.localizedString("DNS lookup for {0} failed", "Error"), hostname), exception);
            }
            log.warn(String.format("Canceled resolving %s", hostname));
            throw new ResolveCanceledException();
        }
        return resolved;
    }

    /**
     * Unblocks the #resolve method for the hostname lookup to finish. #resolve will
     * throw a ResolveCanceledException
     *
     * @see #resolve
     * @see ResolveCanceledException
     */
    public void cancel() {
        signal.countDown();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Resolver{");
        sb.append("resolved=").append(resolved);
        sb.append('}');
        return sb.toString();
    }
}