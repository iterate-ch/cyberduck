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

import ch.cyberduck.core.serializer.HostWriterFactory;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @version $Id$
 */
public class FolderBookmarkCollection extends AbstractFolderHostCollection {
    private static Logger log = Logger.getLogger(FolderBookmarkCollection.class);

    private static final FolderBookmarkCollection FAVORITES_COLLECTION = new FolderBookmarkCollection(
            LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "Bookmarks")
    );

    private static final String PREFIX = "bookmark.";

    private static final long serialVersionUID = -675342412129904735L;

    /**
     * @return Singleton instance
     */
    public static FolderBookmarkCollection favoritesCollection() {
        return FAVORITES_COLLECTION;
    }

    /**
     * Reading bookmarks from this folder
     *
     * @param f Parent directory to look for bookmarks
     */
    public FolderBookmarkCollection(Local f) {
        super(f);
    }

    @Override
    public void collectionItemAdded(Host bookmark) {
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        this.save(bookmark);
        this.index();
        super.collectionItemAdded(bookmark);
    }

    protected void save(Host bookmark) {
        HostWriterFactory.instance().write(bookmark, this.getFile(bookmark));
    }

    @Override
    public boolean addAll(java.util.Collection<? extends Host> c) {
        List<Host> temporary = new ArrayList<Host>();
        for(Host host : c) {
            if(temporary.contains(host)) {
                log.warn(String.format("Reset UUID of duplicate in collection for %s", host));
                host.setUuid(null);
            }
            temporary.add(host);
        }
        return super.addAll(temporary);
    }

    /**
     * Update index of bookmark positions
     */
    protected void index() {
        for(int i = 0; i < this.size(); i++) {
            Preferences.instance().setProperty(PREFIX + this.get(i).getUuid(), i);
        }
    }

    @Override
    public void save() {
        this.index();
    }

    /**
     * Importer for legacy bookmarks.
     *
     * @param c Existing collection
     */
    @Override
    public void load(Collection<Host> c) {
        super.load(c);
        // Create index for imported collection
        this.index();
        this.sort();
        for(Host bookmark : this) {
            this.save(bookmark);
        }
        this.collectionLoaded();
    }

    @Override
    protected synchronized void sort() {
        Collections.sort(this, new Comparator<Host>() {
            @Override
            public int compare(Host o1, Host o2) {
                return Integer.valueOf(Preferences.instance().getInteger(PREFIX + o1.getUuid())).compareTo(
                        Preferences.instance().getInteger(PREFIX + o2.getUuid())
                );
            }
        });
    }
}
