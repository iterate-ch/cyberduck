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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.serializer.HostReaderFactory;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

    @Override
    public String getName() {
        return Locale.localizedString("Favorites");
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
    public boolean allowsAdd() {
        return FolderBookmarkCollection.favoritesCollection().allowsAdd();
    }

    @Override
    public boolean allowsDelete() {
        return FolderBookmarkCollection.favoritesCollection().allowsDelete();
    }

    @Override
    public boolean allowsEdit() {
        return FolderBookmarkCollection.favoritesCollection().allowsEdit();
    }

    @Override
    public Host lookup(String uuid) {
        return FolderBookmarkCollection.favoritesCollection().lookup(uuid);
    }

    @Override
    public int size() {
        return FolderBookmarkCollection.favoritesCollection().size();
    }

    @Override
    public boolean isEmpty() {
        return FolderBookmarkCollection.favoritesCollection().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return FolderBookmarkCollection.favoritesCollection().contains(o);
    }

    @Override
    public Host get(int row) {
        return FolderBookmarkCollection.favoritesCollection().get(row);
    }

    @Override
    public boolean addAll(Collection<? extends Host> hosts) {
        return FolderBookmarkCollection.favoritesCollection().addAll(hosts);
    }

    @Override
    public boolean add(Host host) {
        return FolderBookmarkCollection.favoritesCollection().add(host);
    }

    @Override
    public void add(int row, Host host) {
        FolderBookmarkCollection.favoritesCollection().add(row, host);
    }

    @Override
    public Host remove(int row) {
        return FolderBookmarkCollection.favoritesCollection().remove(row);
    }

    @Override
    public boolean remove(Object host) {
        return FolderBookmarkCollection.favoritesCollection().remove(host);
    }

    @Override
    protected void sort() {
        FolderBookmarkCollection.favoritesCollection().sort();
    }

    @Override
    public int indexOf(Object elem) {
        return FolderBookmarkCollection.favoritesCollection().indexOf(elem);
    }

    @Override
    public int lastIndexOf(Object elem) {
        return FolderBookmarkCollection.favoritesCollection().lastIndexOf(elem);
    }

    @Override
    public void addListener(CollectionListener<Host> l) {
        FolderBookmarkCollection.favoritesCollection().addListener(l);
    }

    @Override
    public void removeListener(CollectionListener<Host> l) {
        FolderBookmarkCollection.favoritesCollection().removeListener(l);
    }

    @Override
    public void clear() {
        FolderBookmarkCollection.favoritesCollection().clear();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return FolderBookmarkCollection.favoritesCollection().removeAll(c);
    }

    @Override
    public void collectionItemChanged(Host item) {
        FolderBookmarkCollection.favoritesCollection().collectionItemChanged(item);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Host> c) {
        return FolderBookmarkCollection.favoritesCollection().addAll(index, c);
    }

    @Override
    public Iterator<Host> iterator() {
        return FolderBookmarkCollection.favoritesCollection().iterator();
    }

    @Override
    public ListIterator<Host> listIterator() {
        return FolderBookmarkCollection.favoritesCollection().listIterator();
    }

    @Override
    public ListIterator<Host> listIterator(int index) {
        return FolderBookmarkCollection.favoritesCollection().listIterator(index);
    }

    @Override
    public List<Host> subList(int fromIndex, int toIndex) {
        return FolderBookmarkCollection.favoritesCollection().subList(fromIndex, toIndex);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return FolderBookmarkCollection.favoritesCollection().containsAll(c);
    }

    private boolean locked = true;

    @Override
    public boolean isLocked() {
        return locked;
    }

    /**
     * Saves this collection of bookmarks in to a file to the users's application support directory
     * in a plist xml format
     */
    @Override
    public void save() {
        if(locked) {
            log.debug("Do not write locked collection");
            return;
        }
        FolderBookmarkCollection.favoritesCollection().save();
    }

    /**
     * Deserialize all the bookmarks saved previously in the users's application support directory
     */
    @Override
    public void load() {
        FolderBookmarkCollection favorites = FolderBookmarkCollection.favoritesCollection();
        if(file.exists()) {
            log.info("Found Bookmarks file: " + file.getAbsolute());
            favorites.load(HostReaderFactory.instance().readCollection(file));
            log.info("Moving deprecated bookmarks file to Trash");
            file.delete(true);
        }
        else {
            favorites.load();
        }
        locked = false;
    }
}