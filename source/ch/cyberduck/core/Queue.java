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

import ch.cyberduck.ui.cocoa.threading.BackgroundAction;
import ch.cyberduck.ui.cocoa.threading.BackgroundException;

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
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @version $Id$
 */
public abstract class Queue extends NSObject {
    protected static Logger log = Logger.getLogger(Queue.class);

    /**
     *
     */
    private List roots = new ArrayList();

    /**
     * Contains all references to #Path
     */
    protected List jobs;

    /**
     *
     */
    protected double size = -1;

    /**
     *
     */
    private double current = 0;

    /**
     * The transfer has been canceled and not continue any forther processing
     */
    private boolean canceled;

    /**
     * Creating an empty queue containing no items. Items have to be added later
     * using the <code>addRoot</code> method.
     */
    public Queue() {

    }

    /**
     *
     * @param root
     */
    public Queue(Path root) {
        this.roots.add(root);
    }

    private Vector queueListeners = new Vector();

    /**
     * @param listener
     */
    public void addListener(QueueListener listener) {
        queueListeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeListener(QueueListener listener) {
        queueListeners.remove(listener);
    }

    protected void fireQueueStartedEvent() {
        running = true;
        QueueListener[] l = (QueueListener[]) queueListeners.toArray(new QueueListener[]{});
        for(int i = 0; i < l.length; i++) {
            l[i].queueStarted();
        }
    }

    protected void fireQueueStoppedEvent() {
        running = false;
        QueueListener[] l = (QueueListener[]) queueListeners.toArray(new QueueListener[]{});
        for(int i = 0; i < l.length; i++) {
            l[i].queueStopped();
        }
    }

    protected void fireTransferStartedEvent(Path path) {
        QueueListener[] l = (QueueListener[]) queueListeners.toArray(new QueueListener[]{});
        for(int i = 0; i < l.length; i++) {
            l[i].transferStarted(path);
        }
    }

    protected void fireTransferStoppedEvent(Path path) {
        QueueListener[] l = (QueueListener[]) queueListeners.toArray(new QueueListener[]{});
        for(int i = 0; i < l.length; i++) {
            l[i].transferStopped(path);
        }
    }

    public Queue(NSDictionary dict) {
        Object hostObj = dict.objectForKey("Host");
        if(hostObj != null) {
            Host host = new Host((NSDictionary) hostObj);
            Session s = SessionFactory.createSession(host);
            Object rootsObj = dict.objectForKey("Roots");
            if(rootsObj != null) {
                NSArray r = (NSArray) rootsObj;
                for(int i = 0; i < r.count(); i++) {
                    this.addRoot(PathFactory.createPath(s, (NSDictionary) r.objectAtIndex(i)));
                }
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
     * Add an item to the queue
     *
     * @param item The path to be added in the queue
     */
    public void addRoot(Path item) {
        this.roots.add(item);
    }

    public Path getRoot() {
        return (Path) roots.get(0);
    }

    public List getRoots() {
        return this.roots;
    }

    public Session getSession() {
        return this.getRoot().getSession();
    }

    public Host getHost() {
        return this.getSession().getHost();
    }

    public String getName() {
        String name = "";
        for(Iterator iter = this.roots.iterator(); iter.hasNext();) {
            name = name + ((Path) iter.next()).getLocal().getName() + " ";
        }
        return name;
    }

    public List getChilds() {
        List childs = new ArrayList();
        for(Iterator rootIter = this.getRoots().iterator(); rootIter.hasNext() && !this.isCanceled();) {
            this.getChilds(childs, (Path) rootIter.next());
        }
        return childs;
    }

    /**
     * @param childs
     * @param root
     */
    protected abstract List getChilds(List childs, Path root);

    /**
     * @param tokenizer
     * @param filename
     */
    protected boolean isSkipped(StringTokenizer tokenizer, String filename) {
        while(tokenizer.hasMoreTokens()) {
            if(tokenizer.nextToken().equals(filename)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param p
     */
    protected abstract void transfer(Path p);

    /**
     * Display file validation dialog sheet prompting the user about duplicate files
     */
    private boolean interactive;

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

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
     * Process the queue. All files will be downloaded/uploaded/synced rerspectively.
     */
    public void run() {
        try {
            this.canceled = false;
            this.fireQueueStartedEvent();
            try {
                this.getSession().connect();
            }
            catch(IOException e) {
                this.getSession().error("Connection failed", e);
                this.cancel();
            }
            if(this.isCanceled()) {
                return;
            }
            List validated = this.validate();
            if(this.isCanceled()) {
                return;
            }
            this.jobs = validated;
            this.reset();
            for(Iterator iter = jobs.iterator(); iter.hasNext();) {
                if(!this.getSession().isConnected()) {
                    this.cancel();
                }
                if(this.isCanceled()) {
                    return;
                }
                final Path path = (Path)iter.next();
                this.fireTransferStartedEvent(path);
                this.transfer(path);
                this.fireTransferStoppedEvent(path);
            }
        }
        finally {
            this.getSession().close();
            this.getSession().cache().clear();
            this.fireQueueStoppedEvent();
        }
    }

    private List validate() {
        final List childs = this.getChilds();
        final List validated = new ArrayList();
        Validator v = null;
        for (Iterator iter = childs.iterator(); iter.hasNext();) {
            if(this.isCanceled()) {
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
                    if(interactive) {
                        if(null == v) {
                            v = ValidatorFactory.create(this);
                        }
                        v.prompt(child);
                    }
//                    else {
//                        // In cases we run withtout prompting the user, then just overwrite files.
//                        // This is when running from an AppleScript
//                        // e.g. CDBrowserController#handleUploadScriptCommand
//                        child.status.setResume(false);
//                        validated.add(child);
//                    }
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
     *
     * @param path
     * @return true if the directory should be added to the queue
     */
    protected boolean validateDirectory(Path path) {
        return true;
    }

    /**
     *
     * @param p
     * @param resumeRequested
     * @return true if the file should be added to the queue
     */
    protected boolean validateFile(final Path p, final boolean resumeRequested, final boolean reloadRequested) {
        return true;
    }

    public void interrupt() {
        this.getSession().interrupt();
    }

    public void cancel() {
        if(this.isCanceled()) {
            // Called prevously; now force
            this.interrupt();
        }
        else {
            if(this.isInitialized()) {
                for(Iterator iter = this.jobs.iterator(); iter.hasNext();) {
                    ((Path) iter.next()).status.setCanceled();
                }
            }
            this.canceled = true;
        }
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    /**
     *
     */
    private boolean running;

    public boolean isRunning() {
        return running;
    }

    /**
     * Reset this queue; e.g. recalculating its size
     */
    protected abstract void reset();

    public boolean isInitialized() {
        return this.jobs != null;
    }

    public int numberOfRoots() {
        return this.roots.size();
    }

    public boolean isComplete() {
        return this.getSize() == this.getCurrent();
    }

    public double getSize() {
        return this.size; //cached value
    }

    public double getCurrent() {
        if(this.isInitialized()) {
            double size = 0;
            for(Iterator iter = this.jobs.iterator(); iter.hasNext();) {
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