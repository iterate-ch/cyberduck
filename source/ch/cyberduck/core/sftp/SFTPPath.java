package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSDictionary;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.*;

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

    private SFTPSession session;

    private SFTPPath(SFTPSession session) {
        super();
        this.session = session;
    }

    private SFTPPath(SFTPSession session, String parent, String name) {
        super(parent, name);
        this.session = session;
    }

    private SFTPPath(SFTPSession session, String path) {
        super(path);
        this.session = session;
    }

    private SFTPPath(SFTPSession session, String parent, Local file) {
        super(parent, file);
        this.session = session;
    }

    private SFTPPath(SFTPSession session, NSDictionary dict) {
        super(dict);
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }

    public synchronized List list() {
        return this.list(false);
    }

    public synchronized List list(boolean refresh) {
        return this.list(refresh, Preferences.instance().getProperty("browser.showHidden").equals("true"));
    }

    public synchronized List list(boolean refresh, boolean showHidden) {
        List files = this.getSession().cache().get(this.getAbsolute());
        session.addPathToHistory(this);
        if (refresh || files.size() == 0) {
            files.clear();
            session.log("Listing " + this.getAbsolute(), Message.PROGRESS);
            SftpFile workingDirectory = null;
            try {
                session.check();
                workingDirectory = session.SFTP.openDirectory(this.getAbsolute());
                List children = new ArrayList();
                int read = 1;
                while (read > 0) {
                    read = session.SFTP.listChildren(workingDirectory, children);
                }
                java.util.Iterator i = children.iterator();
                while (i.hasNext()) {
                    SftpFile x = (SftpFile) i.next();
                    if (!x.getFilename().equals(".") && !x.getFilename().equals("..")) {
                        if (!(x.getFilename().charAt(0) == '.') || showHidden) {
                            Path p = PathFactory.createPath(session, this.getAbsolute(), x.getFilename());
                            p.attributes.setOwner(x.getAttributes().getUID().toString());
                            p.attributes.setGroup(x.getAttributes().getGID().toString());
                            p.status.setSize(x.getAttributes().getSize().intValue());
                            p.attributes.setTimestamp(Long.parseLong(x.getAttributes().getModifiedTime().toString()) * 1000L);
//							if(x.getAttributes().isFile()) {
//								p.attributes.setType(Path.FILE_TYPE);
//							}
//							else if(x.getAttributes().isDirectory()) {
//								p.attributes.setType(Path.DIRECTORY_TYPE);
//							}
//							else if(x.getAttributes().isLink()) {
//								p.attributes.setType(Path.SYMBOLIC_LINK_TYPE);
//							}
//							else {
//								p.attributes.setType(Path.FILE_TYPE);
//							}
                            //hack
                            String permStr = x.getAttributes().getPermissionsString();
                            if (permStr.charAt(0) == 'd') {
                                p.attributes.setType(Path.DIRECTORY_TYPE);
                            }
                            else if (permStr.charAt(0) == 'l') {
                                p.attributes.setType(Path.SYMBOLIC_LINK_TYPE);
                            }
                            else {
                                p.attributes.setType(Path.FILE_TYPE);
                            }
                            p.attributes.setPermission(new Permission(permStr));
                            files.add(p);
                        }
                    }
                }
                this.setCache(files);
            }
            catch (SshException e) {
                session.log("SSH Error: " + e.getMessage(), Message.ERROR);
            }
            catch (IOException e) {
                session.log("IO Error: " + e.getMessage(), Message.ERROR);
            }
            finally {
                session.log("Idle", Message.STOP);
                if (workingDirectory != null) {
                    try {
                        workingDirectory.close();
                    }
                    catch (SshException e) {
                        session.log("SSH Error: " + e.getMessage(), Message.ERROR);
                    }
                    catch (IOException e) {
                        session.log("IO Error: " + e.getMessage(), Message.ERROR);
                    }
                }
            }
        }
        session.callObservers(this);
        return files;
    }

    public void delete() {
        log.debug("delete:" + this.toString());
        try {
            session.check();
            if (this.isFile()) {
                session.log("Deleting " + this.getName(), Message.PROGRESS);
                session.SFTP.removeFile(this.getAbsolute());
            }
            else if (this.isDirectory()) {
                List files = this.list(false, true);
                java.util.Iterator iterator = files.iterator();
                Path file = null;
                while (iterator.hasNext()) {
                    file = (Path) iterator.next();
                    if (file.isDirectory()) {
                        file.delete();
                    }
                    if (file.isFile()) {
                        session.log("Deleting " + this.getName(), Message.PROGRESS);
                        session.SFTP.removeFile(file.getAbsolute());
                    }
                }
                session.SFTP.openDirectory(this.getParent().getAbsolute());
                session.log("Deleting " + this.getName(), Message.PROGRESS);
                session.SFTP.removeDirectory(this.getAbsolute());
            }
        }
        catch (SshException e) {
            session.log("SSH Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void changeOwner(String uid, boolean recursive) {
        //@todo assert uid is a number
        log.debug("changeOwner:" + uid);
        try {
            session.check();
            FileAttributes attrs = session.SFTP.getAttributes(this.getAbsolute());
            attrs.setUID(new UnsignedInteger32(uid));
            session.SFTP.setAttributes(this.getAbsolute(), attrs);
        }
        catch (SshException e) {
            session.log("SSH Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void changeGroup(String gid, boolean recursive) {
        //@todo assert gid is a number
        log.debug("changeGroup:" + gid);
        try {
            session.check();
            FileAttributes attrs = session.SFTP.getAttributes(this.getAbsolute());
            attrs.setGID(new UnsignedInteger32(gid));
            session.SFTP.setAttributes(this.getAbsolute(), attrs);
        }
        catch (SshException e) {
            session.log("SSH Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void changePermissions(Permission perm, boolean recursive) {
        log.debug("changePermissions");
        try {
            session.check();
            session.SFTP.changePermissions(this.getAbsolute(), perm.getDecimalCode());
        }
        catch (SshException e) {
            session.log("SSH Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void download() {
        InputStream in = null;
        OutputStream out = null;
        try {
            log.debug("download:" + this.toString());
            this.session.check();
            this.getLocal().getParentFile().mkdirs();
            out = new FileOutputStream(this.getLocal().getTemp(), this.status.isResume());
            if (out == null) {
                throw new IOException("Unable to buffer data");
            }
            SftpFile p = this.session.SFTP.openFile(this.getAbsolute(), SftpSubsystemClient.OPEN_READ);
            this.status.setSize(p.getAttributes().getSize().intValue());
            in = new SftpFileInputStream(p);
            if (in == null) {
                throw new IOException("Unable opening data stream");
            }
            if (this.status.isResume()) {
                this.status.setCurrent(this.getLocal().getTemp().length());
                long skipped = in.skip(this.status.getCurrent());
                log.info("Skipping " + skipped + " bytes");
                if (skipped < this.status.getCurrent()) {
                    throw new IOException("Resume failed: Skipped " + skipped + " bytes instead of " + this.status.getCurrent());
                }
            }
            this.download(in, out);
            if (Preferences.instance().getProperty("queue.download.changePermissions").equals("true")) {
                Permission perm = this.attributes.getPermission();
                if (!perm.isUndefined()) {
                    this.getLocal().setPermission(perm);
                }
            }
        }
        catch (SshException e) {
            this.session.log("SSH Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException e) {
				e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    public void upload() {
        InputStream in = null;
        SftpFileOutputStream out = null;
        try {
            log.debug("upload:" + this.toString());
            this.session.check();
            if (!this.getParent().exists()) {
                this.session.mkdir(this.getParent().getParent().getAbsolute()+"/"+this.getParent().getName());
            }
            in = new FileInputStream(this.getLocal());
            if (in == null) {
                throw new IOException("Unable to buffer data");
            }
            SftpFile p = null;
            if (this.status.isResume()) {
                p = this.session.SFTP.openFile(this.getAbsolute(),
                        SftpSubsystemClient.OPEN_WRITE | //File open flag, opens the file for writing.
                        SftpSubsystemClient.OPEN_APPEND); //File open flag, forces all writes to append data at the end of the file.
            }
            else {
                p = this.session.SFTP.openFile(this.getAbsolute(),
                        SftpSubsystemClient.OPEN_CREATE | //File open flag, if specified a new file will be created if one does not already exist.
                        SftpSubsystemClient.OPEN_WRITE | //File open flag, opens the file for writing.
                        SftpSubsystemClient.OPEN_TRUNCATE); //File open flag, forces an existing file with the same name to be truncated to zero length when creating a file by specifying OPEN_CREATE.
            }
//			this.changePermissions(this.getLocal().getPermission(), false);
            if (this.status.isResume()) {
                this.status.setCurrent(p.getAttributes().getSize().intValue());
            }
            out = new SftpFileOutputStream(p);
            if (out == null) {
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
            if (Preferences.instance().getProperty("queue.upload.changePermissions").equals("true")) {
                Permission perm = this.getLocal().getPermission();
                if (!perm.isUndefined()) {
                    this.changePermissions(perm, false);
                }
            }
        }
        catch (SshException e) {
            this.session.log("SSH Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            this.session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException e) {
                log.error(e.getMessage());
				e.printStackTrace();
            }
        }
    }
}
