package ch.cyberduck.core.sftp;

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

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.growl.Growl;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class SFTPPath extends Path {
    private static Logger log = Logger.getLogger(SFTPPath.class);

    static {
        PathFactory.addFactory(Session.SFTP, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String parent, String name) {
            return new SFTPPath((SFTPSession) session, parent, name);
        }

        protected Path create(Session session, String path) {
            return new SFTPPath((SFTPSession) session, path);
        }

        protected Path create(Session session) {
            return new SFTPPath((SFTPSession) session);
        }

        protected Path create(Session session, String path, Local file) {
            return new SFTPPath((SFTPSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new SFTPPath((SFTPSession) session, dict);
        }
    }

    private final SFTPSession session;

    private SFTPPath(SFTPSession s) {
        super();
        this.session = s;
    }

    private SFTPPath(SFTPSession s, String parent, String name) {
        super(parent, name);
        this.session = s;
    }

    private SFTPPath(SFTPSession s, String path) {
        super(path);
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

    public AttributedList list(Comparator comparator, Filter filter, boolean verbosity) {
        synchronized (session) {
            if (!session.cache().containsKey(this) || session.cache().isInvalid(this)) {
                AttributedList files = new AttributedList();
                session.message(NSBundle.localizedString("Listing directory", "Status", "") + " " + this.getAbsolute());
                try {
                    session.check();
                    SftpFile workingDirectory = session.SFTP.openDirectory(this.getAbsolute());
                    List children = new ArrayList();
                    int read = 1;
                    while (read > 0) {
                        read = session.SFTP.listChildren(workingDirectory, children);
                    }
                    workingDirectory.close();
                    java.util.Iterator i = children.iterator();
                    while (i.hasNext() && session.isConnected()) {
                        SftpFile x = (SftpFile) i.next();
                        if (!x.getFilename().equals(".") && !x.getFilename().equals("..")) {
                            Path p = PathFactory.createPath(session, this.getAbsolute(), x.getFilename());
                            p.attributes.setOwner(x.getAttributes().getUID().toString());
                            p.attributes.setGroup(x.getAttributes().getGID().toString());
                            p.attributes.setSize(x.getAttributes().getSize().doubleValue());
                            p.attributes.setTimestamp(Long.parseLong(x.getAttributes().getModifiedTime().toString()) * 1000L);
                            String permStr = x.getAttributes().getPermissionsString();
                            if (permStr.charAt(0) == 'd') {
                                p.attributes.setType(Path.DIRECTORY_TYPE);
                            }
                            else if (permStr.charAt(0) == 'l') {
                                try {
                                    p.cwdir();
                                    p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                                }
                                catch (java.io.IOException e) {
                                    p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                                }
                            }
                            else {
                                p.attributes.setType(Path.FILE_TYPE);
                            }
                            p.attributes.setPermission(new Permission(permStr.substring(1, permStr.length())));
                            files.add(p);
                        }
                    }
                    session.cache().put(this, files);
                }
                catch (SshException e) {
                    if(verbosity)
                        session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));

                }
                catch (IOException e) {
                    session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                    session.close();
                }
	            finally {
	                session.activityStopped();
	            }
            }
            return session.cache().get(this, comparator, filter);
        }
    }

    public void cwdir() throws IOException {
        synchronized (session) {
            session.check();
            session.SFTP.openDirectory(this.getAbsolute());
        }
    }

    public void mkdir(boolean recursive) {
        synchronized (session) {
            log.debug("mkdir:" + this.getName());
            try {
                if (recursive) {
                    if (!this.getParent().exists()) {
                        this.getParent().mkdir(recursive);
                    }
                }
                session.check();
                session.message(NSBundle.localizedString("Make directory", "Status", "") + " " + this.getName());
                session.SFTP.makeDirectory(this.getAbsolute());
                session.cache().put(this, new AttributedList());
                this.getParent().invalidate();
            }
            catch (SshException e) {
                session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
            }
            catch (IOException e) {
                session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                session.close();
            }
            finally {
                session.activityStopped();
            }
        }
    }

    public void rename(String filename) {
        synchronized (session) {
            try {
                session.check();
                session.message("Renaming " + this.getName() + " to " + filename);
                session.SFTP.renameFile(this.getAbsolute(), filename);
                this.getParent().invalidate();
                this.setPath(filename);
                //this.getParent().invalidate();
            }
            catch (SshException e) {
                session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
            }
            catch (IOException e) {
                session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                session.close();
            }
            finally {
                session.activityStopped();
            }
        }
    }

    public void reset() {
        synchronized (session) {
            if (this.attributes.isFile() && this.attributes.isUndefined()) {
                if (this.exists()) {
                    try {
                        session.check();
                        session.message(NSBundle.localizedString("Getting timestamp of", "Status", "") + " " + this.getName());
                        SftpFile f = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
                        this.attributes.setTimestamp(Long.parseLong(f.getAttributes().getModifiedTime().toString()) * 1000L);
                        session.message(NSBundle.localizedString("Getting size of", "Status", "") + " " + this.getName());
                        this.attributes.setSize(f.getAttributes().getSize().doubleValue());
                        f.close();
                    }
                    catch (SshException e) {
                        session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
                    }
                    catch (IOException e) {
                        session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                        session.close();
                    }
                }
            }
        }
    }

    public void delete() {
        synchronized (session) {
            log.debug("delete:" + this.toString());
            try {
                if (this.attributes.isFile()) {
                    session.check();
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.SFTP.removeFile(this.getAbsolute());
                }
                else if (this.attributes.isDirectory() && !this.attributes.isSymbolicLink()) {
                    List files = this.list();
                    if (files != null && files.size() > 0) {
                        for (Iterator iter = files.iterator(); iter.hasNext();) {
                            ((Path) iter.next()).delete();
                        }
                    }
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.SFTP.removeDirectory(this.getAbsolute());
                }
                this.getParent().invalidate();
            }
            catch (SshException e) {
                session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
            }
            catch (IOException e) {
                session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                session.close();
            }
            finally {
                session.activityStopped();
            }
        }
    }

    public void changeOwner(String owner, boolean recursive) {
        synchronized (session) {
            log.debug("changeOwner");
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.message("Changing owner to " + owner + " on " + this.getName()); //todo localize
                    //session.SFTP.changeOwner(this.getAbsolute(), owner);
                }
                else if (this.attributes.isDirectory()) {
                    session.message("Changing owner to " + owner + " on " + this.getName()); //todo localize
                    //session.SFTP.changeOwner(this.getAbsolute(), owner);
                    if (recursive) {
                        List files = this.list();
                        if (files != null) {
                            for (Iterator iter = files.iterator(); iter.hasNext();) {
                                ((Path) iter.next()).changeOwner(owner, recursive);
                            }
                        }
                    }
                }
                this.getParent().invalidate();
            }
            catch (SshException e) {
                session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
            }
            catch (IOException e) {
                session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                session.close();
            }
            finally {
                session.activityStopped();
            }
        }
    }

    public void changeGroup(String group, boolean recursive) {
        synchronized (session) {
            log.debug("changeGroup");
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.message("Changing group to " + group + " on " + this.getName()); //todo localize
                    //session.SFTP.changeGroup(this.getAbsolute(), group);
                }
                else if (this.attributes.isDirectory()) {
                    session.message("Changing group to " + group + " on " + this.getName()); //todo localize
                    //session.SFTP.changeGroup(this.getAbsolute(), group);
                    if (recursive) {
                        List files = this.list();
                        if (files != null) {
                            for (Iterator iter = files.iterator(); iter.hasNext();) {
                                ((Path) iter.next()).changeGroup(group, recursive);
                            }
                        }
                    }
                }
                this.getParent().invalidate();
            }
            catch (SshException e) {
                session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
            }
            catch (IOException e) {
                session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                session.close();
            }
            finally {
                session.activityStopped();
            }
        }
    }

    public void changePermissions(Permission perm, boolean recursive) {
        synchronized (session) {
            log.debug("changePermissions");
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
	                session.message("Changing permission to " + perm.getOctalCode() + " on " + this.getName()); //todo localize
                    session.SFTP.changePermissions(this.getAbsolute(), perm.getMask());
                }
                else if (this.attributes.isDirectory()) {
	                session.message("Changing permission to " + perm.getOctalCode() + " on " + this.getName()); //todo localize
                    session.SFTP.changePermissions(this.getAbsolute(), perm.getMask());
                    if (recursive) {
                        List files = this.list();
                        if (files != null) {
                            for (Iterator iter = files.iterator(); iter.hasNext();) {
                                ((Path) iter.next()).changePermissions(perm, recursive);
                            }
                        }
                    }
                }
                this.getParent().invalidate();
            }
            catch (SshException e) {
                session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
            }
            catch (IOException e) {
                session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                session.close();
            }
            finally {
                session.activityStopped();
            }
        }
    }

    public void download() {
        synchronized (session) {
            log.debug("download:" + this.toString());
            InputStream in = null;
            OutputStream out = null;
            try {
                if (this.attributes.isFile()) {
                    session.check();
                    out = new FileOutputStream(this.getLocal(), this.status.isResume());
                    if (null == out) {
                        throw new IOException("Unable to buffer data");
                    }
                    SftpFile f = session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
                    in = new SftpFileInputStream(f);
                    if (null == in) {
                        throw new IOException("Unable opening data stream");
                    }
                    if (this.status.isResume()) {
                        this.status.setCurrent((long)this.getLocal().getSize());
                        long skipped = in.skip(this.status.getCurrent());
                        log.info("Skipping " + skipped + " bytes");
                        if (skipped < this.status.getCurrent()) {
                            throw new IOException("Resume failed: Skipped " + skipped + " bytes instead of " + this.status.getCurrent());
                        }
                    }
                    this.download(in, out);
                }
                if (this.attributes.isDirectory()) {
                    this.getLocal().mkdirs();
                }
                if (Preferences.instance().getBoolean("queue.download.changePermissions")) {
                    log.info("Updating permissions");
                    Permission perm = null;
                    if (this.attributes.isFile()
                            && Preferences.instance().getBoolean("queue.download.permissions.useDefault")) {
                        perm = new Permission(Preferences.instance().getProperty("queue.download.permissions.default"));
                    }
                    else {
                        perm = this.attributes.getPermission();
                        perm.getOwnerPermissions()[Permission.WRITE] = true;
                    }
                    if (!perm.isUndefined()) {
                        this.getLocal().setPermission(perm);
                    }
                }
                if (Preferences.instance().getBoolean("queue.download.preserveDate")) {
                    if (this.attributes.getTimestamp() != -1) {
                        log.info("Updating timestamp");
                        this.getLocal().setLastModified(this.attributes.getTimestamp());
                    }
                }
            }
            catch (SshException e) {
                session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
                Growl.instance().notify(
                        NSBundle.localizedString("Download failed", "Growl", "Growl Notification"),
                        this.getName());
            }
            catch (IOException e) {
                session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                Growl.instance().notify(
                        NSBundle.localizedString("Download failed", "Growl", "Growl Notification"),
                        this.getName());
                session.close();
            }
            finally {
                session.activityStopped();
                try {
                    if (in != null) {
                        in.close();
                        in = null;
                    }
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                }
                catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public void upload() {
        synchronized (session) {
            log.debug("upload:" + this.toString());
            InputStream in = null;
            SftpFileOutputStream out = null;
            try {
                SftpFile f = null;
                if (this.attributes.isFile()) {
                    session.check();
                    in = new FileInputStream(this.getLocal());
                    if (null == in) {
                        throw new IOException("Unable to buffer data");
                    }
                    if (this.status.isResume()) {
                        f = session.SFTP.openFile(this.getAbsolute(),
                                SftpSubsystemClient.OPEN_WRITE | //File open flag, opens the file for writing.
                                        SftpSubsystemClient.OPEN_APPEND); //File open flag, forces all writes to append data at the end of the file.
                    }
                    else {
                        f = session.SFTP.openFile(this.getAbsolute(),
                                SftpSubsystemClient.OPEN_CREATE | //File open flag, if specified a new file will be created if one does not already exist.
                                        SftpSubsystemClient.OPEN_WRITE | //File open flag, opens the file for writing.
                                        SftpSubsystemClient.OPEN_TRUNCATE); //File open flag, forces an existing file with the same name to be truncated to zero length when creating a file by specifying OPEN_CREATE.
                    }
                    // We do set the permissions here as otehrwise we might have an empty mask for
                    // interrupted file transfers
                    if (Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                        try {
                            Permission perm = null;
                            if (this.attributes.isFile()
                                    && Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                                perm = new Permission(Preferences.instance().getProperty("queue.upload.permissions.default"));
                            }
                            else {
                                perm = this.getLocal().getPermission();
                            }
                            if (!perm.isUndefined()) {
                                session.SFTP.changePermissions(this.getAbsolute(), perm.getMask());
                            }
                        }
                        catch(SshException e) {
                            log.warn(e.getMessage());
                        }
                    }
                    if (this.status.isResume()) {
                        this.status.setCurrent(f.getAttributes().getSize().intValue());
                    }
                    out = new SftpFileOutputStream(f);
                    if (null == out) {
                        throw new IOException("Unable opening data stream");
                    }
                    if (this.status.isResume()) {
                        long skipped = out.skip(this.status.getCurrent());
                        log.info("Skipping " + skipped + " bytes");
                        if (skipped < this.status.getCurrent()) {
                            throw new IOException("Resume failed: Skipped " + skipped + " bytes instead of " + this.status.getCurrent());
                        }
                    }
                    this.upload(out, in);
                }
                if (this.attributes.isDirectory()) {
                    this.mkdir();
                    if (Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                        Permission perm = this.getLocal().getPermission();
                        if (!perm.isUndefined()) {
                            session.SFTP.changePermissions(this.getAbsolute(), perm.getMask());
                        }
                    }
                    f = session.SFTP.openFile(this.getAbsolute(),
                            SftpSubsystemClient.OPEN_READ);
                }
                if (Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                    try {
                        FileAttributes attrs = new FileAttributes();
                        attrs.setTimes(f.getAttributes().getModifiedTime(),
                                new UnsignedInteger32(this.getLocal().getTimestamp() / 1000));
                        session.SFTP.setAttributes(this.getAbsolute(), attrs);
                    }
                    catch(SshException e) {
                        log.warn(e.getMessage());
                    }
                }
                this.getParent().invalidate();
            }
            catch (SshException e) {
                session.error(new SshException(e.getMessage()+" (" + this.getName() + ")"));
                Growl.instance().notify(
                        NSBundle.localizedString("Upload failed", "Growl", "Growl Notification"),
                        this.getName());
            }
            catch (IOException e) {
                session.error(new IOException(e.getMessage()+" ("+this.getName()+")"));
                Growl.instance().notify(
                        NSBundle.localizedString("Upload failed", "Growl", "Growl Notification"),
                        this.getName());
                session.close();
            }
            finally {
                session.activityStopped();
                try {
                    if (in != null) {
                        in.close();
                        in = null;
                    }
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
