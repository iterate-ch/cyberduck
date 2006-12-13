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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @version $Id$
 */
public class UploadTransfer extends Transfer {

    public UploadTransfer() {
        super();
    }

    public UploadTransfer(Path root) {
        super(root);
    }

    public UploadTransfer(NSDictionary dict) {
        super(dict);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(TransferFactory.KIND_UPLOAD), "Kind");
        return dict;
    }

    public void fireQueueStoppedEvent() {
        if(this.isComplete() && !this.isCanceled()) {
            this.getSession().message(
                    NSBundle.localizedString("Upload complete", "Growl", "Growl Notification"),
                    this.getName());
        }
        super.fireQueueStoppedEvent();
    }

    protected List getChilds(List childs, Path p) {
        if(!this.isCanceled()) {
            if(p.getLocal().exists()) {// && p.getLocal().canRead()) {
                childs.add(p);
                if(p.exists()) {
                    List list = p.getParent().list();
                    //Honor existing permissions when replacing files
                    p.attributes.setPermission(
                            ((Path)list.get(list.indexOf(p))).attributes.getPermission());
                }
                else {
                    p.attributes.setPermission(new Permission());
                }
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

    protected void transfer(final Path p) {
        p.upload();
    }

    protected boolean validateDirectory(Path p) {
        if(!p.getRemote().exists()) {
            //Directory does not exist yet; include so it will be created on the server
            return true;
        }
        //Directory already exists; do not include as this would throw "file already exists"
        return false;
    }

    protected boolean validateFile(final Path p, final boolean resumeRequested, final boolean reloadRequested) {
        if(resumeRequested) { // resume existing files independant of settings in preferences
            p.status.setResume(p.exists());
            return true;
        }
        String action = null;
        if(reloadRequested) {
            action = Preferences.instance().getProperty("queue.upload.reload.fileExists");
        }
        else {
            action = Preferences.instance().getProperty("queue.upload.fileExists");
        }
        // When overwriting file anyway we don't have to check if the file already exists
        if(action.equals(Validator.OVERWRITE)) {
            log.info("Will overwrite file:" + p.getName());
            p.status.setResume(false);
            return true;
        }
        if(p.exists()) {
            p.readAttributes();
            if(action.equals(Validator.RESUME)) {
                log.debug("Will resume file:" + p.getName());
                p.status.setResume(true);
                return true;
            }
            if(action.equals(Validator.SIMILAR)) {
                log.debug("Will rename file:" + p.getName());
                p.status.setResume(false);
                this.adjustFilename(p);
                log.info("Changed name to:" + p.getName());
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
        final String parent = path.getParent().getAbsolute();
        final String filename = path.getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        while(path.exists()) {
            no++;
            if(index != -1 && index != 0) {
                proposal = filename.substring(0, index)
                        + "-" + no + filename.substring(index);
            }
            else {
                proposal = filename + "-" + no;
            }
            path.setPath(parent, proposal);
        }
    }
}