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

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public class Resolver implements Runnable {
    private static Logger log = Logger.getLogger(Resolver.class);

    private final Object signal = new Object();

    /**
     * The hostname to lookup
     */
    private String hostname;

    /**
     * The IP address resolved for this hostname
     */
    private InetAddress resolved;

    /**
     * @return
     */
    public boolean isResolved() {
        return this.resolved != null;
    }

    private UnknownHostException exception;

    /**
     * @return True if the lookup has failed and the host is unkown
     */
    public boolean hasFailed() {
        return this.exception != null;
    }

    /**
     * @param hostname The hostname to lookup
     */
    public Resolver(String hostname) {
        this.hostname = hostname;
    }

    static {
        // Ticket #2539
        if(Preferences.instance().getBoolean("connection.dns.ipv6")) {
            System.setProperty("java.net.preferIPv6Addresses", String.valueOf(true));
        }
    }

    /**
     * This method is blocking until the hostname has been resolved or the lookup
     * has been canceled using #cancel
     *
     * @return The resolved IP address for this hostname
     * @throws UnknownHostException     If the hostname cannot be resolved
     * @throws ResolveCanceledException If the lookup has been interrupted
     * @see #cancel
     */
    public InetAddress resolve()
            throws UnknownHostException, ResolveCanceledException {

        if(this.isResolved()) {
            // Return immediatly if successful before
            return this.resolved;
        }
        this.resolved = null;
        this.exception = null;

        Thread t = new Thread(this, this.toString());
        t.start();

        synchronized(this.signal) {
            if(!this.isResolved() && !this.hasFailed()) {
                // The lookup has not finished yet
                try {
                    log.debug("Waiting for resolving of " + this.hostname);
                    // Wait for #run to finish
                    this.signal.wait();
                }
                catch(InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        }
        if(!this.isResolved()) {
            if(this.hasFailed()) {
                throw this.exception;
            }
            log.warn("Canceled resolving " + this.hostname);
            throw new ResolveCanceledException();
        }
        return this.resolved;
    }

    /**
     * Unblocks the #resolve method for the hostname lookup to finish. #resolve will
     * throw a ResolveCanceledException
     *
     * @see #resolve
     * @see ResolveCanceledException
     */
    public void cancel() {
        synchronized(this.signal) {
            this.signal.notify();
        }
    }

    /**
     * Runs the hostname resolution in the background
     */
    public void run() {
        try {
            this.resolved = InetAddress.getByName(this.hostname);
            if(log.isInfoEnabled()) {
                log.info("Resolved " + this.hostname + " to " + this.resolved.getHostAddress());
            }
        }
        catch(UnknownHostException e) {
            log.warn("Failed resolving " + this.hostname);
            this.exception = e;
        }
        finally {
            synchronized(this.signal) {
                // Notify #resolve to proceed
                this.signal.notify();
            }
        }
    }

    @Override
    public String toString() {
        return "Resolver for " + this.hostname;
    }
}
