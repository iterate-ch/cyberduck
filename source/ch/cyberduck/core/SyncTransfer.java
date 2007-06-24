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

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class SyncTransfer extends Transfer {

    public SyncTransfer(Path root) {
        super(root);
    }

    public SyncTransfer(Collection roots) {
        super(roots);
    }

    public SyncTransfer(NSDictionary dict, Session s) {
        super(dict, s);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(TransferFactory.KIND_SYNC), "Kind");
        return dict;
    }

    /**
     * The delegate for files to upload
     */
    private Transfer _delegateUpload;

    /**
     * The delegate for files to download
     */
    private Transfer _delegateDownload;

    protected void init() {
        log.debug("init");
        _delegateUpload = new UploadTransfer(this.roots);
        _delegateDownload = new DownloadTransfer(this.roots);
    }

    public void setBandwidth(float bytesPerSecond) {
        ;
    }

    public float getBandwidth() {
        return BandwidthThrottle.UNLIMITED;
    }

    public String getName() {
        return this.getRoot().getName() + " \u2194 " /*left-right arrow*/ + this.getRoot().getLocal().getName();
    }

    public double getSize() {
        final double size = _delegateDownload.getSize() + _delegateUpload.getSize();
        if(0 == size) {
            return super.getSize();
        }
        return size;
    }

    public double getTransferred() {
        final double transferred = _delegateDownload.getTransferred() + _delegateUpload.getTransferred();
        if(0 == transferred) {
            return super.getTransferred();
        }
        return transferred;
    }

    private Action action = ACTION_MIRROR;

    /**
     * @param action
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     *
     * @return
     */
    public Action getAction() {
        return this.action;
    }

    /**
     *
     */
    public static class Action {
        public boolean equals(Object other) {
            if(null == other) {
                return false;
            }
            return this == other;
        }
    }

    public static final Action ACTION_DOWNLOAD = new Action();
    public static final Action ACTION_UPLOAD = new Action();
    public static final Action ACTION_MIRROR = new Action();

    public TransferFilter filter(final TransferAction action) {
        log.debug("filter:" + action);
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            // When synchronizing, either cancel or overwrite. Resume is not supported
            return new TransferFilter() {
                /**
                 * Download delegate filter
                 */
                private TransferFilter _delegateFilterDownload
                        = _delegateDownload.filter(TransferAction.ACTION_OVERWRITE);

                /**
                 * Upload delegate filter
                 */
                private TransferFilter _delegateFilterUpload
                        = _delegateUpload.filter(TransferAction.ACTION_OVERWRITE);

                public void prepare(Path file) {
                    super.prepare(file);

                    Comparison compare = compare(file);
                    if(compare.equals(COMPARISON_REMOTE_NEWER)) {
                        _delegateFilterDownload.prepare(file);
                    }
                    else if(compare.equals(COMPARISON_LOCAL_NEWER)) {
                        _delegateFilterUpload.prepare(file);
                    }
                }

                public boolean accept(AbstractPath file) {
                    Comparison compare = SyncTransfer.this.compare((Path) file);
                    if(!COMPARISON_EQUAL.equals(compare)) {
                        if(compare.equals(COMPARISON_REMOTE_NEWER)) {
                            if(SyncTransfer.this.action.equals(ACTION_UPLOAD)) {
                                log.info("Skipping "+file);
                                return false;
                            }
                            // Ask the download delegate for inclusion
                            return _delegateFilterDownload.accept(file);
                        }
                        else if(compare.equals(COMPARISON_LOCAL_NEWER)) {
                            if(SyncTransfer.this.action.equals(ACTION_DOWNLOAD)) {
                                log.info("Skipping "+file);
                                return false;
                            }
                            // Ask the upload delegate for inclusion
                            return _delegateFilterUpload.accept(file);
                        }
                    }
                    return false;
                }
            };
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            TransferAction result = prompt.prompt(this);
            return this.filter(result);
        }
        return super.filter(action);
    }

    private final Cache _cache = new Cache();

    public AttributedList childs(final Path parent) {
        if(!_cache.containsKey(parent)) {
            Set childs = new HashSet();
            childs.addAll(_delegateDownload.childs(parent));
            childs.addAll(_delegateUpload.childs(parent));
            _cache.put(parent, new AttributedList(childs));
        }
        return _cache.get(parent);
    }

    public boolean isCached(Path file) {
        return _cache.containsKey(file);
    }

    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        log.debug("action:" + resumeRequested + "," + reloadRequested);
        // Always prompt for synchronization
        return TransferAction.ACTION_CALLBACK;
    }

    protected void _transferImpl(final Path p) {
        Comparison compare = this.compare(p);
        if(compare.equals(COMPARISON_REMOTE_NEWER)) {
            _delegateDownload._transferImpl(p);
        }
        else if(compare.equals(COMPARISON_LOCAL_NEWER)) {
            _delegateUpload._transferImpl(p);
        }
    }

    protected void clear() {
        _comparisons.clear();
        _delegateDownload.clear();
        _delegateUpload.clear();
        _cache.clear();
        super.clear();
    }

    protected void reset() {
        _delegateDownload.reset();
        _delegateUpload.reset();
    }

    private final Map _comparisons = new HashMap();

    /**
     *
     */
    public static class Comparison {
        public boolean equals(Object other) {
            if(null == other) {
                return false;
            }
            return this == other;
        }
    }

    /**
     * Remote file is newer or local file does not exist
     */
    public static final Comparison COMPARISON_REMOTE_NEWER = new Comparison();
    /**
     * Local file is newer or remote file does not exist
     */
    public static final Comparison COMPARISON_LOCAL_NEWER = new Comparison();
    /**
     * Files are identical or directories
     */
    public static final Comparison COMPARISON_EQUAL = new Comparison();

    /**
     * @return > 0 if the remote path exists and is newer than
     *         the local file; < 0 if the local path exists and is newer than
     *         the remote file; 0 if both files don't exist or have an equal timestamp
     */
    public Comparison compare(Path p) {
        if(!_comparisons.containsKey(p)) {
            log.debug("compare:" + p);
            Comparison result = null;
            if(exists(p) && exists(p.getLocal())) {
                if(p.attributes.isDirectory()) {
                    result = COMPARISON_EQUAL;
                }
                if(p.attributes.isFile()) {
                    result = this.compareSize(p);
                    if(result.equals(COMPARISON_EQUAL)) {
                        if(!Preferences.instance().getBoolean("queue.sync.timestamp.ignore")) {
                            //both files have a valid size; compare using timestamp
                            result = this.compareTimestamp(p);
                        }
                    }
                }
            }
            else if(exists(p)) {
                // only the remote file exists
                result = COMPARISON_REMOTE_NEWER;
            }
            else if(exists(p.getLocal())) {
                // only the local file exists
                result = COMPARISON_LOCAL_NEWER;
            }
            else {
                // both files don't exist yet
                result = COMPARISON_EQUAL;
            }
            _comparisons.put(p, result);
        }
        return (Comparison) _comparisons.get(p);
    }

    /**
     * @param p
     * @return
     */
    private Comparison compareSize(Path p) {
        log.debug("compareSize:" + p);
        if(p.attributes.getSize() == -1) {
            p.readSize();
        }
        //fist make sure both files are larger than 0 bytes
        if(p.attributes.getSize() == 0 && p.getLocal().attributes.getSize() == 0) {
            return COMPARISON_EQUAL;
        }
        if(p.attributes.getSize() == 0) {
            return COMPARISON_LOCAL_NEWER;
        }
        if(p.getLocal().attributes.getSize() == 0) {
            return COMPARISON_REMOTE_NEWER;
        }
        //different file size - further comparison check
        return COMPARISON_EQUAL;
    }

    /**
     * @param p
     * @return
     */
    private Comparison compareTimestamp(Path p) {
        log.debug("compareTimestamp:" + p);
        if(p.attributes.getModificationDate() == -1) {
            p.readTimestamp();
        }
        Calendar remote = this.asCalendar(
                p.attributes.getModificationDate(),
//                        -this.getHost().getTimezone().getRawOffset()
                p.getHost().getTimezone(),
                Calendar.SECOND);
        Calendar local = this.asCalendar(p.getLocal().attributes.getModificationDate(),
                TimeZone.getDefault(),
                Calendar.SECOND);
        if(local.before(remote)) {
            return COMPARISON_REMOTE_NEWER;
        }
        if(local.after(remote)) {
            return COMPARISON_LOCAL_NEWER;
        }
        //same timestamp
        return COMPARISON_EQUAL;
    }

    /**
     * @param timestamp
     * @param timezone
     * @param precision
     * @return
     */
    private Calendar asCalendar(final long timestamp, final TimeZone timezone, final int precision) {
        log.debug("asCalendar:" + timestamp);
        Calendar c = Calendar.getInstance(timezone);
        c.setTimeInMillis(timestamp);
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
}