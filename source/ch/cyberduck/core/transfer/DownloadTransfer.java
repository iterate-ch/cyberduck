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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.filter.DownloadRegexFilter;
import ch.cyberduck.core.io.AbstractStreamListener;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.local.IconService;
import ch.cyberduck.core.local.IconServiceFactory;
import ch.cyberduck.core.transfer.download.CompareFilter;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.download.RenameExistingFilter;
import ch.cyberduck.core.transfer.download.RenameFilter;
import ch.cyberduck.core.transfer.download.ResumeFilter;
import ch.cyberduck.core.transfer.download.SkipFilter;
import ch.cyberduck.core.transfer.normalizer.DownloadRootPathsNormalizer;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * @version $Id$
 */
public class DownloadTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(DownloadTransfer.class);

    private Filter<Path> filter
            = new DownloadRegexFilter();

    private final IconService icon
            = IconServiceFactory.get();

    private Read reader;

    public DownloadTransfer(final Session<?> session, final Path root) {
        this(session, Collections.singletonList(root));
        reader = session.getFeature(Read.class);
    }

    public DownloadTransfer(final Session<?> session, final List<Path> roots, final Filter<Path> f) {
        super(session, new DownloadRootPathsNormalizer().normalize(roots), new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
        reader = session.getFeature(Read.class);
        filter = f;
    }

    public DownloadTransfer(final Session<?> session, final List<Path> roots) {
        super(session, new DownloadRootPathsNormalizer().normalize(roots), new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
        reader = session.getFeature(Read.class);
    }

    public <T> DownloadTransfer(final T dict, final Session<?> s) {
        super(dict, s, new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
        reader = session.getFeature(Read.class);
    }

    @Override
    public Type getType() {
        return Type.download;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final TransferStatus parent) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List children for %s", directory));
        }
        if(directory.attributes().isSymbolicLink()
                && new DownloadSymlinkResolver(this.getRoots()).resolve(directory)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Do not list children for symbolic link %s", directory));
            }
            return AttributedList.emptyList();
        }
        else {
            final AttributedList<Path> list = session.list(directory, new DisabledListProgressListener());
            for(Path download : list) {
                // Change download path relative to parent local folder
                download.setLocal(LocalFactory.createLocal(directory.getLocal(), download.getName()));
            }
            // Return copy with filtered result only
            return new AttributedList<Path>(list.filter(filter));
        }
    }

    @Override
    public TransferPathFilter filter(final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        final SymlinkResolver resolver = new DownloadSymlinkResolver(this.getRoots());
        if(action.equals(TransferAction.ACTION_RESUME)) {
            return new ResumeFilter(resolver, session);
        }
        if(action.equals(TransferAction.ACTION_RENAME)) {
            return new RenameFilter(resolver, session);
        }
        if(action.equals(TransferAction.ACTION_RENAME_EXISTING)) {
            return new RenameExistingFilter(resolver, session);
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return new SkipFilter(resolver, session);
        }
        if(action.equals(TransferAction.ACTION_COMPARISON)) {
            return new CompareFilter(resolver, session);
        }
        return new OverwriteFilter(resolver, session);
    }

    @Override
    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested,
                                 final TransferPrompt prompt) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action for Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
        final TransferAction action;
        if(resumeRequested) {
            // Force resume
            action = TransferAction.ACTION_RESUME;
        }
        else if(reloadRequested) {
            action = TransferAction.forName(
                    Preferences.instance().getProperty("queue.download.reload.fileExists"));
        }
        else {
            // Use default
            action = TransferAction.forName(
                    Preferences.instance().getProperty("queue.download.fileExists")
            );
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            for(Path download : this.getRoots()) {
                final Local local = download.getLocal();
                if(local.exists()) {
                    if(local.attributes().isDirectory()) {
                        if(local.list().isEmpty()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    if(local.attributes().isFile()) {
                        if(local.attributes().getSize() == 0) {
                            // Dragging a file to the local volume creates the file already
                            continue;
                        }
                    }
                    // Prompt user to choose a filter
                    return prompt.prompt();
                }
            }
            // No files exist yet therefore it is most straightforward to use the overwrite action
            return TransferAction.ACTION_OVERWRITE;
        }
        return action;
    }

    @Override
    public void transfer(final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        final Local local = file.getLocal();
        final SymlinkResolver symlinkResolver = new DownloadSymlinkResolver(this.getRoots());
        if(file.attributes().isSymbolicLink() && symlinkResolver.resolve(file)) {
            // Make relative symbolic link
            final String target = symlinkResolver.relativize(file.getAbsolute(),
                    file.getSymlinkTarget().getAbsolute());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Create symbolic link from %s to %s", file.getLocal(), target));
            }
            file.getLocal().symlink(target);
        }
        else if(file.attributes().isFile()) {
            session.message(MessageFormat.format(LocaleFactory.localizedString("Downloading {0}", "Status"),
                    file.getName()));
            local.getParent().mkdir();
            // Transfer
            this.download(file, bandwidth, new AbstractStreamListener() {
                // Only update the file custom icon if the size is > 5MB. Otherwise creating too much
                // overhead when transferring a large amount of files
                private final boolean threshold
                        = file.attributes().getSize() > Preferences.instance().getLong("queue.download.icon.threshold");

                // An integer between 0 and 9
                private int step = 0;

                @Override
                public void bytesReceived(long bytes) {
                    addTransferred(bytes);
                    if(threshold) {
                        if(Preferences.instance().getBoolean("queue.download.icon.update")) {
                            int fraction = (int) (status.getCurrent() / file.attributes().getSize() * 10);
                            if(fraction > step) {
                                // Another 10 percent of the file has been transferred
                                icon.set(local, ++step);
                            }
                        }
                    }
                }
            }, status);
        }
        else if(file.attributes().isDirectory()) {
            if(!status.isExists()) {
                local.mkdir();
            }
        }
    }

    private void download(final Path file, final BandwidthThrottle throttle, final StreamListener listener,
                          final TransferStatus status) throws BackgroundException {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = reader.read(file, status);
                out = file.getLocal().getOutputStream(status.isAppend());
                new StreamCopier(status).transfer(new ThrottledInputStream(in, throttle), 0, out, listener);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, file);
        }
    }
}