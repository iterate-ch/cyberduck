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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;

import ch.cyberduck.core.*;
import ch.cyberduck.core.io.BandwidthThrottle;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.io.SFTPInputStream;
import ch.ethz.ssh2.io.SFTPOutputStream;
import ch.ethz.ssh2.sftp.SFTPException;
import ch.ethz.ssh2.sftp.SFTPv3DirectoryEntry;
import ch.ethz.ssh2.sftp.SFTPv3FileAttributes;
import ch.ethz.ssh2.sftp.SFTPv3FileHandle;

/**
 * @version $Id$
 */
public class SFTPPath extends Path {
    private static Logger log = Logger.getLogger(SFTPPath.class);

    static {
        PathFactory.addFactory(Protocol.SFTP, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String path, int type) {
            return new SFTPPath((SFTPSession) session, path, type);
        }

        protected Path create(Session session, String parent, String name, int type) {
            return new SFTPPath((SFTPSession) session, parent, name, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new SFTPPath((SFTPSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new SFTPPath((SFTPSession) session, dict);
        }
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

    private SFTPPath(SFTPSession s, NSDictionary dict) {
        super(dict);
        this.session = s;
    }

    public Session getSession() {
        return this.session;
    }

    public AttributedList list(final ListParseListener listener) {
        synchronized(session) {
            AttributedList childs = new AttributedList() {
                public boolean add(Object object) {
                    boolean result = super.add(object);
                    listener.parsed(this);
                    return result;
                }
            };
            try {
                session.check();
                session.message(NSBundle.localizedString("Listing directory", "Status", "") + " " + this.getAbsolute());
                List children = session.sftp().ls(this.getAbsolute());
                Iterator i = children.iterator();
                while(i.hasNext()) {
                    SFTPv3DirectoryEntry f = (SFTPv3DirectoryEntry) i.next();
                    if(!f.filename.equals(".") && !f.filename.equals("..")) {
                        Path p = new SFTPPath(session, this.getAbsolute(),
                                f.filename, f.attributes.isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                        p.setParent(this);
                        if(null != f.attributes.uid) {
                            p.attributes.setOwner(f.attributes.uid.toString());
                        }
                        if(null != f.attributes.gid) {
                            p.attributes.setGroup(f.attributes.gid.toString());
                        }
                        if(null != f.attributes.size) {
                            p.attributes.setSize(f.attributes.size.longValue());
                        }
                        if(null != f.attributes.mtime) {
                            p.attributes.setModificationDate(Long.parseLong(f.attributes.mtime.toString()) * 1000L);
                        }
                        if(null != f.attributes.atime) {
                            p.attributes.setAccessedDate(Long.parseLong(f.attributes.atime.toString()) * 1000L);
                        }
                        if(f.attributes.isSymlink()) {
                            try {
                                String target = session.sftp().readLink(p.getAbsolute());
                                if(!target.startsWith("/")) {
                                    target = Path.normalize(this.getAbsolute() + Path.DELIMITER + target);
                                }
                                p.setSymbolicLinkPath(target);
                                SFTPv3FileAttributes attr = session.sftp().stat(target);
                                if(attr.isDirectory()) {
                                    p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                                }
                                else if(attr.isRegularFile()) {
                                    p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                                }
                            }
                            catch(IOException e) {
                                log.warn("Cannot read symbolic link target of " + p.getAbsolute() + ":" + e.getMessage());
                                p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                            }
                        }
                        String perm = f.attributes.getOctalPermissions();
                        if(null != perm) {
                            p.attributes.setPermission(new Permission(Integer.parseInt(perm.substring(perm.length() - 3))));
                        }
                        p.getStatus().setSkipped(this.getStatus().isSkipped());
                        childs.add(p);
                    }
                }
            }
            catch(SFTPException e) {
                childs.attributes().setReadable(false);
                this.error("Listing directory failed", e);
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                session.fireActivityStoppedEvent();
            }
            return childs;
        }
    }

    public void mkdir(boolean recursive) {
        synchronized(session) {
            log.debug("mkdir:" + this.getName());
            try {
                if(recursive) {
                    if(!this.getParent().exists()) {
                        this.getParent().mkdir(recursive);
                    }
                }
                session.check();
                session.message(NSBundle.localizedString("Make directory", "Status", "") + " " + this.getName());
                Permission perm = new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default"));
                session.sftp().mkdir(this.getAbsolute(), new Integer(perm.getOctalNumber()).intValue());
            }
            catch(SFTPException e) {
                this.error("Cannot create folder", e);
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                session.fireActivityStoppedEvent();
            }
        }
    }

    public void rename(String absolute) {
        synchronized(session) {
            try {
                session.check();
                session.message(NSBundle.localizedString("Renaming to", "Status", "") + " " + absolute + " (" + this.getName() + ")");
                session.sftp().mv(this.getAbsolute(), absolute);
                this.setPath(absolute);
            }
            catch(SFTPException e) {
                if(this.attributes.isFile()) {
                    this.error("Cannot rename file", e);
                }
                if(this.attributes.isDirectory()) {
                    this.error("Cannot rename folder", e);
                }
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                session.fireActivityStoppedEvent();
            }
        }
    }

    public void delete() {
        synchronized(session) {
            log.debug("delete:" + this.toString());
            try {
                session.check();
                if(this.attributes.isFile() || this.attributes.isSymbolicLink()) {
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.sftp().rm(this.getAbsolute());
                }
                else if(this.attributes.isDirectory()) {
                    for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                        if(!session.isConnected()) {
                            break;
                        }
                        ((AbstractPath) iter.next()).delete();
                    }
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.sftp().rmdir(this.getAbsolute());
                }
            }
            catch(SFTPException e) {
                if(this.attributes.isFile()) {
                    this.error("Cannot delete file", e);
                }
                if(this.attributes.isDirectory()) {
                    this.error("Cannot delete folder", e);
                }
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                session.fireActivityStoppedEvent();
            }
        }
    }

    public void readSize() {
        synchronized(session) {
            if(this.attributes.isFile()) {
                SFTPv3FileHandle handle = null;
                try {
                    session.check();
                    handle = session.sftp().openFileRO(this.getAbsolute());
                    SFTPv3FileAttributes attr = session.sftp().fstat(handle);
                    session.message(NSBundle.localizedString("Getting size of", "Status", "") + " " + this.getName());
                    this.attributes.setSize(attr.size.longValue());
                    session.sftp().closeFile(handle);
                }
                catch(SFTPException e) {
                    this.error("Cannot read file attributes", e);
                }
                catch(IOException e) {
                    this.error("Connection failed", e);
                    session.interrupt();
                }
                finally {
                    if(handle != null) {
                        try {
                            session.sftp().closeFile(handle);
                        }
                        catch(IOException e) {
                            ;
                        }
                    }
                    session.fireActivityStoppedEvent();
                }
            }
        }
    }

    public void readTimestamp() {
        synchronized(session) {
            if(this.attributes.isFile()) {
                SFTPv3FileHandle handle = null;
                try {
                    session.check();
                    session.message(NSBundle.localizedString("Getting timestamp of", "Status", "") + " " + this.getName());
                    handle = session.sftp().openFileRO(this.getAbsolute());
                    SFTPv3FileAttributes attr = session.sftp().fstat(handle);
                    this.attributes.setModificationDate(Long.parseLong(attr.mtime.toString()) * 1000L);
                    session.sftp().closeFile(handle);
                }
                catch(SFTPException e) {
                    this.error("Cannot read file attributes", e);
                }
                catch(IOException e) {
                    this.error("Connection failed", e);
                    session.interrupt();
                }
                finally {
                    if(handle != null) {
                        try {
                            session.sftp().closeFile(handle);
                        }
                        catch(IOException e) {
                            ;
                        }
                    }
                    session.fireActivityStoppedEvent();
                }
            }
        }
    }

    public void readPermission() {
        synchronized(session) {
            if(this.attributes.isFile()) {
                SFTPv3FileHandle handle = null;
                try {
                    session.check();
                    session.message(NSBundle.localizedString("Getting permission of", "Status", "") + " " + this.getName());
                    handle = session.sftp().openFileRO(this.getAbsolute());
                    SFTPv3FileAttributes attr = session.sftp().fstat(handle);
                    String perm = attr.getOctalPermissions();
                    try {
                        this.attributes.setPermission(new Permission(Integer.parseInt(perm.substring(perm.length() - 3))));
                    }
                    catch(NumberFormatException e) {
                        this.attributes.setPermission(Permission.EMPTY);
                    }
                    session.sftp().closeFile(handle);
                }
                catch(SFTPException e) {
                    this.error("Cannot read file attributes", e);
                }
                catch(IOException e) {
                    this.error("Connection failed", e);
                    session.interrupt();
                }
                finally {
                    if(handle != null) {
                        try {
                            session.sftp().closeFile(handle);
                        }
                        catch(IOException e) {
                            ;
                        }
                    }
                    session.fireActivityStoppedEvent();
                }
            }
        }
    }

    public void writeOwner(String owner, boolean recursive) {
        synchronized(session) {
            log.debug("changeOwner");
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing owner to", "Status", "")
                        + " " + owner + " (" + this.getName() + ")");
                SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
                attr.uid = new Integer(owner);
                session.sftp().setstat(this.getAbsolute(), attr);
                if(this.attributes.isDirectory()) {
                    if(recursive) {
                        for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((Path) iter.next()).writeOwner(owner, recursive);
                        }
                    }
                }
            }
            catch(NumberFormatException e) {
                this.error("Cannot change owner", e);
            }
            catch(SFTPException e) {
                this.error("Cannot change owner", e);
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                session.fireActivityStoppedEvent();
            }
        }
    }

    public void writeGroup(String group, boolean recursive) {
        synchronized(session) {
            log.debug("changeGroup");
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing group to", "Status", "")
                        + " " + group + " (" + this.getName() + ")");
                SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
                attr.gid = new Integer(group);
                session.sftp().setstat(this.getAbsolute(), attr);
                if(this.attributes.isDirectory()) {
                    if(recursive) {
                        for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((Path) iter.next()).writeGroup(group, recursive);
                        }
                    }
                }
            }
            catch(NumberFormatException e) {
                this.error("Cannot change group", e);
            }
            catch(SFTPException e) {
                this.error("Cannot change group", e);
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                session.fireActivityStoppedEvent();
            }
        }
    }

    public void writePermissions(Permission perm, boolean recursive) {
        synchronized(session) {
            log.debug("changePermissions");
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing permission to", "Status", "")
                        + " " + perm.getOctalString() + " (" + this.getName() + ")");
                SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
                attr.permissions = new Integer(perm.getOctalNumber());
                session.sftp().setstat(this.getAbsolute(), attr);
                if(this.attributes.isDirectory()) {
                    if(recursive) {
                        for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((AbstractPath) iter.next()).writePermissions(perm, recursive);
                        }
                    }
                }
            }
            catch(SFTPException e) {
                this.error("Cannot change permissions", e);
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                session.fireActivityStoppedEvent();
            }
        }
    }

    public void download(BandwidthThrottle throttle, StreamListener listener) {
        synchronized(session) {
            log.debug("download:" + this.toString());
            InputStream in = null;
            OutputStream out = null;
            try {
                if(this.attributes.isFile()) {
                    out = new Local.OutputStream(this.getLocal(), getStatus().isResume());
                    this.getLocal().touch();
                    if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                        SFTPv3FileHandle handle = session.sftp().openFileRO(this.getAbsolute());
                        in = new SFTPInputStream(handle);
                        if(getStatus().isResume()) {
                            getStatus().setCurrent((long) this.getLocal().attributes.getSize());
                            long skipped = in.skip(getStatus().getCurrent());
                            log.info("Skipping " + skipped + " bytes");
                            if(skipped < this.getStatus().getCurrent()) {
                                throw new IOResumeException("Skipped " + skipped + " bytes instead of " + this.getStatus().getCurrent());
                            }
                        }
                    }
                    if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SCP.getIdentifier())) {
                        SCPClient scp = session.openScp();
                        scp.setCharset(session.getEncoding());
                        in = scp.get(this.getAbsolute());
                    }
                    this.download(in, out, throttle, listener);
                }
                else if(attributes.isDirectory()) {
                    this.getLocal().mkdir(true);
                }
            }
            catch(SFTPException e) {
                this.error("Download failed", e);
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                try {
                    if(in != null) {
                        in.close();
                        in = null;
                    }
                    if(out != null) {
                        out.close();
                        out = null;
                    }
                }
                catch(IOException e) {
                    log.error(e.getMessage());
                }
                finally {
                    session.fireActivityStoppedEvent();
                }
            }
        }
    }

    public void upload(BandwidthThrottle throttle, StreamListener listener, final Permission p) {
        synchronized(session) {
            log.debug("upload:" + this.toString());
            InputStream in = null;
            OutputStream out = null;
            SFTPv3FileHandle handle = null;
            try {
                if(attributes.isDirectory()) {
                    this.mkdir();
                }
                if(attributes.isFile()) {
                    in = new Local.InputStream(this.getLocal());
                    if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                        if(getStatus().isResume() && this.exists()) {
                            handle = session.sftp().openFileRWAppend(this.getAbsolute());
                        }
                        else {
                            handle = session.sftp().createFileTruncate(this.getAbsolute());
                        }
                    }
                    // We do set the permissions here as otherwise we might have an empty mask for
                    // interrupted file transfers
                    if(null != p) {
                        if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                            try {
                                log.info("Updating permissions:" + p.getOctalString());
                                SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
                                attr.permissions = new Integer(p.getOctalNumber());
                                session.sftp().fsetstat(handle, attr);
                            }
                            catch(SFTPException e) {
                                // We might not be able to change the attributes if we are
                                // not the owner of the file; but then we still want to proceed as we
                                // might have group write privileges
                                log.warn(e.getMessage());
                            }
                        }
                    }
                    if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                        if(getStatus().isResume()) {
                            getStatus().setCurrent(
                                    session.sftp().stat(this.getAbsolute()).size.intValue());
                        }
                    }
                    if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                        out = new SFTPOutputStream(handle);
                        if(getStatus().isResume()) {
                            long skipped = ((SFTPOutputStream) out).skip(getStatus().getCurrent());
                            log.info("Skipping " + skipped + " bytes");
                            if(skipped < this.getStatus().getCurrent()) {
                                throw new IOResumeException("Skipped " + skipped + " bytes instead of " + this.getStatus().getCurrent());
                            }
                        }
                    }
                    if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SCP.getIdentifier())) {
                        SCPClient scp = session.openScp();
                        scp.setCharset(session.getEncoding());
                        out = scp.put(this.getName(), (long) this.getLocal().attributes.getSize(),
                                this.getParent().getAbsolute(),
                                "0" + p.getOctalString());
                    }
                    this.upload(out, in, throttle, listener);
                }
                if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier())) {
                    if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                        if(attributes.isFile()) {
                            log.info("Updating timestamp");
                            SFTPv3FileAttributes attrs = new SFTPv3FileAttributes();
                            int t = (int) (this.getLocal().attributes.getModificationDate() / 1000);
                            // We must both set the accessed and modified time
                            // See AttribFlags.SSH_FILEXFER_ATTR_V3_ACMODTIME
                            attrs.atime = new Integer(t);
                            attrs.mtime = new Integer(t);
                            try {
                                if(null == handle) {
                                    if(attributes.isFile()) {
                                        handle = session.sftp().openFileRW(this.getAbsolute());
                                    }
//                            if(attributes.isDirectory()) {
//                                handle = session.sftp().openDirectory(this.getAbsolute());
//                            }
                                }
                                session.sftp().fsetstat(handle, attrs);
                            }
                            catch(SFTPException e) {
                                // We might not be able to change the attributes if we are
                                // not the owner of the file; but then we still want to proceed as we
                                // might have group write privileges
                                log.warn(e.getMessage());
                            }
                        }
                    }
                }
            }
            catch(SFTPException e) {
                this.error("Upload failed", e);
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                session.interrupt();
            }
            finally {
                try {
                    if(handle != null) {
                        session.sftp().closeFile(handle);
                    }
                    if(in != null) {
                        in.close();
                        in = null;
                    }
                    if(out != null) {
                        out.close();
                        out = null;
                    }
                }
                catch(IOException e) {
                    log.error(e.getMessage());
                }
                finally {
                    session.fireActivityStoppedEvent();
                }
            }
        }
    }
}