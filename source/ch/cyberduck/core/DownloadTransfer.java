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
import ch.cyberduck.ui.DateFormatterFactory;
import ch.cyberduck.ui.growl.Growl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public class DownloadTransfer extends Transfer {
    private static Logger log = Logger.getLogger(DownloadTransfer.class);

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
            this.getSession().message(MessageFormat.format(Locale.localizedString("Prepare {0}", "Status"), download.getName()));
            boolean duplicate = false;
            for(Iterator<Path> iter = normalized.iterator(); iter.hasNext();) {
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
                        log.info("Changed local name to:" + download.getName());
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
        return dict.<T>getSerialized();
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

    /**
     *
     */
    private abstract class DownloadTransferFilter extends TransferFilter {
        public boolean accept(Path file) {
            if(file.attributes().isSymbolicLink()) {
                if(!DownloadTransfer.this.isSymlinkSupported(file)) {
                    final AbstractPath target = file.getSymlinkTarget();
                    // Do not transfer files referenced from symlinks pointing to files also included
                    for(Path root : roots) {
                        if(target.isChild(root)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public void prepare(Path file) {
            if(file.attributes().getSize() == -1) {
                file.readSize();
            }
            if(file.getSession().isTimestampSupported()) {
                if(file.attributes().getModificationDate() == -1) {
                    if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
                        file.readTimestamp();
                    }
                }
            }
            if(file.getSession().isUnixPermissionsSupported()) {
                if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
                    if(file.attributes().getPermission().equals(Permission.EMPTY)) {
                        file.readUnixPermission();
                    }
                }
            }
            if(file.attributes().isFile()) {
                if(file.attributes().isSymbolicLink()) {
                    if(!DownloadTransfer.this.isSymlinkSupported(file)) {
                        // A server will resolve the symbolic link when the file is requested.
                        final Path target = (Path) file.getSymlinkTarget();
                        if(target.attributes().getSize() == -1) {
                            target.readSize();
                        }
                        size += target.attributes().getSize();
                    }
                    else {
                        // No file size increase for symbolic link to be created locally
                    }
                }
                else {
                    // Read file size
                    size += file.attributes().getSize();
                }
                if(file.status().isResume()) {
                    transferred += file.getLocal().attributes().getSize();
                }
            }
            if(!file.getLocal().getParent().exists()) {
                // Create download folder if missing
                file.getLocal().getParent().mkdir(true);
            }
        }

        @Override
        public void complete(Path p) {
            ;
        }
    }

    private final PathFilter<Path> exclusionRegexFilter = new PathFilter<Path>() {
        final Pattern pattern
                = Pattern.compile(Preferences.instance().getProperty("queue.download.skip.regex"));

        public boolean accept(Path child) {
            if(Preferences.instance().getBoolean("queue.download.skip.enable")) {
                try {
                    if(pattern.matcher(child.getName()).matches()) {
                        return false;
                    }
                }
                catch(PatternSyntaxException e) {
                    log.warn(e.getMessage());
                }
            }
            return true;
        }
    };

    @Override
    public AttributedList<Path> children(final Path parent) {
        if(log.isDebugEnabled()) {
            log.debug("children:" + parent);
        }
        if(parent.attributes().isSymbolicLink()
                && this.isSymlinkSupported(parent)) {
            if(log.isDebugEnabled()) {
                log.debug("Do not list children for symbolic link:" + parent);
            }
            return AttributedList.emptyList();
        }
        final AttributedList<Path> list = parent.children(exclusionRegexFilter);
        for(Path download : list) {
            // Change download path relative to parent local folder
            download.setLocal(LocalFactory.createLocal(parent.getLocal(), download.getLocal().getName()));
        }
        return list;
    }

    private final TransferFilter OVERWRITE_FILTER = new DownloadTransferFilter() {
        @Override
        public boolean accept(final Path file) {
            if(file.attributes().isDirectory()) {
                if(file.getLocal().exists()) {
                    return false;
                }
            }
            return super.accept(file);
        }

        @Override
        public void prepare(final Path file) {
            if(file.attributes().isFile()) {
                file.status().setResume(false);
            }
            super.prepare(file);
        }
    };

    private final TransferFilter RESUME_FILTER = new DownloadTransferFilter() {
        @Override
        public boolean accept(final Path file) {
            if(file.attributes().isDirectory()) {
                if(file.getLocal().exists()) {
                    return false;
                }
            }
            if(file.status().isComplete()
                    || file.getLocal().attributes().getSize() == file.attributes().getSize()) {
                // No need to resume completed transfers
                file.status().setComplete(true);
                return false;
            }
            return super.accept(file);
        }

        @Override
        public void prepare(final Path file) {
            if(file.attributes().isFile()) {
                final boolean resume = file.getLocal().exists()
                        && file.getLocal().attributes().getSize() > 0;
                file.status().setResume(resume);
                long skipped = file.getLocal().attributes().getSize();
                file.status().setCurrent(skipped);
            }
            super.prepare(file);
        }
    };

    private final TransferFilter RENAME_FILTER = new DownloadTransferFilter() {
        @Override
        public void prepare(final Path file) {
            if(file.attributes().isFile()) {
                file.status().setResume(false);
            }
            if(file.getLocal().exists() && file.getLocal().attributes().getSize() > 0) {
                final String parent = file.getLocal().getParent().getAbsolute();
                final String filename = file.getName();
                int no = 0;
                while(file.getLocal().exists()) {
                    no++;
                    String proposal = FilenameUtils.getBaseName(filename) + "-" + no;
                    if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                        proposal += "." + FilenameUtils.getExtension(filename);
                    }
                    file.setLocal(LocalFactory.createLocal(parent, proposal));
                }
                if(log.isInfoEnabled()) {
                    log.info("Changed local name to:" + file.getLocal().getName());
                }
            }
            super.prepare(file);
        }
    };

    /**
     * Rename existing file on disk if there is a conflict.
     */
    private final TransferFilter RENAME_EXISTING_FILTER = new DownloadTransferFilter() {
        @Override
        public void prepare(final Path file) {
            Local renamed = file.getLocal();
            while(renamed.exists()) {
                String proposal = MessageFormat.format(Preferences.instance().getProperty("queue.upload.file.rename.format"),
                        FilenameUtils.getBaseName(file.getName()),
                        DateFormatterFactory.instance().getLongFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                        StringUtils.isNotEmpty(file.getExtension()) ? "." + file.getExtension() : "");
                renamed = LocalFactory.createLocal(renamed.getParent().getAbsolute(), proposal);
            }
            if(!renamed.equals(file.getLocal())) {
                file.getLocal().rename(renamed);
            }
            if(file.attributes().isFile()) {
                file.status().setResume(false);
            }
            super.prepare(file);
        }
    };

    private final DownloadTransferFilter SKIP_FILTER = new DownloadTransferFilter() {
        @Override
        public boolean accept(final Path file) {
            if(file.getLocal().exists()) {
                // Set completion status for skipped files
                file.status().setComplete(true);
                return false;
            }
            return super.accept(file);
        }
    };

    @Override
    public TransferFilter filter(final TransferAction action) {
        log.debug("filter:" + action);
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return OVERWRITE_FILTER;
        }
        if(action.equals(TransferAction.ACTION_RESUME)) {
            return RESUME_FILTER;
        }
        if(action.equals(TransferAction.ACTION_RENAME)) {
            return RENAME_FILTER;
        }
        if(action.equals(TransferAction.ACTION_RENAME_EXISTING)) {
            return RENAME_EXISTING_FILTER;
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return SKIP_FILTER;
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
        log.debug("action:" + resumeRequested + "," + reloadRequested);
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

    private boolean isSymlinkSupported(Path file) {
        if(Preferences.instance().getBoolean("path.symboliclink.resolve")) {
            return false;
        }
        // Create symbolic link only if choosen in the preferences. Otherwise download target file
        final AbstractPath target = file.getSymlinkTarget();
        // Only create symbolic link if target is included in the download
        for(Path root : roots) {
            if(target.isChild(root)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void transfer(final Path file) {
        log.debug("transfer:" + file);
        final Local local = file.getLocal();
        if(file.attributes().isSymbolicLink() && this.isSymlinkSupported(file)) {
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
            });
        }
        else if(file.attributes().isDirectory()) {
            local.mkdir(true);
        }
        if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
            Permission permission = Permission.EMPTY;
            if(Preferences.instance().getBoolean("queue.download.permissions.useDefault")) {
                if(file.attributes().isFile()) {
                    permission = new Permission(
                            Preferences.instance().getInteger("queue.download.permissions.file.default"));
                }
                if(file.attributes().isDirectory()) {
                    permission = new Permission(
                            Preferences.instance().getInteger("queue.download.permissions.folder.default"));
                }
            }
            else {
                permission = file.attributes().getPermission();
            }
            if(!Permission.EMPTY.equals(permission)) {
                if(file.attributes().isDirectory()) {
                    // Make sure we can read & write files to directory created.
                    permission.getOwnerPermissions()[Permission.READ] = true;
                    permission.getOwnerPermissions()[Permission.WRITE] = true;
                    permission.getOwnerPermissions()[Permission.EXECUTE] = true;
                }
                if(file.attributes().isFile()) {
                    // Make sure the owner can always read and write.
                    permission.getOwnerPermissions()[Permission.READ] = true;
                    permission.getOwnerPermissions()[Permission.WRITE] = true;
                }
                if(log.isInfoEnabled()) {
                    log.info("Updating permissions:" + local + "," + permission);
                }
                local.writeUnixPermission(permission, false);
            }
        }
        if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
            if(file.attributes().getModificationDate() != -1) {
                long timestamp = file.attributes().getModificationDate();
                if(log.isInfoEnabled()) {
                    log.info("Updating timestamp:" + local + "," + timestamp);
                }
                local.writeTimestamp(-1, timestamp, -1);
            }
        }
    }

    @Override
    protected void fireTransferDidEnd() {
        if(this.isReset() && this.isComplete() && !this.isCanceled() && !(this.getTransferred() == 0)) {
            Growl.instance().notify("Download complete", getName());
            if(this.shouldOpenWhenComplete()) {
                this.getRoot().getLocal().open();
            }
            this.getRoot().getLocal().bounce();
        }
        super.fireTransferDidEnd();
    }

    /**
     * @return
     */
    protected boolean shouldOpenWhenComplete() {
        return Preferences.instance().getBoolean("queue.postProcessItemWhenComplete");
    }

    @Override
    public boolean isResumable() {
        return getSession().isDownloadResumable();
    }
}