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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.IOResumeException;

import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * @version $Id: $
 */
public class DAVPath extends Path {
    private static Logger log = Logger.getLogger(DAVPath.class);

    private static class Factory extends PathFactory<DAVSession> {
        @Override
        protected Path create(DAVSession session, String path, int type) {
            return new DAVPath(session, path, type);
        }

        @Override
        protected Path create(DAVSession session, String parent, String name, int type) {
            return new DAVPath(session, parent, name, type);
        }

        @Override
        protected Path create(DAVSession session, String parent, Local file) {
            return new DAVPath(session, parent, file);
        }

        @Override
        protected <T> Path create(DAVSession session, T dict) {
            return new DAVPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
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

    protected <T> DAVPath(DAVSession s, T dict) {
        super(dict);
        this.session = s;
    }

    @Override
    public DAVSession getSession() {
        return session;
    }

    @Override
    public void readSize() {
        if(this.attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                        this.getName()));

                this.getSession().getClient().setPath(this.attributes().isDirectory() ?
                        this.getAbsolute() + String.valueOf(Path.DELIMITER) : this.getAbsolute());

                this.getSession().getClient().setProperties(WebdavResource.BASIC, DepthSupport.DEPTH_1);
                attributes().setSize(this.getSession().getClient().getGetContentLength());
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
    }

    @Override
    public void readTimestamp() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                    this.getName()));

            this.getSession().getClient().setPath(this.attributes().isDirectory() ?
                    this.getAbsolute() + String.valueOf(Path.DELIMITER) : this.getAbsolute());

            this.getSession().getClient().setProperties(WebdavResource.BASIC, DepthSupport.DEPTH_1);
            attributes().setModificationDate(this.getSession().getClient().getGetLastModified());
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void delete() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    this.getName()));

            if(!this.getSession().getClient().deleteMethod(this.getAbsolute())) {
                throw new IOException(this.getSession().getClient().getStatusMessage());
            }
            // The directory listing is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            this.error("Cannot delete {0}", e);
        }
    }


    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> children = new AttributedList<Path>();
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                        this.getName()));

                this.getSession().setWorkdir(this);
                this.getSession().getClient().setContentType("text/xml");
                WebdavResource[] resources = this.getSession().getClient().listWebdavResources();

                for(final WebdavResource resource : resources) {
                    if(null == resource.getResourceType()) {
                        log.warn("Skipping unknown resource type:" + resource);
                        continue;
                    }
                    try {
                        // Try to parse as RFC 2396
                        URI uri = new URI(new String(resource.getHttpURL().getRawURI()));
                        Path p = PathFactory.createPath(this.getSession(), uri.getPath(),
                                resource.getResourceType().isCollection() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                        p.setParent(this);
                        if(!p.isChild(this)) {
                            log.warn("Skipping invalid resource:" + resource);
                            continue;
                        }
                        p.attributes().setOwner(resource.getOwner());
                        if(resource.getGetLastModified() > 0) {
                            p.attributes().setModificationDate(resource.getGetLastModified());
                        }
                        if(resource.getCreationDate() > 0) {
                            p.attributes().setCreationDate(resource.getCreationDate());
                        }
                        p.attributes().setSize(resource.getGetContentLength());

                        children.add(p);
                    }
                    catch(URISyntaxException e) {
                        log.error("Failure parsing URI:" + e.getMessage());
                    }
                }
            }
            catch(IOException e) {
                log.warn("Listing directory failed:" + e.getMessage());
                children.attributes().setReadable(false);
                if(this.cache().isEmpty()) {
                    this.error(e.getMessage(), e);
                }
            }
        }
        return children;
    }

    @Override
    public void mkdir() {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                        this.getName()));

                this.getSession().getClient().setContentType("text/xml");
                if(!this.getSession().getClient().mkcolMethod(this.getAbsolute())) {
                    throw new IOException(this.getSession().getClient().getStatusMessage());
                }
                this.cache().put(this.getReference(), AttributedList.<Path>emptyList());
                // The directory listing is no more current
                this.getParent().invalidate();
            }
            catch(IOException e) {
                this.error("Cannot create folder {0}", e);
            }
        }
    }

    @Override
    public void readUnixPermission() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeUnixPermission(Permission perm, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTimestamp(long created, long modified, long accessed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(AbstractPath renamed) {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed.getName()));

            if(!this.getSession().getClient().moveMethod(this.getAbsolute(), renamed.getAbsolute())) {
                throw new IOException(this.getSession().getClient().getStatusMessage());
            }
            // The directory listing of the target is no more current
            renamed.getParent().invalidate();
            // The directory listing of the source is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            this.error("Cannot rename {0}", e);
        }
    }

    @Override
    public void copy(AbstractPath copy) {
        if(((Path) copy).getSession().equals(this.getSession())) {
            // Copy on same server
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Copying {0} to {1}", "Status"),
                        this.getName(), copy));

                if(attributes().isFile()) {
                    if(!this.getSession().getClient().copyMethod(this.getAbsolute(), copy.getAbsolute())) {
                        throw new IOException(this.getSession().getClient().getStatusMessage());
                    }
                }
                else if(this.attributes().isDirectory()) {
                    for(AbstractPath i : this.children()) {
                        if(!this.getSession().isConnected()) {
                            break;
                        }
                        i.copy(PathFactory.createPath(this.getSession(), copy.getAbsolute(),
                                i.getName(), i.attributes().getType()));
                    }
                }
                // The directory listing is no more current
                copy.getParent().invalidate();
            }
            catch(IOException e) {
                this.error("Cannot copy {0}");
            }
        }
        else {
            // Copy to different host
            super.copy(copy);
        }
    }

    @Override
    protected void download(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        if(attributes().isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    this.getSession().check();
                }
                if(this.status().isResume()) {
                    this.getSession().getClient().addRequestHeader("Range", "bytes=" + this.status().getCurrent() + "-");
                }
                this.getSession().getClient().addRequestHeader("Accept-Encoding", "gzip");
                in = this.getSession().getClient().getMethodData(this.getAbsolute());
                // Content-Range header in response not found
                if(!this.getSession().getClient().isResume()) {
                    this.status().setResume(false);
                }
                out = this.getLocal().getOutputStream(this.status().isResume());
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
        if(attributes().isFile()) {
            try {
                if(check) {
                    this.getSession().check();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        this.getName()));

                final InputStream in = this.getLocal().getInputStream();
                try {
                    final Status status = this.status();
                    if(status.isResume()) {
                        this.getSession().getClient().addRequestHeader("Content-Range", "bytes "
                                + status.getCurrent()
                                + "-" + (this.getLocal().attributes().getSize() - 1)
                                + "/" + this.getLocal().attributes().getSize()
                        );
                        long skipped = in.skip(status.getCurrent());
                        log.info("Skipping " + skipped + " bytes");
                        if(skipped < status.getCurrent()) {
                            throw new IOResumeException("Skipped " + skipped + " bytes instead of " + status.getCurrent());
                        }
                    }
                    if(!this.getSession().getClient().putMethod(this.getAbsolute(),
                            new InputStreamRequestEntity(in, this.getLocal().attributes().getSize() - status.getCurrent(),
                                    this.getLocal().getMimeType()) {
                                @Override
                                public void writeRequest(OutputStream out) throws IOException {
                                    DAVPath.this.upload(out, in, throttle, listener);
                                }
                            })) {
                        // Upload failed
                        throw new IOException(this.getSession().getClient().getStatusMessage());
                    }
                }
                finally {
                    IOUtils.closeQuietly(in);
                }
            }
            catch(IOException e) {
                this.error("Upload failed", e);
            }
        }
    }

    @Override
    public String toHttpURL() {
        return this.toURL();
    }
}