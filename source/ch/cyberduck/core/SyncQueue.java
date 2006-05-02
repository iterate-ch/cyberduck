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

import ch.cyberduck.ui.cocoa.growl.Growl;
import ch.cyberduck.ui.cocoa.CDSyncQueueValidatorController;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class SyncQueue extends Queue {

    public SyncQueue() {
        super();
    }

    public SyncQueue(Path root) {
        super(root);
    }

    public SyncQueue(NSDictionary dict) {
        super(dict);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(QueueFactory.KIND_SYNC), "Kind");
        return dict;
    }

    public String getName() {
        return NSBundle.localizedString("Synchronize", "") + " " + this.getRoot().getAbsolute() + " "
                + NSBundle.localizedString("with", "") + " " + this.getRoot().getLocal().getName();
    }

    public void queueStopped(boolean headless) {
        super.queueStopped(headless);
        if(this.isComplete() && !this.isCanceled()) {
            this.getSession().message(
                    NSBundle.localizedString("Synchronization complete", "Growl", "Growl Notification"));
            Growl.instance().notify(
                    NSBundle.localizedString("Synchronization complete", "Growl", "Growl Notification"),
                    this.getName());
        }
    }

    private void addLocalChilds(List childs, Path p) {
        if(!this.isCanceled()) {
            if(p.getLocal().exists()) {// && p.getLocal().canRead()) {
                if(!childs.contains(p)) {
                    childs.add(p);
                }
                if(p.attributes.isDirectory()) {
                    if(!p.getRemote().exists()) {
                        //hack
                        p.getSession().cache().put(p, new AttributedList());
                    }
                    File[] files = p.getLocal().listFiles();
                    for(int i = 0; i < files.length; i++) {
                        Path child = PathFactory.createPath(p.getSession(), p.getAbsolute(),
                                new Local(files[i].getAbsolutePath()));
                        if(!this.isSkipped(new StringTokenizer(
                                Preferences.instance().getProperty("queue.download.skip")), child.getName())) {
                            this.addLocalChilds(childs, child);
                        }
                    }
                }
            }
        }
    }

    private void addRemoteChilds(List childs, Path p) {
        if(!this.isCanceled()) {
            if(p.getRemote().exists()) {
                if(!childs.contains(p)) {
                    childs.add(p);
                }
                if(p.attributes.isDirectory() && !p.attributes.isSymbolicLink()) {
                    p.attributes.setSize(0);
                    List files = p.list();
                    if(null == files) {
                        return;
                    }
                    for(Iterator i = files.iterator(); i.hasNext();) {
                        Path child = (Path) i.next();
                        child.setLocal(new Local(p.getLocal(), child.getName()));
                        if(!this.isSkipped(new StringTokenizer(
                                Preferences.instance().getProperty("queue.upload.skip")), child.getName())) {
                            this.addRemoteChilds(childs, child);
                        }
                    }
                }
            }
        }
    }

    protected List getChilds(List childs, Path root) {
        this.addRemoteChilds(childs, root);
        this.addLocalChilds(childs, root);
        return childs;
    }

    protected void reset() {
        this.size = 0;
        for(Iterator iter = this.jobs.iterator(); iter.hasNext();) {
            Path p = ((Path) iter.next());
            if(p.getRemote().exists() && p.getLocal().exists()) {
                Calendar remote = this.getCalendar(
                        p.getRemote().attributes.getTimestamp(),
                        Calendar.MINUTE);
                Calendar local = this.getCalendar(
                        p.getLocal().getTimestamp(),
                        Calendar.MINUTE);
                if(local.before(remote)) {
                    this.size += p.getRemote().attributes.getSize();
                }
                if(local.after(remote)) {
                    this.size += p.getLocal().getSize();
                }
            }
            else if(p.getRemote().exists()) {
                this.size += p.getRemote().attributes.getSize();
            }
            else if(p.getLocal().exists()) {
                this.size += p.getLocal().getSize();
            }
        }
    }

    protected void transfer(Path p) {
        try {
            Preferences.instance().setProperty("queue.upload.preserveDate.fallback", true);
            if (p.getRemote().exists() && p.getLocal().exists()) {
                if (p.attributes.isFile()) {
                    Calendar remote = this.getCalendar(
                            p.getRemote().attributes.getTimestamp(),
                            Calendar.MINUTE);
                    Calendar local = this.getCalendar(
                            p.getLocal().getTimestamp(),
                            Calendar.MINUTE);
                    if (local.before(remote)) {
                        p.download();
                    }
                    if (local.after(remote)) {
                        p.upload();
                    }
                }
            }
            else if (p.getRemote().exists()) {
                p.download();
            }
            else if (p.getLocal().exists()) {
                p.upload();
            }
        }
        finally {
            Preferences.instance().setProperty("queue.upload.preserveDate.fallback", false);
        }
    }

    public Calendar getCalendar(final long timestamp, final int precision) {
        Calendar c = Calendar.getInstance(); //default timezone
        c.setTimeInMillis(timestamp); //UTC milliseconds!
        if(precision == Calendar.MILLISECOND) {
            return c;
        }
        c.clear(Calendar.MILLISECOND);
        if(precision == Calendar.SECOND) {
            return c;
        }
        c.clear(Calendar.SECOND);
        if(precision == Calendar.MINUTE) {
            return c;
        }
        c.clear(Calendar.MINUTE);
        if(precision == Calendar.HOUR) {
            return c;
        }
        c.clear(Calendar.HOUR);
        return c;
    }

    protected Validator getValidator() {
        return new CDSyncQueueValidatorController(this);
    }
}