package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import org.apache.log4j.Logger;

import ch.cyberduck.ui.cocoa.CDQueueController;

/**
 * @version $Id$
 */
public class Queue implements Observer { //Thread {
    private static Logger log = Logger.getLogger(Queue.class);

    /**
     * Estimation time till end of processing
     */
    private Timer leftTimer;
    /**
     * File transfer pogress
     */
    private Timer progressTimer;
    /**
     * Time left since start of processing
     */
    private Timer elapsedTimer;

    private Calendar calendar = Calendar.getInstance();

    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;
    /**
     * What kind of this, either KIND_DOWNLOAD or KIND_UPLOAD
     */
    private int kind;

    /**
     * The file currently beeing processed in the queue
     */
    private Path currentJob;

    /**
     * The root of the queue; either the file itself or the parent directory of all files
     */
    private List roots = new ArrayList();

    public Path getRoot() {
        return (Path)roots.get(0);
    }

    /**
     * This has the same size as the roots and contains the root
     * path itself and all subelements (in case of a directory)
     */
    private List jobs = new ArrayList();

    /**
     * The this has been canceled from processing for any reason
     */
    private boolean running;
    private boolean canceled;

    /*
     * 	current speed (bytes/second)
     */
    private long speed;

    private long timeLeft = -1;

    private String status = "";
//    private String error = "";

    /**
     * Creating an empty queue containing no items. Items have to be added later
     * using the <code>addRoot</code> method.
     * 
     * @param kind Either <code>KIND_DOWNLOAD</code> or <code>KIND_UPLOAD</code>
     */
    public Queue(int kind) {
        this.kind = kind;
        this.init();
    }

    public Queue(NSDictionary dict) {
        Object kindObj = dict.objectForKey("Kind");
        if (kindObj != null) {
            this.kind = Integer.parseInt((String)kindObj);
        }
        Object hostObj = dict.objectForKey("Host");
        if (hostObj != null) {
            Host host = new Host((NSDictionary)hostObj);
            Session s = SessionFactory.createSession(host);
            Object rootsObj = dict.objectForKey("Roots");
            if (rootsObj != null) {
                NSArray r = (NSArray)rootsObj;
                for (int i = 0; i < r.count(); i++) {
                    this.addRoot(PathFactory.createPath(s, (NSDictionary)r.objectAtIndex(i)));
                }
            }
            Object itemsObj = dict.objectForKey("Items");
            if (itemsObj != null) {
                NSArray items = (NSArray)itemsObj;
                if (null != items) {
                    for (int i = 0; i < items.count(); i++) {
                        this.jobs.add(PathFactory.createPath(s, (NSDictionary)items.objectAtIndex(i)));
                    }
                }
            }
        }
        this.init();
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.kind + "", "Kind");
        dict.setObjectForKey(this.getRoot().getHost().getAsDictionary(), "Host");
        NSMutableArray r = new NSMutableArray();
        for (Iterator iter = this.roots.iterator(); iter.hasNext();) {
            r.addObject(((Path)iter.next()).getAsDictionary());
        }
        dict.setObjectForKey(r, "Roots");
        NSMutableArray items = new NSMutableArray();
        for (Iterator iter = jobs.iterator(); iter.hasNext();) {
            items.addObject(((Path)iter.next()).getAsDictionary());
        }
        dict.setObjectForKey(items, "Items");
        return dict;
    }

    /**
     * Add an item to the queue
     *
     * @param item The path to be added in the queue
     */
    public void addRoot(Path item) {
        if (log.isDebugEnabled()) {
            log.debug("add:" + item);
        }
        this.roots.add(item);
    }

    public List getRoots() {
        return this.roots;
    }

    /**
     * @return Either <code>KIND_DOWNLOAD</code> or <code>KIND_UPLOAD</code>
     */
    public int kind() {
        return this.kind;
    }

    public void update(Observable o, Object arg) {
        if (arg instanceof Message) {
            Message msg = (Message)arg;
            if (msg.getTitle().equals(Message.DATA)) {
                CDQueueController.instance().update(this, arg);
            }
            else if (msg.getTitle().equals(Message.PROGRESS)) {
                this.status = (String)msg.getContent();
                CDQueueController.instance().update(this, arg);
            }
            else if (msg.getTitle().equals(Message.ERROR)) {
                // this.error = " : "+(String)msg.getContent();
                CDQueueController.instance().update(this, arg);
            }
        }
    }

    /**
     * Process the queue. All files will be downloaded or uploaded rerspectively.
     *
     * @param validator A callback class where the user can decide what to do if
     *                  the file already exists at the download or upload location respectively
     */
    public synchronized void start(final Validator validator) {
        log.debug("start");
//        this.error = "";
        this.jobs.clear();
        new Thread() {
            public void run() {
                int mypool = NSAutoreleasePool.push();

                Queue.this.elapsedTimer.start();
                Queue.this.running = true;
                Queue.this.canceled = false;
                CDQueueController.instance().update(Queue.this, new Message(Message.QUEUE_START));

                Queue.this.getRoot().getSession().addObserver(Queue.this);
                Queue.this.getRoot().getSession().cache().clear();
                for (Iterator i = roots.iterator(); i.hasNext() && !Queue.this.isCanceled();) {
                    Path r = (Path)i.next();
                    log.debug("Iterating over childs of " + r);
                    Iterator childs = r.getChilds(Queue.this.kind).iterator();
                    while (childs.hasNext() && !Queue.this.isCanceled()) {
                        Path child = (Path)childs.next();
                        log.debug("Adding " + child.getName() + " as child to queue.");
                        Queue.this.jobs.add(child);
                    }
                }

                for (Iterator iter = jobs.iterator(); iter.hasNext() && !Queue.this.isCanceled();) {
                    Path item = (Path)iter.next();
                    log.debug("Validating " + item.toString());
                    if (!validator.validate(item)) {
                        iter.remove();
                    }
                    item.status.reset();
                }

                Queue.this.progressTimer.start();
                Queue.this.leftTimer.start();
                for (Iterator iter = jobs.iterator(); iter.hasNext() && !Queue.this.isCanceled();) {
                    Queue.this.currentJob = (Path)iter.next();
                    Queue.this.currentJob.status.addObserver(Queue.this);

                    switch (kind) {
                        case KIND_DOWNLOAD:
                            Queue.this.currentJob.download();
                            break;
                        case KIND_UPLOAD:
                            Queue.this.currentJob.upload();
                            break;
                    }

                    Queue.this.currentJob.status.deleteObserver(Queue.this);
                }
                Queue.this.progressTimer.stop();
                Queue.this.leftTimer.stop();

                Queue.this.getRoot().getSession().close();
                Queue.this.getRoot().getSession().deleteObserver(Queue.this);

                Queue.this.running = false;
                Queue.this.elapsedTimer.stop();
                CDQueueController.instance().update(Queue.this, new Message(Message.QUEUE_STOP));

                NSAutoreleasePool.pop(mypool);
            }
        }.start();
    }

    /**
     * Stops the currently running thread processing the queue.
     *
     * @pre The thread must be running
     */
    public void cancel() {
        //this.currentJob.status.setCanceled(true);
        for (Iterator iter = jobs.iterator(); iter.hasNext();) {
            ((Path)iter.next()).status.setCanceled(true);
        }
        this.canceled = true;
    }

    /**
     * @return True if this queue's thread is running
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * @return True if the processing of the queue has been stopped,
     *         either becasuse the transfers have all been completed or
     *         been cancled by the user.
     */
    private boolean isCanceled() {
        return this.canceled;
    }

    public int numberOfRoots() {
        return this.roots.size();
    }

    /**
     * @return Number of jobs in the this.
     */
    public int numberOfJobs() {
        return this.jobs.size();
    }

    /**
     * @return rue if all items in the this queue have been processed sucessfully.
     */
    public boolean isComplete() {
        return this.getSize() == this.getCurrent();
    }

    public String getStatus() {
        return this.getElapsedTime() + " " + this.status;
//        return this.getElapsedTime() + " " + this.status + " " + error;
    }

    public String getProgress() {
        return this.getCurrentAsString() + " of " + this.getSizeAsString();
    }

    /**
     * @return The cummulative file size of all files remaining in the this
     */
    public long getSize() {
        return this.calculateTotalSize();
    }


    private long calculateTotalSize() {
        long value = 0;
        for (Iterator iter = jobs.iterator(); iter.hasNext();) {
            value += ((Path)iter.next()).status.getSize();
        }
        return value;
    }

    public String getSizeAsString() {
        return Status.getSizeAsString(this.getSize());
    }

    /**
     * @return The number of bytes already processed of all elements in the whole this.
     */
    public long getCurrent() {
        return this.calculateCurrentSize();
    }

    private long calculateCurrentSize() {
        long value = 0;
        for (Iterator iter = jobs.iterator(); iter.hasNext();) {
            value += ((Path)iter.next()).status.getCurrent();
        }
        return value;
    }

    public String getCurrentAsString() {
        return Status.getSizeAsString(this.getCurrent());
    }

    /**
     * @return double current bytes/second
     */
    public String getSpeedAsString() {
        if (this.isRunning()) {
            if (this.getSpeed() > -1) {
                return Status.getSizeAsString(this.getSpeed()) + "/sec";
            }
        }
        return "";
    }

    /**
     * @return The bytes being processed per second
     */
    public long getSpeed() {
        return this.speed;
    }

    private void setSpeed(long s) {
        this.speed = s;
    }

    private void setTimeLeft(int seconds) {
        this.timeLeft = seconds;
    }

    public String getTimeLeft() {
        if (this.isRunning()) {
            //@todo: implementation of better 'time left' management.
            if (this.timeLeft != -1) {
                if (this.timeLeft >= 60) {
                    return (int)this.timeLeft / 60 + " minutes remaining.";
                }
                else {
                    return this.timeLeft + " seconds remaining.";
                }
            }
        }
        return "";
    }

    public String getElapsedTime() {
        if (calendar.get(Calendar.HOUR) > 0) {
            return this.parseTime(calendar.get(Calendar.HOUR))
                    + ":"
                    + parseTime(calendar.get(Calendar.MINUTE))
                    + ":"
                    + parseTime(calendar.get(Calendar.SECOND));
        }
        else {
            return this.parseTime(calendar.get(Calendar.MINUTE))
                    + ":"
                    + parseTime(calendar.get(Calendar.SECOND));
        }
    }

    private String parseTime(int t) {
        if (t > 9) {
            return String.valueOf(t);
        }
        else {
            return "0" + t;
        }
    }

    private void init() {
        log.debug("init");

        this.calendar.set(Calendar.HOUR, 0);
        this.calendar.set(Calendar.MINUTE, 0);
        this.calendar.set(Calendar.SECOND, 0);
        this.calendar.set(Calendar.HOUR, 0);
        this.calendar.set(Calendar.MINUTE, 0);
        this.calendar.set(Calendar.SECOND, 0);
        this.elapsedTimer = new Timer(1000,
                new ActionListener() {
                    int seconds = 0;
                    int minutes = 0;
                    int hours = 0;

                    public void actionPerformed(ActionEvent event) {
                        seconds++;
                        // calendar.set(year, mont, date, hour, minute, second)
                        // >= one hour
                        if (seconds >= 3600) {
                            hours = (int)(seconds / 60 / 60);
                            minutes = (int)((seconds - hours * 60 * 60) / 60);
                            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), hours, minutes, seconds - minutes * 60);
                        }
                        else {
                            // >= one minute
                            if (seconds >= 60) {
                                minutes = (int)(seconds / 60);
                                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), minutes, seconds - minutes * 60);
                            }
                            // only seconds
                            else {
                                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), seconds);
                            }
                        }
                        CDQueueController.instance().update(Queue.this, new Message(Message.PROGRESS));
//                        Queue.this.callObservers(new Message(Message.PROGRESS));
                    }
                });

        this.progressTimer = new Timer(500,
                new ActionListener() {
                    int i = 0;
                    long current;
                    long last;
                    long[] speeds = new long[8];

                    public void actionPerformed(ActionEvent e) {
                        long diff = 0;
                        current = currentJob.status.getCurrent(); // Bytes
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
                            Queue.this.setSpeed((diff / speeds.length)); //Bytes per second
                        }

                    }
                });

        this.leftTimer = new Timer(1000,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (getSpeed() > 0) {
                            Queue.this.setTimeLeft((int)((Queue.this.getSize() - currentJob.status.getCurrent()) / getSpeed()));
                        }
                        else {
                            Queue.this.setTimeLeft(-1);
                        }
                    }
                });
    }
}