package ch.cyberduck.core.transfer;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullPathFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.synchronization.CachingComparisonServiceFilter;
import ch.cyberduck.core.synchronization.ComparisionServiceFilter;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.transfer.synchronisation.SynchronizationPathFilter;

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
    private static final Logger log = Logger.getLogger(SyncTransfer.class);

    /**
     * The delegate for files to upload
     */
    private Transfer upload;

    /**
     * The delegate for files to download
     */
    private Transfer download;

    private CachingComparisonServiceFilter comparison;

    private Map<Path, Comparison> cache = Collections.<Path, Comparison>synchronizedMap(new LRUMap(
            Preferences.instance().getInteger("transfer.cache.size")));

    public SyncTransfer(final Host host, final Path root) {
        super(host, root, new BandwidthThrottle(Preferences.instance().getFloat("queue.upload.bandwidth.bytes")));
        this.init();
    }

    public <T> SyncTransfer(final T dict) {
        super(dict, new BandwidthThrottle(Preferences.instance().getFloat("queue.upload.bandwidth.bytes")));
        this.init();
    }

    private void init() {
        upload = new UploadTransfer(host, this.getRoots(), new NullPathFilter<Local>());
        download = new DownloadTransfer(host, this.getRoots(), new NullPathFilter<Path>());
    }

    @Override
    public Type getType() {
        return Type.sync;
    }

    @Override
    public void setBandwidth(float bytesPerSecond) {
        upload.setBandwidth(bytesPerSecond);
        download.setBandwidth(bytesPerSecond);
    }

    @Override
    public String getName() {
        return this.getRoot().getName() + " \u2194 " /*left-right arrow*/ + this.getRoot().getLocal().getName();
    }

    @Override
    public long getTransferred() {
        // Include super for serialized state.
        return super.getTransferred() + download.getTransferred() + upload.getTransferred();
    }

    @Override
    public TransferPathFilter filter(final Session<?> session, final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        // Set chosen action (upload, download, mirror) from prompt
        return new SynchronizationPathFilter(
                comparison = new CachingComparisonServiceFilter(new ComparisionServiceFilter(session, session.getHost().getTimezone())),
                download.filter(session, TransferAction.overwrite),
                upload.filter(session, TransferAction.overwrite),
                action
        );
    }

    @Override
    public AttributedList<Path> list(final Session<?> session, final Path directory) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Children for %s", directory));
        }
        final Set<Path> children = new HashSet<Path>();
        if(session.getFeature(Find.class).find(directory)) {
            children.addAll(download.list(session, directory));
        }
        if(directory.getLocal().exists()) {
            children.addAll(upload.list(session, directory));
        }
        return new AttributedList<Path>(children);
    }

    @Override
    public TransferAction action(final Session<?> session, final boolean resumeRequested, final boolean reloadRequested, final TransferPrompt prompt) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action for Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
        // Prompt for synchronization.
        return prompt.prompt();
    }

    @Override
    public void transfer(final Session<?> session, final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        final Comparison compare = comparison.compare(file);
        if(compare.equals(Comparison.remote)) {
            download.transfer(session, file, options, status);
        }
        else if(compare.equals(Comparison.local)) {
            upload.transfer(session, file, options, status);
        }
    }

    /**
     * @param file The path to compare
     */
    public Comparison compare(final Path file) {
        return comparison.get(file);
    }

    @Override
    public void reset() {
        download.reset();
        upload.reset();
        super.reset();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SyncTransfer{");
        sb.append("upload=").append(upload);
        sb.append(", download=").append(download);
        sb.append('}');
        return sb.toString();
    }
}