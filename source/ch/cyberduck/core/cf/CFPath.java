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
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFPath extends CloudPath {
    private static Logger log = Logger.getLogger(CFPath.class);

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
        protected Path create(CFSession session, String parent, Local file) {
            return new CFPath(session, parent, file);
        }

        @Override
        protected <T> Path create(CFSession session, T dict) {
            return new CFPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
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
    public CFSession getSession() {
        return session;
    }

    @Override
    public boolean exists() {
        if(this.isRoot()) {
            return true;
        }
        try {
            if(this.isContainer()) {
                return this.getSession().getClient().containerExists(this.getName());
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
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                    this.getName()));

            if(this.isContainer()) {
                attributes().setSize(
                        this.getSession().getClient().getContainerInfo(this.getContainerName()).getTotalSize()
                );
            }
            else if(this.attributes().isFile()) {
                attributes().setSize(
                        Long.valueOf(this.getSession().getClient().getObjectMetaData(this.getContainerName(), this.getKey()).getContentLength())
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
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                    this.getName()));

            if(!this.isContainer()) {
                try {
                    attributes().setModificationDate(
                            ServiceUtils.parseRfc822Date(this.getSession().getClient().getObjectMetaData(this.getContainerName(),
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
    public AttributedList<Path> list() {
        final AttributedList<Path> children = new AttributedList<Path>();
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            if(this.isRoot()) {
                // List all containers
                for(FilesContainerInfo container : this.getSession().getClient().listContainersInfo()) {
                    Path p = PathFactory.createPath(this.getSession(), this.getAbsolute(), container.getName(),
                            Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                    p.attributes().setSize(container.getTotalSize());
                    p.attributes().setOwner(this.getSession().getClient().getUserName());

                    children.add(p);
                }
            }
            else {
                for(FilesObject object : this.getSession().getClient().listObjects(this.getContainerName(), this.getKey(), -1, null)) {
                    final Path file = PathFactory.createPath(this.getSession(), this.getContainerName(), object.getName(),
                            "application/directory".equals(object.getMimeType()) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                    if(file.getParent().getAbsolute().equals(this.getAbsolute())) {
                        file.setParent(this);
                        if(file.attributes().getType() == Path.FILE_TYPE) {
                            file.attributes().setSize(object.getSize());
                            file.attributes().setChecksum(object.getMd5sum());
                        }
                        if(file.attributes().getType() == Path.DIRECTORY_TYPE) {
                            file.attributes().setPlaceholder(true);
                        }
                        try {
                            final Date modified = DateParser.parse(object.getLastModified());
                            if(null != modified) {
                                file.attributes().setModificationDate(modified.getTime());
                            }
                        }
                        catch(InvalidDateException e) {
                            log.warn("Not ISO 8601 format:" + e.getMessage());
                        }
                        file.attributes().setOwner(this.attributes().getOwner());

                        children.add(file);
                    }
                }
            }
            this.getSession().setWorkdir(this);
        }
        catch(IOException e) {
            children.attributes().setReadable(false);
        }
        return children;
    }

    @Override
    protected void download(final BandwidthThrottle throttle, final StreamListener listener, boolean check) {
        if(attributes().isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    this.getSession().check();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                        this.getName()));

                in = this.getSession().getClient().getObjectAsStream(this.getContainerName(), this.getKey());
                if(null == in) {
                    throw new IOException("Unable opening data stream");
                }
                final Status status = this.status();
                status.setResume(false);
                out = this.getLocal().getOutputStream(status.isResume());
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
    }

    @Override
    protected void upload(final BandwidthThrottle throttle, final StreamListener listener, boolean check) {
        try {
            if(attributes().isFile()) {
                if(check) {
                    this.getSession().check();
                }
                // No Content-Range support
                final Status status = this.status();
                status.setResume(false);
                this.getSession().message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        this.getName()));
                String md5sum = this.getLocal().attributes().getChecksum();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        this.getName()));

                final InputStream in = this.getLocal().getInputStream();
                try {
                    final HashMap<String, String> metadata = new HashMap<String, String>();
                    this.getSession().getClient().storeObjectAs(this.getContainerName(), this.getKey(),
                            new InputStreamRequestEntity(in,
                                    this.getLocal().attributes().getSize() - status.getCurrent(),
                                    this.getLocal().getMimeType()) {

                                @Override
                                public void writeRequest(OutputStream out) throws IOException {
                                    CFPath.this.upload(out, in, throttle, listener);
                                }
                            },
                            metadata, md5sum
                    );
                }
                finally {
                    IOUtils.closeQuietly(in);
                }
            }
        }
        catch(IOException e) {
            this.error("Upload failed", e);
        }
    }

    @Override
    public void mkdir() {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                        this.getName()));

                if(this.isContainer()) {
                    // Create container at top level
                    this.getSession().getClient().createContainer(this.getName());
                }
                else {
                    // Create virtual directory
                    this.getSession().getClient().createFullPath(this.getContainerName(), this.getKey());
                }
                this.cache().put(this.getReference(), AttributedList.<Path>emptyList());
                // The directory listing is no more current
                this.getParent().invalidate();
            }
            catch(IOException e) {
                this.error("Cannot create folder", e);
            }
        }
    }

    @Override
    public void delete() {
        try {
            this.getSession().check();
            if(!this.isContainer()) {
                this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                this.getSession().getClient().deleteObject(this.getContainerName(), this.getKey());
            }
            else if(attributes().isDirectory()) {
                for(AbstractPath i : this.children()) {
                    if(!this.getSession().isConnected()) {
                        break;
                    }
                    i.delete();
                }
                if(this.isContainer()) {
                    this.getSession().getClient().deleteContainer(this.getContainerName());
                }
            }
            // The directory listing is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            if(this.attributes().isFile()) {
                this.error("Cannot delete file", e);
            }
            if(this.attributes().isDirectory()) {
                this.error("Cannot delete folder", e);
            }
        }
    }

    /**
     * @return Modifiable HTTP header metatdata key and values
     */
    @Override
    public void readMetadata() {
        if(attributes().isFile() || attributes().isPlaceholder()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
                        this.getName()));

                final FilesObjectMetaData meta
                        = this.getSession().getClient().getObjectMetaData(this.getContainerName(), this.getKey());
                this.attributes().setMetadata(meta.getMetaData());
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
    }

    @Override
    public void writeMetadata(Map<String, String> meta) {
        if(attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                        this.getName()));

                this.getSession().getClient().updateObjectMetadata(this.getContainerName(), this.getKey(), meta);
            }
            catch(IOException e) {
                this.error("Cannot write file attributes", e);
            }
            finally {
                this.attributes().clear(false, false, false, true);
            }
        }
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
        final Distribution distribution
                = this.getSession().readDistribution(this.getContainerName(), Distribution.DOWNLOAD);
        return distribution.getUrl(this.getKey());
    }
}