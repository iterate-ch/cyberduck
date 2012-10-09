package ch.cyberduck.core;

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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.download.DownloadSymlinkResolver;
import ch.cyberduck.core.transfer.download.MoveLocalFilter;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.download.RegexFilter;
import ch.cyberduck.core.transfer.download.RenameFilter;
import ch.cyberduck.core.transfer.download.ResumeFilter;
import ch.cyberduck.core.transfer.download.SkipFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class DownloadTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(DownloadTransfer.class);

    private RegexFilter filter
            = new RegexFilter();

    public DownloadTransfer(Path root) {
        super(root);
    }

    public DownloadTransfer(List<Path> roots) {
        super(roots);
    }

    public <T> DownloadTransfer(T dict, Session s) {
        super(dict, s);
    }

    @Override
    protected void normalize() {
        log.debug("normalize");
        final List<Path> normalized = new Collection<Path>();
        for(Path download : this.getRoots()) {
            if(!this.check()) {
                return;
            }
            session.message(MessageFormat.format(Locale.localizedString("Prepare {0}", "Status"), download.getName()));
            boolean duplicate = false;
            for(Iterator<Path> iter = normalized.iterator(); iter.hasNext(); ) {
                Path n = iter.next();
                if(download.isChild(n)) {
                    // The selected file is a child of a directory already included
                    duplicate = true;
                    break;
                }
                if(n.isChild(download)) {
                    iter.remove();
                }
                if(download.getLocal().equals(n.getLocal())) {
                    // The selected file has the same name; if downloaded as a root element
                    // it would overwrite the earlier
                    final String parent = download.getLocal().getParent().getAbsolute();
                    final String filename = download.getName();
                    String proposal;
                    int no = 0;
                    do {
                        no++;
                        proposal = FilenameUtils.getBaseName(filename) + "-" + no;
                        if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                            proposal += "." + FilenameUtils.getExtension(filename);
                        }
                        download.setLocal(LocalFactory.createLocal(parent, proposal));
                    }
                    while(download.getLocal().exists());
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Changed local name from %s to %s", filename, download.getName()));
                    }
                }
            }
            // Prunes the list of selected files. Files which are a child of an already included directory
            // are removed from the returned list.
            if(!duplicate) {
                normalized.add(download);
            }
        }
        this.setRoots(normalized);
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = super.getSerializer();
        dict.setStringForKey(String.valueOf(KIND_DOWNLOAD), "Kind");
        return dict.getSerialized();
    }

    /**
     * Set download bandwidth
     */
    @Override
    protected void init() {
        log.debug("init");
        this.bandwidth = new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes"));
    }

    @Override
    public AttributedList<Path> children(final Path parent) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Children for %s", parent));
        }
        if(parent.attributes().isSymbolicLink() && new DownloadSymlinkResolver(roots).resolve(parent)) {
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
    public TransferPathFilter filter(final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        final DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(roots);
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
            return new MoveLocalFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return new SkipFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            for(Path download : this.getRoots()) {
                if(!this.check()) {
                    return null;
                }
                if(download.getLocal().exists()) {
                    if(download.getLocal().attributes().isDirectory()) {
                        if(0 == download.getLocal().children().size()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    if(download.getLocal().attributes().isFile()) {
                        if(download.getLocal().attributes().getSize() == 0) {
                            // Do not prompt for zero sized files
                            continue;
                        }
                    }
                    // Prompt user to choose a filter
                    TransferAction result = prompt.prompt();
                    return this.filter(result); //break out of loop
                }
            }
            // No files exist yet therefore it is most straightforward to use the overwrite action
            return this.filter(TransferAction.ACTION_OVERWRITE);
        }
        return super.filter(action);
    }

    @Override
    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        log.debug(String.format("Resume=%s,Reload=%s", resumeRequested, reloadRequested));
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
    protected void transfer(final Path file, final TransferOptions options) {
        log.debug("transfer:" + file);
        final Local local = file.getLocal();
        if(file.attributes().isSymbolicLink() && new DownloadSymlinkResolver(roots).resolve(file)) {
            // Make relative symbolic link
            final String target = StringUtils.substringAfter(file.getSymlinkTarget().getAbsolute(),
                    file.getParent().getAbsolute() + Path.DELIMITER);
            if(log.isDebugEnabled()) {
                log.debug("Symlink " + file.getLocal() + ":" + target);
            }
            file.getLocal().symlink(target);
            file.status().setComplete(true);
        }
        else if(file.attributes().isFile()) {
            file.download(bandwidth, new AbstractStreamListener() {
                @Override
                public void bytesReceived(long bytes) {
                    transferred += bytes;
                }
            }, false, options.quarantine);
        }
        else if(file.attributes().isDirectory()) {
            local.mkdir(true);
        }
    }

    @Override
    protected void fireTransferDidEnd() {
        if(this.isReset() && this.isComplete() && !this.isCanceled() && !(this.getTransferred() == 0)) {
            if(this.shouldOpenWhenComplete()) {
                this.getRoot().getLocal().open();
            }
            this.getRoot().getLocal().bounce();
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