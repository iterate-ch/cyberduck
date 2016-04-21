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
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.filter.UploadRegexFilter;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
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

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class UploadTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(UploadTransfer.class);

    private Filter<Local> filter;

    private Comparator<Local> comparator;

    private PathCache cache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    public UploadTransfer(final Host host, final Path root, final Local local) {
        this(host, Collections.singletonList(new TransferItem(root, local)),
                PreferencesFactory.get().getBoolean("queue.upload.skip.enable") ? new UploadRegexFilter() : new NullFilter<Local>());
    }

    public UploadTransfer(final Host host, final Path root, final Local local, final Filter<Local> f) {
        this(host, Collections.singletonList(new TransferItem(root, local)), f);
    }

    public UploadTransfer(final Host host, final List<TransferItem> roots) {
        this(host, new UploadRootPathsNormalizer().normalize(roots),
                PreferencesFactory.get().getBoolean("queue.upload.skip.enable") ? new UploadRegexFilter() : new NullFilter<Local>());
    }

    public UploadTransfer(final Host host, final List<TransferItem> roots, final Filter<Local> f) {
        this(host, roots, f, new UploadRegexPriorityComparator());
    }

    public UploadTransfer(final Host host, final List<TransferItem> roots, final Filter<Local> f, final Comparator<Local> comparator) {
        super(host, new UploadRootPathsNormalizer().normalize(roots), new BandwidthThrottle(
                PreferencesFactory.get().getFloat("queue.upload.bandwidth.bytes")));
        this.filter = f;
        this.comparator = comparator;
    }

    @Override
    public Transfer withCache(final PathCache cache) {
        this.cache = cache;
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
        final List<TransferItem> children = new ArrayList<TransferItem>();
        for(Local local : directory.list().filter(comparator, filter)) {
            children.add(new TransferItem(new Path(remote, local.getName(),
                    local.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file)), local));
        }
        return children;
    }

    @Override
    public AbstractUploadFilter filter(final Session<?> session, final TransferAction action, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action));
        }
        final Symlink symlink = session.getFeature(Symlink.class);
        final UploadSymlinkResolver resolver = new UploadSymlinkResolver(symlink, roots);
        final UploadFilterOptions options = new UploadFilterOptions();
        options.withTemporary(options.temporary && session.getFeature(Write.class).temporary());
        if(action.equals(TransferAction.resume)) {
            return new ResumeFilter(resolver, session, options).withCache(cache);
        }
        if(action.equals(TransferAction.rename)) {
            return new RenameFilter(resolver, session, options).withCache(cache);
        }
        if(action.equals(TransferAction.renameexisting)) {
            return new RenameExistingFilter(resolver, session, options).withCache(cache);
        }
        if(action.equals(TransferAction.skip)) {
            return new SkipFilter(resolver, session, options).withCache(cache);
        }
        if(action.equals(TransferAction.comparison)) {
            return new CompareFilter(resolver, session, options, listener).withCache(cache);
        }
        return new OverwriteFilter(resolver, session, options).withCache(cache);
    }

    @Override
    public TransferAction action(final Session<?> session, final boolean resumeRequested, final boolean reloadRequested,
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
                    PreferencesFactory.get().getProperty("queue.upload.reload.action"));
        }
        else {
            // Use default
            action = TransferAction.forName(
                    PreferencesFactory.get().getProperty("queue.upload.action"));
        }
        if(action.equals(TransferAction.callback)) {
            for(TransferItem upload : roots) {
                final Upload write = session.getFeature(Upload.class);
                final Write.Append append = write.append(upload.remote, upload.local.attributes().getSize(), cache);
                if(append.override || append.append) {
                    // Found remote file
                    if(upload.remote.isDirectory()) {
                        if(this.list(session, upload.remote, upload.local, listener).isEmpty()) {
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
    public void pre(final Session<?> session, final Map<Path, TransferStatus> files) throws BackgroundException {
        final Bulk feature = session.getFeature(Bulk.class);
        if(null != feature) {
            final Object id = feature.pre(Type.upload, files);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained bulk id %s for transfer %s", id, this));
            }
        }
    }

    @Override
    public void transfer(final Session<?> session, final Path file, final Local local, final TransferOptions options,
                         final TransferStatus status, final ConnectionCallback callback,
                         final ProgressListener listener, final StreamListener streamListener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        if(local.isSymbolicLink()) {
            final Symlink feature = session.getFeature(Symlink.class);
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
            final Upload upload = session.getFeature(Upload.class);
            upload.upload(file, local, bandwidth, new DisabledStreamListener() {
                @Override
                public void sent(long bytes) {
                    addTransferred(bytes);
                    streamListener.sent(bytes);
                    super.sent(bytes);
                }
            }, status, callback);
        }
        else if(file.isDirectory()) {
            if(!status.isExists()) {
                listener.message(MessageFormat.format(LocaleFactory.localizedString("Making directory {0}", "Status"),
                        file.getName()));
                final Directory feature = session.getFeature(Directory.class);
                feature.mkdir(file);
                status.setComplete();
            }
        }
    }

}
