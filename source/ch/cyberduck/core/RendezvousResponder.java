package ch.cyberduck.core;

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

import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;

import org.apache.log4j.Logger;

import com.apple.dnssd.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Id$
 */
public class RendezvousResponder extends AbstractRendezvous
        implements BrowseListener, ResolveListener {
    private static Logger log = Logger.getLogger(RendezvousResponder.class);

    private Map<String, DNSSDService> browsers;

    private static RendezvousResponder instance = null;

    public static void register() {
        RendezvousFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends RendezvousFactory {
        @Override
        protected Rendezvous create() {
            return new RendezvousResponder();
        }
    }

    private static final Object lock = new Object();

    private RendezvousResponder() {
        log.debug("Rendezvous");
        this.browsers = new ConcurrentHashMap<String, DNSSDService>();
    }

    public void init() {
        log.debug("init");
        try {
            for(String protocol : this.getServiceTypes()) {
                if(log.isInfoEnabled()) {
                    log.info("Adding Rendezvous service listener for " + protocol);
                }
                this.browsers.put(protocol, DNSSD.browse(protocol, this));
            }
        }
        catch(DNSSDException e) {
            log.error(e.getMessage());
            this.quit();
        }
    }

    public void quit() {
        for(String protocol : this.getServiceTypes()) {
            if(log.isInfoEnabled()) {
                log.info("Removing Rendezvous service listener for " + protocol);
            }
            DNSSDService service = this.browsers.get(protocol);
            if(null == service) {
                continue;
            }
            service.stop();
        }
    }

    /**
     * @param browser
     * @param flags
     * @param ifIndex
     * @param servicename
     * @param regType
     * @param domain
     */
    public void serviceFound(DNSSDService browser, int flags, int ifIndex, String servicename,
                             String regType, String domain) {
        log.debug("serviceFound:" + servicename);
        try {
            DNSSD.resolve(flags, ifIndex, servicename, regType, domain, this);
        }
        catch(DNSSDException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * @param browser
     * @param flags
     * @param ifIndex
     * @param serviceName
     * @param regType
     * @param domain
     */
    public void serviceLost(DNSSDService browser, int flags, int ifIndex, String serviceName,
                            String regType, String domain) {
        log.debug("serviceLost:" + serviceName);
        final NSAutoreleasePool pool = NSAutoreleasePool.push();
        try {
            String identifier = DNSSD.constructFullName(serviceName, regType, domain);
            this.remove(identifier);
        }
        catch(DNSSDException e) {
            log.error(e.getMessage());
        }
        finally {
            pool.drain();
        }
    }

    /**
     * @param resolver
     * @param errorCode
     */
    public void operationFailed(DNSSDService resolver, int errorCode) {
        log.warn("operationFailed:" + errorCode);
        resolver.stop();
    }

    /**
     * @param resolver
     * @param flags
     * @param ifIndex
     * @param fullname
     * @param hostname
     * @param port
     * @param txtRecord
     */
    public void serviceResolved(DNSSDService resolver, int flags, int ifIndex,
                                final String fullname, final String hostname, int port, TXTRecord txtRecord) {
        log.debug("serviceResolved:" + fullname);
        final NSAutoreleasePool pool = NSAutoreleasePool.push();
        try {
            String user = null;
            String password = null;
            String path = null;
            log.debug("TXT Record:" + txtRecord);
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
            pool.drain();
        }
    }
}