package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.IOResumeException;
import ch.cyberduck.ui.DateFormatterFactory;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.sftp.*;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id$
 */
public class SFTPPath extends Path {
    private static Logger log = Logger.getLogger(SFTPPath.class);

    private static class Factory extends PathFactory<SFTPSession> {
        @Override
        protected Path create(SFTPSession session, String path, int type) {
            return new SFTPPath(session, path, type);
        }

        @Override
        protected Path create(SFTPSession session, String parent, String name, int type) {
            return new SFTPPath(session, parent, name, type);
        }

        @Override
        protected Path create(SFTPSession session, String parent, Local file) {
            return new SFTPPath(session, parent, file);
        }

        @Override
        protected <T> Path create(SFTPSession session, T dict) {
            return new SFTPPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
    }

    private final SFTPSession session;

    private SFTPPath(SFTPSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    private SFTPPath(SFTPSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    private SFTPPath(SFTPSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    private <T> SFTPPath(SFTPSession s, T dict) {
        super(dict);
        this.session = s;
    }

    @Override
    public SFTPSession getSession() {
        return session;
    }

    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> children = new AttributedList<Path>();
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            for(SFTPv3DirectoryEntry f : (List<SFTPv3DirectoryEntry>) this.getSession().sftp().ls(this.getAbsolute())) {
                if(f.filename.equals(".") || f.filename.equals("..")) {
                    continue;
                }
                SFTPv3FileAttributes attributes = f.attributes;
                SFTPPath p = new SFTPPath(this.getSession(), this.getAbsolute(),
                        f.filename, attributes.isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                p.setParent(this);
                p.readAttributes(attributes);
                children.add(p);
            }
            this.getSession().setWorkdir(this);
        }
        catch(IOException e) {
            children.attributes().setReadable(false);
        }
        return children;
    }

    @Override
    public void mkdir() {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                        this.getName()));

                this.getSession().sftp().mkdir(this.getAbsolute(),
                        new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default")).getOctalNumber());

                this.cache().put(this.getReference(), AttributedList.<Path>emptyList());
                // The directory listing is no more current
                this.getParent().invalidate();
            }
            catch(IOException e) {
                this.error("Cannot create folder", e);
            }
        }
    }

    @Override
    public void rename(AbstractPath renamed) {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed));

            if(renamed.exists()) {
                renamed.delete();
            }
            this.getSession().sftp().mv(this.getAbsolute(), renamed.getAbsolute());
            // The directory listing is no more current
            renamed.getParent().invalidate();
            this.getParent().invalidate();
        }
        catch(IOException e) {
            if(this.attributes().isFile()) {
                this.error("Cannot rename file", e);
            }
            if(this.attributes().isDirectory()) {
                this.error("Cannot rename folder", e);
            }
        }
    }

    @Override
    public void delete() {
        try {
            this.getSession().check();
            if(this.attributes().isFile() || this.attributes().isSymbolicLink()) {
                this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                this.getSession().sftp().rm(this.getAbsolute());
            }
            else if(this.attributes().isDirectory()) {
                for(AbstractPath child : this.children()) {
                    if(!this.getSession().isConnected()) {
                        break;
                    }
                    child.delete();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                this.getSession().sftp().rmdir(this.getAbsolute());
            }
            // The directory listing is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            if(this.attributes().isFile()) {
                this.error("Cannot delete file", e);
            }
            if(this.attributes().isDirectory()) {
                this.error("Cannot delete folder", e);
            }
        }
    }

    protected void readAttributes() {
        try {
            this.readAttributes(this.getSession().sftp().stat(this.getAbsolute()));
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    protected void readAttributes(SFTPv3FileAttributes attributes) {
        if(null != attributes.size) {
            this.attributes().setSize(attributes.size);
        }
        String perm = attributes.getOctalPermissions();
        if(null != perm) {
            try {
                this.attributes().setPermission(new Permission(Integer.parseInt(perm.substring(perm.length() - 3))));
            }
            catch(NumberFormatException e) {
                log.error(e.getMessage());
            }
        }
        if(null != attributes.uid) {
            this.attributes().setOwner(attributes.uid.toString());
        }
        if(null != attributes.gid) {
            this.attributes().setGroup(attributes.gid.toString());
        }
        if(null != attributes.mtime) {
            this.attributes().setModificationDate(Long.parseLong(attributes.mtime.toString()) * 1000L);
        }
        if(null != attributes.atime) {
            this.attributes().setAccessedDate(Long.parseLong(attributes.atime.toString()) * 1000L);
        }
        if(attributes.isSymlink()) {
            try {
                String target = this.getSession().sftp().readLink(this.getAbsolute());
                if(!target.startsWith(String.valueOf(Path.DELIMITER))) {
                    target = Path.normalize(this.getParent().getAbsolute() + String.valueOf(Path.DELIMITER) + target);
                }
                this.setSymlinkTarget(target);
                SFTPv3FileAttributes targetAttributes = this.getSession().sftp().stat(target);
                if(targetAttributes.isDirectory()) {
                    this.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                }
                else if(targetAttributes.isRegularFile()) {
                    this.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                }
            }
            catch(IOException e) {
                log.error("Cannot read symbolic link target of " + this.getAbsolute() + ":" + e.getMessage());
                this.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
            }
        }
    }

    protected void writeAttributes(SFTPv3FileAttributes attributes) throws IOException {
        this.getSession().sftp().setstat(this.getAbsolute(), attributes);
    }

    @Override
    public void readSize() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                    this.getName()));

            this.readAttributes();
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void readTimestamp() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                    this.getName()));

            this.readAttributes();
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void readUnixPermission() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting permission of {0}", "Status"),
                    this.getName()));

            this.readAttributes();
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void writeOwner(String owner, boolean recursive) {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Changing owner of {0} to {1}", "Status"),
                    this.getName(), owner));

            SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
            attr.uid = new Integer(owner);
            this.writeAttributes(attr);
            if(this.attributes().isDirectory()) {
                if(recursive) {
                    for(AbstractPath child : this.children()) {
                        if(!this.getSession().isConnected()) {
                            break;
                        }
                        ((Path) child).writeOwner(owner, recursive);
                    }
                }
            }
        }
        catch(IOException e) {
            this.error("Cannot change owner", e);
        }
    }

    @Override
    public void writeGroup(String group, boolean recursive) {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Changing group of {0} to {1}", "Status"),
                    this.getName(), group));

            SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
            attr.gid = new Integer(group);
            this.writeAttributes(attr);
            if(this.attributes().isDirectory()) {
                if(recursive) {
                    for(AbstractPath child : this.children()) {
                        if(!this.getSession().isConnected()) {
                            break;
                        }
                        ((Path) child).writeGroup(group, recursive);
                    }
                }
            }
        }
        catch(IOException e) {
            this.error("Cannot change group", e);
        }
    }

    @Override
    public void writeUnixPermission(Permission perm, boolean recursive) {
        try {
            this.getSession().check();
            this.writeUnixPermissionImpl(perm, recursive);
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
    }

    private void writeUnixPermissionImpl(Permission perm, boolean recursive) throws IOException {
        this.getSession().message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                this.getName(), perm.getOctalString()));

        try {
            SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
            attr.permissions = perm.getOctalNumber();
            this.writeAttributes(attr);
            if(this.attributes().isDirectory()) {
                if(recursive) {
                    for(AbstractPath child : this.children()) {
                        if(!this.getSession().isConnected()) {
                            break;
                        }
                        ((SFTPPath) child).writeUnixPermissionImpl(perm, recursive);
                    }
                }
            }
        }
        finally {
            this.attributes().clear(false, false, true, false);
        }
    }

    @Override
    public void writeTimestamp(long millis) {
        try {
            this.writeModificationDateImpl(millis, millis);
        }
        catch(IOException e) {
            this.error("Cannot change timestamp", e);
        }
    }

    private void writeModificationDateImpl(long modified, long created) throws IOException {
        this.getSession().message(MessageFormat.format(Locale.localizedString("Changing timestamp of {0} to {1}", "Status"),
                this.getName(), DateFormatterFactory.instance().getShortFormat(modified)));
        try {
            SFTPv3FileAttributes attrs = new SFTPv3FileAttributes();
            int t = (int) (modified / 1000);
            // We must both set the accessed and modified time. See AttribFlags.SSH_FILEXFER_ATTR_V3_ACMODTIME
            attrs.atime = t;
            attrs.mtime = t;
            this.writeAttributes(attrs);
        }
        finally {
            this.attributes().clear(true, false, false, false);
        }
    }

    @Override
    protected void download(BandwidthThrottle throttle, StreamListener listener, final boolean check) {
        InputStream in = null;
        OutputStream out = null;
        try {
            if(check) {
                this.getSession().check();
            }
            if(attributes().isFile()) {
                if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                    SFTPv3FileHandle handle = this.getSession().sftp().openFileRO(this.getAbsolute());
                    in = new SFTPInputStream(handle);
                    if(status().isResume()) {
                        log.info("Skipping " + status().getCurrent() + " bytes");
                        final long skipped = in.skip(status().getCurrent());
                        if(skipped < status().getCurrent()) {
                            throw new IOResumeException("Skipped " + skipped + " bytes instead of " + this.status().getCurrent());
                        }
                    }
                }
                if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SCP.getIdentifier())) {
                    SCPClient scp = this.getSession().openScp();
                    scp.setCharset(this.getSession().getEncoding());
                    in = scp.get(this.getAbsolute());
                }
                out = this.getLocal().getOutputStream(this.status().isResume());
                this.download(in, out, throttle, listener);
            }
        }
        catch(IOException e) {
            this.error("Download failed", e);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    protected void upload(BandwidthThrottle throttle, StreamListener listener, final boolean check) {
        InputStream in = null;
        OutputStream out = null;
        SFTPv3FileHandle handle = null;
        try {
            if(attributes().isFile()) {
                if(check) {
                    this.getSession().check();
                }
                in = this.getLocal().getInputStream();
                if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                    try {
                        SFTPv3FileAttributes attrs = new SFTPv3FileAttributes();
                        if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                            int t = (int) (this.attributes().getModificationDate() / 1000);
                            // We must both set the accessed and modified time. See AttribFlags.SSH_FILEXFER_ATTR_V3_ACMODTIME
                            attrs.atime = t;
                            attrs.mtime = t;
                        }
                        if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                            // We do set the permissions here as otherwise we might have an empty mask for
                            // interrupted file transfers
                            attrs.permissions = this.attributes().getPermission().getOctalNumber();
                        }
                        if(status().isResume() && this.exists()) {
                            handle = this.getSession().sftp().openFile(this.getAbsolute(),
                                    SFTPv3Client.SSH_FXF_WRITE | SFTPv3Client.SSH_FXF_APPEND, attrs);
                        }
                        else {
                            handle = this.getSession().sftp().openFile(this.getAbsolute(),
                                    SFTPv3Client.SSH_FXF_CREAT | SFTPv3Client.SSH_FXF_TRUNC | SFTPv3Client.SSH_FXF_WRITE, attrs);
                        }
                    }
                    catch(SFTPException ignore) {
                        // We might not be able to change the attributes if we are
                        // not the owner of the file; but then we still want to proceed as we
                        // might have group write privileges
                        log.warn(ignore.getMessage());

                        if(status().isResume() && this.exists()) {
                            handle = this.getSession().sftp().openFile(this.getAbsolute(),
                                    SFTPv3Client.SSH_FXF_WRITE | SFTPv3Client.SSH_FXF_APPEND, null);
                        }
                        else {
                            handle = this.getSession().sftp().openFile(this.getAbsolute(),
                                    SFTPv3Client.SSH_FXF_CREAT | SFTPv3Client.SSH_FXF_TRUNC | SFTPv3Client.SSH_FXF_WRITE, null);
                        }
                    }
                    out = new SFTPOutputStream(handle);
                    if(status().isResume()) {
                        long skipped = ((SFTPOutputStream) out).skip(status().getCurrent());
                        log.info("Skipping " + skipped + " bytes");
                        if(skipped < this.status().getCurrent()) {
                            throw new IOResumeException("Skipped " + skipped + " bytes instead of " + this.status().getCurrent());
                        }
                    }
                }
                else if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SCP.getIdentifier())) {
                    SCPClient scp = this.getSession().openScp();
                    scp.setCharset(this.getSession().getEncoding());
                    out = scp.put(this.getName(), this.getLocal().attributes().getSize(),
                            this.getParent().getAbsolute(),
                            "0" + this.attributes().getPermission().getOctalString());
                }

                this.upload(out, in, throttle, listener);

                if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                    try {
                        this.writeModificationDateImpl(this.attributes().getModificationDate(),
                                this.attributes().getCreationDate());
                    }
                    catch(SFTPException ignore) {
                        // We might not be able to change the attributes if we are not the owner of the file
                        log.warn(ignore.getMessage());
                    }
                }
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    try {
                        // Even if specified above when creating the file handle, we still need to update the
                        // permissions after the upload. SSH_FXP_OPEN does not support setting
                        // attributes in version 4 or lower.
                        this.writeUnixPermissionImpl(this.attributes().getPermission(), false);
                    }
                    catch(SFTPException ignore) {
                        log.warn(ignore.getMessage());
                    }
                }
            }
        }
        catch(IOException e) {
            this.error("Upload failed", e);
        }
        finally {
            try {
                if(handle != null) {
                    this.getSession().sftp().closeFile(handle);
                }
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void touch() {
        if(this.attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        this.getName()));

                SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
                Permission permission = new Permission(Preferences.instance().getInteger("queue.upload.permissions.file.default"));
                attr.permissions = permission.getOctalNumber();
                this.getSession().sftp().createFile(this.getAbsolute(), attr);
                try {
                    // Even if specified above when creating the file handle, we still need to update the
                    // permissions after the creating the file. SSH_FXP_OPEN does not support setting
                    // attributes in version 4 or lower.
                    this.writeUnixPermissionImpl(permission, false);
                }
                catch(SFTPException ignore) {
                    log.warn(ignore.getMessage());
                }
                // The directory listing is no more current
                this.getParent().invalidate();
            }
            catch(IOException e) {
                this.error("Cannot create file", e);
            }
        }
    }
}