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

import java.io.*;
import java.util.List;

import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.io.ToNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

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
            return new FTPPath((FTPSession)session, parent, name);
        }

        protected Path create(Session session) {
            return new FTPPath((FTPSession)session);
        }

        protected Path create(Session session, String path) {
            return new FTPPath((FTPSession)session, path);
        }

        protected Path create(Session session, String path, Local file) {
            return new FTPPath((FTPSession)session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new FTPPath((FTPSession)session, dict);
        }
    }

    private FTPSession session;

    /**
     * @param session The connection to work with for regular file operations
     * @param parent  The parent directory relative to this file
     * @param name    The filename of this path
     */
    private FTPPath(FTPSession s, String parent, String name) {
        super(parent, name);
        this.session = s;
    }

    private FTPPath(FTPSession s, String path) {
        super(path);
        this.session = s;
    }

    private FTPPath(FTPSession s) {
        super();
        this.session = s;
    }

    /**
     * @param session The connection to work with for regular file operations
     * @param parent  The parent directory relative to this file
     * @param file    The corresponding local file to the remote path
     */
    private FTPPath(FTPSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    private FTPPath(FTPSession s, NSDictionary dict) {
        super(dict);
        this.session = s;
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
        List files = session.cache().get(this.getAbsolute());
        session.addPathToHistory(this);
        if (refresh || files.size() == 0) {
            files.clear();
            session.log("Listing " + this.getName(), Message.PROGRESS);
            try {
                session.check();
                session.FTP.setTransferType(FTPTransferType.ASCII);
                session.FTP.chdir(this.getAbsolute());
                FTPFileEntryParser parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(session.host.getIdentification());
                String[] lines = session.FTP.dir();
                for (int i = 0; i < lines.length; i++) {
                    Path p = parser.parseFTPEntry(this, lines[i]);
                    if (p != null) {
                        String filename = p.getName();
                        if (!(filename.equals(".") || filename.equals(".."))) {
                            if (!(filename.charAt(0) == '.') || showHidden) {
                                files.add(p);
                            }
                        }
                    }
                }

                this.setCache(files);
            }
            catch (FTPException e) {
                session.log("FTP Error: " + e.getMessage(), Message.ERROR);
            }
            catch (IOException e) {
                session.log("IO Error: " + e.getMessage(), Message.ERROR);
            }
            finally {
                session.log("Idle", Message.STOP);
            }
        }
        session.callObservers(this);
        return files;
    }
	
    public void cwdir() {
        try {
            session.check();
            session.FTP.chdir(this.getAbsolute());
        }
        catch (FTPException e) {
            session.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void mkdir(boolean recursive) {
        log.debug("mkdir:" + this.getName());
        try {
            if (recursive) {
                if (!this.getParent().exists()) {
                    this.getParent().mkdir(recursive);
                }
            }
            session.check();
            session.log("Make directory " + this.getName(), Message.PROGRESS);
            session.FTP.mkdir(this.getAbsolute());
            this.getParent().invalidate();
        }
        catch (FTPException e) {
            session.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void rename(String filename) {
        log.debug("rename:" + filename);
        try {
            session.check();
            session.log("Renaming " + this.getName() + " to " + filename, Message.PROGRESS);
            session.FTP.rename(this.getAbsolute(), filename);
            this.setPath(filename);
//			this.setPath(this.getParent().getAbsolute(), filename);
            this.getParent().invalidate();
        }
        catch (FTPException e) {
            session.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void delete() {
        log.debug("delete:" + this.toString());
        try {
            session.check();
            if (this.isFile()) {
                session.log("Deleting " + this.getName(), Message.PROGRESS);
                session.FTP.delete(this.getName());
            }
            else if (this.isDirectory()) {
                session.FTP.chdir(this.getAbsolute());
                List files = this.list(true, true);
                java.util.Iterator iterator = files.iterator();
                Path file = null;
                while (iterator.hasNext()) {
                    file = (Path)iterator.next();
                    if (file.isDirectory()) {
                        file.delete();
                    }
                    if (file.isFile()) {
                        session.log("Deleting " + this.getName(), Message.PROGRESS);
                        session.FTP.delete(file.getName());
                    }
                }
                session.FTP.cdup();
                session.log("Deleting " + this.getName(), Message.PROGRESS);
                session.FTP.rmdir(this.getName());
            }
            this.getParent().invalidate();
        }
        catch (FTPException e) {
            session.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void changePermissions(Permission perm, boolean recursive) {
        log.debug("changePermissions:" + perm);
        String command = "chmod";
        try {
            session.check();
            //@todo support recursion
            session.FTP.site(command + " " + perm.getOctalCode() + " " + this.getAbsolute());
        }
        catch (FTPException e) {
            session.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    public void download() {
        try {
            log.debug("download:" + this.toString());
            session.check();
            this.getLocal().getParentFile().mkdirs();
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
                throw new FTPException("Transfer type not set");
            }
            if (Preferences.instance().getProperty("queue.download.changePermissions").equals("true")) {
                Permission perm = this.attributes.getPermission();
                if (!perm.isUndefined()) {
                    this.getLocal().setPermission(perm);
                }
            }
            if (Preferences.instance().getProperty("queue.download.preserveDate").equals("true")) {
                this.getLocal().setLastModified(this.attributes.getTimestamp().getTime());
            }
        }
        catch (FTPException e) {
            session.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    private void downloadBinary() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            this.status.setSize(session.FTP.size(this.getAbsolute()));
            if (this.status.isResume()) {
                this.status.setCurrent(this.getLocal().getTemp().length());
            }
            out = new FileOutputStream(this.getLocal().getTemp(), this.status.isResume());
            if (out == null) {
                throw new IOException("Unable to buffer data");
            }
            in = session.FTP.get(this.getAbsolute(), this.status.isResume() ? this.getLocal().getTemp().length() : 0);
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
        }
        finally {
            session.log("Idle", Message.STOP);
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
        try {
            session.FTP.setTransferType(FTPTransferType.ASCII);
            this.status.setSize(session.FTP.size(this.getAbsolute()));
            if (this.status.isResume()) {
                this.status.setCurrent(this.getLocal().getTemp().length());
            }
            out = new FromNetASCIIOutputStream(new FileOutputStream(this.getLocal().getTemp(),
                    this.status.isResume()),
                    lineSeparator);
            if (out == null) {
                throw new IOException("Unable to buffer data");
            }
            in = new FromNetASCIIInputStream(session.FTP.get(this.getAbsolute(),
                    this.status.isResume() ? this.getLocal().getTemp().length() : 0),
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
        }
        finally {
            session.log("Idle", Message.STOP);
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
        log.debug("upload:" + this.toString());
        try {
            session.check();
            if (!this.getParent().exists()) {
                PathFactory.createPath(session, this.getParent().getParent().getAbsolute(), this.getParent().getName()).mkdir(true);
            }
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
            if (Preferences.instance().getProperty("queue.upload.changePermissions").equals("true")) {
                Permission perm = this.getLocal().getPermission();
                if (!perm.isUndefined()) {
                    this.changePermissions(perm, false);
                }
            }
        }
        catch (FTPException e) {
            session.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            session.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            session.log("Idle", Message.STOP);
        }
    }

    private void uploadBinary() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            this.status.setSize(this.getLocal().length());
            if (this.status.isResume()) {
                this.status.setCurrent(session.FTP.size(this.getAbsolute()));
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
        }
        finally {
            session.log("Idle", Message.STOP);
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
            this.status.setSize(this.getLocal().length());
            if (this.status.isResume()) {
                this.status.setCurrent(session.FTP.size(this.getAbsolute()));
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
        }
        finally {
            session.log("Idle", Message.STOP);
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
	