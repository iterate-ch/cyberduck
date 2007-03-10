package ch.cyberduck.core.ftp;

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

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

import ch.cyberduck.core.*;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSPathUtilities;

import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.io.ToNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
        protected Path create(Session session) {
            return new FTPPath((FTPSession) session);
        }

        protected Path create(Session session, String path) {
            return new FTPPath((FTPSession) session, path);
        }

        protected Path create(Session session, String parent, String name) {
            return new FTPPath((FTPSession) session, parent, name);
        }

        protected Path create(Session session, String path, Local file) {
            return new FTPPath((FTPSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new FTPPath((FTPSession) session, dict);
        }
    }

    private final FTPSession session;

    protected FTPPath(FTPSession s) {
        this.session = s;
    }

    protected FTPPath(FTPSession s, String parent, String name) {
        super(parent, name);
        this.session = s;
    }

    protected FTPPath(FTPSession s, String path) {
        super(path);
        this.session = s;
    }

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

    public AttributedList list(Comparator comparator, PathFilter filter) {
        if(!this.isCached() || this.cache().attributes().isDirty()) {
            synchronized(session) {
                AttributedList childs = new AttributedList();
                try {
                    session.check();
                    session.message(NSBundle.localizedString("Listing directory", "Status", "") + " " + this.getAbsolute());
                    session.FTP.setTransferType(FTPTransferType.ASCII);
                    this.cwdir();
                    String[] lines = session.FTP.dir(this.session.getEncoding());
                    // Read line for line if the connection hasn't been interrupted since
                    for(int i = 0; i < lines.length; i++) {
                        Path p = session.parser.parseFTPEntry(this, lines[i]);
                        if(p != null) {
                            childs.add(p);
                        }
                    }
                }
                catch(FTPException e) {
                    childs.attributes().setReadable(false);
                    this.error("Listing directory failed", e);
                }
                catch(IOException e) {
                    this.error("Connection failed", e);
                    session.interrupt();
                }
                finally {
                    session.cache().put(this, childs);
                    session.fireActivityStoppedEvent();
                }
            }
        }
        return session.cache().get(this, comparator, filter);
    }

    public void cwdir() throws IOException {
        synchronized(session) {
            session.FTP.chdir(this.getAbsolute());
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
                this.getParent().cwdir();
                session.FTP.mkdir(this.getName());
                session.cache().put(this, new AttributedList());
                this.getParent().invalidate();
            }
            catch(FTPException e) {
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

    public void rename(String filename) {
        synchronized(session) {
            log.debug("rename:" + filename);
            try {
                session.check();
                session.message(NSBundle.localizedString("Renaming to", "Status", "") + " " + filename + " (" + this.getName() + ")");
                this.getParent().cwdir();
                session.FTP.rename(this.getName(), filename);
                this.getParent().invalidate();
                this.setPath(filename);
                this.getParent().invalidate();
            }
            catch(FTPException e) {
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

    public void readAttributes() {
        synchronized(session) {
            if(this.attributes.isFile() && this.attributes.isMissing()) {
                try {
                    session.check();
                    session.message(NSBundle.localizedString("Getting timestamp of", "Status", "") + " " + this.getName());
                    try {
                        this.attributes.setModificationDate(session.FTP.modtime(this.getAbsolute(),
                                this.getHost().getTimezone()));
                    }
                    catch(FTPException e) {
                        log.warn("Cannot read timestamp:" + e.getMessage());
                    }
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.AUTO.toString())) {
                        if(this.getTextFiletypePattern().matcher(this.getName()).matches()) {
                            session.FTP.setTransferType(FTPTransferType.ASCII);
                        }
                        else {
                            session.FTP.setTransferType(FTPTransferType.BINARY);
                        }
                    }
                    else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                            FTPTransferType.BINARY.toString())) {
                        session.FTP.setTransferType(FTPTransferType.BINARY);
                    }
                    else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                            FTPTransferType.ASCII.toString())) {
                        session.FTP.setTransferType(FTPTransferType.ASCII);
                    }
                    else {
                        throw new FTPException("Transfer type not set");
                    }
                    session.message(NSBundle.localizedString("Getting size of", "Status", "") + " " + this.getName());
                    try {
                        this.attributes.setSize(session.FTP.size(this.getAbsolute()));
                    }
                    catch(FTPException e) {
                        log.warn("Cannot read size:" + e.getMessage());
                    }
                }
                catch(FTPException e) {
                    this.error("Cannot get file attributes", e);
                }
                catch(IOException e) {
                    this.error("Connection failed", e);
                    session.interrupt();
                }
            }
        }
    }

    public void delete() {
        synchronized(session) {
            log.debug("delete:" + this.toString());
            try {
                session.check();
                if(this.attributes.isFile() || this.attributes.isSymbolicLink()) {
                    this.getParent().cwdir();
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.FTP.delete(this.getName());
                }
                else if(this.attributes.isDirectory()) {
                    this.cwdir();
                    for(Iterator iter = this.list().iterator(); iter.hasNext();) {
                        if(!session.isConnected()) {
                            break;
                        }
                        Path file = (Path) iter.next();
                        if(file.attributes.isFile()) {
                            session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + file.getName());
                            session.FTP.delete(file.getName());
                        }
                        else if(file.attributes.isDirectory()) {
                            file.delete();
                        }
                    }
                    this.getParent().cwdir();
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.FTP.rmdir(this.getName());
                }
                this.getParent().invalidate();
            }
            catch(FTPException e) {
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

    public void changeOwner(String owner, boolean recursive) {
        synchronized(session) {
            String command = "chown";
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing owner to", "Status", "") + " " + this.attributes.getOwner() + " (" + this.getName() + ")");
                this.getParent().cwdir();
                if(this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.FTP.site(command + " " + owner + " " + this.getName());
                }
                else if(this.attributes.isDirectory()) {
                    session.FTP.site(command + " " + owner + " " + this.getName());
                    if(recursive) {
                        for(Iterator iter = this.list().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((Path) iter.next()).changeOwner(owner, recursive);
                        }
                    }
                }
                this.getParent().invalidate();
            }
            catch(FTPException e) {
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

    public void changeGroup(String group, boolean recursive) {
        synchronized(session) {
            String command = "chgrp";
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing group to", "Status", "") + " " + this.attributes.getGroup() + " (" + this.getName() + ")");
                this.getParent().cwdir();
                if(this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.FTP.site(command + " " + group + " " + this.getName());
                }
                else if(this.attributes.isDirectory()) {
                    session.FTP.site(command + " " + group + " " + this.getName());
                    if(recursive) {
                        for(Iterator iter = this.list().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((Path) iter.next()).changeGroup(group, recursive);
                        }
                    }
                }
                this.getParent().invalidate();
            }
            catch(FTPException e) {
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

    public void changePermissions(Permission perm, boolean recursive) {
        synchronized(session) {
            log.debug("changePermissions:" + perm);
            final String command = "CHMOD";
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing permission to", "Status", "") + " " + perm.getOctalString() + " (" + this.getName() + ")");
                this.getParent().cwdir();
                if(this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.FTP.site(command + " " + perm.getOctalString() + " " + this.getName());
                }
                else if(this.attributes.isDirectory()) {
                    session.FTP.site(command + " " + perm.getOctalString() + " " + this.getName());
                    if(recursive) {
                        for(Iterator iter = this.list().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((Path) iter.next()).changePermissions(perm, recursive);
                        }
                    }
                }
                this.getParent().invalidate();
            }
            catch(FTPException e) {
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

    public void download() {
        synchronized(session) {
            log.debug("download:" + this.toString());
            try {
                this.status.reset();
                if(this.attributes.isFile()) {
                    session.check();
                    this.getParent().cwdir();
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.AUTO.toString())) {
                        if(this.getTextFiletypePattern().matcher(this.getName()).matches()) {
                            this.downloadASCII();
                        }
                        else {
                            this.downloadBinary();
                        }
                    }
                    else
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.BINARY.toString())) {
                        this.downloadBinary();
                    }
                    else
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.ASCII.toString())) {
                        this.downloadASCII();
                    }
                    else {
                        throw new FTPException("Transfer mode not set");
                    }
                }
                if(this.attributes.isDirectory()) {
                    this.getLocal().mkdirs();
                }
                if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
                    log.info("Updating permissions");
                    Permission perm;
                    if(Preferences.instance().getBoolean("queue.download.permissions.useDefault")
                            && this.attributes.isFile()) {
                        perm = new Permission(Preferences.instance().getInteger("queue.download.permissions.file.default"));
                    }
                    else {
                        perm = this.attributes.getPermission();
                    }
                    if(null != perm) {
                        if(this.attributes.isDirectory()) {
                            perm.getOwnerPermissions()[Permission.WRITE] = true;
                            perm.getOwnerPermissions()[Permission.EXECUTE] = true;
                        }
                        this.getLocal().setPermission(perm);
                    }
                }
                if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
                    log.info("Updating timestamp");
                    if(-1 == this.attributes.getModificationDate()) {
                        // First try to read the timestamp using MDTM
                        this.readAttributes();
                    }
                    if(-1 == this.attributes.getModificationDate()) {
                        if(this.exists()) {
                            // Read the timestamp from the directory listing
                            List l = this.getParent().list();
                            this.attributes.setModificationDate(((Path)l.get(l.indexOf(this))).attributes.getModificationDate());
                        }
                    }
                    if(this.attributes.getModificationDate() != -1) {
                        long timestamp = this.attributes.getModificationDate();
                        this.getLocal().setLastModified(timestamp/*, this.getHost().getTimezone()*/);
                    }
                }
            }
            catch(FTPException e) {
                this.error("Download failed", e);
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

    private void downloadBinary() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            if(this.status.isResume()) {
                this.status.setCurrent((long) this.getLocal().getSize());
            }
            out = new FileOutputStream(this.getLocal(), this.status.isResume());
            if(null == out) {
                throw new IOException("Unable to buffer data");
            }
            in = session.FTP.get(this.getName(), this.status.isResume() ? (long) this.getLocal().getSize() : 0);
            if(null == in) {
                throw new IOException("Unable opening data stream");
            }
            this.download(in, out);
            if(this.status.isComplete()) {
                if(in != null) {
                    in.close();
                    in = null;
                }
                if(out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.validateTransfer();
            }
            if(this.status.isCanceled()) {
                if(in != null) {
                    in.close();
                    in = null;
                }
                if(out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.abor();
            }
        }
        finally {
            try {
                if(in != null) {
                    in.close();
                }
                if(out != null) {
                    out.close();
                }
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void downloadASCII() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            String lineSeparator = System.getProperty("line.separator"); //default value
            if(Preferences.instance().getProperty("ftp.line.separator").equals("unix")) {
                lineSeparator = UNIX_LINE_SEPARATOR;
            }
            else if(Preferences.instance().getProperty("ftp.line.separator").equals("mac")) {
                lineSeparator = MAC_LINE_SEPARATOR;
            }
            else if(Preferences.instance().getProperty("ftp.line.separator").equals("win")) {
                lineSeparator = DOS_LINE_SEPARATOR;
            }
            session.FTP.setTransferType(FTPTransferType.ASCII);
            out = new FromNetASCIIOutputStream(new FileOutputStream(this.getLocal(), false),
                    lineSeparator);
            if(null == out) {
                throw new IOException("Unable to buffer data");
            }
            in = new FromNetASCIIInputStream(session.FTP.get(this.getName(), 0),
                    lineSeparator);
            if(null == in) {
                throw new IOException("Unable opening data stream");
            }
            this.download(in, out);
            if(this.status.isComplete()) {
                if(in != null) {
                    in.close();
                    in = null;
                }
                if(out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.validateTransfer();
            }
            if(this.status.isCanceled()) {
                if(in != null) {
                    in.close();
                    in = null;
                }
                if(out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.abor();
            }
        }
        finally {
            try {
                if(in != null) {
                    in.close();
                }
                if(out != null) {
                    out.close();
                }
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void upload() {
        synchronized(session) {
            log.debug("upload:" + this.toString());
            try {
                this.status.reset();
                if(this.attributes.isFile()) {
                    session.check();
                    this.getParent().cwdir();
                    this.attributes.setSize(this.getLocal().getSize());
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.AUTO.toString())) {
                        if(this.getTextFiletypePattern().matcher(this.getName()).matches()) {
                            this.uploadASCII();
                        }
                        else {
                            this.uploadBinary();
                        }
                    }
                    else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                            FTPTransferType.BINARY.toString())) {
                        this.uploadBinary();
                    }
                    else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                            FTPTransferType.ASCII.toString())) {
                        this.uploadASCII();
                    }
                    else {
                        throw new FTPException("Transfer mode not set");
                    }
                }
                if(this.attributes.isDirectory()) {
                    this.mkdir();
                }
                if(session.isConnected()) {
                    if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                        log.info("Updating permissions");
                        if(null == this.attributes.getPermission()) {
                            if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                                if(this.attributes.isFile()) {
                                    this.attributes.setPermission(new Permission(
                                            Preferences.instance().getInteger("queue.upload.permissions.file.default"))
                                    );
                                }
                                if(this.attributes.isDirectory()) {
                                    this.attributes.setPermission(new Permission(
                                            Preferences.instance().getInteger("queue.upload.permissions.folder.default"))
                                    );
                                }
                            }
                            else {
                                this.attributes.setPermission(this.getLocal().getPermission());
                            }
                        }
                        try {
                            if(null != this.attributes.getPermission()) {
                                session.FTP.setPermissions(this.attributes.getPermission().getOctalString(),
                                        this.getName());
                            }
                        }
                        catch(FTPException ignore) {
                            //CHMOD not supported; ignore
                            log.warn(ignore.getMessage());
                        }
                    }
                    if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                        log.info("Updating timestamp");
                        try {
                            session.FTP.utime(this.getLocal().getModificationDate(),
                                    this.getLocal().getCreationDate(), this.getName(), this.getHost().getTimezone());
                        }
                        catch(FTPException e) {
                            if(Preferences.instance().getBoolean("queue.upload.preserveDate.fallback")) {
                                if(!this.getLocal().getParent().equals(NSPathUtilities.temporaryDirectory())) {
                                    if(-1 == this.attributes.getModificationDate()) {
                                        this.readAttributes();
                                    }
                                    if(this.attributes.getModificationDate() != -1) {
                                        this.getLocal().setLastModified(this.attributes.getModificationDate()/*,
                                                this.getHost().getTimezone()*/);
                                    }
                                }
                            }
                        }
                    }
                }
                this.getParent().invalidate();
            }
            catch(FTPException e) {
                this.error("Upload failed", e);
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

    private void uploadBinary() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            if(this.status.isResume()) {
                try {
                    this.status.setCurrent(this.session.FTP.size(this.getName()));
                }
                catch(FTPException e) {
                    log.error(e.getMessage());
                    //ignore; SIZE command not recognized
                    this.status.setCurrent(0);
                }
            }
            in = new FileInputStream(this.getLocal());
            if(null == in) {
                throw new IOException("Unable to buffer data");
            }
            out = session.FTP.put(this.getName(), this.status.isResume());
            if(null == out) {
                throw new IOException("Unable opening data stream");
            }
            this.upload(out, in);
            if(this.status.isComplete()) {
                if(in != null) {
                    in.close();
                    in = null;
                }
                if(out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.validateTransfer();
            }
            if(status.isCanceled()) {
                if(in != null) {
                    in.close();
                    in = null;
                }
                if(out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.abor();
            }
        }
        finally {
            try {
                if(in != null) {
                    in.close();
                }
                if(out != null) {
                    out.close();
                }
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void uploadASCII() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.ASCII);
            if(this.status.isResume()) {
                try {
                    this.status.setCurrent(this.session.FTP.size(this.getName()));
                }
                catch(FTPException e) {
                    log.error(e.getMessage());
                    //ignore; SIZE command not recognized
                    this.status.setCurrent(0);
                }
            }
            in = new ToNetASCIIInputStream(new FileInputStream(this.getLocal()));
            if(null == in) {
                throw new IOException("Unable to buffer data");
            }
            out = new ToNetASCIIOutputStream(session.FTP.put(this.getName(),
                    this.status.isResume()));
            if(null == out) {
                throw new IOException("Unable opening data stream");
            }
            this.upload(out, in);
            if(this.status.isComplete()) {
                if(in != null) {
                    in.close();
                    in = null;
                }
                if(out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.validateTransfer();
            }
            if(status.isCanceled()) {
                if(in != null) {
                    in.close();
                    in = null;
                }
                if(out != null) {
                    out.close();
                    out = null;
                }
                session.FTP.abor();
            }
        }
        finally {
            try {
                if(in != null) {
                    in.close();
                }
                if(out != null) {
                    out.close();
                }
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
	