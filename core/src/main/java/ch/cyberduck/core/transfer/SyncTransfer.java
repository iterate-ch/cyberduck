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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.synchronization.CachingComparePathFilter;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.DefaultComparePathFilter;
import ch.cyberduck.core.transfer.synchronisation.SynchronizationPathFilter;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SyncTransfer extends Transfer {
    private static final Logger log = LogManager.getLogger(SyncTransfer.class);

    /**
     * The delegate for files to upload
     */
    private final Transfer upload;
    /**
     * The delegate for files to download
     */
    private final Transfer download;
    private final TransferItem item;

    private CachingComparePathFilter comparison;
    /**
     * Last selected action to apply on resume
     */
    private TransferAction action;

    private Cache<Path> cache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    private final Map<TransferItem, Comparison> comparisons = Collections.synchronizedMap(new LRUMap<>(
            PreferencesFactory.get().getInteger("transfer.cache.size")));

    public SyncTransfer(final Host host, final TransferItem item) {
        this(host, item, TransferAction.callback);
    }

    public SyncTransfer(final Host host, final TransferItem item, final TransferAction action) {
        super(host, Collections.singletonList(item),
                new BandwidthThrottle(PreferencesFactory.get().getFloat("queue.upload.bandwidth.bytes")));
        this.upload = new UploadTransfer(host, roots).withCache(cache);
        this.download = new DownloadTransfer(host, roots).withCache(cache);
        this.item = item;
        this.action = action;
    }

    @Override
    public Transfer withCache(final Cache<Path> cache) {
        this.cache = cache;
        upload.withCache(cache);
        download.withCache(cache);
        return this;
    }

    @Override
    public <T> T serialize(final Serializer<T> dict) {
        dict.setStringForKey(this.getType().name(), "Type");
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
        log.debug("Filter transfer with action {}", action);
        final Find find = new CachingFindFeature(source, cache,
                source.getFeature(Find.class, new DefaultFindFeature(source)));
        final AttributesFinder attributes = new CachingAttributesFinderFeature(source, cache,
                source.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(source)));
        // Set chosen action (upload, download, mirror) from prompt
        comparison = new CachingComparePathFilter(comparisons, new DefaultComparePathFilter(source, find, attributes));
        return new SynchronizationPathFilter(comparison,
                download.filter(source, destination, TransferAction.overwrite, listener),
                upload.filter(source, destination, TransferAction.overwrite, listener),
                action
        );
    }

    @Override
    public void pre(final Session<?> source, final Session<?> destination, final Map<TransferItem, TransferStatus> files, final TransferPathFilter filter, final TransferErrorCallback error, final ProgressListener listener, final ConnectionCallback callback) throws BackgroundException {
        final Map<TransferItem, TransferStatus> downloads = new HashMap<>();
        final Map<TransferItem, TransferStatus> uploads = new HashMap<>();
        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
            switch(comparison.compare(entry.getKey().remote, entry.getKey().local, new DisabledListProgressListener())) {
                case remote:
                    downloads.put(entry.getKey(), entry.getValue());
                    break;
                case local:
                    uploads.put(entry.getKey(), entry.getValue());
                    break;
            }
        }
        download.pre(source, destination, downloads, filter, error, listener, callback);
        upload.pre(source, destination, uploads, filter, error, listener, callback);
    }

    @Override
    public List<TransferItem> list(final Session<?> session, final Path directory, final Local local,
                                   final ListProgressListener listener) throws BackgroundException {
        log.debug("Children for {}", directory);
        final Set<TransferItem> children = new HashSet<>();
        final Find finder = new CachingFindFeature(session, cache, session.getFeature(Find.class, new DefaultFindFeature(session)));
        if(finder.find(directory)) {
            children.addAll(download.list(session, directory, local, listener));
        }
        if(local.exists()) {
            children.addAll(upload.list(session, directory, local, listener));
        }
        return new ArrayList<>(children);
    }

    @Override
    public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                 final TransferPrompt prompt, final ListProgressListener listener) {
        log.debug("Find transfer action with prompt {}", prompt);
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
    public void transfer(final Session<?> source, final Session<?> destination, final Path file, final Local local,
                         final TransferOptions options, final TransferStatus overall, final TransferStatus segment, final ConnectionCallback connectionCallback,
                         final ProgressListener progressListener, final StreamListener streamListener) throws BackgroundException {
        log.debug("Transfer file {} with options {}", file, options);
        switch(comparison.compare(file, local, progressListener)) {
            case remote:
                download.transfer(source, destination, file, local, options, overall, segment, connectionCallback, progressListener, streamListener);
                break;
            case local:
                upload.transfer(source, destination, file, local, options, overall, segment, connectionCallback, progressListener, streamListener);
                break;
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
    public void normalize() {
        download.normalize();
        upload.normalize();
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
