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

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class Queue extends Observable {
    protected static Logger log = Logger.getLogger(Queue.class);

    //	private Worker worker;
    private List roots = new ArrayList();
    private List jobs;

    protected double size = -1;
    private double current = 0;
    private double speed;

    private Validator validator;

    private Timer progress;

    private boolean running;
    private boolean canceled;

    /**
     * Creating an empty queue containing no items. Items have to be added later
     * using the <code>addRoot</code> method.
     */
    public Queue() {
        //
    }

    public Queue(Path root) {
        this.roots.add(root);
    }

    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;
    public static final int KIND_SYNC = 2;

    public static Queue createQueue(NSDictionary dict) {
        Queue q = null;
        Object kindObj = dict.objectForKey("Kind");
        if (kindObj != null) {
            int kind = Integer.parseInt((String) kindObj);
            switch (kind) {
                case Queue.KIND_DOWNLOAD:
                    q = new DownloadQueue();
                    break;
                case Queue.KIND_UPLOAD:
                    q = new UploadQueue();
                    break;
                case Queue.KIND_SYNC:
                    q = new SyncQueue();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown queue");
            }
        }
        Object hostObj = dict.objectForKey("Host");
        if (hostObj != null) {
            Host host = new Host((NSDictionary) hostObj);
            Session s = SessionFactory.createSession(host);
            Object rootsObj = dict.objectForKey("Roots");
            if (rootsObj != null) {
                NSArray r = (NSArray) rootsObj;
                for (int i = 0; i < r.count(); i++) {
                    q.addRoot(PathFactory.createPath(s, (NSDictionary) r.objectAtIndex(i)));
                }
            }
        }
        Object sizeObj = dict.objectForKey("Size");
        if (sizeObj != null) {
            q.size = Double.parseDouble((String) sizeObj);
        }
        Object currentObj = dict.objectForKey("Current");
        if (currentObj != null) {
            q.current = Double.parseDouble((String) currentObj);
        }
        return q;
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.getRoot().getHost().getAsDictionary(), "Host");
        NSMutableArray r = new NSMutableArray();
        for (Iterator iter = this.roots.iterator(); iter.hasNext();) {
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
        for (Iterator iter = this.roots.iterator(); iter.hasNext();) {
            name = name + ((Path) iter.next()).getName() + " ";
        }
        return name;
    }

    /**
     * Notify all observers
     *
     * @param arg The message to send to the observers
     * @see ch.cyberduck.core.Message
     */
    public void callObservers(Object arg) {
        this.setChanged();
        this.notifyObservers(arg);
    }

    public List getChilds() {
        List childs = new ArrayList();
        for (Iterator rootIter = this.getRoots().iterator(); rootIter.hasNext() && !this.isCanceled();) {
            this.getChilds(childs, (Path) rootIter.next());
        }
        return childs;
    }

    protected abstract List getChilds(List childs, Path root);

    protected abstract void process(Path p);

    public List getJobs() {
        return this.jobs;
    }

    /**
     * Process the queue. All files will be downloaded/uploaded/synced rerspectively.
     */
    public void process(boolean resumeRequested, boolean shouldValidate) {
        try {
            if (this.init(resumeRequested, shouldValidate)) {
                this.reset();
                for (Iterator iter = this.getJobs().iterator(); iter.hasNext() && !this.isCanceled();) {
                    ((Path) iter.next()).status.reset();
                }
                for (Iterator iter = this.getJobs().iterator(); iter.hasNext() && !this.isCanceled();) {
                    this.process((Path) iter.next());
                }
            }
            else {
                this.cancel();
            }
        }
        catch (IOException e) {
            this.canceled = true;
            this.callObservers(new Message(Message.ERROR, e.getMessage()));
        }
        finally {
            this.finish();
        }
    }

    private boolean init(boolean resumeRequested, boolean shouldValidate)
            throws IOException {
        this.canceled = false;
        this.running = true;
        this.validator = ValidatorFactory.createValidator(this.getClass());
        this.progress = new Timer(500,
                new java.awt.event.ActionListener() {
                    int i = 0;
                    double current;
                    double last;
                    double[] speeds = new double[8];

                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        double diff = 0;
                        current = getCurrent(); // Bytes
                        if (current <= 0) {
                            setSpeed(0);
                        }
                        else {
                            speeds[i] = (current - last) * 2; // Bytes per second
                            i++;
                            last = current;
                            if (i == 8) { // wir wollen immer den schnitt der letzten vier sekunden
                                i = 0;
                            }
                            for (int k = 0; k < speeds.length; k++) {
                                diff = diff + speeds[k]; // summe der differenzen zwischen einer halben sekunde
                            }
                            setSpeed((diff / speeds.length)); //Bytes per second
                        }

                    }
                });
        this.progress.start();
        this.jobs = null;
        this.callObservers(new Message(Message.QUEUE_START));

        this.getSession().check();

        if (shouldValidate) {
            List childs = this.getChilds();
            if (!this.isCanceled()) {
                if (this.validator.validate(childs, resumeRequested)) {
                    if (this.validator.getValidated().size() > 0) {
                        this.jobs = this.validator.getValidated();
                        return true;
                    }
                }
            }
            return false;
        }
        this.jobs = this.getChilds();
        return true;
    }

    protected void finish() {
        this.running = false;
        this.progress.stop();
        this.getRoot().getSession().close();
    }

    public void cancel() {
        if (this.isInitialized()) {
            for (Iterator iter = this.jobs.iterator(); iter.hasNext();) {
                ((Path) iter.next()).status.setCanceled(true);
            }
        }
        this.canceled = true;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public boolean isRunning() {
        return this.running;
    }

    protected abstract void reset();

    public boolean isInitialized
            () {
        return this.getJobs() != null;
    }

    public int numberOfRoots
            () {
        return this.roots.size();
    }

    public boolean isComplete
            () {
        return this.getSize() == this.getCurrent();
    }

    public double getSize
            () {
        return this.size; //cached value
    }

    public String getSizeAsString
            () {
        return Status.getSizeAsString(this.getSize());
    }

    public double getCurrent
            () {
        if (this.isInitialized()) {
            double size = 0;
            for (Iterator iter = this.getJobs().iterator(); iter.hasNext();) {
                size += ((Path) iter.next()).status.getCurrent();
            }
            this.current = size;
        }
        return this.current; //cached value
    }

    public String getCurrentAsString
            () {
        return Status.getSizeAsString(this.getCurrent());
    }

    /**
     * @return double current bytes/second
     */
    public String getSpeedAsString
            () {
        if (this.isRunning() && this.isInitialized()) {
            if (this.getSpeed() > -1) {
                return "(" + Status.getSizeAsString(this.getSpeed()) + "/sec)";
            }
        }
        return "";
    }

    /**
     * @return The bytes being processed per second
     */
    public double getSpeed
            () {
        return this.speed;
    }

    private void setSpeed
            (double s) {
        this.speed = s;
    }
}