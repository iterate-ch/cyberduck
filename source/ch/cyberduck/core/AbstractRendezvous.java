package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRendezvous {
    private static Logger log = Logger.getLogger(AbstractRendezvous.class);

    /**
     * sftp-ssh
     * Secure File Transfer Protocol over SSH
     * Bryan Cole <bryan.cole at teraview.com>
     * Protocol description: draft-ietf-secsh-filexfer-13.txt
     * Defined TXT keys: u=<username> p=<password> path=<path>
     */
    protected static final String SERVICE_TYPE_SFTP = "_sftp-ssh._tcp.";
    /**
     * ftp
     * File Transfer
     * Service name originally allocated for Jon Postel <postel at isi.edu>
     * Now advertised and browsed-for by numerous independent
     * server and client implementations.
     * Protocol description: RFC 959
     * Defined TXT keys: u=<username> p=<password> path=<path>
     */
    protected static final String SERVICE_TYPE_FTP = "_ftp._tcp.";
    /**
     * webdav
     * World Wide Web Distributed Authoring and Versioning (WebDAV)
     * Y. Y. Goland <yarong at microsoft.com>
     * Protocol description: RFC 2518
     * Defined TXT keys: u=<username> p=<password> path=<path>
     */
    protected static final String SERVICE_TYPE_WEBDAV = "_webdav._tcp";
    /**
     * webdavs
     * WebDAV over SSL/TLS
     * Y. Y. Goland <yarong at microsoft.com>
     * Protocol description: RFC 2518
     * Defined TXT keys: u=<username> p=<password> path=<path>
     */
    protected static final String SERVICE_TYPE_WEBDAV_TLS = "_webdavs._tcp";

    private final String[] serviceTypes = new String[]{
            SERVICE_TYPE_SFTP, SERVICE_TYPE_FTP, SERVICE_TYPE_WEBDAV, SERVICE_TYPE_WEBDAV_TLS
    };

    public String[] getServiceTypes() {
        return serviceTypes;
    }

    private Map<String, Host> services = new ConcurrentHashMap<String, Host>();

    private Set<RendezvousListener> listeners =
            Collections.synchronizedSet(new HashSet<RendezvousListener>());

    private RendezvousListener notifier = new RendezvousListener() {

        public void serviceResolved(final String servicename, final String hostname) {
            log.info("Service resolved:" + servicename);
            RendezvousListener[] l = listeners.toArray(
                    new RendezvousListener[listeners.size()]);
            for(RendezvousListener listener : l) {
                listener.serviceResolved(servicename, hostname);
            }
        }

        public void serviceLost(final String servicename) {
            log.info("Service lost:" + servicename);
            RendezvousListener[] l = listeners.toArray(
                    new RendezvousListener[listeners.size()]);
            for(RendezvousListener listener : l) {
                listener.serviceLost(servicename);
            }
        }
    };

    /**
     * Start browsing for rendezvous servcices for all registered service types
     */
    public abstract void init();

    /**
     * Halt all service discvery browsers
     */
    public abstract void quit();

    /**
     * Register a listener to be notified
     *
     * @param listener
     */
    public void addListener(RendezvousListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the listener from the notification queue
     *
     * @param listener
     */
    public void removeListener(RendezvousListener listener) {
        listeners.remove(listener);
    }


    /**
     * @return The number of services found; 0 <= services < n
     */
    public int numberOfServices() {
        return services.size();
    }

    public Host getService(int index) {
        return services.values().toArray(new Host[services.size()])[index];
    }

    public Iterator<Host> iterator() {
        return services.values().iterator();
    }

    /**
     * @param index
     * @return A nicely formatted informative string
     */
    public String getDisplayedName(int index) {
        if(index < this.numberOfServices()) {
            Host host = services.values().toArray(new Host[services.size()])[index];
            return host.getNickname();
        }
        return Locale.localizedString("Unknown");
    }

    /**
     * @param identifier The full service domain name
     * @return A nicely formatted informative string
     */
    public String getDisplayedName(String identifier) {
        Host host = services.get(identifier);
        if(null == host) {
            return Locale.localizedString("Unknown");
        }
        return host.getNickname();
    }

    protected void add(String fullname, String hostname, int port, String user, String password, String path) {
        final Protocol protocol = this.getProtocol(fullname, port);
        if(null == protocol) {
            log.warn("Unknown service type:" + fullname);
            return;
        }
        final Host host = new Host(protocol, hostname, port);
        host.getCredentials().setUsername(user);
        host.getCredentials().setPassword(password);
        host.setDefaultPath(Path.normalize(path));
        this.add(fullname, host);
    }


    /**
     * @param fullname
     * @return Null if no protocol can be found for the given Rendezvous service type.
     * @see "http://developer.apple.com/qa/qa2001/qa1312.html"
     */
    public Protocol getProtocol(final String fullname, final int port) {
        if(fullname.contains(SERVICE_TYPE_SFTP)) {
            return Protocol.SFTP;
        }
        if(fullname.contains(SERVICE_TYPE_FTP)) {
            return Protocol.FTP;
        }
        if(fullname.contains(SERVICE_TYPE_WEBDAV)) {
            return Protocol.WEBDAV;
        }
        if(fullname.contains(SERVICE_TYPE_WEBDAV_TLS)) {
            return Protocol.WEBDAV_SSL;
        }
        log.warn("Cannot find service type in:" + fullname);
        return null;
    }

    /**
     * @param fullname
     * @param host
     */
    protected void add(String fullname, Host host) {
        if(null == this.services.put(fullname, host)) {
            this.notifier.serviceResolved(fullname, host.getHostname());
        }
    }

    protected void remove(String identifier) {
        if(null == services.remove(identifier)) {
            return;
        }
        notifier.serviceLost(identifier);
    }
}