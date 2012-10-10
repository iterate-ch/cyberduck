package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.filter.UploadRegexFilter;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.upload.CompareFilter;
import ch.cyberduck.core.transfer.upload.MoveRemoteFilter;
import ch.cyberduck.core.transfer.upload.OverwriteFilter;
import ch.cyberduck.core.transfer.upload.RenameFilter;
import ch.cyberduck.core.transfer.upload.ResumeFilter;
import ch.cyberduck.core.transfer.upload.SkipFilter;
import ch.cyberduck.core.transfer.upload.UploadSymlinkResolver;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @version $Id$
 */
public class UploadTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(UploadTransfer.class);

    private UploadRegexFilter filter = new UploadRegexFilter();

    public UploadTransfer(Path root) {
        super(root);
    }

    public UploadTransfer(List<Path> roots) {
        super(roots);
    }

    public <T> UploadTransfer(T dict, Session s) {
        super(dict, s);
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = super.getSerializer();
        dict.setStringForKey(String.valueOf(KIND_UPLOAD), "Kind");
        return dict.<T>getSerialized();
    }

    @Override
    protected void init() {
        log.debug("init");
        this.bandwidth = new BandwidthThrottle(
                Preferences.instance().getFloat("queue.upload.bandwidth.bytes"));
    }

    @Override
    protected void normalize() {
        log.debug("normalize");
        final List<Path> normalized = new Collection<Path>();
        for(Path upload : this.getRoots()) {
            if(!this.check()) {
                return;
            }
            session.message(MessageFormat.format(Locale.localizedString("Prepare {0}", "Status"), upload.getName()));
            boolean duplicate = false;
            for(Iterator<Path> iter = normalized.iterator(); iter.hasNext(); ) {
                Path n = iter.next();
                if(upload.getLocal().isChild(n.getLocal())) {
                    // The selected file is a child of a directory already included
                    duplicate = true;
                    break;
                }
                if(n.getLocal().isChild(upload.getLocal())) {
                    iter.remove();
                }
                if(upload.equals(n)) {
                    // The selected file has the same name; if uploaded as a root element
                    // it would overwrite the earlier
                    final String parent = upload.getParent().getAbsolute();
                    final String filename = upload.getName();
                    String proposal;
                    int no = 0;
                    int index = filename.lastIndexOf('.');
                    do {
                        no++;
                        if(index != -1 && index != 0) {
                            proposal = filename.substring(0, index)
                                    + "-" + no + filename.substring(index);
                        }
                        else {
                            proposal = filename + "-" + no;
                        }
                        upload.setPath(parent, proposal);
                    }
                    while(false);//(upload.exists());
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Changed name from %s to %s", filename, upload.getName()));
                    }
                }
            }
            // Prunes the list of selected files. Files which are a child of an already included directory
            // are removed from the returned list.
            if(!duplicate) {
                normalized.add(upload);
            }
        }
        this.setRoots(normalized);
    }

    /**
     * File listing cache for children of the root paths not part of the session cache because
     * they only exist on the local file system.
     */
    private final Cache cache = new Cache() {
        @Override
        public void clear() {
            super.clear();
            session.cache().clear();
        }
    };

    @Override
    public Cache cache() {
        return cache;
    }

    @Override
    public AttributedList<Path> children(final Path parent) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Children for %s", parent));
        }
        if(parent.getLocal().attributes().isSymbolicLink() && new UploadSymlinkResolver(roots).resolve(parent)) {
            if(log.isDebugEnabled()) {
                log.debug("Do not list children for symbolic link:" + parent);
            }
            this.cache().put(parent.getReference(), AttributedList.<Path>emptyList());
        }
        else if(!this.cache().containsKey(parent.getReference())) {
            if(!parent.getLocal().exists()) {
                // Cannot fetch file listing of non existant file
                this.cache().put(parent.getReference(), AttributedList.<Path>emptyList());
            }
            else {
                final AttributedList<Path> children = new AttributedList<Path>();
                for(AbstractPath child : parent.getLocal().children(filter)) {
                    final Local local = LocalFactory.createLocal(child.getAbsolute());
                    Path upload = PathFactory.createPath(session, parent.getAbsolute(), local);
                    if(upload.exists()) {
                        upload = session.cache().lookup(upload.getReference());
                        upload.setLocal(local);
                    }
                    children.add(upload);
                }
                this.cache().put(parent.getReference(), children);
            }
        }
        return this.cache().get(parent.getReference());
    }

    @Override
    public boolean isResumable() {
        return session.isUploadResumable();
    }

    @Override
    public boolean isReloadable() {
        return true;
    }

    @Override
    public TransferPathFilter filter(final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        final UploadSymlinkResolver resolver = new UploadSymlinkResolver(roots);
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
            return new MoveRemoteFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return new SkipFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_COMPARISON)) {
            return new CompareFilter(resolver);
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            for(Path upload : this.getRoots()) {
                if(!this.check()) {
                    return null;
                }
                if(upload.exists()) {
                    if(upload.getLocal().attributes().isDirectory()) {
                        if(0 == this.children(upload).size()) {
                            // Do not prompt for existing empty directories
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
        if(log.isDebugEnabled()) {
            log.debug(String.format("Resume=%s,Reload=%s", resumeRequested, reloadRequested));
        }
        if(resumeRequested) {
            // Force resume
            return TransferAction.ACTION_RESUME;
        }
        if(reloadRequested) {
            return TransferAction.forName(
                    Preferences.instance().getProperty("queue.upload.reload.fileExists")
            );
        }
        // Use default
        return TransferAction.forName(
                Preferences.instance().getProperty("queue.upload.fileExists")
        );
    }


    @Override
    protected void transfer(final Path file, final TransferOptions options) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Upload file %s", file.getName()));
        }
        if(session.isUnixPermissionsSupported()) {
            if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                if(file.exists()) {
                    // Do not overwrite permissions for existing file.
                    if(file.attributes().getPermission().equals(Permission.EMPTY)) {
                        file.readUnixPermission();
                    }
                }
                else {
                    if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                        if(file.attributes().isFile()) {
                            file.attributes().setPermission(new Permission(
                                    Preferences.instance().getInteger("queue.upload.permissions.file.default")));
                        }
                        else if(file.attributes().isDirectory()) {
                            file.attributes().setPermission(new Permission(
                                    Preferences.instance().getInteger("queue.upload.permissions.folder.default")));
                        }
                    }
                    else {
                        if(file.getLocal().exists()) {
                            // Read permissions from local file
                            file.attributes().setPermission(file.getLocal().attributes().getPermission());
                        }
                    }
                }
            }
        }
        if(file.getLocal().attributes().isSymbolicLink() && new UploadSymlinkResolver(roots).resolve(file)) {
            // Make relative symbolic link
            final String target = StringUtils.substringAfter(file.getLocal().getSymlinkTarget().getAbsolute(),
                    file.getLocal().getParent().getAbsolute() + Path.DELIMITER);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Create symbolic link from %s to %s", file, target));
            }
            file.symlink(target);
            file.status().setComplete(true);
        }
        else if(file.attributes().isFile()) {
            String original = file.getName();
            if(Preferences.instance().getBoolean("queue.upload.file.temporary")
                    && file.getSession().isRenameSupported(file)) {
                String temporary = MessageFormat.format(Preferences.instance().getProperty("queue.upload.file.temporary.format"),
                        file.getName(), UUID.randomUUID().toString());
                file.setPath(file.getParent(), temporary);
            }
            // Transfer
            file.upload(bandwidth, new AbstractStreamListener() {
                @Override
                public void bytesSent(long bytes) {
                    transferred += bytes;
                }
            });
            if(file.status().isComplete()) {
                if(Preferences.instance().getBoolean("queue.upload.file.temporary")
                        && file.getSession().isRenameSupported(file)) {
                    file.rename(PathFactory.createPath(file.getSession(), file.getParent().getAbsolute(),
                            original, file.attributes().getType()));
                    file.setPath(file.getParent(), original);
                }
            }
        }
        else if(file.attributes().isDirectory()) {
            if(file.getSession().isCreateFolderSupported(file)) {
                file.mkdir();
            }
        }
    }

    @Override
    public String getStatus() {
        return this.isComplete() ? "Upload complete" : "Transfer incomplete";
    }

    @Override
    public String getImage() {
        return "transfer-upload.tiff";
    }
}
