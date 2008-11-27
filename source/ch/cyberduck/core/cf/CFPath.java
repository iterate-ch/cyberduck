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

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import com.mosso.client.cloudfiles.*;

/**
 * Mosso Cloud Files Implementation
 *
 * @version $Id:$
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

    private FilesContainer _container;

    /**
     * @return
     */
    private FilesContainer getContainer() {
        if(null == _container) {
            _container = new FilesContainer(this.getContainerName(), session.CF);
        }
        return _container;
    }

    public String getKey() {
        return this.getName();
    }

    /**
     * @param enabled Enable content distribution for the container
     * @param cnames  Currently ignored
     */
    public void writeDistribution(boolean enabled, String[] cnames) {
        final String container = this.getContainer().getName();
        try {
            if(enabled) {
                session.message(MessageFormat.format(NSBundle.localizedString("Enable distribution for {0}", "Status", ""),
                        container));
            }
            else {
                session.message(MessageFormat.format(NSBundle.localizedString("Disable distribution for {0}", "Status", ""),
                        container));
            }
            try {
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
            catch(FilesAuthorizationException e) {
                throw new MossoException(e.getHttpStatusMessage(), e);
            }
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
    }

    public Distribution readDistribution() {
        try {
            final FilesCDNContainer info;
            try {
                info = session.CF.getCDNContainerInfo(this.getContainer().getName());
            }
            catch(FilesAuthorizationException e) {
                throw new MossoException(e.getMessage(), e);
            }
            if(null == info) {
                // Not found.
                return new Distribution(false, this.getContainerName(),
                        null, NSBundle.localizedString("CDN Disabled", "Mosso"));
            }
            return new Distribution(info.isEnabled(),
                    this.getContainerName(), info.getCdnURL(),
                    info.isEnabled() ? NSBundle.localizedString("CDN Enabled", "Mosso") : NSBundle.localizedString("CDN Disabled", "Mosso"));
        }
        catch(IOException e) {
            this.error(e.getMessage(), e);
        }
        return new Distribution(false);
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
        return super.exists();
    }

    public void readSize() {
        try {
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Getting size of {0}", "Status", ""),
                    this.getName()));

            attributes.setSize(
                    Long.valueOf(session.CF.getObjectMetaData(this.getContainer().getName(), this.getName()).getContentLength())
            );
        }
        catch(IOException e) {
            log.warn("Cannot read size:" + e.getMessage());
        }
    }

    public void readTimestamp() {
        ;
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

    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            session.check();
            session.message(NSBundle.localizedString("Listing directory", "Status", "") + " "
                    + this.getAbsolute());

            if(this.isRoot()) {
                // List all containers
                for(FilesContainer container : session.CF.listContainers()) {
                    CFPath p = new CFPath(session, this.getAbsolute(), container.getName(),
                            Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                    p._container = container;

                    p.attributes.setSize(container.getInfo().getTotalSize());
                    p.attributes.setOwner(session.CF.getUserName());

                    childs.add(p);
                }
            }
            else {
                final FilesContainer container = this.getContainer();
                for(FilesObject object : container.getObjects()) {
                    final CFPath child = new CFPath(session, container.getName(), object.getName(),
                            Path.FILE_TYPE);
                    child.setParent(this);
                    child._container = container;
                    child.attributes.setSize(object.getSize());
//                    child.attributes.setModificationDate(object.getMetaData().getLastModified());
                    child.attributes.setOwner(this.attributes.getOwner());

                    childs.add(child);
                }
            }
        }
        catch(FilesAuthorizationException e) {
            childs.attributes().setReadable(false);
            this.error("Listing directory failed", e);
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

                in = this.session.CF.getObjectAsStream(this.getContainer().getName(), this.getName());

                if(null == in) {
                    throw new IOException("Unable opening data stream");
                }

                this.download(in, out, throttle, listener);
            }
            catch(IOException e) {
                ;
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
            if(attributes.isDirectory()) {
                this.getLocal().mkdir(true);
            }
        }
    }

    protected void upload(BandwidthThrottle throttle, StreamListener listener, Permission p, boolean check) {
        try {
            if(check) {
                session.check();
            }
            if(attributes.isFile()) {
                this.getSession().message(MessageFormat.format(NSBundle.localizedString("Uploading {0}", "Status", ""),
                        this.getName()));

                // No Content-Range support
                this.getStatus().setCurrent(0);

                try {
                    final int result = session.CF.storeObject(this.getContainer().getName(), new File(this.getLocal().getAbsolute()),
                            this.getLocal().getMimeType());
                    if(result != FilesConstants.OBJECT_CREATED) {
                        throw new MossoException(String.valueOf(result));
                    }
                    // Manually mark as complete
                    this.getStatus().setComplete(true);
                }
                catch(NoSuchAlgorithmException e) {
                    throw new MossoException(e.getMessage());
                }
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

    public boolean isMkdirSupported() {
        return this.isRoot();
    }

    public void mkdir(boolean recursive) {
        log.debug("mkdir:" + this.getName());
        try {
            if(!this.isContainer()) {
                throw new MossoException("Container can only be created at top level");
            }
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Making directory {0}", "Status", ""),
                    this.getName()));

            final int result = session.CF.createContainer(this.getName());
            if(result != FilesConstants.CONTAINER_CREATED) {
                throw new MossoException(String.valueOf(result));
            }
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

                final int result = session.CF.deleteObject(this.getContainer().getName(), this.getName());
                if(result != FilesConstants.OBJECT_DELETED) {
                    throw new MossoException(String.valueOf(result));
                }
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!session.isConnected()) {
                        break;
                    }
                    i.delete();
                }
                if(this.isContainer()) {
                    final int result = session.CF.deleteContainer(this.getContainer().getName());
                    if(result != FilesConstants.CONTAINER_DELETED) {
                        throw new MossoException(String.valueOf(result));
                    }
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


    public String getWebURL() {
        return this.toURL();
    }

    /**
     * Overwritten to provide publicy accessible URL of given object
     *
     * @return
     */
    public String toURL() {
        try {
            StringBuffer b = new StringBuffer();
            final FilesCDNContainer info = session.CF.getCDNContainerInfo(this.getContainer().getName());
            if(null == info) {
                // Not found.
                return super.toURL();
            }
            b.append(info.getCdnURL());
            b.append(Path.DELIMITER);
            b.append(this.getName());
            return b.toString();
        }
        catch(FilesAuthorizationException e) {
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
        return super.toURL();
    }
}