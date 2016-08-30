package ch.cyberduck.core.bonjour;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;

import java.util.Iterator;

public final class RendezvousCollection extends AbstractHostCollection implements RendezvousListener {
    private static final long serialVersionUID = 6468881403370416829L;

    private static RendezvousCollection RENDEZVOUS_COLLECTION;

    private static final Object lock = new Object();

    public static RendezvousCollection defaultCollection() {
        synchronized(lock) {
            if(null == RENDEZVOUS_COLLECTION) {
                RENDEZVOUS_COLLECTION = new RendezvousCollection();
            }
            return RENDEZVOUS_COLLECTION;
        }
    }

    private final Rendezvous rendezvous;

    private RendezvousCollection() {
        this(RendezvousFactory.instance());
    }

    public RendezvousCollection(final Rendezvous rendezvous) {
        this.rendezvous = rendezvous;
        this.rendezvous.addListener(this);
        this.collectionLoaded();
    }

    @Override
    public void serviceResolved(final String identifier, final Host host) {
        this.collectionItemAdded(host);
    }

    @Override
    public void serviceLost(final Host host) {
        this.collectionItemRemoved(host);
    }

    @Override
    public String getName() {
        return LocaleFactory.localizedString("Bonjour");
    }

    @Override
    public Host get(int row) {
        return rendezvous.getService(row);
    }

    @Override
    public int size() {
        return rendezvous.numberOfServices();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Host remove(int row) {
        return null;
    }

    @Override
    public Object[] toArray() {
        Host[] content = new Host[this.size()];
        int i = 0;
        for(Host host : this) {
            content[i] = host;
        }
        return content;
    }

    @Override
    public Iterator<Host> iterator() {
        return rendezvous.iterator();
    }

    @Override
    public boolean allowsAdd() {
        return false;
    }

    @Override
    public boolean allowsDelete() {
        return false;
    }

    @Override
    public boolean allowsEdit() {
        return false;
    }
}
