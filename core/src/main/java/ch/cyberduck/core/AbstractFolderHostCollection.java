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
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.serializer.Writer;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;

public abstract class AbstractFolderHostCollection extends AbstractHostCollection {
    private static final Logger log = Logger.getLogger(AbstractFolderHostCollection.class);

    private static final long serialVersionUID = 6598370606581477494L;

    private final Writer<Host> writer = HostWriterFactory.get();

    private final Reader<Host> reader = HostReaderFactory.get();

    protected final Local folder;

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
    public void collectionItemAdded(final Host bookmark) {
        this.save(bookmark);
        super.collectionItemAdded(bookmark);
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
        catch(AccessDeniedException e) {
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
                folder.mkdir();
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
            folder.mkdir();
            final AttributedList<Local> bookmarks = folder.list().filter(
                    new Filter<Local>() {
                        @Override
                        public boolean accept(final Local file) {
                            return file.getName().endsWith(".duck");
                        }

                        @Override
                        public Pattern toPattern() {
                            return Pattern.compile(".*\\.duck");
                        }

                    }
            );
            for(Local next : bookmarks) {
                final Host bookmark = reader.read(next);
                if(null == bookmark) {
                    continue;
                }
                this.add(bookmark);
            }
            // Sort using previously built index
            this.sort();
        }
        finally {
            this.unlock();
        }
        super.load();
    }

    @Override
    public void save() {
        // Save individual bookmarks upon add but not collection itself.
    }
}
