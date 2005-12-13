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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.dnssd.BrowseListener;
import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.ResolveListener;
import com.apple.dnssd.TXTRecord;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;

/**
 * @version $Id$
 */
public class Rendezvous
        implements BrowseListener, ResolveListener {

    private static Logger log = Logger.getLogger(Rendezvous.class);

    static {
        // Ensure native keychain library is loaded
        try {
            NSBundle bundle = NSBundle.mainBundle();
            String lib = bundle.resourcePath() + "/Java/" + "libDNSSD.dylib";
            log.info("Locating libDNSSD.dylib at '" + lib + "'");
            System.load(lib);
        }
        catch (UnsatisfiedLinkError e) {
            log.error("Could not load the libDNSSD.dylib library:" + e.getMessage());
        }
    }

    private static final String[] serviceTypes = new String[]{
            "_sftp._tcp.",
            "_ssh._tcp.",
            "_ftp._tcp."
    };

    private Map services;
    private Map browsers;

    private static Rendezvous instance;

    public static Rendezvous instance() {
        if (null == instance) {
            instance = new Rendezvous();
        }
        return instance;
    }

    private Rendezvous() {
        log.debug("Rendezvous");
        this.services = new HashMap();
        this.browsers = new HashMap();
    }

    public void init() {
        log.debug("init");
        try {
            for (int i = 0; i < serviceTypes.length; i++) {
                String protocol = serviceTypes[i];
                log.info("Adding Rendezvous service listener for " + protocol);
                this.browsers.put(protocol, DNSSD.browse(protocol, Rendezvous.this));
            }
        }
        catch (DNSSDException e) {
            log.error(e.getMessage());
            Rendezvous.this.quit();
        }
    }

    public void quit() {
        for (int i = 0; i < serviceTypes.length; i++) {
            String protocol = serviceTypes[i];
            log.info("Removing Rendezvous service listener for " + protocol);
            Object service = this.browsers.get(protocol);
            if(null == service)
                continue;
            ((DNSSDService)service).stop();
        }
    }

    private Vector listeners = new Vector();

    private RendezvousListener notifier = new RendezvousListener() {

        public void serviceResolved(final String servicename) {
            RendezvousListener[] l = null;
            synchronized(Rendezvous.this) {
                l = (RendezvousListener[])listeners.toArray(new RendezvousListener[]{});
            }
            for(int i = 0; i < l.length; i++) {
                l[i].serviceResolved(servicename);
            }
        }

        public void serviceLost(final String servicename) {
            RendezvousListener[] l = null;
            synchronized(Rendezvous.this) {
                l = (RendezvousListener[])listeners.toArray(new RendezvousListener[]{});
            }
            for(int i = 0; i < l.length; i++) {
                l[i].serviceLost(servicename);
            }
        }
    };

    public void addListener(RendezvousListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RendezvousListener listener) {
        listeners.remove(listener);
    }

    public java.util.Collection getServices() {
        return services.keySet();
    }

    public Host getServiceWithIdentifier(String identifier) {
        log.debug("getService:" + identifier);
        return (Host) services.get(identifier);
    }

    public Host getServiceWithDisplayedName(String displayedName) {
        for(Iterator iter = services.values().iterator(); iter.hasNext(); ) {
            Host h = (Host)iter.next();
            if(h.getNickname().equals(displayedName)) {
                return h;
            }
        }
        log.warn("No identifier for displayed name:"+displayedName);
        return null;
    }

    public int numberOfServices() {
        return services.size();
    }

    public String getDisplayedName(int index) {
        if(index < this.numberOfServices())
            return ((Host[])services.values().toArray(new Host[]{}))[index].getNickname();
        return NSBundle.localizedString("Unknown", "");
    }

    public String getDisplayedName(String identifier) {
        Object o = services.get(identifier);
        if(null == o)
            return NSBundle.localizedString("Unknown", "");
        return ((Host)o).getNickname();
    }

    public void serviceFound(DNSSDService browser, int flags, int ifIndex, String servicename,
                             String regType, String domain) {
        log.debug("serviceFound:" + servicename);
        try {
            DNSSD.resolve(flags, ifIndex, servicename, regType, domain, Rendezvous.this);
        }
        catch (DNSSDException e) {
            log.error(e.getMessage());
        }
    }

    public void serviceLost(DNSSDService browser, int flags, int ifIndex, String serviceName,
                            String regType, String domain) {
        log.debug("serviceLost:" + serviceName);
        try {
            String identifier = DNSSD.constructFullName(serviceName, regType, domain);
            notifier.serviceLost(identifier);
            if(null == this.services.remove(identifier))
                return;
        }
        catch (DNSSDException e) {
            log.error(e.getMessage());
        }
    }

    public void operationFailed(DNSSDService resolver, int errorCode) {
        log.debug("operationFailed:" + errorCode);
        resolver.stop();
    }

    public void serviceResolved(DNSSDService resolver, int flags, int ifIndex,
                                String fullname, String hostname, int port, TXTRecord txtRecord) {
        log.debug("serviceResolved:" + hostname);
        Host host = new Host(hostname, port);
        host.setCredentials(Preferences.instance().getProperty("connection.login.name"), null);
        if (host.getProtocol().equals(Session.FTP)) {
            host.setCredentials(null, null); //use anonymous login for FTP
        }
        if(null == this.services.put(fullname, host)) {
            this.notifier.serviceResolved(fullname);
        }
    }
}
