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
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.synchronization.CompareService;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.transfer.TransferPathFilter;

import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @version $Id$
 */
public class SyncTransfer extends Transfer {
    private static Logger log = Logger.getLogger(SyncTransfer.class);

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
        return dict.getSerialized();
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
        log.debug("normalize");
        _delegateUpload.normalize();
        _delegateDownload.normalize();
    }

    @Override
    public void setBandwidth(float bytesPerSecond) {
        _delegateUpload.setBandwidth(bytesPerSecond);
        _delegateDownload.setBandwidth(bytesPerSecond);
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
    public boolean isReloadable() {
        return true;
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

    public void setTransferAction(TransferAction action) {
        this.action = action;
    }

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

    private TransferPathFilter filter = new TransferPathFilter() {
        /**
         * Download delegate filter
         */
        private TransferPathFilter _delegateFilterDownload
                = _delegateDownload.filter(TransferAction.ACTION_OVERWRITE);

        /**
         * Upload delegate filter
         */
        private TransferPathFilter _delegateFilterUpload
                = _delegateUpload.filter(TransferAction.ACTION_OVERWRITE);

        @Override
        public void prepare(Path p) {
            final Comparison compare = SyncTransfer.this.compare(p);
            if(compare.equals(Comparison.REMOTE_NEWER)) {
                _delegateFilterDownload.prepare(p);
            }
            else if(compare.equals(Comparison.LOCAL_NEWER)) {
                _delegateFilterUpload.prepare(p);
            }
        }

        @Override
        public boolean accept(Path p) {
            final Comparison compare = SyncTransfer.this.compare(p);
            if(compare.equals(Comparison.REMOTE_NEWER)) {
                // Ask the download delegate for inclusion
                return _delegateFilterDownload.accept(p);
            }
            else if(compare.equals(Comparison.LOCAL_NEWER)) {
                // Ask the upload delegate for inclusion
                return _delegateFilterUpload.accept(p);
            }
            return false;
        }

        @Override
        public void complete(Path p) {
            final Comparison compare = SyncTransfer.this.compare(p);
            if(compare.equals(Comparison.REMOTE_NEWER)) {
                _delegateFilterDownload.complete(p);
            }
            else if(compare.equals(Comparison.LOCAL_NEWER)) {
                _delegateFilterUpload.complete(p);
            }
            comparisons.remove(p.getReference());
            cache.remove(p.getReference());
        }
    };

    @Override
    public TransferPathFilter filter(final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            // When synchronizing, either cancel or overwrite. Resume is not supported
            return filter;
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            TransferAction result = prompt.prompt();
            return this.filter(result); //break out of loop
        }
        return super.filter(action);
    }

    @Override
    public AttributedList<Path> children(final Path parent) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Children for %s", parent));
        }
        final Set<Path> children = new HashSet<Path>();
        if(parent.exists()) {
            children.addAll(_delegateDownload.children(parent));
        }
        if(parent.getLocal().exists()) {
            children.addAll(_delegateUpload.children(parent));
        }
        for(Path child : children) {
            if(child.getSession().isReadTimestampSupported()) {
                if(child instanceof FTPPath) {
                    // Make sure we have a UTC timestamp
                    child.readTimestamp();
                }
            }
        }
        return new AttributedList<Path>(children);
    }

    /**
     * Set the skipped flag on the file attributes if no synchronisation is needed
     * depending on the current action selection to mirror or only download or upload missing files.
     *
     * @param path File
     */
    @Override
    public boolean isSkipped(final Path path) {
        boolean skipped = false;
        final Comparison comparison = this.compare(path);
        // Updating default skip settings for actual transfer
        if(Comparison.EQUAL.equals(comparison)) {
            skipped = path.attributes().isFile();
        }
        else {
            if(path.attributes().isFile()) {
                if(comparison.equals(Comparison.REMOTE_NEWER)) {
                    skipped = this.getAction().equals(ACTION_UPLOAD);
                }
                else if(comparison.equals(Comparison.LOCAL_NEWER)) {
                    skipped = this.getAction().equals(ACTION_DOWNLOAD);
                }
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Skip file %s:%s", path.getAbsolute(), skipped));
        }
        return skipped;
    }

    /**
     * Contains both download and upload cache
     */
    private final Cache cache = new Cache() {
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
        public AttributedList<Path> get(PathReference reference) {
            final Set<Path> children = new HashSet<Path>();
            if(_delegateDownload.cache().containsKey(reference)) {
                children.addAll(_delegateDownload.cache().get(reference));
            }
            if(_delegateUpload.cache().containsKey(reference)) {
                children.addAll(_delegateUpload.cache().get(reference));
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
                log.warn(String.format("Lookup failed for %s in cache", reference));
            }
            return path;
        }
    };

    @Override
    public Cache cache() {
        return cache;
    }

    @Override
    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        log.debug(String.format("Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        // Always prompt for synchronization
        return TransferAction.ACTION_CALLBACK;
    }

    @Override
    protected void transfer(final Path file, TransferOptions options) {
        log.debug("transfer:" + file);
        final Comparison compare = this.compare(file);
        if(compare.equals(Comparison.REMOTE_NEWER)) {
            _delegateDownload.transfer(file, options);
        }
        else if(compare.equals(Comparison.LOCAL_NEWER)) {
            _delegateUpload.transfer(file, options);
        }
    }

    @Override
    protected void clear(final TransferOptions options) {
        _delegateDownload.clear(options);
        _delegateUpload.clear(options);
        comparisons.clear();
        super.clear(options);
    }

    @Override
    protected void reset() {
        _delegateDownload.reset();
        _delegateUpload.reset();

        super.reset();
    }

    private Map<PathReference, Comparison> comparisons = Collections.<PathReference, Comparison>synchronizedMap(new LRUMap(
            Preferences.instance().getInteger("transfer.cache.size")
    ) {
        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry
                                            entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    /**
     * @param p The path to compare
     * @return Comparison.REMOTE_NEWER, Comparison.LOCAL_NEWER or COMPARISON_EQUAL
     */
    public Comparison compare(final Path p) {
        if(comparisons.containsKey(p.getReference())) {
            return comparisons.get(p.getReference());
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Compare path %s with local", p.getName()));
        }
        final Comparison result = new CompareService().compare(p);
        comparisons.put(p.getReference(), result);
        return result;
    }


    @Override
    public String getStatus() {
        return this.isComplete() ? "Synchronization complete" : "Transfer incomplete";
    }

    @Override
    public String getImage() {
        return "sync.tiff";
    }
}