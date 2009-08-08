package ch.cyberduck.core;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

/**
 * @version $Id$
 */
public class RendezvousCollection extends BookmarkCollection {

    private static RendezvousCollection RENDEZVOUS_COLLECTION
            = new RendezvousCollection();

    /**
     * @return
     */
    public static RendezvousCollection defaultCollection() {
        return RENDEZVOUS_COLLECTION;
    }

    @Override
    public Host get(int row) {
        return Rendezvous.instance().getService(row);
    }

    @Override
    public int size() {
        return Rendezvous.instance().numberOfServices();
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

    @Override
    public void save() {
        ;
    }

    @Override
    public void load() {
        ;
    }
}
