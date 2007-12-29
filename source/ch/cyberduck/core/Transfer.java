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

import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ftp.FTPSession;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * @version $Id$
 */
public abstract class Transfer extends NSObject {
    protected static Logger log = Logger.getLogger(Transfer.class);

    /**
     * Files and folders initially selected to be part of this transfer
     */
    protected List roots;

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

    /**
     * The transfer has been reset
     */
    private boolean reset;

    public boolean isResumable() {
        if(!this.isComplete() && !this.isVirgin() ) {
            if(this.getSession() instanceof SFTPSession) {
                return Preferences.instance().getProperty("ssh.transfer").equals(Session.SFTP);
            }
            if(this.getSession() instanceof FTPSession) {
                return !Preferences.instance().getProperty("ftp.transfermode").equals(
                        FTPTransferType.ASCII.toString());
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
        this(new Collection(Collections.singletonList(root)));
    }

    /**
     * @param roots
     */
    public Transfer(List roots) {
        this.setRoots(roots);
        this.init();
    }

    /**
     * Called from the constructor for initialization
     */
    protected abstract void init();

    public Transfer(NSDictionary dict, Session s) {
        Object rootsObj = dict.objectForKey("Roots");
        if(rootsObj != null) {
            NSArray r = (NSArray) rootsObj;
            roots = new Collection();
            for(int i = 0; i < r.count(); i++) {
                roots.add(PathFactory.createPath(s, (NSDictionary) r.objectAtIndex(i)));
            }
        }
        Object sizeObj = dict.objectForKey("Size");
        if(sizeObj != null) {
            this.size = Double.parseDouble(sizeObj.toString());
        }
        Object currentObj = dict.objectForKey("Current");
        if(currentObj != null) {
            this.transferred = Double.parseDouble(currentObj.toString());
        }
        this.init();
        Object bandwidthObj = dict.objectForKey("Bandwidth");
        if(bandwidthObj != null) {
            this.bandwidth.setRate(Float.parseFloat(bandwidthObj.toString()));
        }
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.getHost().getAsDictionary(), "Host");
        NSMutableArray r = new NSMutableArray();
        for(Iterator iter = this.roots.iterator(); iter.hasNext();) {
            r.addObject(((Path) iter.next()).getAsDictionary());
        }
        dict.setObjectForKey(r, "Roots");
        dict.setObjectForKey("" + this.getSize(), "Size");
        dict.setObjectForKey("" + this.getTransferred(), "Current");
        if(bandwidth != null) {
            dict.setObjectForKey("" + bandwidth.getRate(), "Bandwidth");
        }
        return dict;
    }

    private List listeners = new Vector();

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
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].transferWillStart();
        }
    }

    public void fireTransferQueued() {
        queued = true;
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].transferQueued();
        }
    }

    public void fireTransferPaused() {
        queued = true;
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].transferPaused();
        }
    }

    public void fireTransferResumed() {
        queued = false;
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].transferResumed();
        }
    }

    protected void fireTransferDidEnd() {
        running = false;
        queued = false;
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].transferDidEnd();
        }
        synchronized(lock) {
            lock.notifyAll();
        }
    }

    protected void fireWillTransferPath(Path path) {
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].willTransferPath(path);
        }
    }

    protected void fireDidTransferPath(Path path) {
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].didTransferPath(path);
        }
    }

    protected void fireBandwidthChanged(BandwidthThrottle bandwidth) {
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].bandwidthChanged(bandwidth);
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
        this.fireBandwidthChanged(bandwidth);
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
        return (Path) roots.get(0);
    }

    /**
     * @return All <code>root</code>s added to this transfer
     */
    public List getRoots() {
        return this.roots;
    }

    protected void setRoots(List roots) {
        this.roots = roots;
    }

    public Session getSession() {
        return this.getRoot().getSession();
    }

    /**
     * @see Session#getHost()
     */
    public Host getHost() {
        return this.getSession().getHost();
    }

    /**
     * @return The concatenation of the local filenames of all roots
     * @see #getRoots()
     */
    public String getName() {
        String name = "";
        for(Iterator iter = this.roots.iterator(); iter.hasNext();) {
            name = name + ((Path) iter.next()).getLocal().getName() + " ";
        }
        return name;
    }

    protected abstract class TransferFilter implements PathFilter {
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
     *
     */
    private Map _existing = new HashMap();

    /**
     * Looks for the file in the parent directory listing. Returns cached version if possible for better performance
     *
     * @param file
     * @return True if the file exists
     * @see ch.cyberduck.core.AbstractPath#exists()
     */
    public boolean exists(Path file) {
        if(!_existing.containsKey(file)) {
            log.debug("exists:" + file);
            _existing.put(file, Boolean.valueOf(file.exists()));
        }
        return ((Boolean) _existing.get(file)).booleanValue();
    }

    /**
     * Looks for the file in the parent directory listing. Returns cached version if possible for better performance
     *
     * @param file
     * @return True if the file exists
     * @see ch.cyberduck.core.AbstractPath#exists()
     */
    public boolean exists(Local file) {
        if(!_existing.containsKey(file)) {
            log.debug("exists:" + file);
            _existing.put(file, Boolean.valueOf(file.exists()));
        }
        return ((Boolean) _existing.get(file)).booleanValue();
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
     * @return A list of child items
     */
    public abstract AttributedList childs(final Path parent);

    /**
     * @param file
     * @return True if its child items are cached
     */
    public abstract boolean isCached(Path file);

    /**
     * @param item
     * @return True if the path is not skipped when transferring
     */
    public boolean isIncluded(Path item) {
        return !item.status.isSkipped();
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
        item.status.setSkipped(skipped);
        if(item.attributes.isDirectory()) {
            if(this.isCached(item)) {
                for(Iterator iter = this.childs(item).iterator(); iter.hasNext();) {
                    this.setSkipped((Path) iter.next(), skipped);
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
            return;
        }

        if(!this.check()) {
            return;
        }

        if(filter.accept(p)) {
            this.fireWillTransferPath(p);
            _transferImpl(_current = p);
            this.fireDidTransferPath(p);
        }

        if(!this.check()) {
            return;
        }

        if(p.attributes.isDirectory()) {
            for(Iterator iter = this.childs(p).iterator(); iter.hasNext();) {
                Path child = (Path) iter.next();
                this.transfer(child, filter);

            }
        }

        this.cleanup(p);
    }

    private void cleanup(final Path p) {
        // Save memory
        p.cache().remove(p);
        // Remove from the _existing hashmap
        _existing.remove(p);
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
                session.error(null, NSBundle.localizedString("Connection failed", "Error"), e);
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

            this.clear();

            // Get the transfer filter from the concret transfer class
            final TransferFilter filter = this.filter(action);
            if(null == filter) {
                // The user has canceled choosing a transfer filter
                this.cancel();
                return;
            }

            // Reset the cached size of the transfer and progress value
            this.reset();

            // Calculate some information about the files in advance to give some progress information
            for(Iterator iter = roots.iterator(); iter.hasNext();) {
                this.prepare((Path) iter.next(), filter);
            }

            // Transfer all files sequentially
            for(Iterator iter = roots.iterator(); iter.hasNext();) {
                this.transfer((Path) iter.next(), filter);
            }
        }
        finally {
            this.clear();
            if(options.closeSession) {
                session.close();
            }
            session.cache().clear();
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

        if(p.attributes.isDirectory()) {
            for(Iterator iter = this.childs(p).iterator(); iter.hasNext();) {
                // Call recursively for all childs
                this.prepare((Path) iter.next(), filter);
            }
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
    protected void clear() {
        log.debug("clear");
        _existing.clear();
    }

    /**
     * Calls #start with TransferOptions.DEFAULT
     *
     * @param prompt
     */
    public void start(TransferPrompt prompt) {
        this.start(prompt, TransferOptions.DEFAULT);
    }

    /**
     * Calls #start with queueing off
     *
     * @param prompt
     * @param options
     */
    public void start(TransferPrompt prompt, final TransferOptions options) {
        this.start(prompt, options, false);
    }

    /**
     *
     */
    protected TransferPrompt prompt;

    /**
     * The lock used for queuing transfers
     */
    private static final Object lock = Queue.instance();

    /**
     * @param prompt
     * @param options
     * @param queued
     */
    public void start(TransferPrompt prompt, final TransferOptions options,
                      final boolean queued) {
        log.debug("start:" + prompt);
        try {
            this.fireTransferWillStart();
            if(queued) {
                // This transfer should respect the settings for maximum number of transfers
                final TransferCollection q = TransferCollection.instance();
                while(!this.isCanceled() && q.numberOfRunningTransfers()
                        - q.numberOfQueuedTransfers() - (this.queued ? 0 : 1)
                        >= (int) Preferences.instance().getDouble("queue.maxtransfers")) {
                    log.info("Queuing " + this.toString());
                    // The maximum number of transfers is already reached
                    try {
                        // Wait for transfer slot
                        if(!this.queued) {
                            // Notify if not queued already before
                            this.fireTransferQueued();
                            this.getSession().message(NSBundle.localizedString("Maximum allowed connections exceeded. Waiting...", "Status", ""));
                        }
                        synchronized(lock) {
                            lock.wait();
                        }
                    }
                    catch(InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }
                log.info(this.toString() + " released from queue");
                this.fireTransferResumed();
                if(this.isCanceled()) {
                    // The transfer has been canceled while queued
                    return;
                }
            }
            this.prompt = prompt;
            this.transfer(options);
        }
        finally {
            this.fireTransferDidEnd();
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
        } else {
            if(_current != null) {
                _current.status.setCanceled();
            }
            canceled = true;
        }
        synchronized(lock) {
            lock.notifyAll();
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
     * @return The number of roots
     */
    public int numberOfRoots() {
        return this.roots.size();
    }

    /**
     * @return True if the transfer progress is zero and has presumably never
     *         been started yet
     */
    public boolean isVirgin() {
        return this.getTransferred() == 0;
    }

    /**
     * @return True if the bytes transferred equal the size of the queue and
     *         the bytes transfered is > 0
     */
    public boolean isComplete() {
        log.debug("isComplete");
        if(this.isVirgin()) {
            // No bytes transferred
            if(!reset) {
                // Not even attempted to transfer anything yet
                return false;
            }
        }
        return this.getSize() == this.getTransferred();
    }

    /**
     * @return The sum of all file lengths in this transfer.
     */
    public double getSize() {
        return size;
    }

    /**
     * Should not be called too frequently as it iterates over all items
     *
     * @return The number of bytes transfered of all items in this <code>transfer</code>
     */
    public double getTransferred() {
        return transferred;
    }

    public String toString() {
        return this.getName();
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + super.toString());
        super.finalize();
    }
}