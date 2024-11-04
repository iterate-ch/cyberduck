package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.stream.IntStream;

public class BookmarkCollection extends MonitorFolderHostCollection {
    private static final Logger log = LogManager.getLogger(BookmarkCollection.class);

    private final Preferences preferences = PreferencesFactory.get();

    private static final BookmarkCollection FAVORITES_COLLECTION = new BookmarkCollection(
        LocalFactory.get(SupportDirectoryFinderFactory.get().find(), "Bookmarks")
    ) {
        @Override
        public void collectionItemRemoved(final Host bookmark) {
            final Local file = this.getFile(bookmark);
            try {
                LocalTrashFactory.get().trash(file);
            }
            catch(AccessDeniedException e) {
                log.warn("Failure removing bookmark {}", e.getMessage());
            }
            super.collectionItemRemoved(bookmark);
        }

        @Override
        public void collectionItemAdded(final Host bookmark) {
            bookmark.setWorkdir(null);
            super.collectionItemAdded(bookmark);
        }
    };

    private static String toProperty(final Host bookmark, final String prefix) {
        return String.format("%s%s", prefix, bookmark.getUuid());
    }

    private static final String DEFAULT_PREFIX = "bookmark";

    private final String prefix;

    /**
     * @return Singleton instance
     */
    public static BookmarkCollection defaultCollection() {
        return FAVORITES_COLLECTION;
    }

    /**
     * Reading bookmarks from this folder
     *
     * @param f Parent directory to look for bookmarks
     */
    public BookmarkCollection(final Local f) {
        this(f, DEFAULT_PREFIX);
    }

    public BookmarkCollection(final Local f, final String prefix) {
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
            if(this.isLocked()) {
                log.debug("Skip indexing collection while loading");
            }
            else {
                preferences.deleteProperty(toProperty(bookmark, prefix));
            }
        }
        finally {
            super.collectionItemRemoved(bookmark);
        }
    }

    /**
     * Update index of bookmark positions
     */
    private void index() {
        IntStream.range(0, this.size()).forEach(i -> preferences.setProperty(toProperty(this.get(i), prefix), i));
    }

    @Override
    public void save() {
        try {
            this.index();
        }
        finally {
            super.save();
        }
    }

    /**
     * Ordering using persisted indexes in preferences
     */
    @Override
    public void sort() {
        this.sort(new Comparator<Host>() {
            @Override
            public int compare(Host o1, Host o2) {
                return Integer.compare(preferences.getInteger(toProperty(o1, prefix)), preferences.getInteger(toProperty(o2, prefix)));
            }
        });
    }
}
