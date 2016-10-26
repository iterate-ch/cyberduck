package ch.cyberduck.core.bonjour;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LimitedRendezvousListener implements RendezvousListener {
    private static final Logger log = Logger.getLogger(LimitedRendezvousListener.class);

    /**
     * Rate limit for notifications
     */
    private final TimedSemaphore limit;

    private final Set<RendezvousListener> listeners;

    public LimitedRendezvousListener(final Set<RendezvousListener> listeners) {
        this(new TimedSemaphore(
                1L, TimeUnit.MINUTES, PreferencesFactory.get().getInteger("rendezvous.notification.limit")), listeners);
    }

    public LimitedRendezvousListener(final TimedSemaphore limit, final Set<RendezvousListener> listeners) {
        this.limit = limit;
        this.listeners = listeners;
    }

    public void quit() {
        limit.shutdown();
    }

    @Override
    public void serviceResolved(final String identifier, final Host host) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Service resolved with identifier %s with %s", identifier, host));
        }
        if(PreferencesFactory.get().getBoolean("rendezvous.loopback.suppress")) {
            try {
                if(InetAddress.getByName(host.getHostname()).equals(InetAddress.getLocalHost())) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Suppressed Rendezvous notification for %s", host));
                    }
                    return;
                }
            }
            catch(UnknownHostException e) {
                //Ignore
            }
        }
        if(this.acquire()) {
            for(RendezvousListener listener : listeners) {
                listener.serviceResolved(identifier, host);
            }
        }
    }

    @Override
    public void serviceLost(final Host servicename) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Service with name %s lost", servicename));
        }
        if(this.acquire()) {
            for(RendezvousListener listener : listeners) {
                listener.serviceLost(servicename);
            }
        }
    }

    private boolean acquire() {
        if(limit.getAvailablePermits() > 0) {
            try {
                // Blocking if limit is exceeded
                limit.acquire();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Acquired lock for %s", limit));
                }
                return true;
            }
            catch(InterruptedException e) {
                log.warn(String.format("Failure acquiring lock %s", e.getMessage()));
            }
        }
        return false;
    }
}
