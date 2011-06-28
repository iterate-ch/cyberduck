package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpPath;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.IOResumeException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import com.googlecode.sardine.DavResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id: $
 */
public class DAVPath extends HttpPath {
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

                this.readAttributes();
            }
            catch(IOException e) {
                log.warn("Cannot read file attributes");
            }
        }
    }

    @Override
    public void readTimestamp() {
        if(this.attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                        this.getName()));

                this.readAttributes();
            }
            catch(IOException e) {
                log.warn("Cannot read file attributes");
            }
        }
    }

    private void readAttributes() throws IOException {
        final List<DavResource> resources = this.getSession().getClient().list(this.toURL());
        for(final DavResource resource : resources) {
            this.readAttributes(resource);
        }
    }

    private void readAttributes(DavResource resource) {
        if(resource.getModified() != null) {
            this.attributes().setModificationDate(resource.getModified().getTime());
        }
        if(resource.getCreation() != null) {
            this.attributes().setCreationDate(resource.getCreation().getTime());
        }
        if(resource.getContentLength() != null) {
            this.attributes().setSize(resource.getContentLength());
        }
        this.attributes().setChecksum(resource.getEtag());
    }

    @Override
    public boolean exists() {
        if(super.exists()) {
            return true;
        }
        if(this.attributes().isDirectory()) {
            // Parent directory may not be accessible. Issue #5662
            try {
                return this.getSession().getClient().exists(this.toURL());
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
        return false;
    }

    @Override
    public void delete() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    this.getName()));

            this.getSession().getClient().delete(this.toURL());
            // The directory listing is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            this.error("Cannot delete {0}", e);
        }
    }

    @Override
    public AttributedList<Path> list(final AttributedList<Path> children) {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                        this.getName()));

                List<DavResource> resources = this.getSession().getClient().list(this.toURL());

                for(final DavResource resource : resources) {
                    // Try to parse as RFC 2396
                    final URI uri = resource.getHref();
                    DAVPath p = (DAVPath) PathFactory.createPath(this.getSession(), uri.getPath(),
                            resource.isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                    p.setParent(this);

                    p.readAttributes(resource);

                    children.add(p);
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

                this.getSession().getClient().createDirectory(this.toURL());

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

    /**
     * @return Modifiable HTTP header metatdata key and values
     */
    public void readMetadata() {
        if(attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
                        this.getName()));

                List<DavResource> resources = this.getSession().getClient().list(this.toURL());
                for(DavResource resource : resources) {
                    this.attributes().setMetadata(resource.getCustomProps());
                }
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
    }

    public void writeMetadata(Map<String, String> meta) {
        if(attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                        this.getName()));

                this.getSession().getClient().setCustomProps(this.toURL(),
                        meta, Collections.<java.lang.String>emptyList());
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
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed.getName()));

            this.getSession().getClient().move(this.toURL(), renamed.toURL());

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
                    this.getSession().getClient().copy(this.toURL(), copy.toURL());
                }
                else if(this.attributes().isDirectory()) {
                    copy.mkdir();
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
    public InputStream read(boolean check) throws IOException {
        if(check) {
            this.getSession().check();
        }
        Map<String, String> headers = new HashMap<String, String>();
        if(this.status().isResume()) {
            headers.put(HttpHeaders.RANGE, "bytes=" + this.status().getCurrent() + "-");
        }
        return this.getSession().getClient().get(this.toURL(), headers);
    }

    @Override
    protected void download(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        if(attributes().isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                in = this.read(check);
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
                InputStream in = null;
                ResponseOutputStream<Void> out = null;
                try {
                    in = this.getLocal().getInputStream();
                    if(this.status().isResume()) {
                        long skipped = in.skip(this.status().getCurrent());
                        log.info("Skipping " + skipped + " bytes");
                        if(skipped < this.status().getCurrent()) {
                            throw new IOResumeException("Skipped " + skipped + " bytes instead of " + this.status().getCurrent());
                        }
                    }
                    out = this.write(check);

                    this.upload(out, in, throttle, listener);

                    // The directory listing is no more current
                    this.getParent().invalidate();
                }
                finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
                if(null != out) {
                    out.getResponse();
                }
            }
            catch(IOException e) {
                this.error("Upload failed", e);
            }
        }
    }

    @Override
    public ResponseOutputStream<Void> write(boolean check) throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        Status status = this.status();
        if(status.isResume()) {
            headers.put(HttpHeaders.CONTENT_RANGE, "bytes "
                    + status.getCurrent()
                    + "-" + (this.getLocal().attributes().getSize() - 1)
                    + "/" + this.getLocal().attributes().getSize()
            );
        }
        headers.put(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
        try {
            return this.write(check, headers);
        }
        catch(HttpResponseException e) {
            if(e.getStatusCode() == HttpStatus.SC_EXPECTATION_FAILED) {
                // Retry with the Expect header removed
                headers.remove(HTTP.EXPECT_DIRECTIVE);
                return this.write(check, headers);
            }
            else {
                throw e;
            }
        }
    }

    private ResponseOutputStream<Void> write(boolean check, final Map<String, String> headers) throws IOException {
        if(check) {
            this.getSession().check();
        }
        // Submit store call to background thread
        final DelayedHttpEntityCallable<Void> command = new DelayedHttpEntityCallable<Void>() {
            /**
             *
             * @return The ETag returned by the server for the uploaded object
             */
            public Void call(AbstractHttpEntity entity) throws IOException {
                entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, getLocal().getMimeType()));
                getSession().getClient().put(toURL(), entity, headers);
                return null;
            }

            public long getContentLength() {
                return getLocal().attributes().getSize() - status().getCurrent();
            }
        };
        return this.write(command);
    }

    @Override
    public String toURL() {
        if(this.attributes().isDirectory()) {
            return super.toURL() + Path.DELIMITER;
        }
        return super.toURL();
    }

    @Override
    public String toHttpURL() {
        return this.toURL();
    }
}