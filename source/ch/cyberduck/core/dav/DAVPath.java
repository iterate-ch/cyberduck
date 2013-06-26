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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.SardineExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpPath;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.IOResumeException;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVPath extends HttpPath {
    private static final Logger log = Logger.getLogger(DAVPath.class);

    private final DAVSession session;

    public DAVPath(DAVSession s, Path parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    public DAVPath(DAVSession s, String path, int type) {
        super(s, path, type);
        this.session = s;
    }

    public DAVPath(DAVSession s, Path parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    public <T> DAVPath(DAVSession s, T dict) {
        super(s, dict);
        this.session = s;
    }

    @Override
    public DAVSession getSession() {
        return session;
    }

    @Override
    public void readSize() throws BackgroundException {
        if(this.attributes().isFile()) {
            try {
                session.message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                        this.getName()));

                this.readAttributes();
            }
            catch(SardineException e) {
                throw new SardineExceptionMappingService().map("Cannot read file attributes", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, this);
            }
        }
    }

    @Override
    public void readTimestamp() throws BackgroundException {
        if(this.attributes().isFile()) {
            try {
                session.message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                        this.getName()));

                this.readAttributes();
            }
            catch(SardineException e) {
                throw new SardineExceptionMappingService().map("Cannot read file attributes", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, this);
            }
        }
    }

    private void readAttributes() throws IOException {
        final List<DavResource> resources = session.getClient().list(URIEncoder.encode(this.getAbsolute()));
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
        this.attributes().setETag(resource.getEtag());
    }

    @Override
    public boolean exists() throws BackgroundException {
        if(super.exists()) {
            return true;
        }
        if(this.attributes().isDirectory()) {
            // Parent directory may not be accessible. Issue #5662
            try {
                return session.getClient().exists(URIEncoder.encode(this.getAbsolute()));
            }
            catch(SardineException e) {
                throw new SardineExceptionMappingService().map("Cannot read file attributes", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, this);
            }
        }
        return false;
    }

    @Override
    public void delete(final LoginController prompt) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    this.getName()));
            session.getClient().delete(URIEncoder.encode(this.getAbsolute()));
        }
        catch(SardineException e) {
            throw new SardineExceptionMappingService().map("Cannot delete {0}", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, this);
        }
    }

    @Override
    public AttributedList<Path> list() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            final AttributedList<Path> children = new AttributedList<Path>();

            final List<DavResource> resources = session.getClient().list(URIEncoder.encode(this.getAbsolute()));
            for(final DavResource resource : resources) {
                // Try to parse as RFC 2396
                final URI uri = resource.getHref();
                final DAVPath p = new DAVPath(session, this,
                        Path.getName(uri.getPath()), resource.isDirectory() ? DIRECTORY_TYPE : FILE_TYPE);
                p.readAttributes(resource);
                children.add(p);
            }
            return children;
        }
        catch(SardineException e) {
            log.warn(String.format("Directory listing failure for %s with failure %s", this, e.getMessage()));
            throw new SardineExceptionMappingService().map("Listing directory failed", e, this);
        }
        catch(IOException e) {
            log.warn(String.format("Directory listing failure for %s with failure %s", this, e.getMessage()));
            throw new DefaultIOExceptionMappingService().map(e, this);
        }
    }

    @Override
    public void mkdir() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                    this.getName()));

            session.getClient().createDirectory(URIEncoder.encode(this.getAbsolute()));
        }
        catch(SardineException e) {
            throw new SardineExceptionMappingService().map("Cannot create folder {0}", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, this);
        }
    }

    @Override
    public void readMetadata() throws BackgroundException {
        if(attributes().isFile()) {
            try {
                session.message(MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
                        this.getName()));

                final List<DavResource> resources = session.getClient().list(URIEncoder.encode(this.getAbsolute()));
                for(DavResource resource : resources) {
                    this.attributes().setMetadata(resource.getCustomProps());
                }
            }
            catch(SardineException e) {
                throw new SardineExceptionMappingService().map("Cannot read file attributes", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, this);
            }
        }
    }

    @Override
    public void writeMetadata(Map<String, String> meta) throws BackgroundException {
        if(attributes().isFile()) {
            try {

                session.message(MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                        this.getName()));

                session.getClient().setCustomProps(URIEncoder.encode(this.getAbsolute()),
                        meta, Collections.<java.lang.String>emptyList());
            }
            catch(SardineException e) {
                throw new SardineExceptionMappingService().map("Cannot write file attributes", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, this);
            }
            finally {
                this.attributes().clear(false, false, false, true);
            }
        }
    }

    @Override
    public void rename(final Path renamed) throws BackgroundException {
        try {

            session.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed.getName()));

            session.getClient().move(URIEncoder.encode(this.getAbsolute()), URIEncoder.encode(renamed.getAbsolute()));
        }
        catch(SardineException e) {
            throw new SardineExceptionMappingService().map("Cannot rename {0}", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, this);
        }
    }

    @Override
    public void copy(Path copy, BandwidthThrottle throttle, StreamListener listener, final TransferStatus status) throws BackgroundException {
        if(copy.getSession().equals(session)) {
            // Copy on same server
            try {

                session.message(MessageFormat.format(Locale.localizedString("Copying {0} to {1}", "Status"),
                        this.getName(), copy));

                if(attributes().isFile()) {
                    session.getClient().copy(URIEncoder.encode(this.getAbsolute()), URIEncoder.encode(copy.getAbsolute()));
                    listener.bytesSent(this.attributes().getSize());
                    status.setComplete();
                }
            }
            catch(SardineException e) {
                throw new SardineExceptionMappingService().map("Cannot copy {0}", e, this);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, this);
            }
        }
        else {
            // Copy to different host
            super.copy(copy, throttle, listener, status);
        }
    }

    @Override
    public InputStream read(final TransferStatus status) throws BackgroundException {
        Map<String, String> headers = new HashMap<String, String>();
        if(status.isResume()) {
            headers.put(HttpHeaders.RANGE, "bytes=" + status.getCurrent() + "-");
        }
        try {
            return session.getClient().get(URIEncoder.encode(this.getAbsolute()), headers);
        }
        catch(SardineException e) {
            throw new SardineExceptionMappingService().map("Download failed", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, this);
        }
    }

    @Override
    public void download(final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status) throws BackgroundException {
        OutputStream out = null;
        InputStream in = null;
        try {
            in = this.read(status);
            out = this.getLocal().getOutputStream(status.isResume());
            this.download(in, out, throttle, listener, status);
        }
        catch(SardineException e) {
            throw new SardineExceptionMappingService().map("Download failed", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, this);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public void upload(final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) throws BackgroundException {
        try {
            InputStream in = null;
            ResponseOutputStream<Void> out = null;
            try {
                in = this.getLocal().getInputStream();
                if(status.isResume()) {
                    long skipped = in.skip(status.getCurrent());
                    log.info(String.format("Skipping %d bytes", skipped));
                    if(skipped < status.getCurrent()) {
                        throw new IOResumeException(String.format("Skipped %d bytes instead of %d", skipped, status.getCurrent()));
                    }
                }
                out = this.write(status);
                this.upload(out, in, throttle, listener, status);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
            if(null != out) {
                out.getResponse();
            }
        }
        catch(SardineException e) {
            throw new SardineExceptionMappingService().map("Upload failed", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, this);
        }
    }

    @Override
    public ResponseOutputStream<Void> write(final TransferStatus status) throws BackgroundException {
        final Map<String, String> headers = new HashMap<String, String>();
        if(status.isResume()) {
            headers.put(HttpHeaders.CONTENT_RANGE, "bytes "
                    + status.getCurrent()
                    + "-" + (status.getLength() - 1)
                    + "/" + status.getLength()
            );
        }
        if(Preferences.instance().getBoolean("webdav.expect-continue")) {
            headers.put(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
        }
        return this.write(headers, status);
    }

    private ResponseOutputStream<Void> write(final Map<String, String> headers, final TransferStatus status)
            throws BackgroundException {
        // Submit store call to background thread
        final DelayedHttpEntityCallable<Void> command = new DelayedHttpEntityCallable<Void>() {
            /**
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public Void call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    session.getClient().put(URIEncoder.encode(getAbsolute()), entity, headers);
                }
                catch(SardineException e) {
                    if(e.getStatusCode() == HttpStatus.SC_EXPECTATION_FAILED) {
                        // Retry with the Expect header removed
                        headers.remove(HTTP.EXPECT_DIRECTIVE);
                        return this.call(entity);
                    }
                    else {
                        throw new SardineExceptionMappingService().map("Upload failed", e, DAVPath.this);
                    }
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload failed", e, DAVPath.this);
                }
                return null;
            }

            @Override
            public long getContentLength() {
                return status.getLength() - status.getCurrent();
            }
        };
        return this.write(command);
    }
}
