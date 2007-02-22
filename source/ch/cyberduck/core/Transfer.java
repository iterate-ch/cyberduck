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

import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSMutableDictionary;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public abstract class Transfer extends NSObject {
    protected static Logger log = Logger.getLogger(Transfer.class);

    /**
     * Files and folders initially selected to be part of this transfer
     */
    private List roots = new ArrayList();

    /**
     * All validated files to be transferred
     */
    protected List queue;

    /**
     * The sum of the file length of all files in the <code>queue</code>
     */
    protected double size = -1;

    /**
     * The number bytes already transferred of the ifles in the <code>queue</code>
     */
    private double current = 0;

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

    /**
     * Creating an empty queue containing no items. Items have to be added later
     * using the <code>addRoot</code> method.
     */
    public Transfer() {

    }

    /**
     * Create a transfer with a single root which can
     * be a plain file or a directory
     * @param root File or directory
     */
    public Transfer(Path root) {
        this.roots.add(root);
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

    protected void fireQueueStartedEvent() {
        running = true;
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].queueWillStart();
        }
    }

    protected void fireQueueStoppedEvent() {
        running = false;
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].queueDidEnd();
        }
    }

    protected void fireTransferStartedEvent(Path path) {
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].transferWillStart(path);
        }
    }

    protected void fireTransferStoppedEvent(Path path) {
        TransferListener[] l = (TransferListener[]) listeners.toArray(
                new TransferListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].transferDidEnd(path);
        }
    }

    public Transfer(NSDictionary dict, Session s) {
        Object rootsObj = dict.objectForKey("Roots");
        if(rootsObj != null) {
            NSArray r = (NSArray) rootsObj;
            for(int i = 0; i < r.count(); i++) {
                this.addRoot(PathFactory.createPath(s, (NSDictionary) r.objectAtIndex(i)));
            }
        }
        Object sizeObj = dict.objectForKey("Size");
        if(sizeObj != null) {
            this.size = Double.parseDouble((String) sizeObj);
        }
        Object currentObj = dict.objectForKey("Current");
        if(currentObj != null) {
            this.current = Double.parseDouble((String) currentObj);
        }
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.getRoot().getHost().getAsDictionary(), "Host");
        NSMutableArray r = new NSMutableArray();
        for(Iterator iter = this.roots.iterator(); iter.hasNext();) {
            r.addObject(((Path) iter.next()).getAsDictionary());
        }
        dict.setObjectForKey(r, "Roots");
        dict.setObjectForKey("" + this.getSize(), "Size");
        dict.setObjectForKey("" + this.getCurrent(), "Current");
        return dict;
    }

    /**
     * Add a file or folder to the initial selection
     * @param item File or folder to be added
     */
    public void addRoot(Path item) {
        this.roots.add(item);
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

    /**
     * Iterates over all <code>root</code>s
     * @return Return a list of all <code>roots</code>s and its
     * child items (recursively)
     */
    public List getChilds() {
        List childs = new ArrayList();
        for(Iterator rootIter = this.getRoots().iterator(); rootIter.hasNext()
                && !this.isCanceled(); ) {
            this.getChilds(childs, (Path) rootIter.next());
        }
        return childs;
    }

    /**
     * Should implement to add all childs of <code>root</code> and the <code>root</code>
     * itself to the <code>childs</code>
     * @param childs The list to fill with all eventual childs
     * @param root The parent file
     */
    protected abstract void getChilds(List childs, Path root);

    /**
     * A compiled representation of a regular expression.
     */
    protected Pattern DOWNLOAD_SKIP_PATTERN = null;

    {
        try {
            DOWNLOAD_SKIP_PATTERN = Pattern.compile(
                    Preferences.instance().getProperty("queue.download.skip.regex"));
        }
        catch(PatternSyntaxException e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * A compiled representation of a regular expression.
     */
    protected Pattern UPLOAD_SKIP_PATTERN = null;

    {
        try {
            UPLOAD_SKIP_PATTERN = Pattern.compile(
                    Preferences.instance().getProperty("queue.download.skip.regex"));
        }
        catch(PatternSyntaxException e) {
            log.warn(e.getMessage());
        }
    }

    /**
     *
     * @param p The file to transfer
     */
    protected abstract void transfer(Path p);

    /**
     * The user requested to resume the transfer; mutually exclusive with #reloadRequested
     */
    private boolean resumeRequested;

    public void setResumeReqested(boolean resumeRequested) {
        this.resumeRequested = resumeRequested;
    }

    /**
     * The user requested to reload the transfer; mutually exclusive with #resumeReqested
     */
    private boolean reloadRequested;

    public void setReloadRequested(boolean reloadRequested) {
        this.reloadRequested = reloadRequested;
    }

    /**
     * Process all items in the <code>queue</code>. Does <strong>not</strong> run
     * in a background thread.
     * @param v
     */
    public void run(final Validator v) {
        try {
            this.canceled = false;
            this.fireQueueStartedEvent();
            try {
                // We manually open the connection here first as otherwise
                // every transfer will try again if it should fail
                this.getSession().check();
            }
            catch(IOException e) {
                this.getSession().error(null, "Connection failed", e);
                // Initial connection attempt failed; bail out
                this.cancel();
            }
            if(this.isCanceled()) {
                return;
            }
            List validated = this.validate(v);
            if(this.isCanceled()) {
                return;
            }
            if(validated.size() == 0) {
                return;
            }
            // As the transfer has not been canceled, all validated items
            // should be added to the queue.
            this.queue = validated;
            // Recalculate the size of the <code>queue</code>
            this.reset();
            for(Iterator iter = queue.iterator(); iter.hasNext();) {
                if(!this.getSession().isConnected()) {
                    // Bail out if no more connected
                    this.cancel();
                }
                if(this.isCanceled()) {
                    // Bail out if canceled by the user
                    return;
                }
                final Path path = (Path)iter.next();
                this.fireTransferStartedEvent(path);
                this.transfer(path);
                this.fireTransferStoppedEvent(path);
            }
        }
        finally {
            this.fireQueueStoppedEvent();
        }
    }

    /**
     *
     * @param v The validator to check each file against
     * @return The list of validated items that the user allowed
     * to be added to the <code>queue</code>
     */
    private List validate(final Validator v) {
        final List childs = this.getChilds();
        final List validated = new ArrayList();
        for (Iterator iter = childs.iterator(); iter.hasNext();) {
            if(this.isCanceled()) {
                // Bail out if canceled by the user
                break;
            }
            Path child = (Path) iter.next();
            log.debug("Validating:" + child);
            if (child.attributes.isFile()) {
                if(this.validateFile(child, resumeRequested, reloadRequested)) {
                    log.info("Adding " + child + " to final set.");
                    validated.add(child);
                }
                else {
                    v.prompt(child);
                }
            }
            if (child.attributes.isDirectory()) {
                if(this.validateDirectory(child)) {
                    log.info("Adding " + child + " to final set.");
                    validated.add(child);
                }
            }
        }
        if(null != v) {
            validated.addAll(v.result());
            if(v.isCanceled()) {
                this.cancel();
            }
        }
        return validated;
    }

    /**
     * @return true if the directory should be added to the <code>queue</code>
     */
    protected boolean validateDirectory(Path path) {
        return true;
    }

    /**
     * @return true if the file should be added to the <code>queue</code>
     */
    protected boolean validateFile(final Path p, final boolean resumeRequested, final boolean reloadRequested) {
        return true;
    }

    /**
     * @see Session#interrupt()
     */
    public void interrupt() {
        this.getSession().interrupt();
    }

    /**
     * Marks all items in the queue as canceled. Canceled items will be
     * skipped when processed. If the transfer is already in a <code>canceled</code>
     * state, the underlying session's socket is interrupted to force exit.
     */
    public void cancel() {
        if(this.isCanceled()) {
            // Called prevously; now force
            this.interrupt();
        }
        else {
            if(this.isInitialized()) {
                for(Iterator iter = this.queue.iterator(); iter.hasNext();) {
                    ((Path) iter.next()).status.setCanceled();
                }
            }
            this.canceled = true;
        }
    }

    /**
     * Recalculate the size of the <code>queue</code>
     */
    protected abstract void reset();

    /**
     *
     * @return True if the transfer has been validated and all files
     * to be transferred written to the internal <code>queue</code>
     */
    public boolean isInitialized() {
        return this.queue != null;
    }

    /**
     * @return The number of roots
     */
    public int numberOfRoots() {
        return this.roots.size();
    }

    /**
     * @return True if the transfer progress is zero and has presumably never
     * been started yet
     */
    public boolean isVirgin() {
        return this.getCurrent() == 0;
    }

    /**
     * @return True if the bytes transferred equal the size of the queue and
     * the bytes transfered is > 0
     */
    public boolean isComplete() {
        return this.getSize() == this.getCurrent() && !(this.getCurrent() == 0);
    }

    /**
     * @return The sum of all file lengths in this queue.
     * Returns -1 if the transfer has
     */
    public double getSize() {
        return this.size; //cached value
    }

    /**
     * Should not be called too frequently as it iterates over all items
     * @return The number of bytes transfered of all items in the <code>queue</code>
     */
    public double getCurrent() {
        if(this.isInitialized()) {
            double size = 0;
            for(Iterator iter = this.queue.iterator(); iter.hasNext();) {
                size += ((Path) iter.next()).status.getCurrent();
            }
            this.current = size;
        }
        return this.current; //cached value
    }

    public String toString() {
        return this.getName();
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + super.toString());
        super.finalize();
    }
}