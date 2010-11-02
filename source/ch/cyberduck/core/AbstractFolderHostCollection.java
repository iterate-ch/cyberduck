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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.serializer.HostReaderFactory;
import ch.cyberduck.core.serializer.HostWriterFactory;
import ch.cyberduck.core.serializer.Reader;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class AbstractFolderHostCollection extends AbstractHostCollection {
    private static Logger log = Logger.getLogger(AbstractFolderHostCollection.class);

    protected Local folder;

    /**
     * Reading bookmarks from this folder
     *
     * @param f Parent directory to look for bookmarks
     */
    public AbstractFolderHostCollection(Local f) {
        this.folder = f;
        this.folder.mkdir(true);
    }

    @Override
    public String getName() {
        return Locale.localizedString(folder.getName());
    }

    public void open() {
        folder.open();
    }

    /**
     * @param bookmark
     * @return
     */
    public Local getFile(Host bookmark) {
        return LocalFactory.createLocal(folder, bookmark.getUuid() + ".duck");
    }

    @Override
    public void collectionItemAdded(Host bookmark) {
        if(locked) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        HostWriterFactory.instance().write(bookmark, this.getFile(bookmark));
        super.collectionItemAdded(bookmark);
    }

    @Override
    public void collectionItemRemoved(Host bookmark) {
        if(locked) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        this.getFile(bookmark).delete(false);
        super.collectionItemRemoved(bookmark);
    }

    @Override
    public void collectionItemChanged(Host bookmark) {
        if(locked) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        HostWriterFactory.instance().write(bookmark, this.getFile(bookmark));
        super.collectionItemChanged(bookmark);
    }

    private boolean locked = true;

    /**
     * Locked while loading.
     *
     * @return
     */
    protected boolean isLocked() {
        return locked;
    }

    @Override
    public void load() {
        log.info("Reloading:" + folder);
        final AttributedList<Local> bookmarks = folder.children(
                new PathFilter<Local>() {
                    public boolean accept(Local file) {
                        return file.getName().endsWith(".duck");
                    }
                }
        );
        final Reader<Host> reader = HostReaderFactory.instance();
        for(Local next : bookmarks) {
            Host bookmark = reader.read(next);
            // Legacy support.
            if(!this.getFile(bookmark).equals(next)) {
                // Rename all files previously saved with nickname to UUID.
                next.rename(this.getFile(bookmark));
            }
            this.add(bookmark);
        }
        locked = false;
        // Sort using previously built index
        this.sort();
        this.collectionLoaded();
    }

    /**
     * Importer for legacy bookmarks.
     *
     * @param c
     */
    @Override
    public void load(Collection<Host> c) {
        super.load(c);
        locked = false;
    }

    @Override
    public void save() {
        ;// Save individual bookmarks upon add but not collection itself.
    }


}
