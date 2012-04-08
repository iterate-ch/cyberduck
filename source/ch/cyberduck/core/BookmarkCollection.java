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
 * @version $Id$
 */
public class BookmarkCollection extends AbstractHostCollection {
    private static Logger log = Logger.getLogger(BookmarkCollection.class);

    /**
     * Legacy default bookmark file
     */
    private static final BookmarkCollection DEFAULT_COLLECTION = new BookmarkCollection(
            LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "Favorites.plist")
    );

    /**
     * @return Singleton instance
     */
    public static BookmarkCollection defaultCollection() {
        return DEFAULT_COLLECTION;
    }

    /**
     * The file to persist this collection in
     */
    private Local file;

    @Override
    public String getName() {
        return Locale.localizedString("Favorites");
    }

    /**
     * @param file Favorites Plist
     */
    public BookmarkCollection(Local file) {
        this.file = file;
        this.file.getParent().mkdir(true);
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

    @Override
    public void save() {
        if(this.isLocked()) {
            log.debug("Do not write locked collection");
            return;
        }
        FolderBookmarkCollection.favoritesCollection().save();
    }

    /**
     * Migrate the deprecated bookmarks file to the new format.
     */
    @Override
    public void load() {
        this.lock();
        try {
            FolderBookmarkCollection favorites = FolderBookmarkCollection.favoritesCollection();
            if(file.exists()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Found Bookmarks file %s", file.getAbsolute()));
                }
                favorites.load(HostReaderFactory.instance().readCollection(file));
                log.info("Moving deprecated bookmarks file to Trash");
                file.delete(true);
            }
            else {
                favorites.load();
            }
        }
        finally {
            this.unlock();
        }
        super.load();
    }
}