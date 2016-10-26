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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FolderBookmarkCollection extends AbstractFolderHostCollection {
    private static final Logger log = Logger.getLogger(FolderBookmarkCollection.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private static final FolderBookmarkCollection FAVORITES_COLLECTION = new FolderBookmarkCollection(
            LocalFactory.get(PreferencesFactory.get().getProperty("application.support.path"), "Bookmarks")
    ) {
        private static final long serialVersionUID = 6302021296403107371L;

        @Override
        public void collectionItemAdded(final Host bookmark) {
            bookmark.setWorkdir(null);
            super.collectionItemAdded(bookmark);
        }
    };

    private static final String DEFAULT_PREFIX = "bookmark";

    private static final long serialVersionUID = -675342412129904735L;

    private final String prefix;

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
    public FolderBookmarkCollection(final Local f) {
        this(f, DEFAULT_PREFIX);
    }

    public FolderBookmarkCollection(final Local f, final String prefix) {
        super(f);
        this.prefix = String.format("%s.", prefix);
    }

    @Override
    public void collectionItemAdded(final Host bookmark) {
        try {
            if(this.isLocked()) {
                log.debug("Skip indexing collection while loading");
            }
            else {
                this.index();
            }
        }
        finally {
            super.collectionItemAdded(bookmark);
        }
    }

    @Override
    public void collectionItemRemoved(final Host bookmark) {
        try {
            preferences.deleteProperty(String.format("%s%s", prefix, bookmark.getUuid()));
        }
        finally {
            super.collectionItemRemoved(bookmark);
        }
    }

    @Override
    public boolean addAll(java.util.Collection<? extends Host> c) {
        final List<Host> temporary = new ArrayList<Host>();
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
    private void index() {
        this.lock();
        try {
            for(int i = 0; i < this.size(); i++) {
                preferences.setProperty(String.format("%s%s", prefix, this.get(i).getUuid()), i);
            }
        }
        finally {
            this.unlock();
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
    protected void load(final Collection<Host> c) {
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
                return Integer.valueOf(preferences.getInteger(String.format("%s%s", prefix, o1.getUuid()))).compareTo(
                        preferences.getInteger(String.format("%s%s", prefix, o2.getUuid()))
                );
            }
        });
    }
}
