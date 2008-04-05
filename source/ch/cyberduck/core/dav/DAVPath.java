package ch.cyberduck.core.dav;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.properties.AclProperty;
import org.apache.webdav.lib.properties.GetLastModifiedProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @version $Id: $
 */
public class DAVPath extends Path {
    private static Logger log = Logger.getLogger(DAVPath.class);

    static {
        PathFactory.addFactory(Protocol.WEBDAV, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String path, int type) {
            return new DAVPath((DAVSession) session, path, type);
        }

        protected Path create(Session session, String parent, String name, int type) {
            return new DAVPath((DAVSession) session, parent, name, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new DAVPath((DAVSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new DAVPath((DAVSession) session, dict);
        }
    }

    private final DAVSession session;

    protected DAVPath(DAVSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected DAVPath(DAVSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected DAVPath(DAVSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected DAVPath(DAVSession s, NSDictionary dict) {
        super(dict);
        this.session = s;
    }

    public Session getSession() {
        return this.session;
    }

    public void readSize() {
        synchronized(session) {
            try {
                session.check();
                session.message(NSBundle.localizedString("Getting size of", "Status", "") + " " + this.getName());

                this.cwdir();

                session.DAV.setProperties(WebdavResource.BASIC, DepthSupport.DEPTH_1);
                attributes.setSize(session.DAV.getGetContentLength());
            }
            catch(HttpException e) {
                log.warn("Cannot read size:" + e);
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

                this.cwdir();

                session.DAV.setProperties(WebdavResource.BASIC, DepthSupport.DEPTH_1);
                attributes.setModificationDate(session.DAV.getGetLastModified());
            }
            catch(HttpException e) {
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
//        synchronized(session) {
//            try {
//                session.check();
//                session.message(NSBundle.localizedString("Getting permission of", "Status", "") + " " + this.getName());
//
//                this.cwdir();
//
//                final AclProperty acl = session.DAV.aclfindMethod();
//            }
//            catch(HttpException e) {
//                this.error("Cannot read file attributes", e);
//            }
//            catch(IOException e) {
//                this.error("Connection failed", e);
//                session.interrupt();
//            }
//            finally {
//                session.fireActivityStoppedEvent();
//            }
//        }
    }

    public void delete() {
        synchronized(session) {
            log.debug("delete:" + this.toString());
            try {
                session.check();
                session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());

                session.DAV.deleteMethod(this.getAbsolute());
            }
            catch(HttpException e) {
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

    public void cwdir() throws IOException {
        synchronized(session) {
            session.setWorkdir(this);
        }
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
                session.message(NSBundle.localizedString("Listing directory", "Status", "") + " "
                        + this.getAbsolute());

                this.cwdir();

                WebdavResource[] resources = session.DAV.listWebdavResources();

                for(int i = 0; i < resources.length; i++) {
                    Path p = new DAVPath(session, resources[i].getPath(),
                            resources[i].getResourceType().isCollection() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                    p.setParent(this);

                    p.attributes.setOwner(resources[i].getOwner());
                    p.attributes.setModificationDate(resources[i].getGetLastModified());
                    p.attributes.setCreationDate(resources[i].getCreationDate());
                    p.attributes.setSize(resources[i].getGetContentLength());

                    childs.add(p);
                }
            }
            catch(HttpException e) {
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

                session.DAV.mkcolMethod(this.getAbsolute());
            }
            catch(HttpException e) {
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

    public void writePermissions(Permission perm, boolean recursive) {
//        synchronized(session) {
//            log.debug("changePermissions:" + perm);
//            try {
//                session.check();
//                session.message(NSBundle.localizedString("Changing permission to", "Status", "") + " " + perm.getOctalString() + " (" + this.getName() + ")");
//                session.DAV.aclMethod(this.getAbsolute(), new Ace[]{});
//            }
//            catch(HttpException e) {
//                this.error("Cannot change permissions", e);
//            }
//            catch(IOException e) {
//                this.error("Connection failed", e);
//                session.interrupt();
//            }
//            finally {
//                session.fireActivityStoppedEvent();
//            }
//        }
    }

    public void writeModificationDate(long millis) {
        synchronized(session) {
            try {
                session.check();
                session.DAV.proppatchMethod(this.getAbsolute(), GetLastModifiedProperty.TAG_NAME,
                        new SimpleDateFormat(GetLastModifiedProperty.DATE_FORMAT).format(new Date(millis)));
            }
            catch(HttpException e) {
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

    public void rename(String absolute) {
        synchronized(session) {
            log.debug("rename:" + absolute);
            try {
                session.check();
                session.message(NSBundle.localizedString("Renaming to", "Status", "") + " " + absolute + " (" + this.getName() + ")");

                session.DAV.moveMethod(this.getAbsolute(), absolute);
                this.setPath(absolute);
            }
            catch(HttpException e) {
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

    public void download(BandwidthThrottle throttle, StreamListener listener) {
        synchronized(session) {
            if(attributes.isFile()) {
                OutputStream out = null;
                InputStream in = null;
                try {
                    session.message(NSBundle.localizedString("Downloading", "Status", "") + " " + this.getName());

                    in = session.DAV.getMethodData(this.getAbsolute());
                    if(null == in) {
                        throw new IOException("Unable opening data stream");
                    }
                    out = new Local.OutputStream(this.getLocal(), this.getStatus().isResume());

                    this.download(in, out, throttle, listener);
                }
                catch(HttpException e) {
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
                        }
                        if(out != null) {
                            out.close();
                        }
                    }
                    catch(IOException e) {
                        log.error(e.getMessage());
                    }
                }
                session.fireActivityStoppedEvent();
            }
            if(attributes.isDirectory()) {
                this.getLocal().mkdir(true);
            }
        }
    }

    public void upload(BandwidthThrottle throttle, StreamListener listener, final Permission p) {
        synchronized(session) {
            try {
                if(attributes.isFile()) {
                    session.message(NSBundle.localizedString("Uploading", "Status", "") + " " + this.getName());

                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        if(this.getStatus().isResume()) {
                            this.getStatus().setCurrent((long) attributes.getSize());
                        }
                        in = new Local.InputStream(this.getLocal());
                        if(null == in) {
                            throw new IOException("Unable to buffer data");
                        }

                        if(session.DAV.putMethod(this.getAbsolute(), in)) {
                            // Manually mark as complete
                            final long sent = this.getLocal().attributes.getSize();
                            this.getStatus().setCurrent(sent);
                            this.getStatus().setComplete(true);
                            listener.bytesSent(sent);
                        }

                        if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                            this.writeModificationDate(this.getLocal().attributes.getModificationDate());
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
                if(attributes.isDirectory()) {
                    this.mkdir();
                }
            }
            catch(HttpException e) {
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
}
