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

import com.apple.cocoa.application.NSImage;

import java.util.Iterator;

/**
 * @version $Id$
 */
public class RendezvousCollection extends BookmarkCollection {

    private static RendezvousCollection RENDEZVOUS_COLLECTION
            = new RendezvousCollection();

    /**
     *
     * @return
     */
    public static RendezvousCollection defaultCollection() {
        return RENDEZVOUS_COLLECTION;
    }

    public Host get(int row) {
        return Rendezvous.instance().getService(row);
    }

    public int size() {
        return Rendezvous.instance().numberOfServices();
    }

    public Host remove(int row) {
        return null;
    }

    public Object[] toArray() {
        Host[] content = new Host[this.size()];
        int i = 0;
        for(Iterator<Host> iter = this.iterator(); iter.hasNext(); ) {
            content[i] = iter.next();
        }
        return content;
    }

    public boolean allowsAdd() {
        return false;
    }

    public boolean allowsDelete() {
        return false;
    }

    public boolean allowsEdit() {
        return false;
    }

    public NSImage getIcon() {
        return NSImage.imageNamed("rendezvous.icns");
    }
}
