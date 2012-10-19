package ch.cyberduck.core.transfer.download;

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
import ch.cyberduck.core.local.IconService;
import ch.cyberduck.core.local.IconServiceFactory;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;
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

    private final IconService icon
            = IconServiceFactory.get();

    public DownloadTransfer(final Path root) {
        this(Collections.singletonList(root));
    }

    public DownloadTransfer(final List<Path> roots) {
        super(new DownloadRootPathsNormalizer().normalize(roots), new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
        for(Path download : roots) {
            if(null == download.getLocal()) {
                // No custom download path set
                download.setLocal(LocalFactory.createLocal(session.getHost().getDownloadFolder(), this.getName()));
            }
        }
    }

    public <T> DownloadTransfer(final T dict, final Session s) {
        super(dict, s, new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = super.getSerializer();
        dict.setStringForKey(String.valueOf(KIND_DOWNLOAD), "Kind");
        return dict.getSerialized();
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
            download.setLocal(LocalFactory.createLocal(parent.getLocal(), download.getName()));
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
    public void transfer(final Path file, final TransferOptions options, final TransferStatus status) {
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
            final boolean icon = Preferences.instance().getBoolean("queue.download.icon.update");
            // Only update the file custom icon if the size is > 5MB. Otherwise creating too much
            // overhead when transferring a large amount of files
            final boolean threshold
                    = file.attributes().getSize() > Preferences.instance().getLong("queue.download.icon.threshold");
            // Set the first progress icon
            this.icon.setProgress(local, 0);
            file.download(this.getBandwidth(), new AbstractStreamListener() {
                private int step = 0;

                @Override
                public void bytesReceived(long bytes) {
                    transferred += bytes;
                    if(icon) {
                        if(-1 == bytes) {
                            // Remove custom icon if complete. The Finder will display the default
                            // icon for this filetype
                            DownloadTransfer.this.icon.setProgress(local, -1);
                        }
                        else {
                            if(threshold) {
                                int fraction = (int) (status.getCurrent() / file.attributes().getSize() * 10);
                                // An integer between 0 and 9
                                if(fraction > step) {
                                    // Another 10 percent of the file has been transferred
                                    DownloadTransfer.this.icon.setProgress(local, ++step);
                                }
                            }
                        }
                    }
                }
            }, status);
        }
        else if(file.attributes().isDirectory()) {
            local.mkdir();
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