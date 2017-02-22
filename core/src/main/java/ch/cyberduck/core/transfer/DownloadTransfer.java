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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.filter.DownloadDuplicateFilter;
import ch.cyberduck.core.filter.DownloadRegexFilter;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.local.LocalSymlinkFactory;
import ch.cyberduck.core.local.features.Symlink;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.download.AbstractDownloadFilter;
import ch.cyberduck.core.transfer.download.CompareFilter;
import ch.cyberduck.core.transfer.download.DownloadFilterOptions;
import ch.cyberduck.core.transfer.download.DownloadRegexPriorityComparator;
import ch.cyberduck.core.transfer.download.IconUpdateSreamListener;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.download.RenameExistingFilter;
import ch.cyberduck.core.transfer.download.RenameFilter;
import ch.cyberduck.core.transfer.download.ResumeFilter;
import ch.cyberduck.core.transfer.download.SkipFilter;
import ch.cyberduck.core.transfer.download.TrashFilter;
import ch.cyberduck.core.transfer.normalizer.DownloadRootPathsNormalizer;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DownloadTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(DownloadTransfer.class);

    private final Filter<Path> filter;

    private final Comparator<Path> comparator;

    private PathCache cache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    private final DownloadSymlinkResolver symlinkResolver;

    private DownloadFilterOptions options = new DownloadFilterOptions();

    public DownloadTransfer(final Host host, final Path root, final Local local) {
        this(host, Collections.singletonList(new TransferItem(root, local)),
                PreferencesFactory.get().getBoolean("queue.download.skip.enable") ? new DownloadRegexFilter() : new DownloadDuplicateFilter());
    }

    public DownloadTransfer(final Host host, final Path root, final Local local, final Filter<Path> f) {
        this(host, Collections.singletonList(new TransferItem(root, local)), f);
    }

    public DownloadTransfer(final Host host, final List<TransferItem> roots) {
        this(host, new DownloadRootPathsNormalizer().normalize(roots),
                PreferencesFactory.get().getBoolean("queue.download.skip.enable") ? new DownloadRegexFilter() : new DownloadDuplicateFilter());
    }

    public DownloadTransfer(final Host host, final List<TransferItem> roots, final Filter<Path> f) {
        this(host, roots, f, new DownloadRegexPriorityComparator());
    }

    public DownloadTransfer(final Host host, final List<TransferItem> roots, final Filter<Path> f, final Comparator<Path> comparator) {
        super(host, new DownloadRootPathsNormalizer().normalize(roots), new BandwidthThrottle(
                PreferencesFactory.get().getFloat("queue.download.bandwidth.bytes")));
        this.filter = f;
        this.comparator = comparator;
        this.symlinkResolver = new DownloadSymlinkResolver(roots);
    }

    @Override
    public DownloadTransfer withCache(final PathCache cache) {
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
    public List<TransferItem> list(final Session<?> source, final Session<?> destination, final Path directory,
                                   final Local local, final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List children for %s", directory));
        }
        if(directory.isSymbolicLink()
                && new DownloadSymlinkResolver(roots).resolve(directory)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Do not list children for symbolic link %s", directory));
            }
            return Collections.emptyList();
        }
        else {
            final AttributedList<Path> list;
            if(cache.containsKey(directory)) {
                list = cache.get(directory);
            }
            else {
                list = source.getFeature(ListService.class).list(directory, listener);
                cache.put(directory, list);
            }
            final List<TransferItem> children = new ArrayList<TransferItem>();
            // Return copy with filtered result only
            for(Path f : new AttributedList<Path>(list.filter(comparator, filter))) {
                children.add(new TransferItem(f, LocalFactory.get(local, f.getName())));
            }
            return children;
        }
    }

    @Override
    public AbstractDownloadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action));
        }
        final DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(roots);
        if(action.equals(TransferAction.resume)) {
            return new ResumeFilter(resolver, source, options).withCache(cache);
        }
        if(action.equals(TransferAction.rename)) {
            return new RenameFilter(resolver, source, options).withCache(cache);
        }
        if(action.equals(TransferAction.renameexisting)) {
            return new RenameExistingFilter(resolver, source, options).withCache(cache);
        }
        if(action.equals(TransferAction.skip)) {
            return new SkipFilter(resolver, source, options).withCache(cache);
        }
        if(action.equals(TransferAction.trash)) {
            return new TrashFilter(resolver, source, options).withCache(cache);
        }
        if(action.equals(TransferAction.comparison)) {
            return new CompareFilter(resolver, source, options, listener).withCache(cache);
        }
        return new OverwriteFilter(resolver, source, options).withCache(cache);
    }

    @Override
    public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                 final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action for Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
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
    public void pre(final Session<?> source, final Session<?> destination, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Bulk feature = source.getFeature(Bulk.class);
        final Object id = feature.pre(Type.download, files, callback);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
        }
    }

    @Override
    public void transfer(final Session<?> source, final Session<?> destination, final Path file, final Local local, final TransferOptions options,
                         final TransferStatus status, final ConnectionCallback callback,
                         final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        if(file.isSymbolicLink()) {
            if(symlinkResolver.resolve(file)) {
                // Make relative symbolic link
                final String target = symlinkResolver.relativize(file.getAbsolute(),
                        file.getSymlinkTarget().getAbsolute());
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Create symbolic link from %s to %s", local, target));
                }
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
                folder.mkdir();
            }
            // Transfer
            final Download download = source.getFeature(Download.class);
            download.download(file, local, bandwidth, new IconUpdateSreamListener(streamListener, status, local) {
                @Override
                public void recv(final long bytes) {
                    addTransferred(bytes);
                    super.recv(bytes);
                }
            }, status, callback);
        }
        else if(file.isDirectory()) {
            if(!status.isExists()) {
                listener.message(MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"),
                        local.getName()));
                local.mkdir();
                status.setComplete();
            }
        }
    }

}