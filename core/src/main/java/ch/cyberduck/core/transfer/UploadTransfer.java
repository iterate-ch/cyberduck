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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.filter.UploadRegexFilter;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.normalizer.UploadRootPathsNormalizer;
import ch.cyberduck.core.transfer.symlink.UploadSymlinkResolver;
import ch.cyberduck.core.transfer.upload.AbstractUploadFilter;
import ch.cyberduck.core.transfer.upload.CompareFilter;
import ch.cyberduck.core.transfer.upload.OverwriteFilter;
import ch.cyberduck.core.transfer.upload.RenameExistingFilter;
import ch.cyberduck.core.transfer.upload.RenameFilter;
import ch.cyberduck.core.transfer.upload.ResumeFilter;
import ch.cyberduck.core.transfer.upload.SkipFilter;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;
import ch.cyberduck.core.transfer.upload.UploadRegexPriorityComparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UploadTransfer extends Transfer {
    private static final Logger log = LogManager.getLogger(UploadTransfer.class);

    private final Filter<Local> filter;
    private final Comparator<Local> comparator;

    private Cache<Path> cache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    private UploadFilterOptions options = new UploadFilterOptions(host);

    public UploadTransfer(final Host host, final Path root, final Local local) {
        this(host, Collections.singletonList(new TransferItem(root, local)),
                PreferencesFactory.get().getBoolean("queue.upload.skip.enable") ? new UploadRegexFilter() : new NullFilter<>());
    }

    public UploadTransfer(final Host host, final Path root, final Local local, final Filter<Local> f) {
        this(host, Collections.singletonList(new TransferItem(root, local)), f);
    }

    public UploadTransfer(final Host host, final List<TransferItem> roots) {
        this(host, roots,
                PreferencesFactory.get().getBoolean("queue.upload.skip.enable") ? new UploadRegexFilter() : new NullFilter<>());
    }

    public UploadTransfer(final Host host, final List<TransferItem> roots, final Filter<Local> f) {
        this(host, roots, f, new UploadRegexPriorityComparator());
    }

    public UploadTransfer(final Host host, final List<TransferItem> roots, final Filter<Local> f, final Comparator<Local> comparator) {
        super(host, roots, new BandwidthThrottle(PreferencesFactory.get().getFloat("queue.upload.bandwidth.bytes")));
        this.filter = f;
        this.comparator = comparator;
    }

    @Override
    public Transfer withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

    public Transfer withOptions(final UploadFilterOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public Type getType() {
        return Type.upload;
    }

    @Override
    public List<TransferItem> list(final Session<?> session, final Path remote,
                                   final Local directory, final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List children for %s", directory));
        }
        if(directory.isSymbolicLink()) {
            final Symlink symlink = session.getFeature(Symlink.class);
            if(new UploadSymlinkResolver(symlink, roots).resolve(directory)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Do not list children for symbolic link %s", directory));
                }
                // We can resolve the target of the symbolic link and will create a link on the remote system
                // using the symlink feature of the session
                return Collections.emptyList();
            }
        }
        final List<TransferItem> children = new ArrayList<>();
        for(Local local : directory.list().filter(comparator, filter)) {
            children.add(new TransferItem(new Path(remote, local.getName(),
                    local.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file)), local));
        }
        return children;
    }

    @Override
    public AbstractUploadFilter filter(final Session<?> source, final Session<?> destination, final TransferAction action, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s and options %s", action, options));
        }
        final Symlink symlink = source.getFeature(Symlink.class);
        final UploadSymlinkResolver resolver = new UploadSymlinkResolver(symlink, roots);
        final Find find;
        final AttributesFinder attributes;
        if(roots.size() > 1 || roots.stream().filter(item -> item.remote.isDirectory()).findAny().isPresent()) {
            find = new CachingFindFeature(cache, source.getFeature(Find.class, new DefaultFindFeature(source)));
            attributes = new CachingAttributesFinderFeature(cache, source.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(source)));
        }
        else {
            find = new CachingFindFeature(cache, source.getFeature(Find.class));
            attributes = new CachingAttributesFinderFeature(cache, source.getFeature(AttributesFinder.class));
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Determined features %s and %s", find, attributes));
        }
        if(action.equals(TransferAction.resume)) {
            return new ResumeFilter(resolver, source, options).withFinder(find).withAttributes(attributes);
        }
        if(action.equals(TransferAction.rename)) {
            return new RenameFilter(resolver, source, options).withFinder(find).withAttributes(attributes);
        }
        if(action.equals(TransferAction.renameexisting)) {
            return new RenameExistingFilter(resolver, source, options).withFinder(find).withAttributes(attributes);
        }
        if(action.equals(TransferAction.skip)) {
            return new SkipFilter(resolver, source, options).withFinder(find).withAttributes(attributes);
        }
        if(action.equals(TransferAction.comparison)) {
            return new CompareFilter(resolver, source, options, listener).withFinder(find).withAttributes(attributes);
        }
        return new OverwriteFilter(resolver, source, options).withFinder(find).withAttributes(attributes);
    }

    @Override
    public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested,
                                 final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action with prompt %s", prompt));
        }
        if(resumeRequested) {
            // Force resume by user or retry of failed transfer
            return TransferAction.resume;
        }
        final TransferAction action;
        if(reloadRequested) {
            action = TransferAction.forName(
                    PreferencesFactory.get().getProperty("queue.upload.reload.action"));
        }
        else {
            // Use default
            action = TransferAction.forName(
                    PreferencesFactory.get().getProperty("queue.upload.action"));
        }
        if(action.equals(TransferAction.callback)) {
            for(TransferItem upload : roots) {
                if(new CachingFindFeature(cache, source.getFeature(Find.class, new DefaultFindFeature(source))).find(upload.remote)) {
                    // Found remote file
                    if(upload.remote.isDirectory()) {
                        if(this.list(source, upload.remote, upload.local, listener).isEmpty()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    // Prompt user to choose a filter
                    return prompt.prompt(upload);
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
        final Object id = feature.pre(Type.upload, files, callback);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
        }
        super.pre(source, destination, files, filter, error, progress, callback);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter for directories in transfer %s", this));
        }
        // Create all directories first
        final List<Map.Entry<TransferItem, TransferStatus>> directories = files.entrySet().stream()
                .filter(item -> item.getKey().remote.isDirectory())
                .filter(item -> !item.getValue().isExists())
                .sorted(new Comparator<Map.Entry<TransferItem, TransferStatus>>() {
                    @Override
                    public int compare(final Map.Entry<TransferItem, TransferStatus> o1, final Map.Entry<TransferItem, TransferStatus> o2) {
                        if(o1.getKey().remote.isChild(o2.getKey().remote)) {
                            return 1;
                        }
                        if(o2.getKey().remote.isChild(o1.getKey().remote)) {
                            return -1;
                        }
                        // Same parent
                        return Integer.compare(StringUtils.countMatches(o1.getKey().remote.getAbsolute(), Path.DELIMITER),
                                StringUtils.countMatches(o2.getKey().remote.getAbsolute(), Path.DELIMITER));
                    }
                }).collect(Collectors.toList());
        final Directory mkdir = source.getFeature(Directory.class);
        for(Map.Entry<TransferItem, TransferStatus> entry : directories) {
            final Path file = entry.getKey().remote;
            final TransferStatus status = entry.getValue();
            if(status.isExists()) {
                if(log.isWarnEnabled()) {
                    log.warn(String.format("Skip existing directory %s", file));
                }
                continue;
            }
            status.validate();
            progress.message(MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"), file.getName()));
            try {
                mkdir.mkdir(file, status);
                // Post process of file
                filter.complete(
                        status.getRename().remote != null ? status.getRename().remote : entry.getKey().remote,
                        status.getRename().local != null ? status.getRename().local : entry.getKey().local,
                        status.complete(), progress);
            }
            catch(BackgroundException e) {
                if(error.prompt(entry.getKey(), status, e, files.size())) {
                    // Continue
                    log.warn(String.format("Ignore transfer failure %s", e));
                }
                else {
                    throw new TransferCanceledException(e);
                }
            }
        }
    }

    @Override
    public void post(final Session<?> source, final Session<?> destination, final Map<TransferItem, TransferStatus> files,
                     final TransferErrorCallback error, final ProgressListener listener, final ConnectionCallback callback) throws BackgroundException {
        final Bulk<?> bulk = source.getFeature(Bulk.class);
        try {
            bulk.post(Type.upload, files, callback);
            super.post(source, destination, files, error, listener, callback);
        }
        catch(BackgroundException e) {
            final Optional<Map.Entry<TransferItem, TransferStatus>> entry = files.entrySet().stream().findFirst();
            if(entry.isPresent()) {
                final Map.Entry<TransferItem, TransferStatus> item = entry.get();
                if(log.isWarnEnabled()) {
                    log.warn(String.format("Prompt with failure %s for item %s only", e, item.getKey()));
                }
                if(error.prompt(item.getKey(), item.getValue(), e, files.size())) {
                    // Continue
                    log.warn(String.format("Ignore transfer failure %s", e));
                }
                else {
                    throw new TransferCanceledException(e);
                }
            }
        }
        if(options.versioning) {
            final Versioning versioning = source.getFeature(Versioning.class);
            if(versioning != null) {
                for(TransferItem item : files.keySet()) {
                    if(versioning.getConfiguration(item.remote).isEnabled()) {
                        versioning.cleanup(item.remote, callback);
                    }
                }
            }
        }
    }

    @Override
    public void transfer(final Session<?> source, final Session<?> destination, final Path file, final Local local, final TransferOptions options,
                         final TransferStatus overall, final TransferStatus segment, final ConnectionCallback connectionCallback,
                         final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s and status %s", file, options, segment));
        }
        if(local.isSymbolicLink()) {
            final Symlink feature = source.getFeature(Symlink.class);
            final UploadSymlinkResolver symlinkResolver
                    = new UploadSymlinkResolver(feature, roots);
            if(symlinkResolver.resolve(local)) {
                // Make relative symbolic link
                final String target = symlinkResolver.relativize(local.getAbsolute(),
                        local.getSymlinkTarget().getAbsolute());
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Create symbolic link from %s to %s", file, target));
                }
                feature.symlink(file, target);
                return;
            }
        }
        if(file.isFile()) {
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Uploading {0}", "Status"),
                    file.getName()));
            // Transfer
            final Upload upload = source.getFeature(Upload.class);
            final Object reply = upload.upload(file, local, bandwidth, streamListener, segment, connectionCallback);
        }
    }

    @Override
    public void normalize() {
        List<TransferItem> normalized = new UploadRootPathsNormalizer().normalize(roots);
        roots.clear();
        roots.addAll(normalized);
    }

    @Override
    public void stop() {
        cache.clear();
        super.stop();
    }
}
