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

import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.ui.growl.Growl;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * @version $Id$
 */
public abstract class Transfer implements Serializable {
    protected static Logger log = Logger.getLogger(Transfer.class);

    /**
     * Files and folders initially selected to be part of this transfer
     */
    protected List<Path> roots;

    /**
     * The sum of the file length of all files in the <code>queue</code>
     */
    protected double size = 0;

    /**
     * The number bytes already transferred of the ifles in the <code>queue</code>
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

    protected Transfer() {
        ;
    }

    /**
     * @return True if in <code>canceled</code> state
     */
    public boolean isCanceled() {
        return this.canceled;
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

    private Session session;

    /**
     * The transfer has been reset
     */
    private boolean reset;

    /**
     * Last transfered in milliseconds
     */
    private Date timestamp;

    /**
     * @return
     */
    public boolean isResumable() {
        if(!this.isComplete()) {
            if(this.getSession() instanceof SFTPSession) {
                return Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier());
            }
            if(this.getSession() instanceof FTPSession) {
                return Preferences.instance().getProperty("ftp.transfermode").equals(
                        FTPTransferType.BINARY.toString());
            }
            return true;
        }
        return false;
    }

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
     * @param roots
     */
    public Transfer(List<Path> roots) {
        this.setRoots(roots);
        this.session = this.getRoot().getSession();
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
                roots.add(PathFactory.createPath(this.session, rootDict));
            }
        }
        Object sizeObj = dict.stringForKey("Size");
        if(sizeObj != null) {
            this.size = Double.parseDouble(sizeObj.toString());
        }
        Object timestampObj = dict.stringForKey("Timestamp");
        if(timestampObj != null) {
            this.timestamp = new Date(Long.parseLong(timestampObj.toString()));
        }
        Object currentObj = dict.stringForKey("Current");
        if(currentObj != null) {
            this.transferred = Double.parseDouble(currentObj.toString());
        }
        this.init();
        Object bandwidthObj = dict.stringForKey("Bandwidth");
        if(bandwidthObj != null) {
            this.bandwidth.setRate(Float.parseFloat(bandwidthObj.toString()));
        }
    }

    public abstract <T> T getAsDictionary();

    public Serializer getSerializer() {
        final Serializer dict = SerializerFactory.createSerializer();
        dict.setObjectForKey(this.getSession().getHost(), "Host");
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
     * @param listener
     */
    public void addListener(TransferListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeListener(TransferListener listener) {
        listeners.remove(listener);
    }

    protected void fireTransferWillStart() {
        canceled = false;
        running = true;
        queued = false;
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.transferWillStart();
        }
    }

    public void fireTransferQueued() {
        final Session session = this.getSession();
        Growl.instance().notify("Transfer queued", session.getHost().getHostname());
        session.message(Locale.localizedString("Maximum allowed connections exceeded. Waiting", "Status"));
        queued = true;
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.transferQueued();
        }
    }

    public void fireTransferResumed() {
        queued = false;
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.transferResumed();
        }
    }

    protected void fireTransferDidEnd() {
        running = false;
        queued = false;
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.transferDidEnd();
        }
        synchronized(Queue.instance()) {
            Queue.instance().notify();
        }
        timestamp = new Date();
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
    protected BandwidthThrottle bandwidth;

    /**
     * @param bytesPerSecond
     */
    public void setBandwidth(float bytesPerSecond) {
        log.debug("setBandwidth:" + bytesPerSecond);
        bandwidth.setRate(bytesPerSecond);
        for(TransferListener listener : listeners.toArray(new TransferListener[listeners.size()])) {
            listener.bandwidthChanged(bandwidth);
        }
    }

    /**
     * @return
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

    protected void setRoots(List<Path> roots) {
        this.roots = roots;
    }

    public Session getSession() {
        return this.session;
    }

    /**
     * @return The concatenation of the local filenames of all roots
     * @see #getRoots()
     */
    public String getName() {
        String name = "";
        for(Path next : this.roots) {
            name = name + next.getLocal().getName() + " ";
        }
        return name;
    }

    protected abstract static class TransferFilter implements PathFilter<Path> {
        /**
         * Called before the file will actually get transferred. Should prepare for the transfer
         * such as calculating its size.
         * Must only be called exactly once for each file.
         * Must only be called if #accept for the file returns true
         *
         * @param p
         * @see PathFilter#accept(AbstractPath)
         */
        public abstract void prepare(Path p);
    }

    /**
     * @param action
     * @return Null if the filter could not be determined and the transfer should be canceled instead
     */
    public TransferFilter filter(final TransferAction action) {
        if(action.equals(TransferAction.ACTION_CANCEL)) {
            return null;
        }
        throw new IllegalArgumentException("Unknown transfer action:" + action);
    }

    /**
     * @param resumeRequested
     * @param reloadRequested
     * @return
     */
    public abstract TransferAction action(final boolean resumeRequested, final boolean reloadRequested);

    /**
     * Returns the childs of this path filtering it with the default regex filter
     *
     * @param parent The directory to list the childs
     * @return A list of child items
     */
    public abstract AttributedList<Path> childs(final Path parent);

    /**
     * @param item
     * @return True if the path is not skipped when transferring
     */
    public boolean isIncluded(Path item) {
        return !item.getStatus().isSkipped() && this.isSelectable(item);
    }

    /**
     * If the path can be selected for inclusion
     *
     * @param item
     * @return True if selectable
     */
    public boolean isSelectable(Path item) {
        return true;
    }

    /**
     * Recursively update the status of all cached child items
     *
     * @param item
     * @param skipped True if skipped
     */
    public void setSkipped(Path item, final boolean skipped) {
        item.getStatus().setSkipped(skipped);
        if(item.attributes.isDirectory()) {
            if(item.isCached()) {
                for(Path child : this.childs(item)) {
                    this.setSkipped(child, skipped);
                }
            }
        }
    }

    /**
     * The current path being transferred
     */
    private Path _current = null;

    /**
     * @param p
     * @param filter
     */
    private void transfer(final Path p, final TransferFilter filter) {
        if(!this.isIncluded(p)) {
            p.getStatus().setComplete(true);
            return;
        }

        if(!this.check()) {
            return;
        }

        if(filter.accept(p)) {
            this.fireWillTransferPath(p);
            _current = p;
            if(!roots.contains(p)) {
                // Root objects are already prepared in advance
                filter.prepare(_current);
            }
            _current.getStatus().reset();
            _transferImpl(_current);
            this.fireDidTransferPath(_current);
        }

        if(!this.check()) {
            return;
        }

        if(p.attributes.isDirectory()) {
            boolean failure = false;
            final AttributedList<Path> childs = this.childs(p);
            if(!childs.attributes().isReadable()) {
                failure = true;
            }
            for(Path child : childs) {
                this.transfer(child, filter);
                if(!child.getStatus().isComplete()) {
                    failure = true;
                }
            }
            if(!failure) {
                p.getStatus().setComplete(true);
            }
            session.cache().remove(p);
        }
    }

    /**
     * The actual transfer implementation
     *
     * @param p
     * @see ch.cyberduck.core.Path#download()
     * @see ch.cyberduck.core.Path#upload()
     */
    protected abstract void _transferImpl(final Path p);

    /**
     * @param options
     */
    private void transfer(final TransferOptions options) {
        final Session session = this.getSession();
        try {
            try {
                // We manually open the connection here first as otherwise
                // every transfer will try again if it should fail
                session.check();
            }
            catch(IOException e) {
                return;
            }

            if(!this.check()) {
                return;
            }

            // Determine the filter to match files against
            final TransferAction action = this.action(options.resumeRequested, options.reloadRequested);
            if(action.equals(TransferAction.ACTION_CANCEL)) {
                this.cancel();
                return;
            }

            this.clear(options);

            // Get the transfer filter from the concret transfer class
            final TransferFilter filter = this.filter(action);
            if(null == filter) {
                // The user has canceled choosing a transfer filter
                this.cancel();
                return;
            }

            // Reset the cached size of the transfer and progress value
            this.reset();

            // Calculate some information about the root files in advance to give some progress information
            for(Path next : roots) {
                this.prepare(next, filter);
            }

            // Transfer all files sequentially
            for(Path next : roots) {
                this.transfer(next, filter);
            }
        }
        finally {
            this.clear(options);
            if(options.closeSession) {
                session.close();
            }
        }
    }

    /**
     * To be called before any file is actually transferred
     *
     * @param p
     * @param filter
     */
    private void prepare(Path p, final TransferFilter filter) {
        log.debug("prepare:" + p);
        if(!this.check()) {
            return;
        }

        if(!this.isIncluded(p)) {
            return;
        }

        // Only prepare the path it will be actually transferred
        if(filter.accept(p)) {
            filter.prepare(p);
        }
    }

    /**
     * @return False if the transfer has been canceled or the socket is
     *         no longer connected
     */
    private boolean check() {
        log.debug("check:");
        if(!this.getSession().isConnected()) {
            // Bail out if no more connected
            return false;
        }
        // Bail out if canceled
        return !this.isCanceled();
    }

    /**
     * Clear all cached values
     */
    protected void clear(final TransferOptions options) {
        log.debug("clear");
        if(options.closeSession) {
            session.cache().clear();
        }
    }

    /**
     * Use default transfer options
     *
     * @param prompt
     */
    public void start(TransferPrompt prompt) {
        this.start(prompt, TransferOptions.DEFAULT);
    }

    /**
     *
     */
    protected TransferPrompt prompt;

    /**
     * @param prompt
     * @param options
     */
    public void start(TransferPrompt prompt, final TransferOptions options) {
        log.debug("start:" + prompt);
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
            this.fireTransferDidEnd();
        }
    }

    private void queue() {
        final TransferCollection q = TransferCollection.instance();
        // This transfer should respect the settings for maximum number of transfers
        if(q.numberOfRunningTransfers() - q.numberOfQueuedTransfers() - 1
                >= (int) Preferences.instance().getDouble("queue.maxtransfers")) {
            this.fireTransferQueued();
            log.info("Queuing " + this.toString());
            // The maximum number of transfers is already reached
            try {
                synchronized(Queue.instance()) {
                    // Wait for transfer slot
                    Queue.instance().wait();
                }
            }
            catch(InterruptedException e) {
                log.error(e.getMessage());
            }
            log.info(this.toString() + " released from queue");
            this.fireTransferResumed();
        }
    }

    /**
     * @see Session#interrupt()
     */
    public void interrupt() {
        log.debug("interrupt:");
        this.getSession().interrupt();
    }

    /**
     * Marks all items in the queue as canceled. Canceled items will be
     * skipped when processed. If the transfer is already in a <code>canceled</code>
     * state, the underlying session's socket is interrupted to force exit.
     */
    public void cancel() {
        log.debug("cancel:");
        if(this.isCanceled()) {
            // Called prevously; now force
            this.interrupt();
        }
        else {
            if(_current != null) {
                _current.getStatus().setCanceled();
            }
            canceled = true;
        }
        synchronized(Queue.instance()) {
            Queue.instance().notify();
        }
    }

    /**
     * Recalculate the size of the <code>queue</code>
     */
    protected void reset() {
        log.debug("reset:");
        this.transferred = 0;
        this.size = 0;
        this.reset = true;
    }

    /**
     * @return
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
        log.debug("isComplete");
        for(Path root : this.roots) {
            if(root.getStatus().isSkipped()) {
                continue;
            }
            if(!root.getStatus().isComplete()) {
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

    public void setSize(double size) {
        this.size = size;
    }

    /**
     * Should not be called too frequently as it iterates over all items
     *
     * @return The number of bytes transfered of all items in this <code>transfer</code>
     */
    public double getTransferred() {
        return transferred;
    }

    public void setTransferred(double transferred) {
        this.transferred = transferred;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}