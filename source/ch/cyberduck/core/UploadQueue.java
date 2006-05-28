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

import ch.cyberduck.ui.cocoa.CDUploadQueueValidatorController;
import ch.cyberduck.ui.cocoa.growl.Growl;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @version $Id$
 */
public class UploadQueue extends Queue {

    public UploadQueue() {
        super();
    }

    public UploadQueue(Path root) {
        super(root);
    }

    public UploadQueue(NSDictionary dict) {
        super(dict);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(QueueFactory.KIND_UPLOAD), "Kind");
        return dict;
    }

    public void fireQueueStoppedEvent() {
        if(this.isComplete() && !this.isCanceled()) {
            this.getSession().message(
                    NSBundle.localizedString("Upload complete", "Growl", "Growl Notification"));
            Growl.instance().notify(
                    NSBundle.localizedString("Upload complete", "Growl", "Growl Notification"),
                    this.getName());
        }
        super.fireQueueStoppedEvent();
    }

    protected List getChilds(List childs, Path p) {
        if(!this.isCanceled()) {
            if(p.getLocal().exists()) {// && p.getLocal().canRead()) {
                childs.add(p);
                if(p.attributes.isDirectory()) {
                    if(!p.getRemote().exists()) {
                        //hack
                        p.getSession().cache().put(p, new AttributedList());
                    }
                    p.attributes.setSize(0);
                    File[] files = p.getLocal().listFiles();
                    for(int i = 0; i < files.length; i++) {
                        if(this.isCanceled()) {
                            break;
                        }
                        if(files[i].canRead()) {
                            Path child = PathFactory.createPath(p.getSession(), p.getAbsolute(),
                                    new Local(files[i].getAbsolutePath()));
                            if(!this.isSkipped(new StringTokenizer(
                                    Preferences.instance().getProperty("queue.upload.skip")), child.getName())) {
                                this.getChilds(childs, child);
                            }
                        }
                    }
                }
            }
        }
        return childs;
    }

    protected void reset() {
        this.size = 0;
        for(java.util.Iterator iter = this.jobs.iterator(); iter.hasNext();) {
            this.size += ((Path) iter.next()).getLocal().getSize();
        }
    }

    protected void transfer(Path p) {
        p.upload();
    }

    protected Validator getValidator() {
        return new CDUploadQueueValidatorController(this);
    }
}