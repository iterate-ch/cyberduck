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
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class SyncTransfer extends Transfer {

    public SyncTransfer() {
        super();
    }

    public SyncTransfer(Path root) {
        super(root);
    }

    public SyncTransfer(NSDictionary dict, Session s) {
        super(dict, s);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(TransferFactory.KIND_SYNC), "Kind");
        return dict;
    }

    public String getName() {
        return NSBundle.localizedString("Synchronize", "") + " " + this.getRoot().getAbsolute() + " "
                + NSBundle.localizedString("with", "") + " " + this.getRoot().getLocal().getName();
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
                        if(!Preferences.instance().getBoolean("queue.upload.skip.enable")
                                || !UPLOAD_SKIP_PATTERN.matcher(child.getName()).matches()) {
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
                        if(!Preferences.instance().getBoolean("queue.download.skip.enable")
                                || !DOWNLOAD_SKIP_PATTERN.matcher(child.getName()).matches()) {
                            this.addRemoteChilds(childs, child);
                        }
                    }
                }
            }
        }
    }

    protected void getChilds(List childs, Path root) {
        this.addRemoteChilds(childs, root);
        this.addLocalChilds(childs, root);
    }

    protected void reset() {
        this.size = 0;
        for(Iterator iter = this.queue.iterator(); iter.hasNext();) {
            Path p = (Path) iter.next();
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