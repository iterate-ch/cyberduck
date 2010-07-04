package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.cyberduck.core.serializer.HostReaderFactory;
import ch.cyberduck.core.serializer.HostWriterFactory;
import ch.cyberduck.core.serializer.Reader;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;

/**
 * @version $Id$
 */
public class HistoryCollection extends BookmarkCollection {
    private static Logger log = Logger.getLogger(HistoryCollection.class);

    private static HistoryCollection HISTORY_COLLECTION = new HistoryCollection(
            LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "History")
    );

    /**
     * @return
     */
    public static HistoryCollection defaultCollection() {
        return HISTORY_COLLECTION;
    }

    /**
     * Reading bookmarks from this folder
     *
     * @param folder Parent directory to look for bookmarks
     */
    public HistoryCollection(Local folder) {
        super(folder);
    }

    @Override
    protected void setFile(Local folder) {
        super.setFile(folder);
        folder.mkdir(true);
    }

    /**
     * @param bookmark
     * @return
     */
    public Local getFile(Host bookmark) {
        return LocalFactory.createLocal(file, bookmark.getNickname() + ".duck");
    }

    @Override
    public void add(int row, Host bookmark) {
        HostWriterFactory.instance().write(bookmark, this.getFile(bookmark));
        if(!this.contains(bookmark)) {
            super.add(row, bookmark);
        }
        else {
            this.sort();
        }
    }

    /**
     * @param row
     * @return the element that was removed from the list.
     */
    @Override
    public Host remove(int row) {
        this.getFile(this.get(row)).delete(false);
        return super.remove(row);
    }

    @Override
    public void load() {
        log.info("Reloading " + file);
        final AttributedList<Local> bookmarks = file.childs(
                new PathFilter<Local>() {
                    public boolean accept(Local file) {
                        return file.getName().endsWith(".duck");
                    }
                }
        );
        final Reader<Host> reader = HostReaderFactory.instance();
        for(Local next : bookmarks) {
            super.add(this.size(), reader.read(next));
        }
    }

    @Override
    protected void sort() {
        Collections.sort(this, new Comparator<Host>() {
            public int compare(Host o1, Host o2) {
                Local f1 = getFile(o1);
                Local f2 = getFile(o2);
                if(f1.attributes().getModificationDate() < f2.attributes().getModificationDate()) {
                    return 1;
                }
                if(f1.attributes().getModificationDate() > f2.attributes().getModificationDate()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    @Override
    public void clear() {
        log.debug("Removing all bookmarks from " + file);
        for(Host next : this) {
            this.getFile(next).delete(false);
        }
        super.clear();
    }

    @Override
    public void save() {
        // Do not save collection
    }

    @Override
    protected Host unique(Host bookmark) {
        return bookmark;
    }

    @Override
    public boolean allowsAdd() {
        return false;
    }

    @Override
    public boolean allowsEdit() {
        return false;
    }
}