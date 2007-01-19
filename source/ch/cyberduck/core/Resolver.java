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
     * @return
     * @throws UnknownHostException
     * @throws ResolveCanceledException
     */
    public InetAddress resolve()
            throws UnknownHostException, ResolveCanceledException {

        if(this.isResolved()) {
            return this.resolved;
        }
        this.resolved = null;
        this.exception = null;

        Thread t = new Thread(this, this.toString());
        t.start();

        synchronized(this.signal) {
            try {
                log.debug("Waiting for resolving of " + this.hostname);
                this.signal.wait();
            }
            catch(InterruptedException e) {
                log.error(e.getMessage());
            }
        }
        if(null == this.resolved) {
            if(null == this.exception) {
                log.warn("Canceled resolving " + this.hostname);
                throw new ResolveCanceledException();
            }
            throw this.exception;
        }
        return this.resolved;
    }

    /**
     *
     */
    public void cancel() {
        synchronized(this.signal) {
            this.signal.notify();
        }
    }

    private String hostname;

    private InetAddress resolved;

    /**
     * @return
     */
    public boolean isResolved() {
        return this.resolved != null;
    }

    private UnknownHostException exception;

    public Resolver(String hostname) {
        this.hostname = hostname;
    }

    /**
     *
     */
    public void run() {
        try {
            this.resolved = InetAddress.getByName(this.hostname);
            log.info("Resolved " + this.hostname + " to " + this.resolved.getHostAddress());
        }
        catch(UnknownHostException e) {
            log.warn("Failed resolving " + this.hostname);
            this.exception = e;
        }
        finally {
            synchronized(this.signal) {
                this.signal.notify();
            }
        }
    }

    public String toString() {
        return "Resolver for "+this.hostname;
    }
}
