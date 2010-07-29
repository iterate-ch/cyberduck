package ch.cyberduck.core;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

/**
 * @version $Id: BookmarkCollection.java 6244 2010-07-04 06:39:24Z dkocher $
 */
public class BookmarkCollection extends AbstractHostCollection {
    private static Logger log = Logger.getLogger(BookmarkCollection.class);

    /**
     * Default bookmark file
     */
    private static BookmarkCollection DEFAULT_COLLECTION = new BookmarkCollection(
            LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "Favorites.plist")
    );

    /**
     * @return
     */
    public static BookmarkCollection defaultCollection() {
        return DEFAULT_COLLECTION;
    }

    /**
     * @param file
     */
    public BookmarkCollection(Local file) {
        this.setFile(file);
    }

    /**
     * The file to persist this collection in
     */
    protected Local file;

    /**
     * Will create the parent directory if missing
     *
     * @param file
     */
    protected void setFile(Local file) {
        this.file = file;
        this.file.getParent().mkdir(true);
    }

    /**
     * @return
     */
    public Local getFile() {
        return this.file;
    }

    @Override
    public Host get(int row) {
        return super.get(row);
    }

    /**
     * @param host
     * @return
     * @see Host
     */
    @Override
    public boolean add(Host host) {
        this.add(this.size(), host);
        return true;
    }

    /**
     * @param row
     * @param host
     * @see Host
     */
    @Override
    public void add(int row, Host host) {
        super.add(row, host);
        this.sort();
        this.save();
    }

    /**
     * @param row
     * @return the element that was removed from the list.
     */
    @Override
    public Host remove(int row) {
        final Host previous = super.remove(row);
        this.save();
        return previous;
    }

    @Override
    public boolean remove(Object host) {
        final boolean found = super.remove(host);
        this.save();
        return found;
    }

    protected void sort() {
        //
    }

    /**
     * Saves this collection of bookmarks in to a file to the users's application support directory
     * in a plist xml format
     */
    @Override
    public void save() {
        if(Preferences.instance().getBoolean("favorites.save")) {
            log.info("Saving Bookmarks file: " + file.getAbsolute());
            HostWriterFactory.instance().write(this, file);
        }
    }

    /**
     * Deserialize all the bookmarks saved previously in the users's application support directory
     */
    @Override
    public void load() {
        if(file.exists()) {
            log.info("Found Bookmarks file: " + file.getAbsolute());
            this.addAll(HostReaderFactory.instance().readCollection(file));
            this.sort();
        }
    }
}