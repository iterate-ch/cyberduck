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

import ch.cyberduck.core.*;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;

import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import com.rackspacecloud.client.cloudfiles.FilesContainerInfo;
import com.rackspacecloud.client.cloudfiles.FilesObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFPath extends CloudPath {
    private static Logger log = Logger.getLogger(CFPath.class);

    static {
        PathFactory.addFactory(Protocol.MOSSO, new Factory());
    }

    private static class Factory extends PathFactory<CFSession> {
        @Override
        protected Path create(CFSession session, String path, int type) {
            return new CFPath(session, path, type);
        }

        @Override
        protected Path create(CFSession session, String parent, String name, int type) {
            return new CFPath(session, parent, name, type);
        }

        @Override
        protected Path create(CFSession session, String path, Local file) {
            return new CFPath(session, path, file);
        }

        @Override
        protected <T> Path create(CFSession session, T dict) {
            return new CFPath(session, dict);
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

    protected <T> CFPath(CFSession s, T dict) {
        super(dict);
        this.session = s;
    }

    @Override
    public Session getSession() {
        return this.session;
    }

    @Override
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

    @Override
    public void readSize() {
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                    this.getName()));

            if(this.isContainer()) {
                attributes.setSize(
                        session.CF.getContainerInfo(this.getContainerName()).getTotalSize()
                );
            }
            else if(this.attributes.isFile()) {
                attributes.setSize(
                        Long.valueOf(session.CF.getObjectMetaData(this.getContainerName(), this.getKey()).getContentLength())
                );
            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void readTimestamp() {
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                    this.getName()));

            if(!this.isContainer()) {
                try {
                    attributes.setModificationDate(
                            ServiceUtils.parseRfc822Date(session.CF.getObjectMetaData(this.getContainerName(),
                                    this.getKey()).getLastModified()).getTime()
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

    @Override
    public void readPermission() {
        ;
    }

    /**
     * Only content distribution is possible but no fine grained grants
     *
     * @return Always false
     */
    @Override
    public boolean isWritePermissionsSupported() {
        return false;
    }

    @Override
    public void writePermissions(Permission perm, boolean recursive) {
        ;
    }

    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            if(this.isRoot()) {
                // List all containers
                for(FilesContainerInfo container : session.CF.listContainersInfo()) {
                    Path p = PathFactory.createPath(session, this.getAbsolute(), container.getName(),
                            Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                    p.attributes.setSize(container.getTotalSize());
                    p.attributes.setOwner(session.CF.getUserName());

                    childs.add(p);
                }
            }
            else {
                for(FilesObject object : session.CF.listObjects(this.getContainerName(), this.getKey())) {
                    final Path file = PathFactory.createPath(session, this.getContainerName(), object.getName(),
                            "application/directory".equals(object.getMimeType()) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                    if(file.getParent().equals(this)) {
                        file.setParent(this);
                        if(file.attributes.getType() == Path.FILE_TYPE) {
                            file.attributes.setSize(object.getSize());
                        }
                        try {
                            final Date modified = DateParser.parse(object.getLastModified());
                            if(null != modified) {
                                file.attributes.setModificationDate(modified.getTime());
                            }
                        }
                        catch(InvalidDateException e) {
                            log.warn("Not ISO 8601 format:" + e.getMessage());
                        }
                        file.attributes.setOwner(this.attributes.getOwner());

                        childs.add(file);
                    }
                }
            }
            session.setWorkdir(this);
        }
        catch(IOException e) {
            childs.attributes().setReadable(false);
            this.error("Listing directory failed", e);
        }
        return childs;
    }

    @Override
    public void download(final BandwidthThrottle throttle, final StreamListener listener, boolean check) {
        if(attributes.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    session.check();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                        this.getName()));

                in = this.session.CF.getObjectAsStream(this.getContainerName(), this.getKey());

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
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        if(attributes.isDirectory()) {
            this.getLocal().mkdir(true);
        }
    }

    @Override
    protected void upload(final BandwidthThrottle throttle, final StreamListener listener, Permission p, boolean check) {
        try {
            if(check) {
                session.check();
            }
            if(attributes.isFile()) {
                // No Content-Range support
                final Status stat = this.getStatus();
                stat.setCurrent(0);
                final InputStream in = new Local.InputStream(this.getLocal());
                this.getSession().message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        this.getName()));
                String md5sum = null;
                try {
                    md5sum = ServiceUtils.toHex(ServiceUtils.computeMD5Hash(new Local.InputStream(this.getLocal())));
                    this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                            this.getName()));
                }
                catch(NoSuchAlgorithmException e) {
                    log.error(e.getMessage(), e);
                }

                final HashMap<String, String> metadata = new HashMap<String, String>();

                session.CF.storeObjectAs(this.getContainerName(), this.getKey(),
                        new InputStreamRequestEntity(in, this.getLocal().attributes.getSize(), this.getLocal().getMimeType()) {

                            boolean requested = false;

                            @Override
                            public void writeRequest(OutputStream out) throws IOException {
                                if(requested) {
                                    in.reset();
                                    stat.reset();
                                    stat.setCurrent(0);
                                }
                                try {
                                    CFPath.this.upload(out, in, throttle, listener);
                                }
                                finally {
                                    requested = true;
                                }
                            }

                            @Override
                            public boolean isRepeatable() {
                                return true;
                            }
                        },
                        metadata, md5sum
                );
            }
            if(attributes.isDirectory()) {
                this.mkdir();
            }
        }
        catch(IOException e) {
            this.error("Upload failed", e);
        }
    }

    @Override
    public void mkdir(boolean recursive) {
        log.debug("mkdir:" + this.getName());
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                    this.getName()));

            if(this.isContainer()) {
                // Create container at top level
                session.CF.createContainer(this.getName());
            }
            else {
                // Create virtual directory
                session.CF.createFullPath(this.getContainerName(), this.getKey());
            }
        }
        catch(IOException e) {
            this.error("Cannot create folder", e);
        }
    }

    @Override
    public void delete() {
        log.debug("delete:" + this.toString());
        try {
            session.check();
            if(!this.isContainer()) {
                session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                session.CF.deleteObject(this.getContainerName(), this.getKey());
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!session.isConnected()) {
                        break;
                    }
                    i.delete();
                }
                if(this.isContainer()) {
                    session.CF.deleteContainer(this.getContainerName());
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

    @Override
    public boolean isRenameSupported() {
        return false;
    }

    @Override
    public void rename(AbstractPath renamed) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Publicy accessible URL of given object
     */
    @Override
    public String toHttpURL() {
        final Distribution distribution = session.readDistribution(this.getContainerName());
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