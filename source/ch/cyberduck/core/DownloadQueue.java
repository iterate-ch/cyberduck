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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @version $Id$
 */
public class DownloadQueue extends Queue {

    public DownloadQueue() {
        super();
    }

    public DownloadQueue(Path root) {
        super(root);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(QueueFactory.KIND_DOWNLOAD), "Kind");
        return dict;
    }

    public DownloadQueue(NSDictionary dict) {
        super(dict);
    }

    public void fireQueueStoppedEvent() {
        if(this.isComplete() && !this.isCanceled() && !(this.getCurrent() == 0)) {
            this.getSession().message(
                    NSBundle.localizedString("Download complete", "Growl", "Growl Notification"));
            Growl.instance().notify(
                    NSBundle.localizedString("Download complete", "Growl", "Growl Notification"),
                    this.getName());
        }
        super.fireQueueStoppedEvent();
    }

    protected List getChilds(List childs, Path p) {
        if(!this.isCanceled()) {
            childs.add(p);
            if(p.attributes.isDirectory() && !p.attributes.isSymbolicLink()) {
                p.attributes.setSize(0);
                for(Iterator i = p.list().iterator(); i.hasNext();) {
                    if(this.isCanceled()) {
                        break;
                    }
                    Path child = (Path) i.next();
                    child.setLocal(new Local(p.getLocal(), child.getName()));
                    if(!this.isSkipped(new StringTokenizer(
                            Preferences.instance().getProperty("queue.download.skip")), child.getName())) {
                        this.getChilds(childs, child);
                    }
                }
            }
        }
        return childs;
    }

    protected void reset() {
        this.size = 0;
        for(Iterator iter = this.jobs.iterator(); iter.hasNext();) {
            this.size += ((Path) iter.next()).attributes.getSize();
        }
    }

    protected void transfer(final Path p) {
        p.download();
    }

    protected boolean validateFile(final Path p, final boolean resumeRequested, final boolean reloadRequested) {
        p.readAttributes();
        if(resumeRequested) { // resume existing files independant of settings in preferences
            p.status.setResume(p.getLocal().exists() && p.getLocal().getSize() > 0);
            return true;
        }
        String action = null;
        if(reloadRequested) {
            action = Preferences.instance().getProperty("queue.download.reload.fileExists");
        }
        else {
            action = Preferences.instance().getProperty("queue.download.fileExists");
        }
        // When overwriting file anyway we don't have to check if the file already exists
        if(action.equals(Validator.OVERWRITE)) {
            log.info("Will overwrite file:" + p.getName());
            p.status.setResume(false);
            return true;
        }
        if(p.getLocal().exists() && p.getLocal().getSize() > 0) {
            if(action.equals(Validator.RESUME)) {
                log.debug("Will resume file:" + p.getName());
                p.status.setResume(true);
                return true;
            }
            if(action.equals(Validator.SIMILAR)) {
                log.debug("Will rename file:" + p.getName());
                p.status.setResume(false);
                this.adjustFilename(p);
                log.info("Changed local name to:" + p.getLocal().getName());
                return true;
            }
            log.debug("Prompting for file:" + p.getName());
            return false;
        }
        else {
            p.status.setResume(false);
            return true;
        }
    }

    private void adjustFilename(Path path) {
        final String parent = path.getLocal().getParent();
        final String filename = path.getLocal().getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        while(path.getLocal().exists()) {
            no++;
            if(index != -1 && index != 0) {
                proposal = filename.substring(0, index)
                        + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
            path.setLocal(new Local(parent, proposal));
        }
    }
}