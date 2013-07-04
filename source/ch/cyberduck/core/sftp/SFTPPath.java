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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.SFTPExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.IOResumeException;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPException;
import ch.ethz.ssh2.SFTPInputStream;
import ch.ethz.ssh2.SFTPOutputStream;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import ch.ethz.ssh2.SFTPv3FileAttributes;
import ch.ethz.ssh2.SFTPv3FileHandle;

/**
 * @version $Id$
 */
public class SFTPPath extends Path {
    private static final Logger log = Logger.getLogger(SFTPPath.class);

    private final SFTPSession session;

    public SFTPPath(final SFTPSession s, final Path parent, final String name, final int type) {
        super(parent, name, type);
        this.session = s;
    }

    public SFTPPath(final SFTPSession s, final String path, final int type) {
        super(s, path, type);
        this.session = s;
    }

    public SFTPPath(final SFTPSession s, final Path parent, final Local file) {
        super(parent, file);
        this.session = s;
    }

    public <T> SFTPPath(final SFTPSession s, final T dict) {
        super(s, dict);
        this.session = s;
    }

    @Override
    public SFTPSession getSession() {
        return session;
    }

    @Override
    public boolean exists() throws BackgroundException {
        try {
            return session.sftp().canonicalPath(this.getAbsolute()) != null;
        }
        catch(SFTPException e) {
            return false;
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            final AttributedList<Path> children = new AttributedList<Path>();

            for(SFTPv3DirectoryEntry f : session.sftp().ls(this.getAbsolute())) {
                if(f.filename.equals(".") || f.filename.equals("..")) {
                    continue;
                }
                SFTPv3FileAttributes attributes = f.attributes;
                final SFTPPath p = new SFTPPath(session, this,
                        f.filename, attributes.isDirectory() ? DIRECTORY_TYPE : FILE_TYPE);
                if(null != attributes.size) {
                    if(p.attributes().isFile()) {
                        p.attributes().setSize(attributes.size);
                    }
                }
                String perm = attributes.getOctalPermissions();
                if(null != perm) {
                    try {
                        String octal = Integer.toOctalString(attributes.permissions);
                        p.attributes().setPermission(new Permission(Integer.parseInt(octal.substring(octal.length() - 4))));
                    }
                    catch(IndexOutOfBoundsException e) {
                        log.warn(String.format("Failure parsing mode:%s", e.getMessage()));
                    }
                    catch(NumberFormatException e) {
                        log.warn(String.format("Failure parsing mode:%s", e.getMessage()));
                    }
                }
                if(null != attributes.uid) {
                    p.attributes().setOwner(attributes.uid.toString());
                }
                if(null != attributes.gid) {
                    p.attributes().setGroup(attributes.gid.toString());
                }
                if(null != attributes.mtime) {
                    p.attributes().setModificationDate(Long.parseLong(attributes.mtime.toString()) * 1000L);
                }
                if(null != attributes.atime) {
                    p.attributes().setAccessedDate(Long.parseLong(attributes.atime.toString()) * 1000L);
                }
                if(attributes.isSymlink()) {
                    final String target = session.sftp().readLink(p.getAbsolute());
                    final int type;
                    final SFTPv3FileAttributes targetAttributes = session.sftp().stat(target);
                    if(targetAttributes.isDirectory()) {
                        type = SYMBOLIC_LINK_TYPE | DIRECTORY_TYPE;
                    }
                    else {
                        type = SYMBOLIC_LINK_TYPE | FILE_TYPE;
                    }
                    p.attributes().setType(type);
                    if(target.startsWith(String.valueOf(Path.DELIMITER))) {
                        p.setSymlinkTarget(new SFTPPath(session, target, p.attributes().isFile() ? FILE_TYPE : DIRECTORY_TYPE));
                    }
                    else {
                        p.setSymlinkTarget(new SFTPPath(session, p.getParent(), target, p.attributes().isFile() ? FILE_TYPE : DIRECTORY_TYPE));
                    }
                }
                children.add(p);
            }
            return children;
        }
        catch(IOException e) {
            log.warn(String.format("Directory listing failure for %s with failure %s", this, e.getMessage()));
            throw new SFTPExceptionMappingService().map("Listing directory failed", e, this);
        }
    }

    @Override
    public void mkdir() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                    this.getName()));

            session.sftp().mkdir(this.getAbsolute(),
                    Integer.parseInt(new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default")).getOctalString(), 8));
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot create folder {0}", e, this);
        }
    }

    @Override
    public void rename(final Path renamed) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed));

            if(renamed.exists()) {
                renamed.delete(new DisabledLoginController());
            }
            session.sftp().mv(this.getAbsolute(), renamed.getAbsolute());
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot rename {0}", e, this);
        }
    }

    @Override
    public void delete(final LoginController prompt) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    this.getName()));

            if(this.attributes().isFile() || this.attributes().isSymbolicLink()) {
                session.sftp().rm(this.getAbsolute());
            }
            else if(this.attributes().isDirectory()) {
                for(Path child : this.list()) {
                    if(!session.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    child.delete(prompt);
                }
                session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                session.sftp().rmdir(this.getAbsolute());
            }
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot delete {0}", e, this);
        }
    }

    protected void writeAttributes(SFTPv3FileAttributes attributes) throws BackgroundException {
        try {
            session.sftp().setstat(this.getAbsolute(), attributes);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot write file attributes", e, this);
        }
    }

    @Override
    public void writeUnixOwner(String owner) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing owner of {0} to {1}", "Status"),
                this.getName(), owner));

        SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
        attr.uid = new Integer(owner);
        this.writeAttributes(attr);
    }

    @Override
    public void writeUnixGroup(final String group) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing group of {0} to {1}", "Status"),
                this.getName(), group));

        SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
        attr.gid = new Integer(group);
        this.writeAttributes(attr);
    }

    @Override
    public void writeUnixPermission(final Permission permission) throws BackgroundException {
        try {
            this.writeUnixPermissionImpl(permission);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot change permissions", e, this);
        }
    }

    private void writeUnixPermissionImpl(final Permission permission) throws IOException {
        session.message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                this.getName(), permission.getOctalString()));

        try {
            SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
            attr.permissions = Integer.parseInt(permission.getOctalString(), 8);
            this.writeAttributes(attr);
        }
        catch(BackgroundException ignore) {
            // We might not be able to change the attributes if we are not the owner of the file
            log.warn(ignore.getMessage());
        }
        finally {
            this.attributes().clear(false, false, true, false);
        }
    }

    @Override
    public void writeTimestamp(long created, long modified, long accessed) throws BackgroundException {
        try {
            this.writeModificationDateImpl(modified);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot change timestamp", e, this);
        }
    }

    private void writeModificationDateImpl(long modified) throws IOException {
        session.message(MessageFormat.format(Locale.localizedString("Changing timestamp of {0} to {1}", "Status"),
                this.getName(), UserDateFormatterFactory.get().getShortFormat(modified)));
        try {
            SFTPv3FileAttributes attrs = new SFTPv3FileAttributes();
            int t = (int) (modified / 1000);
            // We must both set the accessed and modified time. See AttribFlags.SSH_FILEXFER_ATTR_V3_ACMODTIME
            attrs.atime = t;
            attrs.mtime = t;
            this.writeAttributes(attrs);
        }
        catch(BackgroundException ignore) {
            // We might not be able to change the attributes if we are not the owner of the file
            log.warn(ignore.getMessage());
        }
        finally {
            this.attributes().clear(true, false, false, false);
        }
    }

    @Override
    public InputStream read(final TransferStatus status) throws BackgroundException {
        InputStream in = null;
        try {
            if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                final SFTPv3FileHandle handle = session.sftp().openFileRO(this.getAbsolute());
                in = new SFTPInputStream(handle);
                if(status.isResume()) {
                    log.info(String.format("Skipping %d bytes", status.getCurrent()));
                    final long skipped = in.skip(status.getCurrent());
                    if(skipped < status.getCurrent()) {
                        throw new IOResumeException(String.format("Skipped %d bytes instead of %d", skipped, status.getCurrent()));
                    }
                }
                // No parallel requests if the file size is smaller than the buffer.
                session.sftp().setRequestParallelism(
                        (int) (status.getLength() / Preferences.instance().getInteger("connection.chunksize")) + 1
                );
            }
            else if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SCP.getIdentifier())) {
                final SCPClient client = new SCPClient(session.getClient());
                client.setCharset(session.getEncoding());
                in = client.get(this.getAbsolute());
            }
            return in;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Download failed", e, this);
        }
    }

    @Override
    public void download(BandwidthThrottle throttle, StreamListener listener,
                         final TransferStatus status) throws BackgroundException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = this.read(status);
            out = this.getLocal().getOutputStream(status.isResume());
            this.download(in, out, throttle, listener, status);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Download failed", e, this);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public void symlink(String target) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                    this.getName()));

            session.sftp().createSymlink(this.getAbsolute(), target);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot create file {0}", e, this);
        }
    }

    @Override
    public OutputStream write(final TransferStatus status) throws BackgroundException {
        try {
            final String mode = Preferences.instance().getProperty("ssh.transfer");
            if(mode.equals(Protocol.SFTP.getIdentifier())) {
                SFTPv3FileHandle handle;
                if(status.isResume()) {
                    handle = session.sftp().openFile(this.getAbsolute(),
                            SFTPv3Client.SSH_FXF_WRITE | SFTPv3Client.SSH_FXF_APPEND, null);
                }
                else {
                    handle = session.sftp().openFile(this.getAbsolute(),
                            SFTPv3Client.SSH_FXF_CREAT | SFTPv3Client.SSH_FXF_TRUNC | SFTPv3Client.SSH_FXF_WRITE, null);
                }
                final OutputStream out = new SFTPOutputStream(handle);
                if(status.isResume()) {
                    long skipped = ((SFTPOutputStream) out).skip(status.getCurrent());
                    log.info(String.format("Skipping %d bytes", skipped));
                    if(skipped < status.getCurrent()) {
                        throw new IOResumeException(String.format("Skipped %d bytes instead of %d", skipped, status.getCurrent()));
                    }
                }
                // No parallel requests if the file size is smaller than the buffer.
                session.sftp().setRequestParallelism(
                        (int) (status.getLength() / Preferences.instance().getInteger("connection.chunksize")) + 1
                );
                return out;
            }
            else if(mode.equals(Protocol.SCP.getIdentifier())) {
                final SCPClient client = new SCPClient(session.getClient());
                client.setCharset(session.getEncoding());
                return client.put(this.getName(), status.getLength(),
                        this.getParent().getAbsolute(),
                        "0" + this.attributes().getPermission().getOctalString());
            }
            throw new IOException("Unknown transfer mode:" + mode);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Upload failed", e, this);
        }
    }

    @Override
    public void upload(final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) throws BackgroundException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = this.getLocal().getInputStream();
            out = this.write(status);
            this.upload(out, in, throttle, listener, status);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Upload failed", e, this);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public boolean touch() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                    this.getName()));

            SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
            Permission permission = new Permission(Preferences.instance().getInteger("queue.upload.permissions.file.default"));
            attr.permissions = Integer.parseInt(permission.getOctalString(), 8);
            session.sftp().createFile(this.getAbsolute(), attr);
            try {
                // Even if specified above when creating the file handle, we still need to update the
                // permissions after the creating the file. SSH_FXP_OPEN does not support setting
                // attributes in version 4 or lower.
                this.writeUnixPermissionImpl(permission);
            }
            catch(SFTPException ignore) {
                log.warn(ignore.getMessage());
            }
            return true;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot create file {0}", e, this);
        }
    }
}
