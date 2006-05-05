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

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @version $Id$
 */
public abstract class Queue extends NSObject implements QueueListener {
    protected static Logger log = Logger.getLogger(Queue.class);

    //	private Worker worker;
    private List roots = new ArrayList();
    protected List jobs;

    protected double size = -1;
    private double current = 0;
    private double speed;

    private Timer progressTimer;

    private boolean running;
    private boolean canceled;

    /**
     * Creating an empty queue containing no items. Items have to be added later
     * using the <code>addRoot</code> method.
     */
    public Queue() {
        ;
    }

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

    public void queueStarted() {
        this.progressTimer = new Timer(500,
                new java.awt.event.ActionListener() {
                    int i = 0;
                    double current;
                    double last;
                    double[] speeds = new double[8];

                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        double diff = 0;
                        current = getCurrent(); // Bytes
                        if(current <= 0) {
                            setSpeed(0);
                        }
                        else {
                            speeds[i] = (current - last) * 2; // Bytes per second
                            i++;
                            last = current;
                            if(i == 8) { // wir wollen immer den schnitt der letzten vier sekunden
                                i = 0;
                            }
                            for(int k = 0; k < speeds.length; k++) {
                                diff = diff + speeds[k]; // summe der differenzen zwischen einer halben sekunde
                            }
                            setSpeed((diff / speeds.length)); //Bytes per second
                        }

                    }
                });
        this.progressTimer.start();
        QueueListener[] l = (QueueListener[]) queueListeners.toArray(new QueueListener[]{});
        for(int i = 0; i < l.length; i++) {
            l[i].queueStarted();
        }
    }

    public void queueStopped() {
        this.progressTimer.stop();
        QueueListener[] l = (QueueListener[]) queueListeners.toArray(new QueueListener[]{});
        for(int i = 0; i < l.length; i++) {
            l[i].queueStopped();
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
            name = name + ((Path) iter.next()).getName() + " ";
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
     * @return
     */
    protected abstract List getChilds(List childs, Path root);

    /**
     * @param tokenizer
     * @param filename
     * @return
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
     * Process the queue. All files will be downloaded/uploaded/synced rerspectively.
     *
     * @param resume   The user requested to resume the transfer
     * @param headless No validation of items in the queue; don't show dialog
     */
    public void run(boolean resume, boolean headless) {
        try {
            this.canceled = false;
            this.queueStarted();
            if(headless) {
                this.jobs = this.getChilds();
            }
            else {
                Validator validator = this.getValidator();
                this.jobs = validator.validate(resume);
            }
            if(this.isCanceled())
                return;
            this.reset();
            for(Iterator iter = this.jobs.iterator(); iter.hasNext() && !this.isCanceled();) {
                ((Path) iter.next()).status.reset();
            }
            for(Iterator iter = this.jobs.iterator(); iter.hasNext() && !this.isCanceled();) {
                this.transfer((Path) iter.next());
            }
        }
        finally {
            this.getSession().close();
            this.getRoot().getSession().cache().clear(); //TODO
            this.queueStopped();
        }
    }

    protected abstract Validator getValidator();

    public void interrupt() {
        this.getSession().interrupt();
    }

    public void cancel() {
        if(canceled) {
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

    public boolean isRunning() {
        return this.getSession().isConnected();
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

    /**
     * @return double current bytes/second
     */
    public String getSpeedAsString() {
        if(this.isRunning() && this.isInitialized()) {
            if(this.getSpeed() > -1) {
                return "(" + Status.getSizeAsString(this.getSpeed()) + "/sec)";
            }
        }
        return "";
    }

    /**
     * @return The bytes being processed per second
     */
    public double getSpeed() {
        return this.speed;
    }

    private void setSpeed(double s) {
        this.speed = s;
    }

    public String toString() {
        return this.getName();
    }
}