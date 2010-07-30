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

import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.ui.growl.Growl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public class DownloadTransfer extends Transfer {

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
    protected void setRoots(List<Path> downloads) {
        final List<Path> normalized = new Collection<Path>();
        for(Path download : downloads) {
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
                    log.info("Changed local name to:" + download.getName());
                }
            }
            // Prunes the list of selected files. Files which are a child of an already included directory
            // are removed from the returned list.
            if(!duplicate) {
                normalized.add(download);
            }
        }
        super.setRoots(normalized);
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
            // Read file size
            if(file.attributes().isFile()) {
                if(file.attributes().isSymbolicLink()) {
                    if(null != file.getSymlinkTarget()) {
                        // A server will resolve the symbolic link when the file is requested.
                        Path symlink = PathFactory.createPath(file.getSession(), file.getSymlinkTarget(),
                                Path.FILE_TYPE);
                        if(symlink.attributes().getSize() == -1) {
                            symlink.readSize();
                        }
                        size += symlink.attributes().getSize();
                    }
                }
                else {
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

    private final PathFilter<Path> childFilter = new PathFilter<Path>() {
        public boolean accept(Path child) {
            if(Preferences.instance().getBoolean("queue.download.skip.enable")) {
                try {
                    if(Pattern.compile(Preferences.instance().getProperty("queue.download.skip.regex")).matcher(child.getName()).matches()) {
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
    public AttributedList<Path> childs(final Path parent) {
        final AttributedList<Path> list = parent.childs(childFilter);
        for(Path download : list) {
            // Change download path relative to parent local folder
            download.setLocal(LocalFactory.createLocal(parent.getLocal(), download.getLocal().getName()));
        }
        return list;
    }

    private final TransferFilter ACTION_OVERWRITE = new DownloadTransferFilter() {
        public boolean accept(final Path p) {
            if(p.attributes().isDirectory()) {
                return !p.getLocal().exists();
            }
            return true;
        }

        @Override
        public void prepare(final Path file) {
            if(file.attributes().isFile()) {
                file.status().setResume(false);
            }
            super.prepare(file);
        }
    };

    private final TransferFilter ACTION_RESUME = new DownloadTransferFilter() {
        public boolean accept(final Path p) {
            if(p.status().isComplete() || p.getLocal().attributes().getSize() == p.attributes().getSize()) {
                // No need to resume completed transfers
                p.status().setComplete(true);
                return false;
            }
            if(p.attributes().isDirectory()) {
                return !p.getLocal().exists();
            }
            return true;
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

    private final TransferFilter ACTION_RENAME = new DownloadTransferFilter() {
        public boolean accept(final Path p) {
            return true;
        }

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
                log.info("Changed local name to:" + file.getLocal().getName());
            }
            super.prepare(file);
        }
    };

    private final DownloadTransferFilter ACTION_SKIP = new DownloadTransferFilter() {
        public boolean accept(final Path p) {
            if(p.getLocal().exists()) {
                return p.getLocal().attributes().getSize() == 0;
            }
            return true;
        }
    };

    @Override
    public TransferFilter filter(final TransferAction action) {
        log.debug("filter:" + action);
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return ACTION_OVERWRITE;
        }
        if(action.equals(TransferAction.ACTION_RESUME)) {
            return ACTION_RESUME;
        }
        if(action.equals(TransferAction.ACTION_RENAME)) {
            return ACTION_RENAME;
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return ACTION_SKIP;
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            for(Path root : this.getRoots()) {
                if(root.getLocal().exists()) {
                    if(root.getLocal().attributes().isDirectory()) {
                        if(0 == root.getLocal().childs().size()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    if(root.getLocal().attributes().isFile()) {
                        if(root.getLocal().attributes().getSize() == 0) {
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

    @Override
    protected void transfer(final Path p) {
        final Local local = p.getLocal();
        if(p.attributes().isFile()) {
            p.download(bandwidth, new AbstractStreamListener() {
                @Override
                public void bytesReceived(long bytes) {
                    transferred += bytes;
                }
            });
        }
        else if(p.attributes().isDirectory()) {
            local.mkdir(true);
        }
        if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
            Permission permission = Permission.EMPTY;
            if(Preferences.instance().getBoolean("queue.download.permissions.useDefault")) {
                if(p.attributes().isFile()) {
                    permission = new Permission(
                            Preferences.instance().getInteger("queue.download.permissions.file.default"));
                }
                if(p.attributes().isDirectory()) {
                    permission = new Permission(
                            Preferences.instance().getInteger("queue.download.permissions.folder.default"));
                }
            }
            else {
                permission = p.attributes().getPermission();
            }
            if(p.attributes().isDirectory()) {
                // Make sure we can write files to directory created.
                permission.getOwnerPermissions()[Permission.WRITE] = true;
                permission.getOwnerPermissions()[Permission.EXECUTE] = true;
            }
            if(p.attributes().isFile()) {
                // Make sure the owner can always read and write.
                permission.getOwnerPermissions()[Permission.READ] = true;
                permission.getOwnerPermissions()[Permission.WRITE] = true;
            }
            log.info("Updating permissions:" + local + "," + permission);
            local.writeUnixPermission(permission, false);
        }
        if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
            if(p.attributes().getModificationDate() != -1) {
                long timestamp = p.attributes().getModificationDate();
                log.info("Updating timestamp:" + local + "," + timestamp);
                local.writeTimestamp(timestamp/*, this.getHost().getTimezone()*/);
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