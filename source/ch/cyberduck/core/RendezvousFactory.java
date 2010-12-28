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

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class RendezvousFactory extends Factory<Rendezvous> {
    private static Logger log = Logger.getLogger(RendezvousFactory.class);

    /**
     * Registered factories
     */
    protected static final Map<Factory.Platform, RendezvousFactory> factories
            = new HashMap<Factory.Platform, RendezvousFactory>();

    /**
     * @param platform
     * @param f
     */
    public static void addFactory(Factory.Platform platform, RendezvousFactory f) {
        factories.put(platform, f);
    }

    private static Rendezvous rendezvous;

    /**
     * @return
     */
    public static Rendezvous instance() {
        if(null == rendezvous) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                log.warn("No implementation for " + NATIVE_PLATFORM);
                return new Rendezvous() {
                    public void init() {
                        ;
                    }

                    public void quit() {
                        ;
                    }

                    public void addListener(RendezvousListener listener) {
                        ;
                    }

                    public void removeListener(RendezvousListener listener) {
                        ;
                    }

                    public int numberOfServices() {
                        return 0;
                    }

                    public Host getService(int index) {
                        return null;
                    }

                    public Iterator<Host> iterator() {
                        return Collections.<Host>emptyList().iterator();
                    }

                    public String getDisplayedName(int index) {
                        return null;
                    }

                    public String getDisplayedName(String identifier) {
                        return null;
                    }
                };
            }
            rendezvous = factories.get(NATIVE_PLATFORM).create();
        }
        return rendezvous;
    }
}
