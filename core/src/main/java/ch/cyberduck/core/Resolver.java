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
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.Uninterruptibles;

public final class Resolver {
    private static final Logger log = LogManager.getLogger(Resolver.class);

    private final ThreadFactory threadFactory
            = new NamedThreadFactory("resolver");

    private final boolean preferIPv6;

    public Resolver() {
        this(PreferencesFactory.get().getBoolean("connection.dns.ipv6"));
    }

    public Resolver(final boolean preferIPv6) {
        this.preferIPv6 = preferIPv6;
    }

    /**
     * This method is blocking until the hostname has been resolved or the lookup has been canceled using #cancel
     *
     * @return The resolved IP address for this hostname
     * @throws ResolveFailedException   If the hostname cannot be resolved
     * @throws ResolveCanceledException If the lookup has been interrupted
     */
    public InetAddress[] resolve(final String hostname, final CancelCallback callback) throws ResolveFailedException, ResolveCanceledException {
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicReference<List<InetAddress>> resolved = new AtomicReference<>();
        final AtomicReference<UnknownHostException> failure = new AtomicReference<>();
        final Thread resolver = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final InetAddress[] allByName = InetAddress.getAllByName(hostname);
                    resolved.set(Arrays.stream(allByName).sorted(new Comparator<InetAddress>() {
                        @Override
                        public int compare(final InetAddress o1, final InetAddress o2) {
                            if(o1 instanceof Inet6Address && o2 instanceof Inet4Address) {
                                return preferIPv6 ? -1 : 1;
                            }
                            if(o2 instanceof Inet6Address && o1 instanceof Inet4Address) {
                                return preferIPv6 ? 1 : -1;
                            }
                            return 0;
                        }
                    }).collect(Collectors.toList()));
                    log.info("Resolved {} to {}", hostname, Arrays.toString(resolved.get().toArray()));
                }
                catch(UnknownHostException e) {
                    log.warn("Failed resolving {}", hostname);
                    failure.set(e);
                }
                finally {
                    signal.countDown();
                }
            }
        });
        resolver.start();
        log.debug("Waiting for resolving of {}", hostname);
        // Wait for #run to finish
        while(!Uninterruptibles.awaitUninterruptibly(signal, Duration.ofMillis(500))) {
            try {
                callback.verify();
            }
            catch(ConnectionCanceledException c) {
                throw new ResolveCanceledException(MessageFormat.format(LocaleFactory.localizedString("DNS lookup for {0} failed", "Error"), hostname), c);
            }
        }
        try {
            callback.verify();
        }
        catch(ConnectionCanceledException c) {
            throw new ResolveCanceledException(MessageFormat.format(LocaleFactory.localizedString("DNS lookup for {0} failed", "Error"), hostname), c);
        }
        if(null == resolved.get()) {
            if(null == failure.get()) {
                log.warn("Canceled resolving {}", hostname);
                throw new ResolveCanceledException(MessageFormat.format(LocaleFactory.localizedString("DNS lookup for {0} failed", "Error"), hostname));
            }
            throw new ResolveFailedException(
                    MessageFormat.format(LocaleFactory.localizedString("DNS lookup for {0} failed", "Error"), hostname), failure.get());
        }
        return resolved.get().toArray(new InetAddress[resolved.get().size()]);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Resolver{");
        sb.append('}');
        return sb.toString();
    }
}
