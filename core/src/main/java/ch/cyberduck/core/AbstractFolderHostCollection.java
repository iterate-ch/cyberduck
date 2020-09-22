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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.serializer.Writer;
import ch.cyberduck.core.text.DefaultLexicographicOrderComparator;

import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.regex.Pattern;

public abstract class AbstractFolderHostCollection extends AbstractHostCollection implements FileWatcherListener {
    private static final Logger log = Logger.getLogger(AbstractFolderHostCollection.class);

    public static final Comparator<Host> SORT_BY_NICKNAME = new Comparator<Host>() {
        @Override
        public int compare(Host o1, Host o2) {
            return new DefaultLexicographicOrderComparator().compare(
                BookmarkNameProvider.toString(o1), BookmarkNameProvider.toString(o2)
            );
        }
    };

    public static final Comparator<Host> SORT_BY_HOSTNAME = new Comparator<Host>() {
        @Override
        public int compare(Host o1, Host o2) {
            return new DefaultLexicographicOrderComparator().compare(o1.getHostname(), o2.getHostname());
        }
    };

    public static final Comparator<Host> SORT_BY_PROTOCOL = new Comparator<Host>() {
        @Override
        public int compare(Host o1, Host o2) {
            return new DefaultLexicographicOrderComparator().compare(o1.getProtocol().getIdentifier(), o2.getProtocol().getIdentifier());
        }
    };

    private final Preferences preferences = PreferencesFactory.get();

    private final Writer<Host> writer = HostWriterFactory.get();
    private final Reader<Host> reader = HostReaderFactory.get();

    protected final Local folder;

    protected static final Filter<Local> FILE_FILTER = new Filter<Local>() {
        @Override
        public boolean accept(final Local file) {
            return file.getName().endsWith(".duck");
        }

        @Override
        public Pattern toPattern() {
            return Pattern.compile(".*\\.duck");
        }
    };

    /**
     * Reading bookmarks from this folder
     *
     * @param f Parent directory to look for bookmarks
     */
    public AbstractFolderHostCollection(final Local f) {
        this.folder = f;
    }

    public String getName() {
        return LocaleFactory.localizedString(folder.getName());
    }

    /**
     * @param bookmark Bookmark
     * @return File for bookmark
     */
    public Local getFile(final Host bookmark) {
        return LocalFactory.get(folder, String.format("%s.duck", bookmark.getUuid()));
    }

    public Local getFolder() {
        return folder;
    }

    @Override
    public void sort(final Comparator<? super Host> c) {
        if(this.isLocked()) {
            log.debug("Skip sorting bookmark collection while loading");
        }
        else {
            super.sort(c);
            // Save new index
            this.save();
        }
    }

    protected void save(final Host bookmark) {
        try {
            if(!folder.exists()) {
                new DefaultLocalDirectoryFeature().mkdir(folder);
            }
            final Local f = this.getFile(bookmark);
            if(log.isInfoEnabled()) {
                log.info(String.format("Save bookmark %s to %s", bookmark, f));
            }
            writer.write(bookmark, f);
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Failure saving item in collection %s", e.getMessage()));
        }
    }

    @Override
    public void load() throws AccessDeniedException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Reloading %s", folder.getAbsolute()));
        }
        this.lock();
        try {
            if(!folder.exists()) {
                new DefaultLocalDirectoryFeature().mkdir(folder);
            }
            final AttributedList<Local> bookmarks = folder.list().filter(FILE_FILTER);
            for(Local f : bookmarks) {
                try {
                    this.add(reader.read(f));
                }
                catch(AccessDeniedException e) {
                    log.error(String.format("Failure %s reading bookmark from %s", e, f));
                }
            }
        }
        finally {
            this.unlock();
        }
        // Sort using previously built index
        this.sort();
        // Mark collection as loaded and notify listeners.
        super.load();
    }

    @Override
    public void collectionItemAdded(final Host bookmark) {
        if(!this.isLocked()) {
            this.save(bookmark);
            this.sort();
        }
        // Notify listeners
        super.collectionItemAdded(bookmark);
    }

    @Override
    public void collectionItemRemoved(final Host bookmark) {
        if(!this.isLocked()) {
            final Local file = this.getFile(bookmark);
            try {
                file.delete();
            }
            catch(AccessDeniedException | NotfoundException e) {
                log.warn(String.format("Failure removing bookmark %s", e.getMessage()));
            }
            this.sort();
        }
        // Notify listeners
        super.collectionItemRemoved(bookmark);
    }

    @Override
    public void collectionItemChanged(final Host bookmark) {
        if(!this.isLocked()) {
            this.save(bookmark);
        }
        super.collectionItemChanged(bookmark);
    }
}
