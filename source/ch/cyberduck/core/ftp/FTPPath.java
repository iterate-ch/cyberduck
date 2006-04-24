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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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

    private final FTPSession session;

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

    public AttributedList list(Comparator comparator, Filter filter) {
        synchronized (session) {
            if (!session.cache().containsKey(this) || session.cache().isInvalid(this)) {
                AttributedList files = new AttributedList();
                session.message(NSBundle.localizedString("Listing directory", "Status", "") + " " + this.getAbsolute());
                try {
                    session.check();
                    session.FTP.setTransferType(FTPTransferType.ASCII);
                    session.FTP.chdir(this.getAbsolute());
                    String[] lines = session.FTP.dir(this.session.getHost().getEncoding());
                    for (int i = 0; i < lines.length; i++) {
                        Path p = session.parser.parseFTPEntry(this, lines[i]);
                        if (p != null) {
                            files.add(p);
                        }
                    }
                    session.cache().put(this, files);
                }
                catch (FTPException e) {
                    session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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
                session.message(NSBundle.localizedString("Make directory", "Status", "") + " " + this.getName());
                session.FTP.mkdir(this.getAbsolute());
                session.cache().put(this, new AttributedList());//todo
                this.getParent().invalidate();
            }
            catch (FTPException e) {
                session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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
            log.debug("rename:" + filename);
            try {
                session.check();
                session.message("Renaming " + this.getName() + " to " + filename); //todo localize
                session.FTP.rename(this.getAbsolute(), filename);
                this.getParent().invalidate();
                this.setPath(filename);
                //this.getParent().invalidate();
            }
            catch (FTPException e) {
                session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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
                        try {
                            this.attributes.setTimestamp(session.FTP.modtime(this.getAbsolute()));
                        }
                        catch (FTPException e) {
                            log.warn(e.getMessage());
                        }
                        if (Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
                            if (this.isASCIIType()) {
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
                        session.message(NSBundle.localizedString("Getting size of", "Status", "") + " " + this.getName());
                        this.attributes.setSize(session.FTP.size(this.getAbsolute()));
                    }
                    catch (FTPException e) {
                        log.error(e.getMessage());
                        //ignore
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
                    session.FTP.chdir(this.getParent().getAbsolute());
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.FTP.delete(this.getName());
                }
                else if (this.attributes.isDirectory() && !this.attributes.isSymbolicLink()) {
                    List files = this.list();
                    if (files != null && files.size() > 0) {
                        for (Iterator iter = files.iterator(); iter.hasNext();) {
                            Path file = (Path) iter.next();
                            if (file.attributes.isFile()) {
                                session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                                session.FTP.delete(file.getName());
                            }
                            else if (file.attributes.isDirectory()) {
                                file.delete();
                            }
                        }
                    }
                    session.FTP.chdir(this.getParent().getAbsolute());
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.FTP.rmdir(this.getName());
                }
                this.getParent().invalidate();
            }
            catch (FTPException e) {
                session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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
            String command = "chown";
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.message("Changing owner to " + this.attributes.getOwner() + " on " + this.getName()); //todo localize
                    session.FTP.site(command + " " + owner + " " + this.getAbsolute());
                }
                else if (this.attributes.isDirectory()) {
                    session.message("Changing owner to " + this.attributes.getOwner() + " on " + this.getName()); //todo localize
                    session.FTP.site(command + " " + owner + " " + this.getAbsolute());
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
            catch (FTPException e) {
                session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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
            String command = "chgrp";
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.message("Changing group to " + this.attributes.getGroup() + " on " + this.getName()); //todo localize
                    session.FTP.site(command + " " + group + " " + this.getAbsolute());
                }
                else if (this.attributes.isDirectory()) {
                    session.message("Changing group to " + this.attributes.getGroup() + " on " + this.getName()); //todo localize
                    session.FTP.site(command + " " + group + " " + this.getAbsolute());
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
            catch (FTPException e) {
                session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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
            log.debug("changePermissions:" + perm);
            String command = "chmod";
            try {
                session.check();
                if (this.attributes.isFile() && !this.attributes.isSymbolicLink()) {
                    session.message("Changing permission to " + perm.getOctalCode() + " on " + this.getName()); //todo localize
                    session.FTP.site(command + " " + perm.getOctalCode() + " " + this.getAbsolute());
                }
                else if (this.attributes.isDirectory()) {
                    session.message("Changing permission to " + perm.getOctalCode() + " on " + this.getName()); //todo localize
                    session.FTP.site(command + " " + perm.getOctalCode() + " " + this.getAbsolute());
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
            catch (FTPException e) {
                session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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
            try {
                if (this.attributes.isFile()) {
                    session.check();
                    if (Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
                        if (this.isASCIIType()) {
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
                }
                if (this.attributes.isDirectory()) {
                    this.getLocal().mkdirs();
                }
                if (Preferences.instance().getBoolean("queue.download.changePermissions")) {
                    log.info("Updating permissions");
                    Permission perm;
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
                    if (this.attributes.getTimestamp() != null) {
                        log.info("Updating timestamp");
                        this.getLocal().setLastModified(this.attributes.getTimestamp().getTime());
                    }
                }
            }
            catch (FTPException e) {
                session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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

    private boolean isASCIIType() {
        if (this.getExtension() != null) {
            StringTokenizer asciiTypes = new StringTokenizer(Preferences.instance().getProperty("ftp.transfermode.ascii.extensions"), " ");
            while (asciiTypes.hasMoreTokens()) {
                if (asciiTypes.nextToken().equalsIgnoreCase(this.getExtension())) {
                    return true;
                }
            }
        }
        return false;
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
        }
        finally {
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
        }
        finally {
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
                        if (this.isASCIIType()) {
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
                }
                if (this.attributes.isDirectory()) {
                    this.mkdir();
                }
                if (Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    try {
                        if (this.attributes.isFile()
                                && Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                            Permission perm = new Permission(Preferences.instance().getProperty("queue.upload.permissions.default"));
                            session.FTP.setPermissions(perm.getOctalCode(), this.getAbsolute());
                        }
                        else {
                            Permission perm = this.getLocal().getPermission();
                            if (!perm.isUndefined()) {
                                session.FTP.setPermissions(perm.getOctalCode(), this.getAbsolute());
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
                        try {
                            if (Preferences.instance().getBoolean("queue.upload.preserveDate.fallback")) {
                                if (!this.getLocal().getParent().equals(NSPathUtilities.temporaryDirectory())) {
                                    this.getLocal().setLastModified(session.FTP.modtime(this.getAbsolute()).getTime());
                                }
                            }
                        }
                        catch (FTPException ignore) {
                            //MDTM not supported
                            log.warn(ignore.getMessage());
                        }
                    }
                }
                this.getParent().invalidate();
            }
            catch (FTPException e) {
                session.error(new FTPException(e.getMessage()+" (" + this.getName() + ")", e.getReplyCode()));
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
        }
        finally {
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
        }
        finally {
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
            }
        }
    }
}
	