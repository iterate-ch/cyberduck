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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;

import ch.cyberduck.core.*;
import ch.cyberduck.core.io.*;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

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
        PathFactory.addFactory(Protocol.FTP, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String path, int type) {
            return new FTPPath((FTPSession) session, path, type);
        }

        protected Path create(Session session, String parent, String name, int type) {
            return new FTPPath((FTPSession) session, parent, name, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new FTPPath((FTPSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new FTPPath((FTPSession) session, dict);
        }
    }

    private final FTPSession session;

    protected FTPPath(FTPSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected FTPPath(FTPSession s, String path, int type) {
        super(path, type);
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
                session.FTP.setTransferType(FTPTransferType.ASCII);
                this.cwdir();
                final BufferedReader reader = session.FTP.dir(this.session.getEncoding());
                if(null == reader) {
                    // This is an empty directory
                    return childs;
                }
                String line = null;
                FTPFileEntryParser parser = session.getFileParser();
                while((line = parser.readNextEntry(reader)) != null) {
                    session.log(line);
                    FTPFile f = parser.parseFTPEntry(line);
                    if(null == f || f.getName().equals(".") || f.getName().equals("..")) {
                        continue;
                    }
                    Path p = new FTPPath(session, this.getAbsolute(), f.getName(), Path.FILE_TYPE);
                    p.setParent(this);
                    switch(f.getType()) {
                        case FTPFile.SYMBOLIC_LINK_TYPE:
                            p.setSymbolicLinkPath(this.getAbsolute(), f.getLink());
                            p.attributes.setType(Path.SYMBOLIC_LINK_TYPE);
                            break;
                        case FTPFile.DIRECTORY_TYPE:
                            p.attributes.setType(Path.DIRECTORY_TYPE);
                            break;
                    }
                    p.attributes.setSize(f.getSize());
                    p.attributes.setOwner(f.getUser());
                    p.attributes.setGroup(f.getGroup());
                    if(session.isPermissionSupported(parser)) {
                        p.attributes.setPermission(new Permission(
                                new boolean[][]{
                                        {f.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION),
                                                f.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION),
                                                f.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION)
                                        },
                                        {f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION),
                                                f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION),
                                                f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION)
                                        },
                                        {f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION),
                                                f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION),
                                                f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION)
                                        }
                                }
                        ));
                    }
                    final Calendar timestamp = f.getTimestamp();
                    if(timestamp != null) {
                        p.attributes.setModificationDate(timestamp.getTimeInMillis());
                    }
                    p.getStatus().setSkipped(this.getStatus().isSkipped());
                    childs.add(p);
                }
                session.FTP.finishDir();
                boolean dirChanged = false;
                for(Iterator iter = childs.iterator(); iter.hasNext();) {
                    Path p = (Path) iter.next();
                    if(p.attributes.getType() == Path.SYMBOLIC_LINK_TYPE) {
                        try {
                            p.cwdir();
                            p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                            dirChanged = true;
                        }
                        catch(FTPException e) {
                            p.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                        }
                    }
                }
                if(dirChanged) {
                    this.cwdir();
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
                session.fireActivityStoppedEvent();
            }
            return childs;
        }
    }

    public void cwdir() throws IOException {
        synchronized(session) {
            session.setWorkdir(this);
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
                this.cache().put(this, new AttributedList());
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
                if(attributes.isFile()) {
                    this.error("Cannot rename file", e);
                }
                if(attributes.isDirectory()) {
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

    public void readSize() {
        synchronized(session) {
            try {
                session.check();
                session.message(NSBundle.localizedString("Getting size of", "Status", "") + " " + this.getName());
                if(attributes.isFile()) {
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
                    try {
                        attributes.setSize(session.FTP.size(this.getAbsolute()));
                    }
                    catch(FTPException e) {
                        log.warn("Cannot read size:" + e.getMessage());
                    }
                }
                if(-1 == attributes.getSize()) {
                    // Read the timestamp from the directory listing
                    List l = this.getParent().childs();
                    if(l.contains(this)) {
                        attributes.setSize(((AbstractPath) l.get(l.indexOf(this))).attributes.getSize());
                    }
                }
            }
            catch(FTPException e) {
                this.error("Cannot read file attributes", e);
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

    public void readTimestamp() {
        synchronized(session) {
            try {
                session.check();
                session.message(NSBundle.localizedString("Getting timestamp of", "Status", "") + " " + this.getName());
                try {
                    attributes.setModificationDate(session.FTP.modtime(this.getAbsolute(),
                            this.getHost().getTimezone()));
                }
                catch(FTPException e) {
                    log.warn("Cannot read timestamp:" + e.getMessage());
                }
                if(-1 == attributes.getModificationDate()) {
                    // Read the timestamp from the directory listing
                    List l = this.getParent().childs();
                    if(l.contains(this)) {
                        attributes.setModificationDate(((AbstractPath) l.get(l.indexOf(this))).attributes.getModificationDate());
                    }
                }
            }
            catch(FTPException e) {
                this.error("Cannot read file attributes", e);
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

    public void readPermission() {
        synchronized(session) {
            try {
                session.check();
                session.message(NSBundle.localizedString("Getting permission of", "Status", "") + " " + this.getName());
                // Read the permission from the directory listing
                List l = this.getParent().childs();
                if(l.contains(this)) {
                    attributes.setPermission(((AbstractPath) l.get(l.indexOf(this))).attributes.getPermission());
                }
            }
            catch(FTPException e) {
                this.error("Cannot read file attributes", e);
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
                if(attributes.isFile() || attributes.isSymbolicLink()) {
                    this.getParent().cwdir();
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.FTP.delete(this.getName());
                }
                else if(attributes.isDirectory()) {
                    this.cwdir();
                    for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                        if(!session.isConnected()) {
                            break;
                        }
                        Path file = (Path) iter.next();
                        if(attributes.isFile() || attributes.isSymbolicLink()) {
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
                if(attributes.isFile()) {
                    this.error("Cannot delete file", e);
                }
                if(attributes.isDirectory()) {
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

    public void writeOwner(String owner, boolean recursive) {
        synchronized(session) {
            String command = "chown";
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing owner to", "Status", "") + " " + attributes.getOwner() + " (" + this.getName() + ")");
                this.getParent().cwdir();
                if(attributes.isFile() && !attributes.isSymbolicLink()) {
                    session.FTP.site(command + " " + owner + " " + this.getName());
                }
                else if(attributes.isDirectory()) {
                    session.FTP.site(command + " " + owner + " " + this.getName());
                    if(recursive) {
                        for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((Path) iter.next()).writeOwner(owner, recursive);
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

    public void writeGroup(String group, boolean recursive) {
        synchronized(session) {
            String command = "chgrp";
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing group to", "Status", "") + " " + attributes.getGroup() + " (" + this.getName() + ")");
                this.getParent().cwdir();
                if(attributes.isFile() && !attributes.isSymbolicLink()) {
                    session.FTP.site(command + " " + group + " " + this.getName());
                }
                else if(attributes.isDirectory()) {
                    session.FTP.site(command + " " + group + " " + this.getName());
                    if(recursive) {
                        for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((Path) iter.next()).writeGroup(group, recursive);
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

    public void writePermissions(Permission perm, boolean recursive) {
        synchronized(session) {
            log.debug("changePermissions:" + perm);
            final String command = "CHMOD";
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing permission to", "Status", "") + " " + perm.getOctalString() + " (" + this.getName() + ")");
                this.getParent().cwdir();
                if(attributes.isFile() && !attributes.isSymbolicLink()) {
                    session.FTP.site(command + " " + perm.getOctalString() + " " + this.getName());
                }
                else if(attributes.isDirectory()) {
                    session.FTP.site(command + " " + perm.getOctalString() + " " + this.getName());
                    if(recursive) {
                        for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((AbstractPath) iter.next()).writePermissions(perm, recursive);
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

    public void writeModificationDate(long millis) {
        synchronized(session) {
            try {
                session.FTP.utime(millis,
                        this.getLocal().attributes.getCreationDate(), this.getName(), this.getHost().getTimezone());
            }
            catch(FTPException e) {
                this.error("Cannot change modification date", e);
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

    public void download(final BandwidthThrottle throttle, final StreamListener listener) {
        synchronized(session) {
            log.debug("download:" + this.toString());
            try {
                if(attributes.isFile()) {
                    session.check();
                    this.getParent().cwdir();
                    this.getLocal().touch();
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.AUTO.toString())) {
                        if(this.getTextFiletypePattern().matcher(this.getName()).matches()) {
                            this.downloadASCII(throttle, listener);
                        }
                        else {
                            this.downloadBinary(throttle, listener);
                        }
                    }
                    else
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.BINARY.toString())) {
                        this.downloadBinary(throttle, listener);
                    }
                    else
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.ASCII.toString())) {
                        this.downloadASCII(throttle, listener);
                    }
                    else {
                        throw new FTPException("Transfer mode not set");
                    }
                }
                if(attributes.isDirectory()) {
                    this.getLocal().mkdir(true);
                    this.getStatus().setComplete(true);
                }
                if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
                    log.info("Updating permissions");
                    Permission perm;
                    if(Preferences.instance().getBoolean("queue.download.permissions.useDefault")
                            && attributes.isFile()) {
                        perm = new Permission(Preferences.instance().getInteger("queue.download.permissions.file.default"));
                    }
                    else {
                        perm = attributes.getPermission();
                    }
                    if(null != perm) {
                        if(attributes.isDirectory()) {
                            perm.getOwnerPermissions()[Permission.WRITE] = true;
                            perm.getOwnerPermissions()[Permission.EXECUTE] = true;
                        }
                        this.getLocal().writePermissions(perm, false);
                    }
                }
                if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
                    log.info("Updating timestamp");
                    if(-1 == attributes.getModificationDate()) {
                        // First try to read the timestamp using MDTM
                        this.readTimestamp();
                    }
                    if(attributes.getModificationDate() != -1) {
                        long timestamp = attributes.getModificationDate();
                        this.getLocal().writeModificationDate(timestamp/*, this.getHost().getTimezone()*/);
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

    private void downloadBinary(final BandwidthThrottle throttle, final StreamListener listener) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            if(this.getStatus().isResume()) {
                long transferred = (long) this.getLocal().attributes.getSize();
                this.getStatus().setCurrent(transferred);
            }
            out = new Local.OutputStream(this.getLocal(), this.getStatus().isResume());
            if(null == out) {
                throw new IOException("Unable to buffer data");
            }
            in = session.FTP.get(this.getName(), this.getStatus().isResume() ? (long) this.getLocal().attributes.getSize() : 0);
            if(null == in) {
                throw new IOException("Unable opening data stream");
            }
            this.download(in, out, throttle, listener);
            if(this.getStatus().isComplete()) {
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
            if(this.getStatus().isCanceled()) {
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

    private void downloadASCII(final BandwidthThrottle throttle, final StreamListener listener) throws IOException {
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
            out = new FromNetASCIIOutputStream(new Local.OutputStream(this.getLocal(), false),
                    lineSeparator);
            if(null == out) {
                throw new IOException("Unable to buffer data");
            }
            in = new FromNetASCIIInputStream(session.FTP.get(this.getName(), 0),
                    lineSeparator);
            if(null == in) {
                throw new IOException("Unable opening data stream");
            }
            this.download(in, out, throttle, listener);
            if(this.getStatus().isComplete()) {
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
            if(this.getStatus().isCanceled()) {
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

    public void upload(final BandwidthThrottle throttle, final StreamListener listener) {
        synchronized(session) {
            log.debug("upload:" + this.toString());
            try {
                if(attributes.isFile()) {
                    session.check();
                    this.getParent().cwdir();
                    if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.AUTO.toString())) {
                        if(this.getTextFiletypePattern().matcher(this.getName()).matches()) {
                            this.uploadASCII(throttle, listener);
                        }
                        else {
                            this.uploadBinary(throttle, listener);
                        }
                    }
                    else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                            FTPTransferType.BINARY.toString())) {
                        this.uploadBinary(throttle, listener);
                    }
                    else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                            FTPTransferType.ASCII.toString())) {
                        this.uploadASCII(throttle, listener);
                    }
                    else {
                        throw new FTPException("Transfer mode not set");
                    }
                }
                if(attributes.isDirectory()) {
                    this.mkdir();
                    getStatus().setComplete(true);
                }
                if(session.isConnected()) {
                    if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                        Permission p = attributes.getPermission();
                        if(null == p) {
                            if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                                if(attributes.isFile()) {
                                    p = new Permission(
                                            Preferences.instance().getInteger("queue.upload.permissions.file.default"));
                                }
                                if(attributes.isDirectory()) {
                                    p = new Permission(
                                            Preferences.instance().getInteger("queue.upload.permissions.folder.default"));
                                }
                            }
                            else {
                                p = this.getLocal().attributes.getPermission();
                            }
                        }
                        if(null != p) {
                            try {
                                log.info("Updating permissions:" + p.getOctalString());
                                session.FTP.setPermissions(p.getOctalString(),
                                        this.getName());
                            }
                            catch(FTPException ignore) {
                                //CHMOD not supported; ignore
                                log.warn(ignore.getMessage());
                            }
                        }
                    }
                    if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                        log.info("Updating timestamp");
                        try {
                            session.FTP.utime(this.getLocal().attributes.getModificationDate(),
                                    this.getLocal().attributes.getCreationDate(), this.getName(), this.getHost().getTimezone());
                        }
                        catch(FTPException ignore) {
                            log.warn(ignore.getMessage());
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

    private void uploadBinary(final BandwidthThrottle throttle, final StreamListener listener) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            if(this.getStatus().isResume()) {
                this.getStatus().setCurrent(attributes.getSize());
            }
            in = new Local.InputStream(this.getLocal());
            if(null == in) {
                throw new IOException("Unable to buffer data");
            }
            out = session.FTP.put(this.getName(), this.getStatus().isResume());
            if(null == out) {
                throw new IOException("Unable opening data stream");
            }
            this.upload(out, in, throttle, listener);
            if(this.getStatus().isComplete()) {
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
            if(getStatus().isCanceled()) {
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

    private void uploadASCII(final BandwidthThrottle throttle, final StreamListener listener) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.ASCII);
            if(this.getStatus().isResume()) {
                try {
                    this.getStatus().setCurrent(this.session.FTP.size(this.getName()));
                }
                catch(FTPException e) {
                    log.error(e.getMessage());
                    //ignore; SIZE command not recognized
                    this.getStatus().setCurrent(0);
                }
            }
            in = new ToNetASCIIInputStream(new Local.InputStream(this.getLocal()));
            if(null == in) {
                throw new IOException("Unable to buffer data");
            }
            out = new ToNetASCIIOutputStream(session.FTP.put(this.getName(),
                    this.getStatus().isResume()));
            if(null == out) {
                throw new IOException("Unable opening data stream");
            }
            this.upload(out, in, throttle, listener);
            if(this.getStatus().isComplete()) {
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
            if(getStatus().isCanceled()) {
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