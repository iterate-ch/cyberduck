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

import ch.cyberduck.core.AbstractStreamListener;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.filter.DownloadRegexFilter;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.transfer.download.CompareFilter;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.download.RenameExistingFilter;
import ch.cyberduck.core.transfer.download.RenameFilter;
import ch.cyberduck.core.transfer.download.ResumeFilter;
import ch.cyberduck.core.transfer.download.SkipFilter;
import ch.cyberduck.core.transfer.normalizer.DownloadRootPathsNormalizer;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * @version $Id$
 */
public class DownloadTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(DownloadTransfer.class);

    private DownloadRegexFilter filter
            = new DownloadRegexFilter();

    public DownloadTransfer(Path root) {
        this(Collections.singletonList(root));
    }

    public DownloadTransfer(List<Path> roots) {
        super(new DownloadRootPathsNormalizer().normalize(roots), new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
    }

    public <T> DownloadTransfer(T dict, Session s) {
        super(dict, s, new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = super.getSerializer();
        dict.setStringForKey(String.valueOf(KIND_DOWNLOAD), "Kind");
        return dict.<T>getSerialized();
    }

    @Override
    public AttributedList<Path> children(final Path parent) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Children for %s", parent));
        }
        if(parent.attributes().isSymbolicLink() && new DownloadSymlinkResolver(this.getRoots()).resolve(parent)) {
            if(log.isDebugEnabled()) {
                log.debug("Do not list children for symbolic link:" + parent);
            }
            return AttributedList.emptyList();
        }
        final AttributedList<Path> list = parent.children(filter);
        for(Path download : list) {
            // Change download path relative to parent local folder
            download.setLocal(LocalFactory.createLocal(parent.getLocal(), download.getLocal().getName()));
        }
        return list;
    }

    @Override
    public TransferPathFilter filter(final TransferPrompt prompt, final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        final DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(this.getRoots());
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return new OverwriteFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_RESUME)) {
            return new ResumeFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_RENAME)) {
            return new RenameFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_RENAME_EXISTING)) {
            return new RenameExistingFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return new SkipFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_COMPARISON)) {
            return new CompareFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            for(Path download : this.getRoots()) {
                if(download.getLocal().exists()) {
                    if(download.getLocal().attributes().isDirectory()) {
                        if(this.children(download).isEmpty()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    // Prompt user to choose a filter
                    final TransferAction result = prompt.prompt();
                    return this.filter(prompt, result);
                }
            }
            // No files exist yet therefore it is most straightforward to use the overwrite action
            return this.filter(prompt, TransferAction.ACTION_OVERWRITE);
        }
        return super.filter(prompt, action);
    }

    @Override
    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Find transfer action for Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
        if(resumeRequested) {
            // Force resume
            return TransferAction.ACTION_RESUME;
        }
        if(reloadRequested) {
            return TransferAction.forName(
                    Preferences.instance().getProperty("queue.download.reload.fileExists")
            );
        }
        // Use default
        return TransferAction.forName(
                Preferences.instance().getProperty("queue.download.fileExists")
        );
    }

    @Override
    protected void transfer(final Path file, final TransferOptions options, final TransferStatus status) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        final Local local = file.getLocal();
        final DownloadSymlinkResolver symlinkResolver = new DownloadSymlinkResolver(this.getRoots());
        if(file.attributes().isSymbolicLink() && symlinkResolver.resolve(file)) {
            // Make relative symbolic link
            final String target = symlinkResolver.relativize(file.getAbsolute(),
                    file.getSymlinkTarget().getAbsolute());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Create symbolic link from %s to %s", file.getLocal(), target));
            }
            file.getLocal().symlink(target);
            status.setComplete();
        }
        else if(file.attributes().isFile()) {
            file.download(this.getBandwidth(), new AbstractStreamListener() {
                @Override
                public void bytesReceived(long bytes) {
                    transferred += bytes;
                }
            }, status);
        }
        else if(file.attributes().isDirectory()) {
            local.mkdir(true);
        }
    }

    @Override
    protected void fireTransferDidEnd() {
        if(this.isReset() && this.isComplete() && !this.isCanceled() && !(this.getTransferred() == 0)) {
            final ApplicationLauncher launcher = ApplicationLauncherFactory.get();
            if(this.shouldOpenWhenComplete()) {
                launcher.open(this.getRoot().getLocal());
            }
            launcher.bounce(this.getRoot().getLocal());
        }
        super.fireTransferDidEnd();
    }

    /**
     * @return Open file with default application
     */
    protected boolean shouldOpenWhenComplete() {
        return Preferences.instance().getBoolean("queue.postProcessItemWhenComplete");
    }

    @Override
    public boolean isResumable() {
        return session.isDownloadResumable();
    }

    @Override
    public boolean isReloadable() {
        return true;
    }

    @Override
    public String getStatus() {
        return this.isComplete() ? "Download complete" : "Transfer incomplete";
    }

    @Override
    public String getImage() {
        return "transfer-download.tiff";
    }
}