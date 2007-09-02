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

import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public class UploadTransfer extends Transfer {

    public UploadTransfer(Path root) {
        super(root);
    }

    public UploadTransfer(List roots) {
        super(roots);
    }

    public UploadTransfer(NSDictionary dict, Session s) {
        super(dict, s);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(TransferFactory.KIND_UPLOAD), "Kind");
        return dict;
    }

    protected void init() {
        log.debug("init");
        this.bandwidth = new BandwidthThrottle(
                Preferences.instance().getFloat("queue.upload.bandwidth.bytes"));
    }

    /**
     *
     */
    private abstract class UploadTransferFilter extends TransferFilter {
        public boolean accept(AbstractPath file) {
            return UploadTransfer.this.exists(((Path) file).getLocal());
        }

        public void prepare(Path p) {
            if(p.attributes.isFile()) {
                // Read file size
                size += p.getLocal().attributes.getSize();
                if(p.status.isResume()) {
                    transferred += p.attributes.getSize();
                }
            }
            if(p.attributes.isDirectory()) {
                if(!UploadTransfer.this.exists(p)) {
                    p.cache().put(p, AttributedList.EMPTY_LIST);
                }
            }
        }
    }

    /**
     * A compiled representation of a regular expression.
     */
    private Pattern UPLOAD_SKIP_PATTERN = null;

    {
        try {
            UPLOAD_SKIP_PATTERN = Pattern.compile(
                    Preferences.instance().getProperty("queue.upload.skip.regex"));
        }
        catch(PatternSyntaxException e) {
            log.warn(e.getMessage());
        }
    }

    private final PathFilter childFilter = new PathFilter() {
        public boolean accept(AbstractPath child) {
            if(Preferences.instance().getBoolean("queue.upload.skip.enable")
                    && UPLOAD_SKIP_PATTERN.matcher(child.getName()).matches()) {
                return false;
            }
            return true;
        }
    };

    private final Cache _cache = new Cache();

    public AttributedList childs(final Path parent) {
        if(!this.exists(parent.getLocal())) {
            // Cannot fetch file listing of non existant file
            _cache.put(parent, new AttributedList(Collections.EMPTY_LIST));
        }
        if(!_cache.containsKey(parent)) {
            AttributedList childs = new AttributedList();
            for(Iterator iter = parent.getLocal().childs(new NullComparator(),
                    childFilter).iterator(); iter.hasNext(); ) {
                Path child = PathFactory.createPath(parent.getSession(),
                        parent.getAbsolute(),
                        new Local(((AbstractPath) iter.next()).getAbsolute()));
                childs.add(child);
            }
            _cache.put(parent, childs);
        }
        return _cache.get(parent);
    }

    public boolean isCached(Path file) {
        return _cache.containsKey(file);
    }

    public TransferFilter filter(final TransferAction action) {
        log.debug("filter:"+action);
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return new UploadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(super.accept(p)) {
                        if(p.attributes.isDirectory()) {
                            // Do not attempt to create a directory that already exists
                            return !UploadTransfer.this.exists((Path)p);
                        }
                        return true;
                    }
                    return false;
                }

                public void prepare(final Path p) {
                    if(p.attributes.isFile()) {
                        p.status.setResume(false);
                    }
                    super.prepare(p);
                }

            };
        }
        if(action.equals(TransferAction.ACTION_RESUME)) {
            return new UploadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(super.accept(p)) {
                        if(((Path)p).status.isComplete()) {
                            // No need to resume completed transfers
                            return false;
                        }
                        if(p.attributes.isDirectory()) {
                            return !UploadTransfer.this.exists((Path)p);
                        }
                        return true;
                    }
                    return false;
                }

                public void prepare(final Path p) {
                    if(UploadTransfer.this.exists(p)) {
                        if(p.attributes.getSize() == -1) {
                            p.readSize();
                        }
                        if(p.attributes.getModificationDate() == -1) {
                            p.readTimestamp();
                        }
                        if(p.attributes.getPermission() == null) {
                            p.readPermission();
                        }
                    }
                    if(p.attributes.isFile()) {
                        // Append to file if size is not zero
                        p.status.setResume(UploadTransfer.this.exists(p) && p.attributes.getSize() > 0);
                    }
                    super.prepare(p);
                }
            };
        }
        if(action.equals(TransferAction.ACTION_RENAME)) {
            return new UploadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    // Rename every file
                    return super.accept(p);
                }

                public void prepare(final Path p) {
                    if(UploadTransfer.this.exists(p)) {
                        final String parent = p.getParent().getAbsolute();
                        final String filename = p.getName();
                        String proposal = filename;
                        int no = 0;
                        int index = filename.lastIndexOf(".");
                        while(p.exists()) { // Do not use cached value of exists!
                            no++;
                            if(index != -1 && index != 0) {
                                proposal = filename.substring(0, index)
                                        + "-" + no + filename.substring(index);
                            }
                            else {
                                proposal = filename + "-" + no;
                            }
                            p.setPath(parent, proposal);
                        }
                        log.info("Changed local name to:" + p.getName());
                    }
                    if(p.attributes.isFile()) {
                        p.status.setResume(false);
                    }
                    super.prepare(p);
                }
            };
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return new UploadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(super.accept(p)) {
                        if(!UploadTransfer.this.exists((Path)p)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            for(Iterator iter = this.getRoots().iterator(); iter.hasNext(); ) {
                Path root = (Path)iter.next();
                if(UploadTransfer.this.exists(root)) {
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

    protected void clear() {
        _cache.clear();
        super.clear();
    }

    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        log.debug("action:"+resumeRequested+","+reloadRequested);
        if(resumeRequested) {
            // Force resume
            return TransferAction.ACTION_RESUME;
        }
        if(reloadRequested) {
            return TransferAction.forName(
                    Preferences.instance().getProperty("queue.upload.reload.fileExists")
            );
        }
        // Use default
        return TransferAction.forName(
                Preferences.instance().getProperty("queue.upload.fileExists")
        );
    }

    protected void _transferImpl(final Path p) {
        p.upload(bandwidth, new AbstractStreamListener() {
            public void bytesSent(int bytes) {
                transferred += bytes;
            }
        });
    }
}