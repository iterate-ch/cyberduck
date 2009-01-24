package ch.cyberduck.core.cf;

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
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.ssl.AbstractX509TrustManager;

import org.apache.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;

import com.mosso.client.cloudfiles.FilesCDNContainer;
import com.mosso.client.cloudfiles.FilesContainerInfo;
import com.mosso.client.cloudfiles.FilesObject;

/**
 * Mosso Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFPath extends CloudPath {
    private static Logger log = Logger.getLogger(CFPath.class);

    static {
        PathFactory.addFactory(Protocol.MOSSO, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String path, int type) {
            return new CFPath((CFSession) session, path, type);
        }

        protected Path create(Session session, String parent, String name, int type) {
            return new CFPath((CFSession) session, parent, name, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new CFPath((CFSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new CFPath((CFSession) session, dict);
        }
    }

    private final CFSession session;

    protected CFPath(CFSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected CFPath(CFSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected CFPath(CFSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected CFPath(CFSession s, NSDictionary dict) {
        super(dict);
        this.session = s;
    }

    public Session getSession() {
        return this.session;
    }

    public String getKey() {
        if(this.isContainer()) {
            return null;
        }
        return this.getName();
    }

    /**
     * @param enabled Enable content distribution for the container
     * @param cnames  Currently ignored
     */
    public void writeDistribution(boolean enabled, String[] cnames) {
        final String container = this.getContainerName();
        final AbstractX509TrustManager trust = session.getTrustManager();
        try {
            session.check();
            trust.setHostname(URI.create(session.CF.getCdnManagementURL()).getHost());
            if(enabled) {
                session.message(MessageFormat.format(NSBundle.localizedString("Enable {0} Distribution", "Status", ""),
                        NSBundle.localizedString("Mosso Cloud Files", "Mosso", "")));
            }
            else {
                session.message(MessageFormat.format(NSBundle.localizedString("Disable {0} Distribution", "Status", ""),
                        NSBundle.localizedString("Mosso Cloud Files", "Mosso", "")));
            }
            if(enabled) {
                final FilesCDNContainer info = session.CF.getCDNContainerInfo(container);
                if(null == info) {
                    // Not found.
                    session.CF.cdnEnableContainer(container);
                }
                else {
                    // Enable content distribution for the container without changing the TTL expiration
                    session.CF.cdnUpdateContainer(container, -1, true);
                }
            }
            else {
                // Disable content distribution for the container
                session.CF.cdnUpdateContainer(container, -1, false);
            }
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
        finally {
            trust.setHostname(URI.create(session.CF.getStorageURL()).getHost());
        }
    }

    public Distribution readDistribution() {
        final String container = this.getContainerName();
        if(null != container) {
            final AbstractX509TrustManager trust = session.getTrustManager();
            try {
                session.check();
                trust.setHostname(URI.create(session.CF.getCdnManagementURL()).getHost());
                final FilesCDNContainer info = session.CF.getCDNContainerInfo(container);
                if(null == info) {
                    // Not found.
                    return new Distribution(false, null, NSBundle.localizedString("CDN Disabled", "Mosso", ""));
                }
                return new Distribution(info.isEnabled(), info.getCdnURL(),
                        info.isEnabled() ? NSBundle.localizedString("CDN Enabled", "Mosso", "") : NSBundle.localizedString("CDN Disabled", "Mosso", ""));
            }
            catch(IOException e) {
                this.error(e.getMessage(), e);
            }
            finally {
                trust.setHostname(URI.create(session.CF.getStorageURL()).getHost());
            }
        }
        return new Distribution(false, null, null);
    }

    public boolean exists() {
        if(this.isRoot()) {
            return true;
        }
        try {
            if(this.isContainer()) {
                return session.CF.containerExists(this.getName());
            }
        }
        catch(IOException e) {
            return false;
        }
        if(!this.isCached()) {
            // Optimization
            return this.list().contains(this);
        }
        return super.exists();
    }

    public void readSize() {
        try {
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Getting size of {0}", "Status", ""),
                    this.getName()));

            if(this.isContainer()) {
                attributes.setSize(
                        Long.valueOf(session.CF.getContainerInfo(this.getContainerName()).getTotalSize())
                );
            }
            else {
                attributes.setSize(
                        Long.valueOf(session.CF.getObjectMetaData(this.getContainerName(), this.getName()).getContentLength())
                );
            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    public void readTimestamp() {
        try {
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Getting timestamp of {0}", "Status", ""),
                    this.getName()));

            if(!this.isContainer()) {
                try {
                    attributes.setModificationDate(
                            ServiceUtils.parseRfc822Date(session.CF.getObjectMetaData(this.getContainerName(),
                                    this.getName()).getLastModified()).getTime()
                    );
                }
                catch(ParseException e) {
                    log.error(e);
                }

            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    public void readPermission() {
        ;
    }

    /**
     * Only content distribution is possible but no fine grained grants
     *
     * @return Always false
     */
    public boolean isWritePermissionsSupported() {
        return false;
    }

    public void writePermissions(Permission perm, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param metadata Read additional metadata
     * @return
     */
    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            session.check();
            session.message(NSBundle.localizedString("Listing directory", "Status", "") + " "
                    + this.getAbsolute());

            if(this.isRoot()) {
                // List all containers
                for(FilesContainerInfo container : session.CF.listContainersInfo()) {
                    CFPath p = (CFPath) PathFactory.createPath(session, this.getAbsolute(), container.getName(),
                            Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                    p.attributes.setSize(container.getTotalSize());
                    p.attributes.setOwner(session.CF.getUserName());

                    childs.add(p);
                }
            }
            else {
                for(FilesObject object : session.CF.listObjects(this.getContainerName())) {
                    final CFPath child = (CFPath) PathFactory.createPath(session, this.getContainerName(), object.getName(),
                            Path.FILE_TYPE);
                    child.setParent(this);
                    child.attributes.setSize(object.getSize());
                    final Date modified = object.getLastModified();
                    if(null != modified) {
                        child.attributes.setModificationDate(modified.getTime());
                    }
                    child.attributes.setOwner(this.attributes.getOwner());

                    childs.add(child);
                }
            }
        }
        catch(IOException e) {
            childs.attributes().setReadable(false);
            this.error("Listing directory failed", e);
        }
        return childs;
    }

    public void download(BandwidthThrottle throttle, StreamListener listener, boolean check) {
        if(attributes.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    session.check();
                }
                this.getSession().message(MessageFormat.format(NSBundle.localizedString("Downloading {0}", "Status", ""),
                        this.getName()));

                in = this.session.CF.getObjectAsStream(this.getContainerName(), this.getName());

                if(null == in) {
                    throw new IOException("Unable opening data stream");
                }

                getStatus().setCurrent(0);
                out = new Local.OutputStream(this.getLocal(), this.getStatus().isResume());

                this.download(in, out, throttle, listener);
            }
            catch(IOException e) {
                this.error("Download failed", e);
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
            this.getLocal().mkdir(true);
        }
    }

    protected void upload(BandwidthThrottle throttle, final StreamListener listener, Permission p, boolean check) {
        try {
            if(check) {
                session.check();
            }
            if(attributes.isFile()) {
                // No Content-Range support
                this.getStatus().setCurrent(0);
                final InputStream in = new Local.InputStream(this.getLocal());
                this.getSession().message(MessageFormat.format(NSBundle.localizedString("Compute MD5 hash of {0}", "Status", ""),
                        this.getName()));
                String md5 = null;
                try {
                    md5 = ServiceUtils.toHex(ServiceUtils.computeMD5Hash(new Local.InputStream(this.getLocal())));
                    this.getSession().message(MessageFormat.format(NSBundle.localizedString("Uploading {0}", "Status", ""),
                            this.getName()));
                }
                catch(NoSuchAlgorithmException e) {
                    log.error(e.getMessage(), e);
                }
                final int result = session.CF.storeObjectAs(this.getContainerName(),
                        this.getName(),
                        new InputStream() {
                            long bytesTransferred = getStatus().getCurrent();

                            public int read() throws IOException {
                                return read(new byte[1]);
                            }

                            int read;

                            public int read(byte buffer[], int offset, int length)
                                    throws IOException {
                                if(getStatus().isCanceled()) {
                                    return -1;
                                }
                                if(read > 0) {
                                    listener.bytesSent(read);
                                    bytesTransferred += read;
                                    getStatus().setCurrent(bytesTransferred);
                                }
                                read = in.read(buffer, offset, length);
                                if(-1 == read) {
                                    // End of file
                                    getStatus().setComplete(true);
                                }
                                return read;
                            }
                        },
                        this.getLocal().attributes.getSize(),
                        this.getLocal().getMimeType(),
                        md5
                );
            }
            if(attributes.isDirectory()) {
                if(this.isContainer()) {
                    // Create container at top level
                    this.mkdir();
                }
            }
        }
        catch(IOException e) {
            this.error("Upload failed", e);
        }
    }

    public void mkdir(boolean recursive) {
        log.debug("mkdir:" + this.getName());
        try {
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Making directory {0}", "Status", ""),
                    this.getName()));

            final int result = session.CF.createContainer(this.getName());
        }
        catch(IOException e) {
            this.error("Cannot create folder", e);
        }
    }

    public void delete() {
        log.debug("delete:" + this.toString());
        try {
            session.check();
            if(attributes.isFile()) {
                session.message(MessageFormat.format(NSBundle.localizedString("Deleting {0}", "Status", ""),
                        this.getName()));

                final int result = session.CF.deleteObject(this.getContainerName(), this.getName());
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!session.isConnected()) {
                        break;
                    }
                    i.delete();
                }
                if(this.isContainer()) {
                    final int result = session.CF.deleteContainer(this.getContainerName());
                }
            }
        }
        catch(IOException e) {
            if(this.attributes.isFile()) {
                this.error("Cannot delete file", e);
            }
            if(this.attributes.isDirectory()) {
                this.error("Cannot delete folder", e);
            }
        }
    }

    public boolean isRenameSupported() {
        return false;
    }

    public void rename(Path renamed) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Publicy accessible URL of given object
     */
    public String toHttpURL() {
        final Distribution distribution = this.readDistribution();
        if(null == distribution.getUrl()) {
            return super.toHttpURL();
        }
        StringBuffer b = new StringBuffer();
        b.append(distribution.getUrl());
        if(!this.isContainer()) {
            b.append(this.encode(this.getKey()));
        }
        return b.toString();
    }
}