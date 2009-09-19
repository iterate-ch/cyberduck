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
                if(download.getLocal().getName().equals(n.getLocal().getName())) {
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
        public void prepare(Path p) {
            if(p.attributes.getSize() == -1) {
                p.readSize();
            }
            if(p.attributes.getModificationDate() == -1) {
                if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
                    p.readTimestamp();
                }
            }
            if(p.attributes.getPermission() == null) {
                if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
                    p.readPermission();
                }
            }
            // Read file size
            if(p.attributes.isFile()) {
                if(p.attributes.isSymbolicLink()) {
                    if(null != p.getSymbolicLinkPath()) {
                        Path symlink = PathFactory.createPath(p.getSession(), p.getSymbolicLinkPath(),
                                Path.FILE_TYPE);
                        if(symlink.attributes.getSize() == -1) {
                            symlink.readSize();
                        }
                        size += symlink.attributes.getSize();
                    }
                }
                else {
                    size += p.attributes.getSize();
                }
                if(p.getStatus().isResume()) {
                    transferred += p.getLocal().attributes.getSize();
                }
            }
            if(!p.getLocal().getParent().exists()) {
                // Create download folder if missing
                p.getLocal().getParent().mkdir(true);
            }
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
        if(!parent.exists()) {
            // Cannot fetch file listing of non existant file
            return AttributedList.emptyList();
        }
        final AttributedList<Path> list = parent.childs(new NullComparator<Path>(), childFilter);
        for(Path download : list) {
            // Change download path relative to parent local folder
            download.setLocal(LocalFactory.createLocal(parent.getLocal(), download.getName()));
            download.getStatus().setSkipped(parent.getStatus().isSkipped());
        }
        return list;
    }

    private final TransferFilter ACTION_OVERWRITE = new DownloadTransferFilter() {
        public boolean accept(final Path p) {
            if(p.attributes.isDirectory()) {
                return !p.getLocal().exists();
            }
            return true;
        }

        @Override
        public void prepare(final Path p) {
            if(p.attributes.isFile()) {
                p.getStatus().setResume(false);
            }
            super.prepare(p);
        }
    };

    private final TransferFilter ACTION_RESUME = new DownloadTransferFilter() {
        public boolean accept(final Path p) {
            if(p.getStatus().isComplete() || p.getLocal().attributes.getSize() == p.attributes.getSize()) {
                // No need to resume completed transfers
                p.getStatus().setComplete(true);
                return false;
            }
            if(p.attributes.isDirectory()) {
                return !p.getLocal().exists();
            }
            return true;
        }

        @Override
        public void prepare(final Path p) {
            if(p.attributes.isFile()) {
                final boolean resume = p.getLocal().exists()
                        && p.getLocal().attributes.getSize() > 0;
                p.getStatus().setResume(resume);
                long skipped = p.getLocal().attributes.getSize();
                p.getStatus().setCurrent(skipped);
            }
            super.prepare(p);
        }
    };

    private final TransferFilter ACTION_RENAME = new DownloadTransferFilter() {
        public boolean accept(final Path p) {
            return true;
        }

        @Override
        public void prepare(final Path p) {
            if(p.attributes.isFile()) {
                p.getStatus().setResume(false);
            }
            if(p.getLocal().exists() && p.getLocal().attributes.getSize() > 0) {
                final String parent = p.getLocal().getParent().getAbsolute();
                final String filename = p.getName();
                int no = 0;
                while(p.getLocal().exists()) {
                    no++;
                    String proposal = FilenameUtils.getBaseName(filename) + "-" + no;
                    if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                        proposal += "." + FilenameUtils.getExtension(filename);
                    }
                    p.setLocal(LocalFactory.createLocal(parent, proposal));
                }
                log.info("Changed local name to:" + p.getName());
            }
            super.prepare(p);
        }
    };

    private final DownloadTransferFilter ACTION_SKIP = new DownloadTransferFilter() {
        public boolean accept(final Path p) {
            return !p.getLocal().exists();
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
                    if(root.getLocal().attributes.isDirectory()) {
                        if(0 == root.getLocal().childs().size()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    if(root.getLocal().attributes.isFile()) {
                        if(root.getLocal().attributes.getSize() == 0) {
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
    protected void _transferImpl(final Path p) {
        p.download(bandwidth, new AbstractStreamListener() {
            @Override
            public void bytesReceived(long bytes) {
                transferred += bytes;
            }
        });
        if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
            log.info("Updating permissions");
            Permission perm;
            if(Preferences.instance().getBoolean("queue.download.permissions.useDefault")
                    && p.attributes.isFile()) {
                perm = new Permission(
                        Preferences.instance().getInteger("queue.download.permissions.file.default")
                );
            }
            else {
                perm = p.attributes.getPermission();
            }
            if(null != perm) {
                if(p.attributes.isDirectory()) {
                    perm.getOwnerPermissions()[Permission.WRITE] = true;
                    perm.getOwnerPermissions()[Permission.EXECUTE] = true;
                }
                p.getLocal().writePermissions(perm, false);
            }
        }
        if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
            log.info("Updating timestamp");
            if(-1 == p.attributes.getModificationDate()) {
                p.readTimestamp();
            }
            if(p.attributes.getModificationDate() != -1) {
                long timestamp = p.attributes.getModificationDate();
                p.getLocal().writeModificationDate(timestamp/*, this.getHost().getTimezone()*/);
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
}