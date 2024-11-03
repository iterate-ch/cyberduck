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
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RendezvousResponder extends AbstractRendezvous implements BrowseListener, ResolveListener {
    private static final Logger log = LogManager.getLogger(RendezvousResponder.class);

    private final Map<String, DNSSDService> browsers;

    public RendezvousResponder() {
        this(ProtocolFactory.get());
    }

    public RendezvousResponder(final ProtocolFactory protocols) {
        super(protocols);
        this.browsers = new ConcurrentHashMap<String, DNSSDService>();
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
        super.quit();
    }

    @Override
    public void serviceFound(final DNSSDService browser, final int flags, final int ifIndex, final String serviceName,
                             final String regType, final String domain) {
        log.debug("Browser found service at {} not yet resolved", serviceName);
        try {
            DNSSD.resolve(flags, ifIndex, serviceName, regType, domain, this);
        }
        catch(DNSSDException e) {
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
    public void operationFailed(final DNSSDService resolver, final int errorCode) {
        log.warn("Operation failed with error code {}", errorCode);
        resolver.stop();
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
            this.add(fullname, hostname, port, user, password, path);
        }
        finally {
            // Note: When the desired results have been returned, the client MUST terminate
            // the resolve by calling DNSSDService.stop().
            resolver.stop();
            autorelease.operate();
        }
    }
}
