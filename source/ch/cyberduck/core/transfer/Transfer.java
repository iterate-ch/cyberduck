package ch.cyberduck.core.transfer;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class Transfer implements Serializable {
    private static final Logger log = Logger.getLogger(Transfer.class);

    /**
     * Files and folders initially selected to be part of this transfer
     */
    private List<Path> roots;

    /**
     * Transfer status determined by filters
     */
    private Map<Path, TransferStatus> table
            = new HashMap<Path, TransferStatus>();

    /**
     * The sum of the file length of all files in the <code>queue</code>
     */
    private long size = 0;

    /**
     * The number bytes already transferred of the files in the <code>queue</code>
     */
    private long transferred = 0;

    public abstract Type getType();

    public enum Type {
        download {
            @Override
            public boolean isReloadable() {
                return true;
            }
        },
        upload {
            @Override
            public boolean isReloadable() {
                return true;
            }
        },
        synchronisation {
            @Override
            public boolean isReloadable() {
                return true;
            }
        },
        copy {
            @Override
            public boolean isReloadable() {
                return true;
            }
        },
        move {
            @Override
            public boolean isReloadable() {
                return false;
            }
        };

        public abstract boolean isReloadable();

    }

    protected Session<?> session;

    /**
     * In Bytes per second
     */
    protected BandwidthThrottle bandwidth
            = new BandwidthThrottle(BandwidthThrottle.UNLIMITED);


    /**
     * The transfer has been reset
     */
    private boolean reset;

    /**
     * Last transferred timestamp
     */
    private Date timestamp;

    /**
     * Prefetched workload
     */
    private Cache cache = new Cache(Integer.MAX_VALUE);

    /**
     * Transfer state
     */
    private State state = State.stopped;

    private enum State {
        running,
        /**
         * The transfer has been canceled and should not continue any further processing
         */
        canceled,
        stopped
    }

    /**
     * @return True if in <code>running</code> state
     */
    public boolean isRunning() {
        return state == State.running;
    }

    /**
     * @return True if in <code>canceled</code> state
     */
    public boolean isCanceled() {
        return state == State.canceled;
    }

    /**
     * Create a transfer with a single root which can
     * be a plain file or a directory
     *
     * @param root File or directory
     */
    public Transfer(final Session session, final Path root, final BandwidthThrottle bandwidth) {
        this(session, new Collection<Path>(Collections.<Path>singletonList(root)), bandwidth);
    }

    /**
     * @param roots List of files to add to transfer
     */
    public Transfer(final Session session, final List<Path> roots, final BandwidthThrottle bandwidth) {
        this.session = session;
        this.roots = roots;
        this.bandwidth = bandwidth;
    }

    public <T> Transfer(final T serialized, final Session session, final BandwidthThrottle bandwidth) {
        this.session = session;
        this.bandwidth = bandwidth;
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        final List rootsObj = dict.listForKey("Roots");
        if(rootsObj != null) {
            roots = new ArrayList<Path>();
            for(Object rootDict : rootsObj) {
                final Path root = new Path(rootDict);
                roots.add(root);
            }
        }
        Object sizeObj = dict.stringForKey("Size");
        if(sizeObj != null) {
            size = (long) Double.parseDouble(sizeObj.toString());
        }
        Object timestampObj = dict.stringForKey("Timestamp");
        if(timestampObj != null) {
            timestamp = new Date(Long.parseLong(timestampObj.toString()));
        }
        Object currentObj = dict.stringForKey("Current");
        if(currentObj != null) {
            transferred = (long) Double.parseDouble(currentObj.toString());
        }
        Object bandwidthObj = dict.stringForKey("Bandwidth");
        if(bandwidthObj != null) {
            bandwidth.setRate(Float.parseFloat(bandwidthObj.toString()));
        }
    }

    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(String.valueOf(this.getType().ordinal()), "Kind");
        dict.setObjectForKey(session.getHost(), "Host");
        dict.setListForKey(roots, "Roots");
        dict.setStringForKey(String.valueOf(this.getSize()), "Size");
        dict.setStringForKey(String.valueOf(this.getTransferred()), "Current");
        if(timestamp != null) {
            dict.setStringForKey(String.valueOf(timestamp.getTime()), "Timestamp");
        }
        if(bandwidth != null) {
            dict.setStringForKey(String.valueOf(bandwidth.getRate()), "Bandwidth");
        }
        return dict.getSerialized();
    }

    /**
     * @param bytesPerSecond Maximum number of bytes to transfer by second
     */
    public void setBandwidth(final float bytesPerSecond) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Throttle bandwidth to %s bytes per second", bytesPerSecond));
        }
        bandwidth.setRate(bytesPerSecond);
    }

    public void setBandwidth(final BandwidthThrottle bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * @return Rate in bytes per second allowed for this transfer
     */
    public BandwidthThrottle getBandwidth() {
        return bandwidth;
    }

    /**
     * @return Time when transfer did end
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @return The first <code>root</code> added to this transfer
     */
    public Path getRoot() {
        return roots.get(0);
    }

    public String getRemote() {
        return session.getFeature(UrlProvider.class).toUrl(this.getRoot()).find(DescriptiveUrl.Type.provider).getUrl();
    }

    public String getLocal() {
        return this.getRoot().getLocal().toURL();
    }

    public List<Path> getRoots() {
        return roots;
    }

    public List<Session<?>> getSessions() {
        return new ArrayList<Session<?>>(Collections.singletonList(session));
    }

    public String getName() {
        if(roots.isEmpty()) {
            return LocaleFactory.localizedString("None");
        }
        final StringBuilder name = new StringBuilder();
        name.append(roots.get(0).getName());
        if(roots.size() > 1) {
            name.append("… (").append(roots.size()).append(")");
        }
        return name.toString();
    }

    /**
     * @return All child elements of root path items to transfer
     */
    public Cache cache() {
        return cache;
    }

    /**
     * @param prompt Callback
     * @param action Transfer action for duplicate files
     * @return Null if the filter could not be determined and the transfer should be canceled instead
     */
    public TransferPathFilter filter(TransferPrompt prompt, final TransferAction action) throws BackgroundException {
        if(action.equals(TransferAction.ACTION_CANCEL)) {
            throw new ConnectionCanceledException();
        }
        throw new ConnectionCanceledException(String.format("Unknown transfer action %s", action));
    }

    /**
     * @param resumeRequested Requested resume
     * @param reloadRequested Requested overwrite
     * @return Duplicate file strategy from preferences or user selection
     */
    public abstract TransferAction action(final boolean resumeRequested, final boolean reloadRequested);

    /**
     * Lookup the path by reference in the session cache
     *
     * @param r Key
     * @return Lookup from cache
     * @see ch.cyberduck.core.Cache#lookup(ch.cyberduck.core.PathReference)
     */
    public Path lookup(final PathReference r) {
        for(Path root : roots) {
            if(r.equals(root.getReference())) {
                return root;
            }
        }
        return cache.lookup(r);
    }

    /**
     * Returns the children of this path filtering it with the default regex filter
     *
     * @param parent The directory to list the children
     * @return A list of child items
     */
    public abstract AttributedList<Path> children(final Path parent) throws BackgroundException;

    /**
     * @param item File
     * @return False if unchecked in prompt to exclude from transfer
     */
    public boolean isSelected(final Path item) {
        if(table.containsKey(item)) {
            return table.get(item).isSelected();
        }
        return true;
    }

    public TransferStatus getStatus(final Path item) {
        return table.get(item);
    }

    /**
     * @param item File
     * @return True if file should not be transferred
     */
    public boolean isSkipped(final Path item) {
        return !this.getRegexFilter().accept(item);
    }

    /**
     * Select the path to be included in the transfer
     *
     * @param item     File
     * @param selected Selected files in transfer prompt
     */
    public void setSelected(final Path item, final boolean selected) {
        table.put(item, new TransferStatus().selected(selected));
    }

    public abstract Filter<Path> getRegexFilter();

    /**
     * @param file    File
     * @param filter  Filter to apply to exclude files from transfer
     * @param options Quarantine option
     */
    protected void transfer(final Path file, final TransferPathFilter filter,
                            final TransferOptions options, final TransferErrorCallback prompt) throws BackgroundException {
        final TransferStatus status = table.get(file);
        if(!status.isSelected()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Skip file %s with status %s", file, status));
            }
            return;
        }
        // Transfer
        try {
            this.transfer(file, options, status);
        }
        catch(BackgroundException e) {
            // Prompt to continue or abort
            if(prompt.prompt(e)) {
                // Continue
                log.warn(String.format("Ignore transfer failure %s", e));
            }
            else {
                throw e;
            }
        }
        if(file.attributes().isFile()) {
            // Post process of file.
            try {
                filter.complete(file, options, status, session);
            }
            catch(BackgroundException e) {
                log.warn(String.format("Ignore failure in completion filter for %s", file));
            }
        }
        if(file.attributes().isDirectory()) {
            for(Path child : cache.get(file.getReference())) {
                // Recursive
                this.transfer(child, filter, options, prompt);
                table.remove(child);
            }
            // Post process of directory.
            try {
                filter.complete(file, options, status, session);
            }
            catch(BackgroundException e) {
                log.warn(String.format("Ignore failure in completion filter for %s", file));
            }
            cache.remove(file.getReference());
        }
    }

    /**
     * The actual transfer implementation
     *
     * @param file    File
     * @param options Quarantine option
     * @param status  Transfer status
     */
    public abstract void transfer(Path file, TransferOptions options, TransferStatus status) throws BackgroundException;

    /**
     * To be called before any file is actually transferred
     *
     * @param file   File
     * @param filter Filter to apply to exclude files from transfer
     */
    public void prepare(final Path file, final TransferStatus parent, final TransferPathFilter filter) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer status of %s for transfer %s", file, this));
        }
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        if(this.isSelected(file)) {
            // Only prepare the path it will be actually transferred
            if(filter.accept(file, parent)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Accepted in %s transfer", file));
                }
                session.message(MessageFormat.format(LocaleFactory.localizedString("Prepare {0}", "Status"), file.getName()));
                final TransferStatus status = filter.prepare(file, parent);
                if(file.attributes().isDirectory()) {
                    // Call recursively for all children
                    final AttributedList<Path> children = this.children(file);
                    // Put into cache for later reference when transferring
                    cache.put(file.getReference(), children);
                    // Call recursively
                    for(Path child : children) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Find transfer status of %s for transfer %s", child, this));
                        }
                        this.prepare(child, status, filter);
                    }
                }
                this.save(file, status);
            }
            else {
                this.save(file, new TransferStatus().selected(false));
            }
        }
        else {
            log.info(String.format("Skip unchecked file %s for transfer %s", file, this));
        }
    }

    public void save(final Path file, final TransferStatus status) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Determined transfer status %s of %s for transfer %s", status, file, this));
        }
        // Add transfer length to total bytes
        size += status.getLength();
        // Add skipped bytes
        transferred += status.getCurrent();
        table.put(file, status);
    }

    /**
     * Clear all cached values
     *
     * @param options Transfer options
     */
    public void clear(final TransferOptions options) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Clear cache with options %s", options));
        }
        cache.clear();
        table.clear();
    }

    /**
     * @param prompt  Transfer prompt callback
     * @param options Transfer options
     */
    public void start(final TransferPrompt prompt, final TransferOptions options, final TransferErrorCallback error) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Start transfer with prompt %s and options %s", prompt, options));
        }
        state = State.running;
        try {
            // Determine the filter to match files against
            final TransferAction action = this.action(options.resumeRequested, options.reloadRequested);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Selected transfer action %s", action));
            }
            if(action.equals(TransferAction.ACTION_CANCEL)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Transfer canceled by user:%s", this));
                }
                throw new ConnectionCanceledException();
            }
            this.clear(options);
            // Get the transfer filter from the concrete transfer class. Will throw connection canceled failure if prompt is dismissed.
            final TransferPathFilter filter = this.filter(prompt, action);
            // Reset the cached size of the transfer and progress value
            this.reset();
            // Calculate information about the files in advance to give progress information
            for(Path next : roots) {
                this.prepare(next, new TransferStatus().exists(true), filter);
            }
            // Transfer all files sequentially
            for(Path next : roots) {
                this.transfer(next, filter, options, error);
            }
            this.clear(options);
        }
        finally {
            state = State.stopped;
            timestamp = new Date();
        }
    }

    /**
     * Marks all items in the queue as canceled. Canceled items will be
     * skipped when processed. If the transfer is already in a <code>canceled</code>
     * state, the underlying session's socket is interrupted to force exit.
     */
    public void cancel() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel transfer %s", this));
        }
        state = State.canceled;
        for(TransferStatus s : table.values()) {
            s.setCanceled();
        }
    }

    /**
     * Recalculate the size of the <code>queue</code>
     */
    public void reset() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reset status for %s", this));
        }
        transferred = 0;
        size = 0;
        reset = true;
    }

    /**
     * @return True if transfer has been reset before starting.
     */
    public boolean isReset() {
        return reset;
    }

    /**
     * @return True if the bytes transferred equal the size of the queue
     */
    public boolean isComplete() {
        if(this.isRunning()) {
            return false;
        }
        return size == transferred;
    }

    /**
     * @return The sum of all file lengths in this transfer.
     */
    public long getSize() {
        return size;
    }

    public void addSize(final long bytes) {
        size += bytes;
    }

    /**
     * @return The number of bytes transferred of all files.
     */
    public long getTransferred() {
        return transferred;
    }

    public void addTransferred(final long bytes) {
        transferred += bytes;
    }
}