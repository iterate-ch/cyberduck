package ch.cyberduck.core;

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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;
import ch.cyberduck.ui.growl.Growl;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @version $Id$
 */
public abstract class Transfer implements Serializable {
    private static Logger log = Logger.getLogger(Transfer.class);

    /**
     * Files and folders initially selected to be part of this transfer
     */
    protected List<Path> roots;

    /**
     * The sum of the file length of all files in the <code>queue</code>
     */
    protected double size = 0;

    /**
     * The number bytes already transferred of the files in the <code>queue</code>
     */
    protected double transferred = 0;

    /**
     * The transfer has been canceled and should
     * not continue any forther processing
     */
    private boolean canceled;

    // Backward compatibilty for serializaton
    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;
    public static final int KIND_SYNC = 2;
    public static final int KIND_COPY = 3;
    public static final int KIND_MOVE = 4;

    protected Transfer() {
        ;
    }

    /**
     * @return True if in <code>canceled</code> state
     */
    public boolean isCanceled() {
        return canceled;
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

    protected Session session;

    /**
     * The transfer has been reset
     */
    private boolean reset;

    /**
     * Last transfered in milliseconds
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
     * Create a transfer with a single root which can
     * be a plain file or a directory
     *
     * @param root File or directory
     */
    public Transfer(Path root) {
        this(new Collection<Path>(Collections.<Path>singletonList(root)));
    }

    /**
     * @param items List of files to add to transfer
     */
    public Transfer(List<Path> items) {
        this.roots = items;
        this.session = this.getRoot().getSession();
        // Intialize bandwidth setting
        this.init();
    }

    /**
     * Called from the constructor for initialization
     */
    protected abstract void init();

    public <T> Transfer(T dict, Session s) {
        this.session = s;
        this.init(dict);
    }

    public <T> void init(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        final List rootsObj = dict.listForKey("Roots");
        if(rootsObj != null) {
            roots = new Collection<Path>();
            for(Object rootDict : rootsObj) {
                roots.add(PathFactory.createPath(session, rootDict));
            }
        }
        Object sizeObj = dict.stringForKey("Size");
        if(sizeObj != null) {
            size = Double.parseDouble(sizeObj.toString());
        }
        Object timestampObj = dict.stringForKey("Timestamp");
        if(timestampObj != null) {
            timestamp = new Date(Long.parseLong(timestampObj.toString()));
        }
        Object currentObj = dict.stringForKey("Current");
        if(currentObj != null) {
            transferred = Double.parseDouble(currentObj.toString());
        }
        this.init();
        Object bandwidthObj = dict.stringForKey("Bandwidth");
        if(bandwidthObj != null) {
            bandwidth.setRate(Float.parseFloat(bandwidthObj.toString()));
        }
    }

    public abstract <T> T getAsDictionary();

    public Serializer getSerializer() {
        final Serializer dict = SerializerFactory.createSerializer();
        dict.setObjectForKey(session.getHost(), "Host");
        dict.setListForKey(this.roots, "Roots");
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
            log.debug("fireTransferWillStart:" + this);
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
            log.debug("fireTransferQueued:" + this);
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
     * In Bytes per second
     */
    protected BandwidthThrottle bandwidth
            = new BandwidthThrottle(BandwidthThrottle.UNLIMITED);

    /**
     * @param bytesPerSecond Maximum number of bytes to transfer by second
     */
    public void setBandwidth(float bytesPerSecond) {
        log.debug("setBandwidth:" + bytesPerSecond);
        bandwidth.setRate(bytesPerSecond);
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.bandwidthChanged(bandwidth);
        }
    }

    /**
     * @return Creation date of transfer
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @return Rate in bytes per second allowed for this transfer
     */
    public float getBandwidth() {
        return bandwidth.getRate();
    }

    /**
     * @return The first <code>root</code> added to this transfer
     */
    public Path getRoot() {
        return roots.get(0);
    }

    /**
     * @return All <code>root</code>s added to this transfer
     */
    public List<Path> getRoots() {
        return this.roots;
    }

    public void setRoots(List<Path> roots) {
        this.roots = roots;
    }

    /**
     * Normalize path names and remove duplicates.
     */
    protected abstract void normalize();

    public List<Session> getSessions() {
        return Collections.singletonList(this.session);
    }

    public String getName() {
        return this.getRoot().getName();
    }

    protected abstract static class TransferFilter implements PathFilter<Path> {
        /**
         * Called before the file will actually get transferred. Should prepare for the transfer
         * such as calculating its size.
         * Must only be called exactly once for each file.
         * Must only be called if #accept for the file returns true
         *
         * @param p File
         * @see PathFilter#accept(AbstractPath)
         */
        public abstract void prepare(Path p);

        /**
         * Post processing.
         *
         * @param p File
         */
        public abstract void complete(Path p);
    }

    /**
     * @param action Transfer action for duplicate files
     * @return Null if the filter could not be determined and the transfer should be canceled instead
     */
    public TransferFilter filter(final TransferAction action) {
        if(action.equals(TransferAction.ACTION_CANCEL)) {
            return null;
        }
        throw new IllegalArgumentException("Unknown transfer action:" + action);
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
     * @see ch.cyberduck.core.Cache#lookup(PathReference)
     */
    public Path lookup(PathReference r) {
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
    public abstract AttributedList<Path> children(final Path parent);

    /**
     * @param item File
     * @return True if the path is not skipped when transferring
     */
    public boolean isIncluded(Path item) {
        return item.status().isSelected() && !this.isSkipped(item);
    }

    /**
     * @param item File
     * @return True if file should not be transferred
     */
    public boolean isSkipped(Path item) {
        return false;
    }

    /**
     * Select the path to be included in the transfer
     *
     * @param item     File
     * @param selected Selected files in transfer prompt
     */
    public void setSelected(Path item, final boolean selected) {
        item.status().setSelected(selected);
        if(item.attributes().isDirectory()) {
            if(item.isCached()) {
                for(Path child : this.children(item)) {
                    this.setSelected(child, selected);
                }
            }
        }
    }

    /**
     * The current path being transferred
     */
    private Path _current = null;

    /**
     * @param p       File
     * @param filter  Filter to apply to exclude files from transfer
     * @param options Quarantine option
     */
    protected void transfer(final Path p, final TransferFilter filter, final TransferOptions options) {
        if(!this.isIncluded(p)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Not included in transfer:%s", p.getAbsolute()));
            }
            p.status().setComplete(true);
            return;
        }

        if(!this.check()) {
            return;
        }

        // Reset transfer status
        p.status().reset();

        if(filter.accept(p)) {
            // Notification
            this.fireWillTransferPath(p);
            _current = p;
            // Transfer
            transfer(p, options);
            if(p.attributes().isFile()) {
                // Post process of file
                filter.complete(p);
            }
            _current = null;
            // Notification
            this.fireDidTransferPath(p);
        }

        if(!this.check()) {
            return;
        }

        if(p.attributes().isDirectory()) {
            boolean failure = false;
            final AttributedList<Path> children = this.children(p);
            if(!children.attributes().isReadable()) {
                failure = true;
            }
            for(Path child : children) {
                this.transfer(child, filter, options);
                if(!child.status().isComplete()) {
                    failure = true;
                }
            }
            // Post process of directory
            filter.complete(p);
            // Set completion status
            if(!failure) {
                p.status().setComplete(true);
            }
            this.cache().remove(p.getReference());
        }
    }

    /**
     * The actual transfer implementation
     *
     * @param file    File
     * @param options Quarantine option
     * @see ch.cyberduck.core.Path#download()
     * @see ch.cyberduck.core.Path#upload()
     */
    protected abstract void transfer(final Path file, TransferOptions options);

    /**
     * @param options Transfer options
     */
    private void transfer(final TransferOptions options) {
        for(Session s : this.getSessions()) {
            if(!this.open(s)) {
                return;
            }
        }

        if(!this.check()) {
            return;
        }

        for(Session s : this.getSessions()) {
            // Do not invalidate cache entries during file transfers
            s.cache().setLifecycle(options.invalidateCache);
        }

        // Determine the filter to match files against
        final TransferAction action = this.action(options.resumeRequested, options.reloadRequested);
        if(action.equals(TransferAction.ACTION_CANCEL)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Transfer canceled by user:%s", this));
            }
            this.cancel();
            return;
        }

        this.clear(options);

        this.normalize();

        if(!this.check()) {
            return;
        }

        // Get the transfer filter from the concret transfer class
        final TransferFilter filter = this.filter(action);
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
        this.transfer(filter, options);

        for(Session s : this.getSessions()) {
            s.cache().setLifecycle(Cache.Lifecycle.INVALIDATED);
        }

        this.clear(options);

        if(options.closeSession) {
            for(Session s : this.getSessions()) {
                this.close(s);
            }
        }
    }

    protected void close(Session session) {
        session.close();
    }

    protected boolean open(Session session) {
        try {
            log.debug("Checking connnection");
            // We manually open the connection here first as otherwise
            // every transfer will try again if it should fail
            session.check();
        }
        catch(IOException e) {
            log.warn(e.getMessage());
            return false;
        }
        return true;
    }

    protected void transfer(final TransferFilter filter, final TransferOptions options) {
        for(Path next : roots) {
            this.transfer(next, filter, options);
        }
    }

    /**
     * To be called before any file is actually transferred
     *
     * @param p      File
     * @param filter Filter to apply to exclude files from transfer
     */
    private void prepare(Path p, final TransferFilter filter) {
        log.debug("prepare:" + p);
        if(!this.check()) {
            return;
        }

        if(!this.isIncluded(p)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Not included in transfer:%s", p.getAbsolute()));
            }
            return;
        }

        // Only prepare the path it will be actually transferred
        if(filter.accept(p)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Accepted in transfer:%s", p.getAbsolute()));
            }
            session.message(MessageFormat.format(Locale.localizedString("Prepare {0}", "Status"), p.getName()));
            filter.prepare(p);
        }

        if(p.attributes().isDirectory()) {
            // Call recursively for all children
            for(Path child : this.children(p)) {
                this.prepare(child, filter);
            }
        }
    }

    /**
     * @return False if the transfer has been canceled or the socket is
     *         no longer connected
     */
    protected boolean check() {
        if(log.isDebugEnabled()) {
            log.debug("check:" + this);
        }
        // Bail out if canceled
        if(this.isCanceled()) {
            log.warn("Canceled transfer in progress:" + this);
            return false;
        }
        for(Session s : this.getSessions()) {
            if(!s.isConnected()) {
                // Bail out if no more connected
                log.warn("Disconnected transfer in progress:" + this);
                return false;
            }
        }
        return true;
    }

    /**
     * Clear all cached values
     *
     * @param options Transfer options
     */
    protected void clear(final TransferOptions options) {
        log.debug("clear:" + options);
        if(options.closeSession) {
            // We have our own session independent of any browser.
            this.cache().clear();
        }
    }

    /**
     * @return The cache of the underlying session
     * @see Session#cache()
     */
    public Cache<Path> cache() {
        return session.cache();
    }

    /**
     *
     */
    protected TransferPrompt prompt;

    /**
     * @param prompt  Transfer prompt callback
     * @param options Transfer options
     */
    public void start(TransferPrompt prompt, final TransferOptions options) {
        if(log.isDebugEnabled()) {
            log.debug("start:" + prompt);
        }
        this.prompt = prompt;
        try {
            this.fireTransferWillStart();
            this.queue();
            if(this.isCanceled()) {
                // The transfer has been canceled while being queued
                return;
            }
            this.transfer(options);
        }
        finally {
            this.prompt = null;
            this.fireTransferDidEnd();
        }
    }

    private synchronized void queue() {
        if(log.isDebugEnabled()) {
            log.debug("queue:" + this);
        }
        Queue.instance().add(this);
    }

    /**
     * @see Session#interrupt()
     */
    public void interrupt() {
        if(log.isDebugEnabled()) {
            log.debug("interrupt:" + this);
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
    public void cancel() {
        if(log.isDebugEnabled()) {
            log.debug("cancel:" + this);
        }
        if(_current != null) {
            _current.status().setCanceled();
        }
        if(this.isCanceled()) {
            // Called prevously; now force
            this.interrupt();
        }
        canceled = true;
        Queue.instance().remove(this);
    }

    /**
     * Called before remove from the transfer queue
     */
    public void cleanup() {
        ;
    }

    /**
     * Recalculate the size of the <code>queue</code>
     */
    protected void reset() {
        if(log.isDebugEnabled()) {
            log.debug("reset:" + this);
        }
        this.transferred = 0;
        this.size = 0;
        this.reset = true;
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
        return this.roots.size();
    }

    /**
     * @return True if the bytes transferred equal the size of the queue and
     *         the bytes transfered is > 0
     */
    public boolean isComplete() {
        for(Path root : this.roots) {
            if(!root.status().isComplete()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return The sum of all file lengths in this transfer.
     */
    public double getSize() {
        return size;
    }

    /**
     * @return The number of bytes transfered of all items in this <code>transfer</code>
     */
    public double getTransferred() {
        return transferred;
    }

    public void setTransferred(double transferred) {
        this.transferred = transferred;
    }
}
