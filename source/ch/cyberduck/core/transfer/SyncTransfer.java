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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.synchronization.CachingComparisonServiceFilter;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonServiceFilter;
import ch.cyberduck.core.transfer.synchronisation.SynchronizationPathFilter;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

    public SyncTransfer(final Host host, final TransferItem item) {
        super(host, Collections.singletonList(item),
                new BandwidthThrottle(Preferences.instance().getFloat("queue.upload.bandwidth.bytes")));
        this.init();
    }

    private void init() {
        upload = new UploadTransfer(host, roots);
        download = new DownloadTransfer(host, roots);
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
        return this.getRoot().remote.getName()
                + " \u2194 " /*left-right arrow*/ + this.getRoot().local.getName();
    }

    @Override
    public long getTransferred() {
        // Include super for serialized state.
        return super.getTransferred() + download.getTransferred() + upload.getTransferred();
    }

    @Override
    public TransferPathFilter filter(final Session<?> session, final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action));
        }
        // Set chosen action (upload, download, mirror) from prompt
        return new SynchronizationPathFilter(
                comparison = new CachingComparisonServiceFilter(new ComparisonServiceFilter(session, session.getHost().getTimezone())),
                download.filter(session, TransferAction.overwrite),
                upload.filter(session, TransferAction.overwrite),
                action
        );
    }

    @Override
    public List<TransferItem> list(final Session<?> session, final Path directory, final Local local,
                                   final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Children for %s", directory));
        }
        final Set<TransferItem> children = new HashSet<TransferItem>();
        if(session.getFeature(Find.class).find(directory)) {
            children.addAll(download.list(session, directory, local, listener));
        }
        if(local.exists()) {
            children.addAll(upload.list(session, directory, local, listener));
        }
        return new ArrayList<TransferItem>(children);
    }

    @Override
    public TransferAction action(final Session<?> session, final boolean resumeRequested, final boolean reloadRequested,
                                 final TransferPrompt prompt) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action for Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
        // Prompt for synchronization.
        return prompt.prompt();
    }

    @Override
    public void transfer(final Session<?> session, final Path file, final Local local,
                         final TransferOptions options, final TransferStatus status, final LoginCallback login) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        final Comparison compare = comparison.compare(file, local);
        if(compare.equals(Comparison.remote)) {
            download.transfer(session, file, local, options, status, login);
        }
        else if(compare.equals(Comparison.local)) {
            upload.transfer(session, file, local, options, status, login);
        }
    }

    /**
     * @param item The path to compare
     */
    public Comparison compare(final TransferItem item) {
        if(null == comparison) {
            log.warn("No comparison filter initialized");
            return Comparison.equal;
        }
        return comparison.get(item);
    }

    @Override
    public void start() {
        download.start();
        upload.start();
        super.start();
    }

    @Override
    public void stop() {
        download.stop();
        upload.stop();
        super.stop();
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