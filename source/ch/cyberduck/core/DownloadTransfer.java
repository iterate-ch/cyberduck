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

import ch.cyberduck.core.io.BandwidthThrottle;

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public class DownloadTransfer extends Transfer {

    public DownloadTransfer(Path root) {
        super(root);
    }

    public DownloadTransfer(List roots) {
        super(roots);
    }

    public DownloadTransfer(NSDictionary dict, Session s) {
        super(dict, s);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(TransferFactory.KIND_DOWNLOAD), "Kind");
        return dict;
    }

    protected void init() {
        bandwidth = new BandwidthThrottle(
                Preferences.instance().getInteger("queue.download.bandwidth.bytes"));
    }

    /**
     *
     */
    private abstract class DownloadTransferFilter extends TransferFilter {
        public void prepare(Path file) {
            // Adjust the download path
            file.setLocal(new Local(file.getLocal().getParent().getAbsolute(), file.getName()));
            // Read file size
            if(file.attributes.isFile()) {
                size += file.attributes.getSize();
                if(file.status.isResume()) {
                    transferred += file.getLocal().attributes.getSize();
                }
            }
            super.prepare(file);
        }
    }

    /**
     * A compiled representation of a regular expression.
     */
    private Pattern DOWNLOAD_SKIP_PATTERN = null;

    {
        try {
            DOWNLOAD_SKIP_PATTERN = Pattern.compile(
                    Preferences.instance().getProperty("queue.download.skip.regex"));
        }
        catch(PatternSyntaxException e) {
            log.warn(e.getMessage());
        }
    }

    public List childs(final Path parent) {
        if(!this.exists(parent)) {
            // Cannot fetch file listing of non existant file
            return Collections.EMPTY_LIST;
        }
        if(parent.attributes.isSymbolicLink()) {
            return Collections.EMPTY_LIST;
        }
        return parent.childs(new NullComparator(), new TransferFilter() {
            public boolean accept(AbstractPath child) {
                if(Preferences.instance().getBoolean("queue.download.skip.enable")
                        && DOWNLOAD_SKIP_PATTERN.matcher(child.getName()).matches()) {
                    return false;
                }
                ((Path)child).setLocal(new Local(parent.getLocal().getAbsolute(), child.getName()));
                return super.accept(child);
            }
        });
    }

    public boolean isCached(Path file) {
        return file.isCached();
    }

    public TransferFilter filter(final TransferAction action) {
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return new DownloadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(p.attributes.isDirectory()) {
                        return !exists(((Path)p).getLocal());
                    }
                    return super.accept(p);
                }
            };
        }
        if(action.equals(TransferAction.ACTION_RESUME)) {
            return new DownloadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(((Path)p).status.isComplete()) {
                        return false;
                    }
                    if(p.attributes.isDirectory()) {
                        return !exists(((Path)p).getLocal());
                    }
                    return super.accept(p);
                }

                public void prepare(final Path p) {
                    if(p.attributes.isFile()) {
                        p.status.setResume(exists(p) && p.attributes.getSize() > 0);
                    }
                    super.prepare(p);
                }
            };
        }
        if(action.equals(TransferAction.ACTION_SIMILARNAME)) {
            return new DownloadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    return super.accept(p);
                }

                public void prepare(final Path p) {
                    if(exists(p.getLocal())) {
                        final String parent = p.getLocal().getParent().getAbsolute();
                        final String filename = p.getName();
                        String proposal = filename;
                        int no = 0;
                        int index = filename.lastIndexOf(".");
                        while(exists(p.getLocal())) {
                            no++;
                            if(index != -1 && index != 0) {
                                proposal = filename.substring(0, index)
                                        + "-" + no + filename.substring(index);
                            }
                            else {
                                proposal = filename + "-" + no;
                            }
                            p.getLocal().setPath(parent, proposal);
                        }
                        log.info("Changed local name to:" + p.getName());
                    }
                    p.status.setResume(false);
                    
                    super.prepare(p);
                }
            };
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return new DownloadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(!exists(((Path)p).getLocal())) {
                        return super.accept(p);
                    }
                    return false;
                }
            };
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            for(Iterator iter = roots.iterator(); iter.hasNext(); ) {
                Path root = (Path)iter.next();
                if(exists(root.getLocal())) {
                    if(root.getLocal().attributes.isDirectory()) {
                        if(0 == root.getLocal().childs().size()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    // Prompt user to choose a filter
                    TransferAction result = prompt.prompt(this);
                    return this.filter(result); //break out of loop
                }
            }
            // No files exist yet therefore it is most straightforward to use the overwrite action
            return this.filter(TransferAction.ACTION_OVERWRITE);
        }
        return super.filter(action);
    }

    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        if(resumeRequested) {
            // Force resume
            return TransferAction.ACTION_RESUME;
        }
        if(reloadRequested) {
            return TransferAction.forName(
                    Preferences.instance().getProperty("queue.download.reload.fileExists")
            );
        }
        // Use default
        return TransferAction.forName(
                Preferences.instance().getProperty("queue.download.fileExists")
        );
    }

    protected void _transfer(final Path p) {
        p.download(bandwidth, new AbstractStreamListener() {
            public void bytesReceived(int bytes) {
                transferred += bytes;
            }
        });
    }
}