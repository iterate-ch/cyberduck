package ch.cyberduck.core.ftp;

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
import com.apple.cocoa.foundation.NSPathUtilities;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.io.ToNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class FTPPath extends Path {
    private static Logger log = Logger.getLogger(FTPPath.class);

    private static final String DOS_LINE_SEPARATOR = "\r\n";
    private static final String MAC_LINE_SEPARATOR = "\r";
    private static final String UNIX_LINE_SEPARATOR = "\n";

    static {
        PathFactory.addFactory(Session.FTP, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String parent, String name) {
            return new FTPPath((FTPSession) session, parent, name);
        }

        protected Path create(Session session) {
            return new FTPPath((FTPSession) session);
        }

        protected Path create(Session session, String path) {
            return new FTPPath((FTPSession) session, path);
        }

        protected Path create(Session session, String path, Local file) {
            return new FTPPath((FTPSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new FTPPath((FTPSession) session, dict);
        }
    }

    private FTPSession session;

    /**
     * @param s      The connection to work with for regular file operations
     * @param parent The parent directory relative to this file
     * @param name   The filename of this path
     */
    protected FTPPath(FTPSession s, String parent, String name) {
        super(parent, name);
        this.session = s;
    }

    protected FTPPath(FTPSession s, String path) {
        super(path);
        this.session = s;
    }

    protected FTPPath(FTPSession s) {
        super();
        this.session = s;
    }

    /**
     * @param s      The connection to work with for regular file operations
     * @param parent The parent directory relative to this file
     * @param file   The corresponding local file to the remote path
     */
    protected FTPPath(FTPSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected FTPPath(FTPSession s, NSDictionary dict) {
        super(dict);
        this.session = s;
    }

    public Session getSession() {
        return this.session;
    }

    public List list(String encoding, boolean refresh, Filter filter, boolean notifyObservers) {
        synchronized (session) {
            if (notifyObservers) {
                session.addPathToHistory(this);
            }
            if (refresh || session.cache().get(this.getAbsolute()) == null) {
                List files = new ArrayList();
                session.log(Message.PROGRESS, "Listing " + this.getAbsolute());
                try {
                    session.check();
                    session.FTP.setTransferType(FTPTransferType.ASCII);
                    session.FTP.chdir(this.getAbsolute());
                    String[] lines = session.FTP.dir(encoding);
                    for (int i = 0; i < lines.length; i++) {
                        Path p = session.parser.parseFTPEntry(this, lines[i]);
                        if (p != null) {
                            files.add(p);
                        }
                    }
                    session.cache().put(this.getAbsolute(), files);
                    session.log(Message.STOP, "Idle");
                }
                catch (FTPException e) {
                    session.log(Message.ERROR, "FTP Error: " + e.getMessage());
                    return null;
                }
                catch (IOException e) {
                    session.log(Message.ERROR, "IO Error: " + e.getMessage());
                    session.close();
                    return null;
                }
            }
            if (notifyObservers) {
                session.callObservers(this);
            }
            return session.cache().get(this.getAbsolute(), filter);
        }
    }

    public void cwdir() throws IOException {
        synchronized (session) {
            session.check();
            session.FTP.chdir(this.getAbsolute());
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
                session.log(Message.PROGRESS, "Make directory " + this.getName());
                session.FTP.mkdir(this.getAbsolute());
                session.cache().put(this.getAbsolute(), new ArrayList());
                this.getParent().invalidate();
                session.log(Message.STOP, "Idle");
            }
            catch (FTPException e) {
                session.log(Message.ERROR, "FTP Error: " + e.getMessage());
            }
            catch (IOException e) {
                session.log(Message.ERROR, "IO Error: " + e.getMessage());
                session.close();
            }
        }
    }

    public void rename(String filename) {
        synchronized (session) {
            log.debug("rename:" + filename);
            try {
                session.check();
                session.log(Message.PROGRESS, "Renaming " + this.getName() + " to " + filename);
                session.FTP.rename(this.getAbsolute(), filename);
                this.setPath(filename);
                this.getParent().invalidate();
                session.log(Message.STOP, "Idle");
            }
            catch (FTPException e) {
                session.log(Message.ERROR, "FTP Error: " + e.getMessage());
            }
            catch (IOException e) {
                session.log(Message.ERROR, "IO Error: " + e.getMessage());
                session.close();
            }
        }
    }

    public void reset() {
        synchronized (session) {
            if (this.attributes.isFile() && this.attributes.isUndefined()) {
                if (this.exists()) {
                    try {
                        session.check();
                        session.log(Message.PROGRESS, "Getting timestamp of " + this.getName());
                        this.attributes.setTimestamp(session.FTP.modtime(this.getAbsolute()));
                        if (Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
                            if (this.getExtension() != null && Preferences.instance().getProperty("ftp.transfermode.ascii.extensions").indexOf(this.getExtension()) != -1) {
                                session.FTP.setTransferType(FTPTransferType.ASCII);
                            }
                            else {
                                session.FTP.setTransferType(FTPTransferType.BINARY);
                            }
                        }
                        else if (Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
                            session.FTP.setTransferType(FTPTransferType.BINARY);
                        }
                        else if (Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
                            session.FTP.setTransferType(FTPTransferType.ASCII);
                        }
                        else {
                            throw new FTPException("Transfer type not set");
                        }
                        session.log(Message.PROGRESS, "Getting size of " + this.getName());
                        this.attributes.setSize(session.FTP.size(this.getAbsolute()));
                    }
                    catch (FTPException e) {
                        log.error(e.getMessage());
                        //ignore
                    }
                    catch (IOException e) {
                        session.log(Message.ERROR, "IO Error: " + e.getMessage());
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
                    session.FTP.chdir(this.getParent().getAbsolute());
                    session.log(Message.PROGRESS, "Deleting " + this.getName());
                    session.FTP.delete(this.getName());
                }
                else if (this.attributes.isDirectory()) {
                    List files = this.list(true, new NullFilter(), false);
                    java.util.Iterator iterator = files.iterator();
                    Path file = null;
                    while (iterator.hasNext()) {
                        file = (Path) iterator.next();
                        if (file.attributes.isFile()) {
                            session.log(Message.PROGRESS, "Deleting " + this.getName());
                            session.FTP.delete(file.getName());
                        }
                        if (file.attributes.isDirectory()) {
                            file.delete();
                        }
                    }
                    session.FTP.cdup();
                    session.log(Message.PROGRESS, "Deleting " + this.getName());
                    session.FTP.rmdir(this.getName());
                }
                this.getParent().invalidate();
                session.log(Message.STOP, "Idle");
            }
            catch (FTPException e) {
                session.log(Message.ERROR, "FTP Error: " + e.getMessage());
            }
            catch (IOException e) {
                session.log(Message.ERROR, "IO Error: " + e.getMessage());
                session.close();
            }
        }
    }

    public void changeOwner(String owner, boolean recursive) {
        synchronized (session) {
            String command = "chown";
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.log(Message.PROGRESS, "Changing owner to " + this.attributes.getOwner() + " on " + this.getName());
                    session.FTP.site(command + " " + owner + " " + this.getAbsolute());
                }
                else if (this.attributes.isDirectory()) {
                    session.log(Message.PROGRESS, "Changing owner to " + this.attributes.getOwner() + " on " + this.getName());
                    session.FTP.site(command + " " + owner + " " + this.getAbsolute());
                    if (recursive) {
                        List files = this.list(false, new NullFilter(), false);
                        java.util.Iterator iterator = files.iterator();
                        Path file = null;
                        while (iterator.hasNext()) {
                            file = (Path) iterator.next();
                            file.changeOwner(owner, recursive);
                        }
                    }
                }
                this.getParent().invalidate();
                session.log(Message.STOP, "Idle");
            }
            catch (FTPException e) {
                session.log(Message.ERROR, "FTP Error: " + e.getMessage());
            }
            catch (IOException e) {
                session.log(Message.ERROR, "IO Error: " + e.getMessage());
                session.close();
            }
        }
    }

    public void changeGroup(String group, boolean recursive) {
        synchronized (session) {
            String command = "chgrp";
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.log(Message.PROGRESS, "Changing group to " + this.attributes.getGroup() + " on " + this.getName());
                    session.FTP.site(command + " " + group + " " + this.getAbsolute());
                }
                else if (this.attributes.isDirectory()) {
                    session.log(Message.PROGRESS, "Changing group to " + this.attributes.getGroup() + " on " + this.getName());
                    session.FTP.site(command + " " + group + " " + this.getAbsolute());
                    if (recursive) {
                        List files = this.list(false, new NullFilter(), false);
                        java.util.Iterator iterator = files.iterator();
                        Path file = null;
                        while (iterator.hasNext()) {
                            file = (Path) iterator.next();
                            file.changeGroup(group, recursive);
                        }
                    }
                }
                this.getParent().invalidate();
                session.log(Message.STOP, "Idle");
            }
            catch (FTPException e) {
                session.log(Message.ERROR, "FTP Error: " + e.getMessage());
            }
            catch (IOException e) {
                session.log(Message.ERROR, "IO Error: " + e.getMessage());
                session.close();
            }
        }
    }

    public void changePermissions(Permission perm, boolean recursive) {
        synchronized (session) {
            log.debug("changePermissions:" + perm);
            String command = "chmod";
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.log(Message.PROGRESS, "Changing permission to " + perm.getOctalCode() + " on " + this.getName());
                    session.FTP.site(command + " " + perm.getOctalCode() + " " + this.getAbsolute());
                }
                else if (this.attributes.isDirectory()) {
                    session.log(Message.PROGRESS, "Changing permission to " + perm.getOctalCode() + " on " + this.getName());
                    session.FTP.site(command + " " + perm.getOctalCode() + " " + this.getAbsolute());
                    if (recursive) {
                        List files = this.list(false, new NullFilter(), false);
                        java.util.Iterator iterator = files.iterator();
                        Path file = null;
                        while (iterator.hasNext()) {
                            file = (Path) iterator.next();
                            file.changePermissions(perm, recursive);
                        }
                    }
                }
                this.getParent().invalidate();
                session.log(Message.STOP, "Idle");
            }
            catch (FTPException e) {
                session.log(Message.ERROR, "FTP Error: " + e.getMessage());
            }
            catch (IOException e) {
                session.log(Message.ERROR, "IO Error: " + e.getMessage());
                session.close();
            }
        }
    }

    public void download() {
        synchronized (session) {
            log.debug("download:" + this.toString());
            try {
                if (this.attributes.isFile()) {
                    session.check();
                    if (Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
                        if (this.getExtension() != null && Preferences.instance().getProperty("ftp.transfermode.ascii.extensions").indexOf(this.getExtension()) != -1) {
                            this.downloadASCII();
                        }
                        else {
                            this.downloadBinary();
                        }
                    }
                    else if (Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
                        this.downloadBinary();
                    }
                    else if (Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
                        this.downloadASCII();
                    }
                    else {
                        throw new FTPException("Transfer mode not set");
                    }
                    if (this.status.isComplete()) {
                        if (Preferences.instance().getBoolean("queue.download.changePermissions")) {
                            log.info("Updating permissions");
                            Permission perm = null;
                            if (Preferences.instance().getBoolean("queue.download.permissions.useDefault")) {
                                perm = new Permission(Preferences.instance().getProperty("queue.download.permissions.default"));
                            }
                            else {
                                perm = this.attributes.getPermission();
                            }
                            if (!perm.isUndefined()) {
                                this.getLocal().setPermission(perm);
                            }
                        }
                    }
                    if (Preferences.instance().getBoolean("queue.download.preserveDate")) {
                        if (!this.attributes.isUndefined()) {
                            this.getLocal().setLastModified(this.attributes.getTimestamp().getTime());
                        }
                    }
                }
                if (this.attributes.isDirectory()) {
                    this.getLocal().mkdirs();
                }
                session.log(Message.STOP, "Idle");
            }
            catch (FTPException e) {
                session.log(Message.ERROR, "FTP Error: (" + this.getName() + ") " + e.getMessage());
            }
            catch (IOException e) {
                session.log(Message.ERROR, "IO Error: " + e.getMessage());
                session.close();
            }
        }
    }

    private void downloadBinary() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            if (this.status.isResume()) {
                this.status.setCurrent(this.getLocal().getSize());
            }
            out = new FileOutputStream(this.getLocal(), this.status.isResume());
            if (out == null) {
                throw new IOException("Unable to buffer data");
            }
            in = session.FTP.get(this.getAbsolute(), this.status.isResume() ? this.getLocal().getSize() : 0);
            if (in == null) {
                throw new IOException("Unable opening data stream");
            }
            this.download(in, out);
            if (this.status.isComplete()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.validateTransfer();
            }
            if (this.status.isCanceled()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.abor();
            }
            session.log(Message.STOP, "Idle");
        }
        finally {
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
                e.printStackTrace();
            }
        }
    }

    private void downloadASCII() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            String lineSeparator = System.getProperty("line.separator"); //default value
            if (Preferences.instance().getProperty("ftp.line.separator").equals("unix")) {
                lineSeparator = UNIX_LINE_SEPARATOR;
            }
            else if (Preferences.instance().getProperty("ftp.line.separator").equals("mac")) {
                lineSeparator = MAC_LINE_SEPARATOR;
            }
            else if (Preferences.instance().getProperty("ftp.line.separator").equals("win")) {
                lineSeparator = DOS_LINE_SEPARATOR;
            }
            session.FTP.setTransferType(FTPTransferType.ASCII);
            out = new FromNetASCIIOutputStream(new FileOutputStream(this.getLocal(), false),
                    lineSeparator);
            if (out == null) {
                throw new IOException("Unable to buffer data");
            }
            in = new FromNetASCIIInputStream(session.FTP.get(this.getAbsolute(), 0),
                    lineSeparator);
            if (in == null) {
                throw new IOException("Unable opening data stream");
            }
            this.download(in, out);
            if (this.status.isComplete()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.validateTransfer();
            }
            if (this.status.isCanceled()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.abor();
            }
            session.log(Message.STOP, "Idle");
        }
        finally {
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
                e.printStackTrace();
            }
        }
    }

    public void upload() {
        synchronized (session) {
            log.debug("upload:" + this.toString());
            try {
                if (this.attributes.isFile()) {
                    session.check();
                    this.attributes.setSize(this.getLocal().getSize());
                    if (Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
                        if (this.getExtension() != null && Preferences.instance().getProperty("ftp.transfermode.ascii.extensions").indexOf(this.getExtension()) != -1) {
                            this.uploadASCII();
                        }
                        else {
                            this.uploadBinary();
                        }
                    }
                    else if (Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
                        this.uploadBinary();
                    }
                    else if (Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
                        this.uploadASCII();
                    }
                    else {
                        throw new FTPException("Transfer mode not set");
                    }
                    if (Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                        try {
                            if (Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                                Permission perm = new Permission(Preferences.instance().getProperty("queue.upload.permissions.default"));
                                session.FTP.site("CHMOD " + perm.getOctalCode() + " " + this.getAbsolute());
                            }
                            else {
                                Permission perm = this.getLocal().getPermission();
                                if (!perm.isUndefined()) {
                                    session.FTP.site("CHMOD " + perm.getOctalCode() + " " + this.getAbsolute());
                                }
                            }
                        }
                        catch (FTPException e) {
                            log.warn(e.getMessage());
                        }
                    }
                    if (Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                        try {
                            session.FTP.setmodtime(this.getLocal().getTimestamp(), this.getAbsolute());
                        }
                        catch (FTPException e) {
                            log.warn(e.getMessage());
                            if(!this.getLocal().getParent().equals(NSPathUtilities.temporaryDirectory())) {
                                this.getLocal().setLastModified(session.FTP.modtime(this.getAbsolute()).getTime());
                            }
                        }
                    }
                }
                if (this.attributes.isDirectory()) {
                    this.mkdir();
                }
                this.getParent().invalidate();
                session.log(Message.STOP, "Idle");
            }
            catch (FTPException e) {
                session.log(Message.ERROR, "FTP Error: (" + this.getName() + ") " + e.getMessage());
            }
            catch (IOException e) {
                session.log(Message.ERROR, "IO Error: " + e.getMessage());
                session.close();
            }
        }
    }

    private void uploadBinary() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            if (this.status.isResume()) {
                try {
                    this.status.setCurrent(this.session.FTP.size(this.getAbsolute()));
                }
                catch (FTPException e) {
                    log.error(e.getMessage());
                    //ignore; SIZE command not recognized
                    this.status.setCurrent(0);
                }
            }
            in = new FileInputStream(this.getLocal());
            if (in == null) {
                throw new IOException("Unable to buffer data");
            }
            out = session.FTP.put(this.getAbsolute(), this.status.isResume());
            if (out == null) {
                throw new IOException("Unable opening data stream");
            }
            this.upload(out, in);
            if (this.status.isComplete()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.validateTransfer();
            }
            if (status.isCanceled()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.abor();
            }
            session.log(Message.STOP, "Idle");
        }
        finally {
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
                e.printStackTrace();
            }
        }
    }

    private void uploadASCII() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.ASCII);
            if (this.status.isResume()) {
                try {
                    this.status.setCurrent(this.session.FTP.size(this.getAbsolute()));
                }
                catch (FTPException e) {
                    log.error(e.getMessage());
                    //ignore; SIZE command not recognized
                    this.status.setCurrent(0);
                }
            }
            in = new ToNetASCIIInputStream(new FileInputStream(this.getLocal()));
            if (in == null) {
                throw new IOException("Unable to buffer data");
            }
            out = new ToNetASCIIOutputStream(session.FTP.put(this.getAbsolute(),
                    this.status.isResume()));
            if (out == null) {
                throw new IOException("Unable opening data stream");
            }
            this.upload(out, in);
            if (this.status.isComplete()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.validateTransfer();
            }
            if (status.isCanceled()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.abor();
            }
            session.log(Message.STOP, "Idle");
        }
        finally {
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
                e.printStackTrace();
            }
        }
    }
}
	