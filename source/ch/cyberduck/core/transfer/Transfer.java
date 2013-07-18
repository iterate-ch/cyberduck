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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathReference;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

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
    private Map<Path, TransferStatus> status;

    /**
     * The sum of the file length of all files in the <code>queue</code>
     */
    private long size = 0;

    /**
     * The number bytes already transferred of the files in the <code>queue</code>
     */
    private long transferred = 0;

    public enum Type {
        download,
        upload,
        sync,
        copy,
        move
    }

    protected Transfer() {
        //
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
    private Cache cache = new Cache();

    /**
     * Transfer state
     */
    private State state;

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
     * @return True if appending to files is supported
     */
    public abstract boolean isResumable();

    public abstract boolean isReloadable();

    public abstract String getStatus();

    public abstract String getImage();

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
        this.status = new HashMap<Path, TransferStatus>();
        for(Path root : this.roots) {
            this.status.put(root, new TransferStatus());
        }
        this.bandwidth = bandwidth;
    }

    public <T> Transfer(final T serialized, final Session session, final BandwidthThrottle bandwidth) {
        this.session = session;
        this.bandwidth = bandwidth;
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        final List rootsObj = dict.listForKey("Roots");
        if(rootsObj != null) {
            roots = new ArrayList<Path>();
            status = new HashMap<Path, TransferStatus>();
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

    @Override
    public abstract <T> T getAsDictionary();

    public Serializer getSerializer() {
        final Serializer dict = SerializerFactory.createSerializer();
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
        return dict;
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
        return session.toURL(this.getRoot().getParent(), false);
    }

    /**
     * @return Download target folder
     */
    public String getLocal() {
        final Local local = this.getRoot().getLocal();
        return local.getParent().toURL();
    }

    /**
     * @return All <code>root</code>s added to this transfer
     */
    public List<Path> getRoots() {
        return roots;
    }

    public List<Session<?>> getSessions() {
        return new ArrayList<Session<?>>(Collections.singletonList(session));
    }

    public String getName() {
        return this.getRoot().getName();
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
    protected abstract TransferAction action(final boolean resumeRequested, final boolean reloadRequested);

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
        return this.cache().lookup(r);
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
        if(status.containsKey(item)) {
            return status.get(item).isSelected();
        }
        return true;
    }

    public TransferStatus status(final Path item) {
        return status.get(item);
    }

    /**
     * @param item File
     * @return True if file should not be transferred
     */
    public boolean isSkipped(final Path item) {
        return false;
    }

    /**
     * Select the path to be included in the transfer
     *
     * @param item     File
     * @param selected Selected files in transfer prompt
     */
    public void setSelected(final Path item, final boolean selected) {
        TransferStatus s = new TransferStatus();
        s.setSelected(selected);
        status.put(item, s);
    }

    /**
     * @param file    File
     * @param filter  Filter to apply to exclude files from transfer
     * @param options Quarantine option
     * @param status  Transfer status
     */
    protected void transfer(final Path file, final TransferPathFilter filter,
                            final TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(!status.isSelected()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Skip %s not selected in prompt", file.getAbsolute()));
            }
            status.setComplete();
            return;
        }
        if(filter.accept(session, file, status)) {
            // Transfer
            this.transfer(file, options, status, session);
            if(file.attributes().isFile()) {
                // Post process of file.
                try {
                    filter.complete(session, file, options, status, session);
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Ignore failure in completion filter for %s", file));
                }
            }
        }
        else {
            status.setComplete();
        }
        if(file.attributes().isDirectory()) {
            boolean failure = false;
            for(Path child : this.cache().get(file.getReference())) {
                // Recursive
                this.transfer(child, filter, options, this.status.get(child));
                if(!this.status.get(child).isComplete()) {
                    failure = true;
                }
                this.status.remove(child);
            }
            // Set completion status
            if(!failure) {
                status.setComplete();
            }
            // Post process of directory.
            try {
                filter.complete(session, file, options, status, session);
            }
            catch(BackgroundException e) {
                log.warn(String.format("Ignore failure in completion filter for %s", file));
            }
            this.cache().remove(file.getReference());
        }
    }

    /**
     * The actual transfer implementation
     *
     * @param file     File
     * @param options  Quarantine option
     * @param status   Transfer status
     * @param listener Progress information callback
     */
    public abstract void transfer(Path file, TransferOptions options, TransferStatus status,
                                  ProgressListener listener) throws BackgroundException;

    /**
     * To be called before any file is actually transferred
     *
     * @param file   File
     * @param filter Filter to apply to exclude files from transfer
     */
    public void prepare(final Path file, final TransferStatus parent, final TransferPathFilter filter,
                        final ProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer status of %s for transfer %s", file, this));
        }
        if(this.isSelected(file)) {
            // Only prepare the path it will be actually transferred
            if(filter.accept(session, file, parent)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Accepted in %s transfer", file));
                }
                listener.message(MessageFormat.format(Locale.localizedString("Prepare {0}", "Status"), file.getName()));
                final TransferStatus s = filter.prepare(session, file, parent);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Determined transfer status %s of %s for transfer %s", status, file, this));
                }
                // Add transfer length to total bytes
                this.addSize(s.getLength());
                // Add skipped bytes
                this.addTransferred(s.getCurrent());
                if(file.attributes().isDirectory()) {
                    // Call recursively for all children
                    final AttributedList<Path> children = this.children(file);
                    // Put into cache for later reference when transferring
                    this.cache().put(file.getReference(), children);
                    // Call recursively
                    for(Path child : children) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Find transfer status of %s for transfer %s", child, this));
                        }
                        this.prepare(child, s, filter, listener);
                    }
                }
                status.put(file, s);
            }
        }
        else {
            log.info(String.format("Skip unchecked file %s for transfer %s", file, this));
        }
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
        status.clear();
    }

    /**
     * @param prompt  Transfer prompt callback
     * @param options Transfer options
     */
    public void start(final TransferPrompt prompt, final TransferOptions options) throws BackgroundException {
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
                this.prepare(next, new TransferStatus().exists(true), filter, session);
            }
            // Transfer all files sequentially
            for(Path next : roots) {
                this.transfer(next, filter, options, status.get(next));
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
            log.debug(String.format("Cancel transfer %s", this.getName()));
        }
        state = State.canceled;
        for(TransferStatus s : status.values()) {
            s.setCanceled();
        }
    }

    /**
     * Recalculate the size of the <code>queue</code>
     */
    public void reset() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reset status for %s", this.getName()));
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
     * @return The number of roots
     */
    public int numberOfRoots() {
        return roots.size();
    }

    /**
     * @return True if the bytes transferred equal the size of the queue
     */
    public boolean isComplete() {
        if(this.isRunning()) {
            return false;
        }
        return this.getSize() == this.getTransferred();
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