package ch.cyberduck.core.bonjour;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.apple.dnssd.BrowseListener;
import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.ResolveListener;
import com.apple.dnssd.TXTRecord;

import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;
import ch.cyberduck.core.threading.ScheduledThreadPool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class RendezvousResponder extends AbstractRendezvous implements BrowseListener {
    private static final Logger log = LogManager.getLogger(RendezvousResponder.class);

    private final Map<String, DNSSDService> browsers;

    /**
     * Pending resolve operations by full service name and interface. Each holds a dedicated thread until stopped.
     */
    private final Map<String, Resolver> resolvers;
    private final ScheduledThreadPool timer;
    private final long timeout;

    public RendezvousResponder() {
        this(ProtocolFactory.get());
    }

    public RendezvousResponder(final ProtocolFactory protocols) {
        this(protocols, PreferencesFactory.get().getLong("bonjour.timeout.ms"));
    }

    /**
     * @param protocols Registered protocols
     * @param timeout   Milliseconds to wait for a service to resolve
     */
    public RendezvousResponder(final ProtocolFactory protocols, final long timeout) {
        super(protocols);
        this.browsers = new ConcurrentHashMap<String, DNSSDService>();
        this.resolvers = new ConcurrentHashMap<String, Resolver>();
        this.timer = new ScheduledThreadPool("rendezvous");
        this.timeout = timeout;
    }

    @Override
    public void init() {
        log.debug("Initialize responder by browsing DNSSD");
        super.init();
        try {
            for(String protocol : this.getServiceTypes()) {
                log.info("Adding service listener for {}", protocol);
                browsers.put(protocol, DNSSD.browse(protocol, this));
            }
        }
        catch(DNSSDException e) {
            log.error(String.format("Failure initializing Bonjour discovery: %s", e.getMessage()), e);
            this.quit();
        }
    }

    @Override
    public void quit() {
        for(String protocol : this.getServiceTypes()) {
            log.info("Removing service listener for {}", protocol);
            final DNSSDService service = browsers.get(protocol);
            if(null == service) {
                continue;
            }
            service.stop();
        }
        for(Resolver resolver : resolvers.values()) {
            resolver.stop();
        }
        timer.shutdown(false);
        super.quit();
    }

    @Override
    public void serviceFound(final DNSSDService browser, final int flags, final int ifIndex, final String serviceName,
                             final String regType, final String domain) {
        log.debug("Browser found service at {} not yet resolved", serviceName);
        try {
            // Resolve on the interface reported by the browser
            final String key = toKey(DNSSD.constructFullName(serviceName, regType, domain), ifIndex);
            final Resolver resolver = new Resolver(key);
            if(null != resolvers.putIfAbsent(key, resolver)) {
                log.debug("Skip resolving service {} with pending resolve", key);
                return;
            }
            try {
                resolver.service = DNSSD.resolve(0, ifIndex, serviceName, regType, domain, resolver);
                resolver.timeout = timer.schedule(resolver::expire, timeout, TimeUnit.MILLISECONDS);
            }
            catch(DNSSDException | RejectedExecutionException e) {
                resolver.stop();
                throw e;
            }
        }
        catch(DNSSDException | RejectedExecutionException e) {
            log.error(String.format("Failure resolving service %s: %s", serviceName, e.getMessage()), e);
        }
    }

    @Override
    public void serviceLost(final DNSSDService browser, final int flags, final int ifIndex, final String serviceName,
                            final String regType, final String domain) {
        log.debug("Service lost for {}", serviceName);
        final ActionOperationBatcher autorelease = ActionOperationBatcherFactory.get();
        try {
            final String identifier = DNSSD.constructFullName(serviceName, regType, domain);
            final Resolver resolver = resolvers.get(toKey(identifier, ifIndex));
            if(null != resolver) {
                log.debug("Stop pending resolve for lost service {}", resolver.key);
                resolver.stop();
            }
            this.remove(identifier);
        }
        catch(DNSSDException e) {
            log.error(String.format("Failure removing service %s: %s", serviceName, e.getMessage()), e);
        }
        finally {
            autorelease.operate();
        }
    }

    @Override
    public void operationFailed(final DNSSDService browser, final int errorCode) {
        log.warn("Operation failed with error code {}", errorCode);
        browser.stop();
    }

    /**
     * @param identifier Full service name
     * @param ifIndex    Interface the service was found on
     * @return Key for pending resolve operation
     */
    private static String toKey(final String identifier, final int ifIndex) {
        return String.format("%s%%%d", identifier, ifIndex);
    }

    /**
     * Resolve operation for a single service. Each operation is polled by a dedicated thread in the DNSSD library
     * until stopped.
     */
    private final class Resolver implements ResolveListener {

        private final String key;
        private volatile DNSSDService service;
        private volatile ScheduledFuture<?> timeout;

        public Resolver(final String key) {
            this.key = key;
        }

        @Override
        public void serviceResolved(final DNSSDService resolver, final int flags, final int ifIndex,
                                    final String fullname, final String hostname, final int port, final TXTRecord txtRecord) {
            log.debug("Resolved service with name {} to {}", fullname, hostname);
            final ActionOperationBatcher autorelease = ActionOperationBatcherFactory.get();
            try {
                String user = null;
                String password = null;
                String path = null;
                log.debug("TXT Record {}", txtRecord);
                if(txtRecord.contains("u")) {
                    user = txtRecord.getValueAsString("u");
                }
                if(txtRecord.contains("p")) {
                    password = txtRecord.getValueAsString("p");
                }
                if(txtRecord.contains("path")) {
                    path = txtRecord.getValueAsString("path");
                }
                RendezvousResponder.this.add(fullname, hostname, port, user, password, path);
            }
            finally {
                // Note: When the desired results have been returned, the client MUST terminate
                // the resolve by calling DNSSDService.stop().
                this.stop(resolver);
                autorelease.operate();
            }
        }

        @Override
        public void operationFailed(final DNSSDService resolver, final int errorCode) {
            log.warn("Resolve for {} failed with error code {}", key, errorCode);
            this.stop(resolver);
        }

        private void expire() {
            log.warn("Timeout resolving service {}", key);
            this.stop();
        }

        private void stop() {
            this.stop(service);
        }

        /**
         * Terminate resolve operation. Can be called multiple times.
         *
         * @param service Resolve operation. Passed from callbacks as these may run before the service returned
         *                from DNSSD.resolve is set. Null if not yet started.
         */
        private void stop(final DNSSDService service) {
            resolvers.remove(key, this);
            final ScheduledFuture<?> timeout = this.timeout;
            if(null != timeout) {
                timeout.cancel(false);
            }
            if(null != service) {
                service.stop();
            }
        }
    }
}
