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

import ch.cyberduck.core.ftp.FTPPath;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.ui.growl.Growl;

import java.util.*;

/**
 * @version $Id$
 */
public class SyncTransfer extends Transfer {

    public SyncTransfer(Path root) {
        super(root);
    }

    public <T> SyncTransfer(T dict, Session s) {
        super(dict, s);
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = super.getSerializer();
        dict.setStringForKey(String.valueOf(KIND_SYNC), "Kind");
        return dict.<T>getSerialized();
    }

    /**
     * The delegate for files to upload
     */
    private Transfer _delegateUpload;

    /**
     * The delegate for files to download
     */
    private Transfer _delegateDownload;

    @Override
    protected void init() {
        log.debug("init");
        _delegateUpload = new UploadTransfer(this.getRoots());
        _delegateDownload = new DownloadTransfer(this.getRoots());
    }

    @Override
    protected void normalize() {
        _delegateUpload.normalize();
        _delegateDownload.normalize();
    }

    @Override
    public void setBandwidth(float bytesPerSecond) {
        ;
    }

    @Override
    public float getBandwidth() {
        return BandwidthThrottle.UNLIMITED;
    }

    @Override
    public String getName() {
        return this.getRoot().getName() + " \u2194 " /*left-right arrow*/ + this.getRoot().getLocal().getName();
    }

    @Override
    public double getSize() {
        final double size = _delegateDownload.getSize() + _delegateUpload.getSize();
        if(0 == size) {
            return super.getSize();
        }
        return size;
    }

    @Override
    public boolean isResumable() {
        return _delegateDownload.isResumable() && _delegateUpload.isResumable();
    }

    @Override
    public double getTransferred() {
        final double transferred = _delegateDownload.getTransferred() + _delegateUpload.getTransferred();
        if(0 == transferred) {
            return super.getTransferred();
        }
        return transferred;
    }

    private TransferAction action = TransferAction.forName(
            Preferences.instance().getProperty("queue.sync.action.default")
    );

    /**
     * @param action
     */
    public void setTransferAction(TransferAction action) {
        this.action = action;
    }

    /**
     * @return
     */
    public TransferAction getAction() {
        return this.action;
    }

    public static final TransferAction ACTION_DOWNLOAD = new TransferAction() {
        public String toString() {
            return "download";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Download");
        }
    };
    public static final TransferAction ACTION_UPLOAD = new TransferAction() {
        public String toString() {
            return "upload";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Upload");
        }
    };
    public static final TransferAction ACTION_MIRROR = new TransferAction() {
        public String toString() {
            return "mirror";
        }

        @Override
        public String getLocalizableString() {
            return Locale.localizedString("Mirror");
        }
    };

    private TransferFilter ACTION_OVERWRITE = new TransferFilter() {
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

        @Override
        public void prepare(Path p) {
            final Comparison compare = SyncTransfer.this.compare(p);
            if(compare.equals(COMPARISON_REMOTE_NEWER)) {
                _delegateFilterDownload.prepare(p);
            }
            else if(compare.equals(COMPARISON_LOCAL_NEWER)) {
                _delegateFilterUpload.prepare(p);
            }
        }

        public boolean accept(Path p) {
            final Comparison compare = SyncTransfer.this.compare(p);
            if(!COMPARISON_EQUAL.equals(compare)) {
                if(compare.equals(COMPARISON_REMOTE_NEWER)) {
                    // Ask the download delegate for inclusion
                    return _delegateFilterDownload.accept(p);
                }
                else if(compare.equals(COMPARISON_LOCAL_NEWER)) {
                    // Ask the upload delegate for inclusion
                    return _delegateFilterUpload.accept(p);
                }
            }
            return false;
        }

        @Override
        public void complete(Path p) {
            ;
        }
    };

    @Override
    public TransferFilter filter(final TransferAction action) {
        log.debug("filter:" + action);
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            // When synchronizing, either cancel or overwrite. Resume is not supported
            return ACTION_OVERWRITE;
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            TransferAction result = prompt.prompt();
            return this.filter(result); //break out of loop
        }
        return super.filter(action);
    }

    @Override
    public AttributedList<Path> children(final Path parent) {
        final Set<Path> children = new HashSet<Path>();
        if(parent.exists()) {
            children.addAll(_delegateDownload.children(parent));
        }
        if(parent.getLocal().exists()) {
            children.addAll(_delegateUpload.children(parent));
        }
        for(Path child : children) {
            this.setStatus(child);
        }
        return new AttributedList<Path>(children);
    }

    private void setStatus(Path path) {
        boolean skipped = false;
        final Comparison comparison = this.compare(path);
        // Updating default skip settings for actual transfer
        if(COMPARISON_EQUAL.equals(comparison)) {
            skipped = path.attributes().isFile();
        }
        else if(path.attributes().isFile()) {
            if(comparison.equals(COMPARISON_REMOTE_NEWER)) {
                skipped = this.getAction().equals(ACTION_UPLOAD);
            }
            else if(comparison.equals(COMPARISON_LOCAL_NEWER)) {
                skipped = this.getAction().equals(ACTION_DOWNLOAD);
            }
        }
        path.status().setSkipped(skipped);
    }

    /**
     * Contains both download and upload cache
     */
    private final Cache<Path> cache = new Cache<Path>() {
        @Override
        public AttributedList<Path> remove(PathReference reference) {
            _delegateDownload.cache().remove(reference);
            _delegateUpload.cache().remove(reference);
            return super.remove(reference);
        }

        @Override
        public void clear() {
            _delegateDownload.cache().clear();
            _delegateUpload.cache().clear();
            super.clear();
        }

        @Override
        public boolean containsKey(PathReference reference) {
            return _delegateDownload.cache().containsKey(reference)
                    || _delegateUpload.cache().containsKey(reference);
        }

        @Override
        public AttributedList<Path> get(PathReference reference, Comparator<Path> c, PathFilter<Path> f) {
            final Set<Path> children = new HashSet<Path>();
            if(_delegateDownload.cache().containsKey(reference)) {
                children.addAll(_delegateDownload.cache().get(reference, c, f));
            }
            if(_delegateUpload.cache().containsKey(reference)) {
                children.addAll(_delegateUpload.cache().get(reference, c, f));
            }
            return new AttributedList<Path>(children);
        }

        @Override
        public Path lookup(PathReference reference) {
            Path path = _delegateUpload.cache().lookup(reference);
            if(null == path) {
                path = _delegateDownload.cache().lookup(reference);
            }
            if(null == path) {
                log.warn("Lookup failed for " + reference + " in cache");
            }
            else {
                setStatus(path);
            }
            return path;
        }
    };

    @Override
    public Cache<Path> cache() {
        return cache;
    }

    @Override
    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        log.debug("action:" + resumeRequested + "," + reloadRequested);
        // Always prompt for synchronization
        return TransferAction.ACTION_CALLBACK;
    }

    /**
     * @param p
     * @see #compare(Path)
     */
    @Override
    protected void transfer(final Path p) {
        final Comparison compare = this.compare(p);
        if(compare.equals(COMPARISON_REMOTE_NEWER)) {
            _delegateDownload.transfer(p);
        }
        else if(compare.equals(COMPARISON_LOCAL_NEWER)) {
            _delegateUpload.transfer(p);
        }
    }

    @Override
    protected void fireTransferDidEnd() {
        if(this.isReset() && this.isComplete() && !this.isCanceled()) {
            Growl.instance().notify("Synchronization complete", getName());
        }
        super.fireTransferDidEnd();
    }

    @Override
    protected void clear(final TransferOptions options) {
        _delegateDownload.clear(options);
        _delegateUpload.clear(options);
        super.clear(options);
    }

    @Override
    protected void reset() {
        _delegateDownload.reset();
        _delegateUpload.reset();

        super.reset();
    }

    /**
     *
     */
    public static class Comparison {
        @Override
        public boolean equals(Object other) {
            return super.equals(other);
        }
    }

    /**
     * Remote file is newer or local file does not exist
     */
    public static final Comparison COMPARISON_REMOTE_NEWER = new Comparison() {
        @Override
        public String toString() {
            return "COMPARISON_REMOTE_NEWER";
        }
    };
    /**
     * Local file is newer or remote file does not exist
     */
    public static final Comparison COMPARISON_LOCAL_NEWER = new Comparison() {
        @Override
        public String toString() {
            return "COMPARISON_LOCAL_NEWER";
        }
    };
    /**
     * Files are identical or directories
     */
    public static final Comparison COMPARISON_EQUAL = new Comparison() {
        @Override
        public String toString() {
            return "COMPARISON_EQUAL";
        }
    };
    /**
     * Files differ in size
     */
    private static final Comparison COMPARISON_UNEQUAL = new Comparison() {
        @Override
        public String toString() {
            return "COMPARISON_UNEQUAL";
        }
    };

    /**
     * @param p The path to compare
     * @return COMPARISON_REMOTE_NEWER, COMPARISON_LOCAL_NEWER or COMPARISON_EQUAL
     */
    public Comparison compare(Path p) {
        log.debug("compare:" + p);
        Comparison result = COMPARISON_EQUAL;
        if(p.getLocal().exists() && p.exists()) {
            if(p.attributes().isFile()) {
                result = this.compareTimestamp(p);
            }
        }
        else if(p.exists()) {
            // only the remote file exists
            result = COMPARISON_REMOTE_NEWER;
        }
        else if(p.getLocal().exists()) {
            // only the local file exists
            result = COMPARISON_LOCAL_NEWER;
        }
        return result;
    }

    /**
     * @param p
     * @return
     */
    private Comparison compareSize(Path p) {
        log.debug("compareSize:" + p);
        if(p.attributes().getSize() == -1) {
            p.readSize();
        }
        //fist make sure both files are larger than 0 bytes
        if(p.attributes().getSize() == 0 && p.getLocal().attributes().getSize() == 0) {
            return COMPARISON_EQUAL;
        }
        if(p.attributes().getSize() == 0) {
            return COMPARISON_LOCAL_NEWER;
        }
        if(p.getLocal().attributes().getSize() == 0) {
            return COMPARISON_REMOTE_NEWER;
        }
        if(p.attributes().getSize() == p.getLocal().attributes().getSize()) {
            return COMPARISON_EQUAL;
        }
        //different file size - further comparison check
        return COMPARISON_UNEQUAL;
    }

    /**
     * @param p
     * @return
     */
    private Comparison compareTimestamp(Path p) {
        log.debug("compareTimestamp:" + p);
        if(p.getSession().isTimestampSupported()) {
            if(p.attributes().getModificationDate() == -1 || p instanceof FTPPath) {
                // Make sure we have a UTC timestamp
                p.readTimestamp();
            }
        }
        final Calendar remote = this.asCalendar(p.attributes().getModificationDate(), Calendar.SECOND);
        final Calendar local = this.asCalendar(p.getLocal().attributes().getModificationDate(), Calendar.SECOND);
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
     * @param precision
     * @return
     */
    private Calendar asCalendar(final long timestamp, final int precision) {
        log.debug("asCalendar:" + timestamp);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
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