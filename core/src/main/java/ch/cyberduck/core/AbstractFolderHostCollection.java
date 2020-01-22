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
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.watchservice.WatchServiceFactory;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.serializer.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

public abstract class AbstractFolderHostCollection extends AbstractHostCollection implements FileWatcherListener {
    private static final Logger log = Logger.getLogger(AbstractFolderHostCollection.class);

    private static final long serialVersionUID = 6598370606581477494L;

    private final Writer<Host> writer = HostWriterFactory.get();
    private final Reader<Host> reader = HostReaderFactory.get();

    protected final Local folder;

    private final FileWatcher monitor
        = new FileWatcher(WatchServiceFactory.get());

    private static final Filter<Local> filter = new Filter<Local>() {
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

    @Override
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
    public boolean addAll(final Collection<? extends Host> c) {
        return super.addAll(c);
    }

    @Override
    public boolean add(final Host bookmark) {
        if(super.add(bookmark)) {
            this.save(bookmark);
            return true;
        }
        return false;
    }

    @Override
    public void add(final int row, final Host bookmark) {
        super.add(row, bookmark);
        this.save(bookmark);
    }

    @Override
    public void collectionItemChanged(final Host bookmark) {
        this.save(bookmark);
        super.collectionItemChanged(bookmark);
    }

    @Override
    public void collectionItemRemoved(final Host bookmark) {
        try {
            this.lock();
            final Local file = this.getFile(bookmark);
            file.delete();
        }
        catch(AccessDeniedException | NotfoundException e) {
            log.error(String.format("Failure removing bookmark %s", e.getMessage()));
        }
        finally {
            this.unlock();
            super.collectionItemRemoved(bookmark);
        }
    }

    protected void save(final Host bookmark) {
        if(this.isLocked()) {
            log.debug(String.format("Skip saving bookmark %s while loading", bookmark));
        }
        else {
            this.lock();
            try {
                if(!folder.exists()) {
                    new DefaultLocalDirectoryFeature().mkdir(folder);
                }
                final Local f = this.getFile(bookmark);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Save bookmark %s", f));
                }
                writer.write(bookmark, f);
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure saving item in collection %s", e.getMessage()));
            }
            finally {
                this.unlock();
            }
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
            final AttributedList<Local> bookmarks = folder.list().filter(filter);
            for(Local f : bookmarks) {
                try {
                    this.add(reader.read(f));
                }
                catch(AccessDeniedException e) {
                    log.error(String.format("Failure reading bookmark from %s. %s", f, e.getMessage()));
                }
            }
            // Sort using previously built index
            this.sort();
        }
        finally {
            this.unlock();
        }
        super.load();
        try {
            monitor.register(folder, filter, this);
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Failure monitoring directory %s", folder.getName()), e);
        }
    }

    @Override
    public void fileWritten(final Local file) {
        if(this.isLocked()) {
            log.debug(String.format("Skip reading bookmark from %s", file));
        }
        else {
            try {
                // Read from disk and re-insert to collection
                final Host bookmark = HostReaderFactory.get().read(file);
                final int index = this.indexOf(bookmark);
                if(index != -1) {
                    super.remove(bookmark);
                    super.add(bookmark);
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading file %s", file));
            }
        }
    }

    @Override
    public void fileDeleted(final Local file) {
        if(this.isLocked()) {
            log.debug(String.format("Skip reading bookmark from %s", file));
        }
        else {
            final Host bookmark = this.lookup(FilenameUtils.getBaseName(file.getName()));
            if(bookmark != null) {
                super.remove(bookmark);
            }
        }
    }

    @Override
    public void fileCreated(final Local file) {
        if(this.isLocked()) {
            log.debug(String.format("Skip reading bookmark from %s", file));
        }
        else {
            try {
                final Host bookmark = HostReaderFactory.get().read(file);
                super.add(bookmark);
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading file %s", file));
            }
        }
    }

    @Override
    public void save() {
        // Save individual bookmarks upon add but not collection itself.
    }
}
