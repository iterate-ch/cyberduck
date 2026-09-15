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

import org.apache.commons.lang3.StringUtils;
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
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.util.concurrent.Uninterruptibles;

public final class Resolver {
    private static final Logger log = LogManager.getLogger(Resolver.class);

    /**
     * Special use domain name reserved in RFC 6761 (6.3)
     */
    private static final String LOCALHOST_NAME = "localhost";
    private static final String LOCALHOST_SUFFIX = "." + LOCALHOST_NAME;

    private static final byte[] LOOPBACK_IPV4 = new byte[]{127, 0, 0, 1};
    private static final byte[] LOOPBACK_IPV6 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

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
        if(this.isLocalhost(hostname)) {
            // RFC 6761 (6.3) Name resolution APIs and libraries SHOULD recognize localhost names as special and SHOULD
            // always return the IP loopback address for address queries. Name resolution APIs SHOULD NOT send queries
            // for localhost names to their configured caching DNS server(s).
            final InetAddress[] loopback = this.loopback(hostname);
            log.info("Resolved reserved name {} to {}", hostname, Arrays.toString(loopback));
            return loopback;
        }
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicReference<List<InetAddress>> resolved = new AtomicReference<>();
        final AtomicReference<UnknownHostException> failure = new AtomicReference<>();
        final Thread resolver = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final InetAddress[] allByName = InetAddress.getAllByName(hostname);
                    resolved.set(Arrays.stream(allByName).sorted(new AddressComparator()).collect(Collectors.toList()));
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

    /**
     * @param hostname Hostname with optional trailing dot for fully qualified domain name
     * @return True if the name is <code>localhost.</code> or falls within the reserved <code>.localhost.</code> domain
     * @see <a href="https://www.rfc-editor.org/rfc/rfc6761.html#section-6.3">RFC 6761 (6.3) Domain Name Reservation
     * Considerations for "localhost."</a>
     */
    private boolean isLocalhost(final String hostname) {
        // Names are case-insensitive and may be fully qualified with a trailing dot
        final String name = StringUtils.removeEnd(StringUtils.lowerCase(hostname, Locale.ROOT), ".");
        return StringUtils.equals(name, LOCALHOST_NAME) || StringUtils.endsWith(name, LOCALHOST_SUFFIX);
    }

    /**
     * @param hostname Hostname to attach to the returned addresses
     * @return Loopback addresses sorted by address family preference
     */
    private InetAddress[] loopback(final String hostname) throws ResolveFailedException {
        try {
            return Stream.of(InetAddress.getByAddress(hostname, LOOPBACK_IPV4), InetAddress.getByAddress(hostname, LOOPBACK_IPV6))
                    .sorted(new AddressComparator()).toArray(InetAddress[]::new);
        }
        catch(UnknownHostException e) {
            throw new ResolveFailedException(
                    MessageFormat.format(LocaleFactory.localizedString("DNS lookup for {0} failed", "Error"), hostname), e);
        }
    }

    private final class AddressComparator implements Comparator<InetAddress> {
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
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Resolver{");
        sb.append('}');
        return sb.toString();
    }
}
