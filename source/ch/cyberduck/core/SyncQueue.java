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
import ch.cyberduck.ui.cocoa.CDValidatorController;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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

    public void fireQueueStoppedEvent() {
        if(this.isComplete() && !this.isCanceled() && !(this.getCurrent() == 0)) {
            this.getSession().message(
                    NSBundle.localizedString("Synchronization complete", "Growl", "Growl Notification"));
            Growl.instance().notify(
                    NSBundle.localizedString("Synchronization complete", "Growl", "Growl Notification"),
                    this.getName());
        }
        super.fireQueueStoppedEvent();
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
                    for(Iterator i = p.list().iterator(); i.hasNext();) {
                        if(this.isCanceled()) {
                            break;
                        }
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
            if(p.compare() > 0) {
                this.size += p.getRemote().attributes.getSize();
            }
            else {
                this.size += p.getLocal().attributes.getSize();
            }
        }
    }

    protected void transfer(final Path p) {
        p.sync();
    }

    protected boolean validateFile(final Path p, final boolean resumeRequsted, final boolean reloadRequested) {
        log.debug("Prompting for file:" + p.getName());
        return false;
    }

    protected boolean validateDirectory(Path p) {
        return !p.getRemote().exists() || !p.getLocal().exists();
    }
}