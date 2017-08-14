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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.synchronization.CachingComparisonServiceFilter;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonServiceFilter;
import ch.cyberduck.core.transfer.synchronisation.SynchronizationPathFilter;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private TransferAction action;

    private final TransferItem item;

    private PathCache cache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    private final Map<TransferItem, Comparison> comparisons = Collections.synchronizedMap(new LRUMap<TransferItem, Comparison>(
            PreferencesFactory.get().getInteger("transfer.cache.size")));

    public SyncTransfer(final Host host, final TransferItem item) {
        this(host, item, TransferAction.callback);
    }

    public SyncTransfer(final Host host, final TransferItem item, final TransferAction action) {
        super(host, Collections.singletonList(item),
                new BandwidthThrottle(PreferencesFactory.get().getFloat("queue.upload.bandwidth.bytes")));
        this.init();
        this.item = item;
        this.action = action;
    }

    private void init() {
        upload = new UploadTransfer(host, roots).withCache(cache);
        download = new DownloadTransfer(host, roots).withCache(cache);
    }

    @Override
    public Transfer withCache(final PathCache cache) {
        this.cache = cache;
        // Populate cache for root items. See #8712
        for(TransferItem root : roots) {
            if(!root.remote.isRoot()) {
                cache.put(root.remote.getParent(), new AttributedList<Path>(Collections.singletonList(root.remote)));
            }
        }
        upload.withCache(cache);
        download.withCache(cache);
        return this;
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(String.valueOf(this.getType().name()), "Type");
        dict.setObjectForKey(host, "Host");
        dict.setListForKey(roots, "Items");
        dict.setStringForKey(uuid, "UUID");
        dict.setStringForKey(String.valueOf(this.getSize()), "Size");
        dict.setStringForKey(String.valueOf(this.getTransferred()), "Current");
        if(timestamp != null) {
            dict.setStringForKey(String.valueOf(timestamp.getTime()), "Timestamp");
        }
        if(bandwidth != null) {
            dict.setStringForKey(String.valueOf(bandwidth.getRate()), "Bandwidth");
        }
        if(action != null) {
            dict.setStringForKey(action.name(), "Action");
        }
        return dict.getSerialized();
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
    public Long getTransferred() {
        // Include super for serialized state.
        return super.getTransferred() + download.getTransferred() + upload.getTransferred();
    }

    @Override
    public TransferPathFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action));
        }
        // Set chosen action (upload, download, mirror) from prompt
        return new SynchronizationPathFilter(
                comparison = new CachingComparisonServiceFilter(
                        new ComparisonServiceFilter(source, source.getHost().getTimezone(), listener).withCache(cache)
                ).withCache(comparisons),
                download.filter(source, destination, TransferAction.overwrite, listener),
                upload.filter(source, destination, TransferAction.overwrite, listener),
                action
        ).withCache(cache);
    }

    @Override
    public void pre(final Session<?> source, final Session<?> destination, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        // Bulk operation is in transfer implementation
    }

    @Override
    public List<TransferItem> list(final Session<?> source, final Session<?> destination, final Path directory, final Local local,
                                   final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Children for %s", directory));
        }
        final Set<TransferItem> children = new HashSet<TransferItem>();
        final Find finder = source.getFeature(Find.class, new DefaultFindFeature(source)).withCache(cache);
        if(finder.find(directory)) {
            children.addAll(download.list(source, destination, directory, local, listener));
        }
        if(local.exists()) {
            children.addAll(upload.list(source, destination, directory, local, listener));
        }
        return new ArrayList<TransferItem>(children);
    }

    @Override
    public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                 final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action for Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
        if(resumeRequested) {
            if(action.equals(TransferAction.callback)) {
                return action = prompt.prompt(item);
            }
            return action;
        }
        // Prompt for synchronization.
        return action = prompt.prompt(item);
    }

    @Override
    public Path transfer(final Session<?> source, final Session<?> destination, final Path file, final Local local,
                         final TransferOptions options, final TransferStatus status, final ConnectionCallback connectionCallback,
                         final PasswordCallback passwordCallback, final ProgressListener progressListener, final StreamListener streamListener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        final Comparison compare = comparison.compare(file, local);
        if(compare.equals(Comparison.remote)) {
            download.pre(source, destination, Collections.singletonMap(file, status), connectionCallback);
            download.transfer(source, destination, file, local, options, status, connectionCallback, passwordCallback, progressListener, streamListener);
        }
        else if(compare.equals(Comparison.local)) {
            upload.pre(source, destination, Collections.singletonMap(file, status), connectionCallback);
            upload.transfer(source, destination, file, local, options, status, connectionCallback, passwordCallback, progressListener, streamListener);
        }
        return file;
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
        cache.clear();
        comparisons.clear();
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