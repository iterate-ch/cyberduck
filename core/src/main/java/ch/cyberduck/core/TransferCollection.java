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
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferProgress;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class TransferCollection extends Collection<Transfer> {
    private static final Logger log = Logger.getLogger(TransferCollection.class);

    private static final long serialVersionUID = -6879481152545265228L;

    private static final TransferCollection DEFAULT_COLLECTION = new TransferCollection(
            LocalFactory.get(PreferencesFactory.get().getProperty("application.support.path"), "Queue.plist")
    );

    public static TransferCollection defaultCollection() {
        return DEFAULT_COLLECTION;
    }

    private final Local file;

    private final Reader<Transfer> reader = TransferReaderFactory.get();

    protected TransferCollection(final Local file) {
        this.file = file;
    }

    @Override
    public int size() {
        return FolderTransferCollection.defaultCollection().size();
    }

    @Override
    public boolean isEmpty() {
        return FolderTransferCollection.defaultCollection().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return FolderTransferCollection.defaultCollection().contains(o);
    }

    @Override
    public Transfer get(int row) {
        return FolderTransferCollection.defaultCollection().get(row);
    }

    @Override
    public boolean addAll(java.util.Collection<? extends Transfer> hosts) {
        return FolderTransferCollection.defaultCollection().addAll(hosts);
    }

    @Override
    public boolean add(Transfer host) {
        return FolderTransferCollection.defaultCollection().add(host);
    }

    @Override
    public void add(int row, Transfer host) {
        FolderTransferCollection.defaultCollection().add(row, host);
    }

    @Override
    public Transfer remove(int row) {
        return FolderTransferCollection.defaultCollection().remove(row);
    }

    @Override
    public boolean remove(Object host) {
        return FolderTransferCollection.defaultCollection().remove(host);
    }

    @Override
    public int indexOf(Object elem) {
        return FolderTransferCollection.defaultCollection().indexOf(elem);
    }

    @Override
    public int lastIndexOf(Object elem) {
        return FolderTransferCollection.defaultCollection().lastIndexOf(elem);
    }

    @Override
    public void addListener(CollectionListener<Transfer> l) {
        FolderTransferCollection.defaultCollection().addListener(l);
    }

    @Override
    public void removeListener(CollectionListener<Transfer> l) {
        FolderTransferCollection.defaultCollection().removeListener(l);
    }

    @Override
    public void clear() {
        FolderTransferCollection.defaultCollection().clear();
    }

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        return FolderTransferCollection.defaultCollection().removeAll(c);
    }

    @Override
    public void collectionItemChanged(Transfer item) {
        FolderTransferCollection.defaultCollection().collectionItemChanged(item);
    }

    @Override
    public boolean addAll(int index, java.util.Collection<? extends Transfer> c) {
        return FolderTransferCollection.defaultCollection().addAll(index, c);
    }

    @Override
    public Iterator<Transfer> iterator() {
        return FolderTransferCollection.defaultCollection().iterator();
    }

    @Override
    public ListIterator<Transfer> listIterator() {
        return FolderTransferCollection.defaultCollection().listIterator();
    }

    @Override
    public ListIterator<Transfer> listIterator(int index) {
        return FolderTransferCollection.defaultCollection().listIterator(index);
    }

    @Override
    public List<Transfer> subList(int fromIndex, int toIndex) {
        return FolderTransferCollection.defaultCollection().subList(fromIndex, toIndex);
    }

    @Override
    public boolean containsAll(java.util.Collection<?> c) {
        return FolderTransferCollection.defaultCollection().containsAll(c);
    }

    @Override
    public Object[] toArray() {
        return FolderTransferCollection.defaultCollection().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return FolderTransferCollection.defaultCollection().toArray(a);
    }

    public void save() {
        if(this.isLocked()) {
            log.debug("Do not write locked collection");
            return;
        }
        FolderTransferCollection.defaultCollection().save();
    }


    /**
     * Migrate the deprecated queue file to the new format.
     */
    @Override
    public void load() throws AccessDeniedException {
        this.lock();
        try {
            final FolderTransferCollection favorites = FolderTransferCollection.defaultCollection();
            if(file.exists()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Found queue file %s", file));
                }
                favorites.load(reader.readCollection(file));
                this.trash();
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

    protected void trash() {
        if(log.isInfoEnabled()) {
            log.info("Moving deprecated queue file to Trash");
        }
        try {
            LocalTrashFactory.get().trash(file);
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Failure trashing bookmark %s %s", file, e.getMessage()));
        }
    }

    /**
     * @return Number of transfers in collection that are running
     * @see ch.cyberduck.core.transfer.Transfer#isRunning()
     */
    public synchronized int numberOfRunningTransfers() {
        return FolderTransferCollection.defaultCollection().numberOfRunningTransfers();
    }

    public synchronized TransferProgress getProgress() {
        return FolderTransferCollection.defaultCollection().getProgress();
    }
}