package ch.cyberduck.core.transfer.synchronisation;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.synchronization.CombinedComparisionService;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.download.DownloadTransfer;
import ch.cyberduck.core.transfer.upload.UploadTransfer;

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

    public SyncTransfer(final Session session, final Path root) {
        super(session, root, new BandwidthThrottle(
                Preferences.instance().getFloat("queue.upload.bandwidth.bytes")));
        this.init();
    }

    public <T> SyncTransfer(final T dict, final Session s) {
        super(dict, s, new BandwidthThrottle(
                Preferences.instance().getFloat("queue.upload.bandwidth.bytes")));
        this.init();
    }

    private void init() {
        _delegateUpload = new UploadTransfer(session, this.getRoots(), new NullPathFilter<Local>());
        _delegateDownload = new DownloadTransfer(session, this.getRoots(), new NullPathFilter<Path>());
    }

    @Override
    public Type getType() {
        return Type.sync;
    }

    @Override
    public Filter<Path> getRegexFilter() {
        return new NullPathFilter<Path>();
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
    public void setBandwidth(float bytesPerSecond) {
        _delegateUpload.setBandwidth(bytesPerSecond);
        _delegateDownload.setBandwidth(bytesPerSecond);
    }

    @Override
    public String getName() {
        return this.getRoot().getName() + " \u2194 " /*left-right arrow*/ + this.getRoot().getLocal().getName();
    }

    @Override
    public long getTransferred() {
        // Include super for serialized state.
        return super.getTransferred() + _delegateDownload.getTransferred() + _delegateUpload.getTransferred();
    }

    private TransferAction action = TransferAction.forName(
            Preferences.instance().getProperty("queue.sync.action.default")
    );

    public void setTransferAction(TransferAction action) {
        this.action = action;
    }

    public TransferAction getTransferAction() {
        return this.action;
    }

    public static final TransferAction ACTION_DOWNLOAD = new TransferAction("download") {
        @Override
        public String getTitle() {
            return LocaleFactory.localizedString("Download");
        }

        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("Download changed and missing files", "Transfer");
        }
    };

    public static final TransferAction ACTION_UPLOAD = new TransferAction("upload") {
        @Override
        public String getTitle() {
            return LocaleFactory.localizedString("Upload");
        }

        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("Upload changed and missing files", "Transfer");
        }
    };

    public static final TransferAction ACTION_MIRROR = new TransferAction("mirror") {
        @Override
        public String getTitle() {
            return LocaleFactory.localizedString("Mirror");
        }

        @Override
        public String getDescription() {
            return LocaleFactory.localizedString("Download and Upload", "Transfer");
        }
    };

    @Override
    public TransferPathFilter filter(final TransferPrompt prompt, final TransferAction action) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            // When synchronizing, either cancel or overwrite. Resume is not supported
            return new DelegateTransferPathFilter(
                    _delegateDownload.filter(null, TransferAction.ACTION_OVERWRITE),
                    _delegateUpload.filter(null, TransferAction.ACTION_OVERWRITE)
            );
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            final TransferAction result = prompt.prompt();
            return this.filter(prompt, result); // Break out. Either cancel or overwrite
        }
        return super.filter(prompt, action);
    }

    @Override
    public AttributedList<Path> children(final Path parent) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Children for %s", parent));
        }
        final Set<Path> children = new HashSet<Path>();
        if(session.getFeature(Find.class).find(parent)) {
            children.addAll(_delegateDownload.children(parent));
        }
        if(parent.getLocal().exists()) {
            children.addAll(_delegateUpload.children(parent));
        }
        for(Path path : children) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Compare path %s with local", path));
            }
            final Comparison result = new CombinedComparisionService(session, session.getHost().getTimezone()).compare(path);
            comparisons.put(path.getReference(), result);
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
        final Comparison comparison = comparisons.get(path.getReference());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Comparison for file %s is %s", path, comparison));
        }
        // Updating default skip settings for actual transfer
        if(Comparison.EQUAL.equals(comparison)) {
            return path.attributes().isFile();
        }
        else {
            if(path.attributes().isFile()) {
                if(comparison.equals(Comparison.REMOTE_NEWER)) {
                    return this.getTransferAction().equals(ACTION_UPLOAD);
                }
                else if(comparison.equals(Comparison.LOCAL_NEWER)) {
                    return this.getTransferAction().equals(ACTION_DOWNLOAD);
                }
            }
            return false;
        }
    }

    @Override
    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        log.debug(String.format("Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        // Always prompt for synchronization
        return TransferAction.ACTION_CALLBACK;
    }

    @Override
    public void transfer(final Path file, TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        final Comparison compare = this.compare(file);
        if(compare.equals(Comparison.REMOTE_NEWER)) {
            _delegateDownload.transfer(file, options, status);
        }
        else if(compare.equals(Comparison.LOCAL_NEWER)) {
            _delegateUpload.transfer(file, options, status);
        }
    }

    @Override
    public void clear(final TransferOptions options) {
        _delegateDownload.clear(options);
        _delegateUpload.clear(options);
        comparisons.clear();
        super.clear(options);
    }

    @Override
    public void reset() {
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
     * @return Comparison.REMOTE_NEWER, Comparison.LOCAL_NEWER or Comparison.EQUAL
     */
    public Comparison compare(final Path p) {
        if(comparisons.containsKey(p.getReference())) {
            return comparisons.get(p.getReference());
        }
        return Comparison.EQUAL;
    }

    private final class DelegateTransferPathFilter implements TransferPathFilter {

        /**
         * Download delegate filter
         */
        private TransferPathFilter _delegateFilterDownload;

        /**
         * Upload delegate filter
         */
        private TransferPathFilter _delegateFilterUpload;

        private DelegateTransferPathFilter(final TransferPathFilter _delegateFilterDownload,
                                           final TransferPathFilter _delegateFilterUpload) {
            this._delegateFilterDownload = _delegateFilterDownload;
            this._delegateFilterUpload = _delegateFilterUpload;
        }

        @Override
        public TransferStatus prepare(final Path p, final TransferStatus parent) throws BackgroundException {
            final Comparison compare = SyncTransfer.this.compare(p);
            if(compare.equals(Comparison.REMOTE_NEWER)) {
                return _delegateFilterDownload.prepare(p, new TransferStatus());
            }
            if(compare.equals(Comparison.LOCAL_NEWER)) {
                return _delegateFilterUpload.prepare(p, new TransferStatus());
            }
            return new TransferStatus();
        }

        @Override
        public boolean accept(final Path p, final TransferStatus parent) throws BackgroundException {
            final Comparison compare = SyncTransfer.this.compare(p);
            if(compare.equals(Comparison.EQUAL)) {
                return false;
            }
            if(compare.equals(Comparison.REMOTE_NEWER)) {
                if(getTransferAction().equals(ACTION_UPLOAD)) {
                    return false;
                }
                // Ask the download delegate for inclusion
                return _delegateFilterDownload.accept(p, parent);
            }
            else if(compare.equals(Comparison.LOCAL_NEWER)) {
                if(getTransferAction().equals(ACTION_DOWNLOAD)) {
                    return false;
                }
                // Ask the upload delegate for inclusion
                return _delegateFilterUpload.accept(p, parent);
            }
            return false;
        }

        @Override
        public void complete(final Path p, final TransferOptions options, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
            final Comparison compare = SyncTransfer.this.compare(p);
            if(compare.equals(Comparison.REMOTE_NEWER)) {
                _delegateFilterDownload.complete(p, options, status, listener);
            }
            else if(compare.equals(Comparison.LOCAL_NEWER)) {
                _delegateFilterUpload.complete(p, options, status, listener);
            }
            comparisons.remove(p.getReference());
        }
    }
}