package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.formatter.SizeFormatter;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.core.serializer.Writer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferProgress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class TransferCollection extends Collection<Transfer> {
    private static final Logger log = LogManager.getLogger(TransferCollection.class);

    private static final TransferCollection TRANSFER_COLLECTION = new TransferCollection(
        LocalFactory.get(SupportDirectoryFinderFactory.get().find(), "Transfers"));

    /**
     * @return Singleton instance
     */
    public static TransferCollection defaultCollection() {
        return TRANSFER_COLLECTION;
    }

    private static final String DEFAULT_PREFIX = "transfer";

    private final Writer<Transfer> writer = TransferWriterFactory.get();
    private final Reader<Transfer> reader = TransferReaderFactory.get();

    private final String prefix;

    /**
     * Formatter for file size
     */
    private final SizeFormatter sizeFormatter = SizeFormatterFactory.get();

    private final Preferences preferences
        = PreferencesFactory.get();

    private final Local folder;

    public TransferCollection(final Local folder) {
        this(folder, DEFAULT_PREFIX);
    }

    public TransferCollection(final Local folder, final String prefix) {
        this.folder = folder;
        this.prefix = String.format("%s.", prefix);
    }

    /**
     * @param transfer Transfer
     * @return File for transfer
     */
    public Local getFile(final Transfer transfer) {
        return LocalFactory.get(folder, String.format("%s.cyberducktransfer", transfer.getUuid()));
    }

    public Local getFolder() {
        return folder;
    }

    @Override
    public void collectionItemRemoved(final Transfer transfer) {
        try {
            this.getFile(transfer).delete();
            preferences.deleteProperty(String.format("%s%s", prefix, transfer.getUuid()));
        }
        catch(AccessDeniedException | NotfoundException e) {
            log.error(String.format("Failure removing transfer %s", e.getMessage()));
        }
        finally {
            super.collectionItemRemoved(transfer);
        }
    }

    @Override
    public void collectionItemChanged(final Transfer transfer) {
        try {
            this.save(transfer);
        }
        finally {
            super.collectionItemChanged(transfer);
        }
    }

    @Override
    public void collectionItemAdded(final Transfer transfer) {
        try {
            this.save(transfer);
            if(this.isLocked()) {
                log.debug("Skip indexing collection while loading");
            }
            else {
                this.index();
            }
        }
        finally {
            super.collectionItemAdded(transfer);
        }
    }

    protected void save(final Transfer transfer) {
        if(this.isLocked()) {
            log.debug(String.format("Skip saving transfer %s while loading", transfer));
        }
        else {
            this.lock();
            try {
                if(!folder.exists()) {
                    new DefaultLocalDirectoryFeature().mkdir(folder);
                }
                final Local f = this.getFile(transfer);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Save transfer %s", f));
                }
                writer.write(transfer, f);
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
            final AttributedList<Local> transfers = folder.list().filter(
                new Filter<Local>() {
                    @Override
                    public boolean accept(final Local file) {
                        return file.getName().endsWith(".cyberducktransfer");
                    }

                    @Override
                    public Pattern toPattern() {
                        return Pattern.compile(".*\\.cyberducktransfer");
                    }
                }
            );
            for(Local f : transfers) {
                try {
                    final Transfer transfer = reader.read(f);
                    // Legacy support.
                    if(!this.getFile(transfer).equals(f)) {
                        this.rename(f, transfer);
                    }
                    this.add(transfer);
                }
                catch(AccessDeniedException e) {
                    log.error(String.format("Failure reading transfer from %s. %s", f, e.getMessage()));
                }
            }
            // Sort using previously built index
            this.sort();
        }
        finally {
            this.unlock();
        }
        super.load();
    }

    protected void rename(final Local next, final Transfer transfer) throws AccessDeniedException {
        // Rename all files previously saved with nickname to UUID.
        next.rename(this.getFile(transfer));
    }

    @Override
    public boolean addAll(java.util.Collection<? extends Transfer> c) {
        final List<Transfer> temporary = new ArrayList<>();
        for(Transfer transfer : c) {
            if(temporary.contains(transfer)) {
                log.warn(String.format("Reset UUID of duplicate in collection for %s", transfer));
                transfer.setUuid(new UUIDRandomStringService().random());
            }
            temporary.add(transfer);
        }
        return super.addAll(temporary);
    }

    /**
     * Update index of bookmark positions
     */
    private void index() {
        IntStream.range(0, this.size()).forEach(i -> preferences.setProperty(String.format("%s%s", prefix, this.get(i).getUuid()), i));
    }

    public void save() {
        try {
            this.index();
        }
        finally {
            super.save();
        }
    }

    /**
     * Importer for legacy transfers.
     *
     * @param c Existing collection
     */
    protected void load(final Collection<Transfer> c) {
        this.addAll(c);
        // Create index for imported collection
        this.index();
        this.sort();
        for(Transfer transfer : this) {
            this.save(transfer);
        }
        this.collectionLoaded();
    }

    /**
     * Ordering using persisted indexes in preferences
     */
    protected void sort() {
        this.sort(new Comparator<Transfer>() {
            @Override
            public int compare(Transfer o1, Transfer o2) {
                return Integer.compare(preferences.getInteger(String.format("%s%s", prefix, o1.getUuid())), preferences.getInteger(String.format("%s%s", prefix, o2.getUuid())));
            }
        });
    }

    /**
     * @return Number of transfers in collection that are running
     * @see ch.cyberduck.core.transfer.Transfer#isRunning()
     */
    public synchronized int numberOfRunningTransfers() {
        int running = 0;
        // Count the number of running transfers
        for(Transfer t : this) {
            if(t.isRunning()) {
                running++;
            }
        }
        return running;
    }

    public synchronized TransferProgress getProgress() {
        long size = 0;
        for(Transfer t : this) {
            if(t.isRunning()) {
                size += t.getSize();
            }
        }
        long transferred = 0;
        for(Transfer t : this) {
            if(t.isRunning()) {
                transferred += t.getTransferred();
            }
        }
        return new TransferProgress(size, transferred, MessageFormat.format(LocaleFactory.localizedString("{0} of {1}"),
            sizeFormatter.format(transferred),
            sizeFormatter.format(size)), -1d);
    }
}
