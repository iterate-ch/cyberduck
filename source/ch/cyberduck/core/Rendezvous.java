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
import java.util.Observable;
import java.util.Iterator;

/**
 * @version $Id$
 */
public class Rendezvous extends Observable
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
    }

    public void init() {
        log.debug("init");
        try {
            for (int i = 0; i < serviceTypes.length; i++) {
                String protocol = serviceTypes[i];
                log.info("Adding Rendezvous service listener for " + protocol);
                DNSSD.browse(protocol, Rendezvous.this);
            }
        }
        catch (DNSSDException e) {
            log.error(e.getMessage());
            Rendezvous.this.quit();
        }
    }

    public void quit() {
        log.info("Removing Rendezvous service listener");
        for(Iterator iter = this.services.keySet().iterator(); iter.hasNext(); ) {
            String identifier = (String)iter.next();
            this.callObservers(new Message(Message.RENDEZVOUS_REMOVE, identifier));
        }
        this.services.clear();
    }

    public void callObservers(Message arg) {
        if (log.isDebugEnabled()) {
            log.debug("callObservers:" + arg);
            log.debug(this.countObservers() + " observer(s) known.");
        }
        this.setChanged();
        this.notifyObservers(arg);
    }

    public Host getService(String key) {
        log.debug("getService:" + key);
        return (Host) services.get(key);
    }

    public String[] getServices() {
        return (String[]) this.services.keySet().toArray(new String[]{});
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
            if(null == this.services.remove(identifier))
                return;
            this.callObservers(new Message(Message.RENDEZVOUS_REMOVE, identifier));
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
        Host h = new Host(hostname, port);
        h.setCredentials(Preferences.instance().getProperty("connection.login.name"), null);
        if (h.getProtocol().equals(Session.FTP)) {
            h.setCredentials(null, null); //use anonymous login for FTP
        }
        if(null == this.services.put(fullname, h))
            this.callObservers(new Message(Message.RENDEZVOUS_ADD, fullname));
    }
}
