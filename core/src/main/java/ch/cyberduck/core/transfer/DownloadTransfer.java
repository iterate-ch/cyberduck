package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.filter.DownloadDuplicateFilter;
import ch.cyberduck.core.filter.DownloadRegexFilter;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.local.LocalSymlinkFactory;
import ch.cyberduck.core.local.features.Symlink;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.download.AbstractDownloadFilter;
import ch.cyberduck.core.transfer.download.CompareFilter;
import ch.cyberduck.core.transfer.download.DownloadFilterOptions;
import ch.cyberduck.core.transfer.download.DownloadRegexPriorityComparator;
import ch.cyberduck.core.transfer.download.IconUpdateStreamListener;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.download.RenameExistingFilter;
import ch.cyberduck.core.transfer.download.RenameFilter;
import ch.cyberduck.core.transfer.download.ResumeFilter;
import ch.cyberduck.core.transfer.download.SkipFilter;
import ch.cyberduck.core.transfer.download.TrashFilter;
import ch.cyberduck.core.transfer.normalizer.DownloadRootPathsNormalizer;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DownloadTransfer extends Transfer {
    private static final Logger log = LogManager.getLogger(DownloadTransfer.class);

    private final Filter<Path> filter;
    private final Comparator<Path> comparator;
    private final DownloadSymlinkResolver symlinkResolver;

    private Cache<Path> cache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    private DownloadFilterOptions options = new DownloadFilterOptions(host);

    public DownloadTransfer(final Host host, final Path root, final Local local) {
        this(host, Collections.singletonList(new TransferItem(root, local)),
                PreferencesFactory.get().getBoolean("queue.download.skip.enable") ? new DownloadRegexFilter() : new DownloadDuplicateFilter());
    }

    public DownloadTransfer(final Host host, final Path root, final Local local, final Filter<Path> f) {
        this(host, Collections.singletonList(new TransferItem(root, local)), f);
    }

    public DownloadTransfer(final Host host, final List<TransferItem> roots) {
        this(host, roots,
                PreferencesFactory.get().getBoolean("queue.download.skip.enable") ? new DownloadRegexFilter() : new DownloadDuplicateFilter());
    }

    public DownloadTransfer(final Host host, final List<TransferItem> roots, final Filter<Path> f) {
        this(host, roots, f, new DownloadRegexPriorityComparator());
    }

    public DownloadTransfer(final Host host, final List<TransferItem> roots, final Filter<Path> f, final Comparator<Path> comparator) {
        super(host, roots, new BandwidthThrottle(PreferencesFactory.get().getFloat("queue.download.bandwidth.bytes")));
        this.filter = f;
        this.comparator = comparator;
        this.symlinkResolver = new DownloadSymlinkResolver(roots);
    }

    @Override
    public DownloadTransfer withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

    public Transfer withOptions(final DownloadFilterOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public Type getType() {
        return Type.download;
    }

    @Override
    public List<TransferItem> list(final Session<?> session, final Path directory,
                                   final Local local, final ListProgressListener listener) throws BackgroundException {
        log.debug("List children for {}", directory);
        if(directory.isSymbolicLink()
                && new DownloadSymlinkResolver(roots).resolve(directory)) {
            log.debug("Do not list children for symbolic link {}", directory);
            return Collections.emptyList();
        }
        else {
            final AttributedList<Path> list;
            if(cache.isCached(directory)) {
                list = cache.get(directory);
            }
            else {
                list = session.getFeature(ListService.class).list(directory, listener);
                cache.put(directory, list);
            }
            final List<TransferItem> children = new ArrayList<>();
            // Return copy with filtered result only
            for(Path f : new AttributedList<>(list.filter(comparator, filter))) {
                children.add(new TransferItem(f, LocalFactory.get(local, f.getName())));
            }
            return children;
        }
    }

    @Override
    public AbstractDownloadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
        log.debug("Filter transfer with action {} and options {}", action, options);
        final DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(roots);
        final AttributesFinder attributes;
        if(roots.size() > 1 || roots.stream().filter(item -> item.remote.isDirectory()).findAny().isPresent()) {
            attributes = new CachingAttributesFinderFeature(source, cache, source.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(source)));
        }
        else {
            attributes = new CachingAttributesFinderFeature(source, cache, source.getFeature(AttributesFinder.class));
        }
        log.debug("Determined feature {}", attributes);
        if(action.equals(TransferAction.resume)) {
            return new ResumeFilter(resolver, source, attributes, options);
        }
        if(action.equals(TransferAction.rename)) {
            return new RenameFilter(resolver, source, attributes, options);
        }
        if(action.equals(TransferAction.renameexisting)) {
            return new RenameExistingFilter(resolver, source, attributes, options);
        }
        if(action.equals(TransferAction.skip)) {
            return new SkipFilter(resolver, source, attributes, options);
        }
        if(action.equals(TransferAction.trash)) {
            return new TrashFilter(resolver, source, attributes, options);
        }
        if(action.equals(TransferAction.comparison)) {
            return new CompareFilter(resolver, source, options);
        }
        return new OverwriteFilter(resolver, source, attributes, options);
    }

    @Override
    public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                 final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
        log.debug("Find transfer action with prompt {}", prompt);
        if(resumeRequested) {
            // Force resume by user or retry of failed transfer
            return TransferAction.resume;
        }
        final TransferAction action;
        if(reloadRequested) {
            action = TransferAction.forName(
                    PreferencesFactory.get().getProperty("queue.download.reload.action"));
        }
        else {
            // Use default
            action = TransferAction.forName(
                    PreferencesFactory.get().getProperty("queue.download.action"));
        }
        if(action.equals(TransferAction.callback)) {
            for(TransferItem download : roots) {
                final Local local = download.local;
                if(local.exists()) {
                    if(local.isDirectory()) {
                        if(local.list().isEmpty()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    if(local.isFile()) {
                        if(local.attributes().getSize() == 0) {
                            // Dragging a file to the local volume creates the file already
                            continue;
                        }
                    }
                    // Prompt user to choose a filter
                    return prompt.prompt(download);
                }
            }
            // No files exist yet therefore it is most straightforward to use the overwrite action
            return TransferAction.overwrite;
        }
        return action;
    }

    @Override
    public void pre(final Session<?> source, final Session<?> destination, final Map<TransferItem, TransferStatus> files,
                    final TransferPathFilter filter, final TransferErrorCallback error, final ProgressListener progress, final ConnectionCallback callback) throws BackgroundException {
        final Bulk<?> feature = source.getFeature(Bulk.class);
        final Object id = feature.pre(Type.download, files, callback);
        log.debug("Obtained bulk id {} for transfer {}", id, this);
        super.pre(source, destination, files, filter, error, progress, callback);
        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
            final Path file = entry.getKey().remote;
            if(file.isDirectory()) {
                final TransferStatus status = entry.getValue();
                if(status.isExists()) {
                    log.warn("Skip existing directory {}", file);
                    continue;
                }
                final Local local = entry.getKey().local;
                progress.message(MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"), local.getName()));
                try {
                    new DefaultLocalDirectoryFeature().mkdir(local);
                    // Post process of file
                    filter.complete(
                            status.getRename().remote != null ? status.getRename().remote : entry.getKey().remote,
                            status.getRename().local != null ? status.getRename().local : entry.getKey().local,
                            status.complete(), progress);
                }
                catch(AccessDeniedException e) {
                    if(error.prompt(entry.getKey(), status, e, files.size())) {
                        // Continue
                        log.warn("Ignore transfer failure {}", e.getMessage());
                    }
                    else {
                        throw new TransferCanceledException(e);
                    }
                }
            }
        }
    }

    @Override
    public void post(final Session<?> source, final Session<?> destination, final Map<TransferItem, TransferStatus> files,
                     final TransferErrorCallback error, final ProgressListener listener, final ConnectionCallback callback) throws BackgroundException {
        final Bulk<?> feature = source.getFeature(Bulk.class);
        try {
            feature.post(Type.download, files, callback);
            super.post(source, destination, files, error, listener, callback);
        }
        catch(BackgroundException e) {
            final Optional<Map.Entry<TransferItem, TransferStatus>> entry = files.entrySet().stream().findFirst();
            if(entry.isPresent()) {
                final Map.Entry<TransferItem, TransferStatus> item = entry.get();
                log.warn("Prompt with failure {} for item {} only", e, item.getKey());
                if(error.prompt(item.getKey(), item.getValue(), e, files.size())) {
                    // Continue
                    log.warn("Ignore transfer failure {}", e.getMessage());
                }
                else {
                    throw new TransferCanceledException(e);
                }
            }
        }
    }

    @Override
    public void transfer(final Session<?> source, final Session<?> destination, final Path file, final Local local, final TransferOptions options,
                         final TransferStatus overall, final TransferStatus segment, final ConnectionCallback connectionCallback,
                         final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
        log.debug("Transfer file {} with options {} and status {}", file, options, segment);
        if(file.isSymbolicLink()) {
            if(symlinkResolver.resolve(file)) {
                // Make relative symbolic link
                final String target = symlinkResolver.relativize(file.getAbsolute(),
                        file.getSymlinkTarget().getAbsolute());
                log.debug("Create symbolic link from {} to {}", local, target);
                final Symlink symlink = LocalSymlinkFactory.get();
                symlink.symlink(local, target);
                return;
            }
        }
        if(file.isFile()) {
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Downloading {0}", "Status"),
                    file.getName()));
            final Local folder = local.getParent();
            if(!folder.exists()) {
                new DefaultLocalDirectoryFeature().mkdir(folder);
            }
            // Transfer
            final Download download = source.getFeature(Download.class);
            download.download(file, local, bandwidth, this.options.icon && segment.getLength() > PreferencesFactory.get().getLong("queue.download.icon.threshold") ?
                    new IconUpdateStreamListener(streamListener, segment, local) : streamListener, segment, connectionCallback);
        }
    }

    @Override
    public void normalize() {
        List<TransferItem> normalized = new DownloadRootPathsNormalizer().normalize(roots);
        roots.clear();
        roots.addAll(normalized);
    }

    @Override
    public void stop() {
        cache.clear();
        super.stop();
    }
}
