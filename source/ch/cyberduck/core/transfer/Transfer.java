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
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.ui.growl.Growl;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * The transfer has been canceled and should
     * not continue any further processing
     */
    private boolean canceled;

    // Backward compatibility for serialization
    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;
    public static final int KIND_SYNC = 2;
    public static final int KIND_COPY = 3;
    public static final int KIND_MOVE = 4;

    protected Transfer() {
        //
    }

    private boolean running;

    /**
     * @return True if in <code>running</code> state
     */
    public boolean isRunning() {
        return running;
    }

    private boolean queued;

    /**
     * @return True if in <code>queued</code> state
     */
    public boolean isQueued() {
        return queued;
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
     * @return True if appending to files is supported
     */
    public abstract boolean isResumable();

    public abstract boolean isReloadable();

    public abstract String getStatus();

    public abstract String getImage();

    /**
     * @return True if in <code>canceled</code> state
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Create a transfer with a single root which can
     * be a plain file or a directory
     *
     * @param root File or directory
     */
    public Transfer(final Path root, final BandwidthThrottle bandwidth) {
        this(new Collection<Path>(Collections.<Path>singletonList(root)), bandwidth);
    }

    /**
     * @param roots List of files to add to transfer
     */
    public Transfer(final List<Path> roots, final BandwidthThrottle bandwidth) {
        this.roots = roots;
        this.status = new HashMap<Path, TransferStatus>();
        for(Path root : this.roots) {
            this.status.put(root, new TransferStatus());
        }
        this.session = this.getRoot().getSession();
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
                final Path root = PathFactory.createPath(session, rootDict);
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

    private Set<TransferListener> listeners
            = Collections.synchronizedSet(new HashSet<TransferListener>());

    /**
     * @param listener Callback
     */
    public void addListener(TransferListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener Callback
     */
    public void removeListener(TransferListener listener) {
        listeners.remove(listener);
    }

    protected void fireTransferWillStart() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer %s starts now", this.getName()));
        }
        canceled = false;
        running = true;
        queued = false;
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.transferWillStart();
        }
    }

    public void fireTransferQueued() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer %s queued", this.getName()));
        }
        for(Session s : this.getSessions()) {
            Growl.instance().notify("Transfer queued", s.getHost().getHostname());
            s.message(Locale.localizedString("Maximum allowed connections exceeded. Waiting", "Status"));
        }
        queued = true;
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.transferQueued();
        }
    }

    public void fireTransferResumed() {
        if(log.isDebugEnabled()) {
            log.debug("fireTransferResumed:" + this);
        }
        queued = false;
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.transferResumed();
        }
    }

    protected void fireTransferDidEnd() {
        if(log.isDebugEnabled()) {
            log.debug("fireTransferDidEnd:" + this);
        }
        if(this.isReset() && this.isComplete() && !this.isCanceled() && !(this.getTransferred() == 0)) {
            Growl.instance().notify(this.getStatus(), this.getName());
        }
        running = false;
        queued = false;
        timestamp = new Date();
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.transferDidEnd();
        }
        Queue.instance().remove(this);
    }

    protected void fireWillTransferPath(Path path) {
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.willTransferPath(path);
        }
    }

    protected void fireDidTransferPath(Path path) {
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.didTransferPath(path);
        }
    }

    /**
     * @param bytesPerSecond Maximum number of bytes to transfer by second
     */
    public void setBandwidth(final float bytesPerSecond) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Throttle bandwidth to %s bytes per second", bytesPerSecond));
        }
        bandwidth.setRate(bytesPerSecond);
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.bandwidthChanged(bandwidth);
        }
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
     * @param prompt Callback
     * @param action Transfer action for duplicate files
     * @return Null if the filter could not be determined and the transfer should be canceled instead
     */
    public TransferPathFilter filter(TransferPrompt prompt, final TransferAction action) throws BackgroundException {
        if(action.equals(TransferAction.ACTION_CANCEL)) {
            return null;
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

    public boolean isSelected(final Path item) {
        if(status.containsKey(item)) {
            return status.get(item).isSelected();
        }
        return true;
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
    private void transfer(final Path file, final TransferPathFilter filter,
                          final TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(!status.isSelected()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Skip %s not selected in prompt", file.getAbsolute()));
            }
            status.setComplete();
            return;
        }
        this.check();
        if(filter.accept(session, file)) {
            // Notification
            this.fireWillTransferPath(file);
            // Transfer
            this.transfer(file, options, status);
            if(file.attributes().isFile()) {
                // Post process of file
                filter.complete(session, file, options, status);
            }
            // Notification
            this.fireDidTransferPath(file);
        }
        else {
            status.setComplete();
        }
        this.check();
        if(file.attributes().isDirectory()) {
            boolean failure = false;
            for(Path child : this.children(file)) {
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
            // Post process of directory
            filter.complete(session, file, options, status);
            this.cache().remove(file.getReference());
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
     * @param prompt  Callback
     * @param options Transfer options
     */
    private void transfer(final TransferPrompt prompt, final TransferOptions options) throws BackgroundException {
        this.check();
        // Determine the filter to match files against
        final TransferAction action = this.action(options.resumeRequested, options.reloadRequested);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Selected transfer action %s", action));
        }
        if(action.equals(TransferAction.ACTION_CANCEL)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Transfer canceled by user:%s", this));
            }
            this.cancel();
            return;
        }
        this.clear(options);
        this.check();
        // Get the transfer filter from the concrete transfer class
        final TransferPathFilter filter = this.filter(prompt, action);
        if(null == filter) {
            // The user has canceled choosing a transfer filter
            this.cancel();
            return;
        }
        // Reset the cached size of the transfer and progress value
        this.reset();
        // Calculate information about the files in advance to give progress information
        for(Path next : roots) {
            this.prepare(next, filter);
        }
        // Transfer all files sequentially
        for(Path next : roots) {
            this.transfer(next, filter, options, status.get(next));
        }
        this.clear(options);
    }

    /**
     * To be called before any file is actually transferred
     *
     * @param p      File
     * @param filter Filter to apply to exclude files from transfer
     */
    private void prepare(final Path p, final TransferPathFilter filter) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer status for path %s", p.getAbsolute()));
        }
        this.check();
        if(!this.isSelected(p)) {
            return;
        }
        final TransferStatus s;
        // Only prepare the path it will be actually transferred
        if(filter.accept(session, p)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Accepted in %s transfer", p.getAbsolute()));
            }
            session.message(MessageFormat.format(Locale.localizedString("Prepare {0}", "Status"), p.getName()));
            s = filter.prepare(session, p);
            // Add transfer length to total bytes
            this.addSize(s.getLength());
            // Add skipped bytes
            this.addTransferred(s.getCurrent());
        }
        else {
            // Empty transfer status for files not accepted by filter
            s = new TransferStatus();
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Determined transfer status %s for %s", s, p.getAbsolute()));
        }
        status.put(p, s);
        if(p.attributes().isDirectory()) {
            // Call recursively for all children
            for(Path child : this.children(p)) {
                this.prepare(child, filter);
            }
        }
    }

    /**
     * @throws ConnectionCanceledException If the transfer has been canceled or the socket is
     *                                     no longer connected
     */
    protected void check() throws ConnectionCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Check connection for transfer %s", this.getName()));
        }
        // Bail out if canceled
        if(this.isCanceled()) {
            throw new ConnectionCanceledException(String.format("Canceled transfer %s in progress", this.getName()));
        }
        for(Session s : this.getSessions()) {
            if(!s.isConnected()) {
                // Bail out if no more connected
                throw new ConnectionCanceledException(String.format("Disconnected transfer %s in progress", this.getName()));
            }
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
    }

    /**
     * @return The cache of the underlying session
     * @see Session#cache()
     */
    public Cache cache() {
        return session.cache();
    }

    /**
     * @param prompt  Transfer prompt callback
     * @param options Transfer options
     */
    public void start(final TransferPrompt prompt, final TransferOptions options) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Start transfer with prompt %s", prompt));
        }
        final SleepPreventer sleep = SleepPreventerFactory.get();
        final String lock = sleep.lock();
        try {
            this.fireTransferWillStart();
            this.queue();
            if(this.isCanceled()) {
                // The transfer has been canceled while being queued
                return;
            }
            this.transfer(prompt, options);
        }
        finally {
            sleep.release(lock);
            this.fireTransferDidEnd();
        }
    }

    private synchronized void queue() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Queue transfer %s", this.getName()));
        }
        Queue.instance().add(this);
    }

    /**
     * @see Session#interrupt()
     */
    public void interrupt() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Interrupt transfer %s", this.getName()));
        }
        for(Session s : this.getSessions()) {
            s.interrupt();
        }
    }

    /**
     * Marks all items in the queue as canceled. Canceled items will be
     * skipped when processed. If the transfer is already in a <code>canceled</code>
     * state, the underlying session's socket is interrupted to force exit.
     */
    public void cancel() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel transfer %s", this.getName()));
        }
        for(TransferStatus s : status.values()) {
            s.setCanceled();
        }
        if(this.isCanceled()) {
            // Called prevously; now force
            this.interrupt();
        }
        canceled = true;
        Queue.instance().remove(this);
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