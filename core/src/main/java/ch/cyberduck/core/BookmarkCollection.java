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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BookmarkCollection extends AbstractHostCollection {
    private static final Logger log = Logger.getLogger(BookmarkCollection.class);

    private static final long serialVersionUID = -74831755267110254L;

    /**
     * Legacy default bookmark file
     */
    private static final BookmarkCollection DEFAULT_COLLECTION = new BookmarkCollection(
            LocalFactory.get(PreferencesFactory.get().getProperty("application.support.path"), "Favorites.plist")
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
    private final Local file;

    @Override
    public String getName() {
        return LocaleFactory.localizedString("Favorites");
    }

    /**
     * @param file Favorites Plist
     */
    public BookmarkCollection(final Local file) {
        this.file = file;
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
    public boolean contains(final Object o) {
        return FolderBookmarkCollection.favoritesCollection().contains(o);
    }

    @Override
    public Host get(final int row) {
        return FolderBookmarkCollection.favoritesCollection().get(row);
    }

    @Override
    public boolean addAll(final Collection<? extends Host> hosts) {
        return FolderBookmarkCollection.favoritesCollection().addAll(hosts);
    }

    @Override
    public boolean add(final Host host) {
        return FolderBookmarkCollection.favoritesCollection().add(host);
    }

    @Override
    public void add(final int row, final Host host) {
        FolderBookmarkCollection.favoritesCollection().add(row, host);
    }

    @Override
    public Host remove(final int row) {
        return FolderBookmarkCollection.favoritesCollection().remove(row);
    }

    @Override
    public boolean remove(final Object host) {
        return FolderBookmarkCollection.favoritesCollection().remove(host);
    }

    @Override
    protected void sort() {
        FolderBookmarkCollection.favoritesCollection().sort();
    }

    @Override
    public int indexOf(final Object elem) {
        return FolderBookmarkCollection.favoritesCollection().indexOf(elem);
    }

    @Override
    public int lastIndexOf(final Object elem) {
        return FolderBookmarkCollection.favoritesCollection().lastIndexOf(elem);
    }

    @Override
    public void addListener(final CollectionListener<Host> l) {
        FolderBookmarkCollection.favoritesCollection().addListener(l);
    }

    @Override
    public void removeListener(final CollectionListener<Host> l) {
        FolderBookmarkCollection.favoritesCollection().removeListener(l);
    }

    @Override
    public void clear() {
        FolderBookmarkCollection.favoritesCollection().clear();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return FolderBookmarkCollection.favoritesCollection().removeAll(c);
    }

    @Override
    public void collectionItemChanged(final Host item) {
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
    public ListIterator<Host> listIterator(final int index) {
        return FolderBookmarkCollection.favoritesCollection().listIterator(index);
    }

    @Override
    public List<Host> subList(final int fromIndex, final int toIndex) {
        return FolderBookmarkCollection.favoritesCollection().subList(fromIndex, toIndex);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return FolderBookmarkCollection.favoritesCollection().containsAll(c);
    }

    @Override
    public Object[] toArray() {
        return FolderBookmarkCollection.favoritesCollection().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return FolderBookmarkCollection.favoritesCollection().toArray(a);
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
    public void load() throws AccessDeniedException {
        this.lock();
        try {
            final FolderBookmarkCollection favorites = FolderBookmarkCollection.favoritesCollection();
            if(file.exists()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Found Bookmarks file %s", file.getAbsolute()));
                }
                favorites.load(HostReaderFactory.get().readCollection(file));
                if(log.isInfoEnabled()) {
                    log.info("Moving deprecated bookmarks file to Trash");
                }
                try {
                    LocalTrashFactory.get().trash(file);
                }
                catch(AccessDeniedException e) {
                    log.warn(String.format("Failure trashing bookmark %s %s", file, e.getMessage()));
                }
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