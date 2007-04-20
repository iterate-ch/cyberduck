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

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

    /**
     *
     */
    private abstract class UploadTransferFilter extends TransferFilter {
        public void prepare(Path file) {
            if(file.attributes.isFile()) {
                // Read file size
                size += file.getLocal().attributes.getSize();
                if(file.status.isResume()) {
                    transferred += file.attributes.getSize();
                }
            }
            super.prepare(file);
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

    private final Cache _cache = new Cache();

    public List childs(final Path parent) {
        if(!exists(parent.getLocal())) {
            // Cannot fetch file listing of non existant file
            return Collections.EMPTY_LIST;
        }
        if(parent.getLocal().attributes.isSymbolicLink()) {
            return Collections.EMPTY_LIST;
        }
        if(!exists(parent)) {
            parent.cache().put(parent, new AttributedList());
        }
        if(!_cache.containsKey(parent)) {
            AttributedList childs = new AttributedList();
            for(Iterator iter = parent.getLocal().childs(new NullComparator(), new TransferFilter() {
                public boolean accept(AbstractPath child) {
                    if(Preferences.instance().getBoolean("queue.upload.skip.enable")
                            && UPLOAD_SKIP_PATTERN.matcher(child.getName()).matches()) {
                        return false;
                    }
                    return super.accept(child);
                }
            }).iterator(); iter.hasNext(); ) {
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
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return new UploadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(p.attributes.isDirectory()) {
                        return !exists(p);
                    }
                    return super.accept(p);
                }
            };
        }
        if(action.equals(TransferAction.ACTION_RESUME)) {
            return new UploadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(((Path)p).status.isComplete()) {
                        return false;
                    }
                    if(p.attributes.isDirectory()) {
                        return !exists(p);
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
            return new UploadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    return super.accept(p);
                }

                public void prepare(final Path p) {
                    if(exists(p)) {
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
                    p.status.setResume(false);

                    super.prepare(p);
                }
            };
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return new UploadTransferFilter() {
                public boolean accept(final AbstractPath p) {
                    if(!exists(p)) {
                        return super.accept(p);
                    }
                    return false;
                }
            };
        }
        return super.filter(action);
    }

    protected void clear() {
        _cache.clear();
        super.clear();
    }

    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
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

    protected void _transfer(final Path p) {
        p.upload(new AbstractStreamListener() {
            public void bytesSent(int bytes) {
                transferred += bytes;
            }
        });
    }
}