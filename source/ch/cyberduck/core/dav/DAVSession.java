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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.IOResumeException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.sardine.impl.SardineException;
import com.googlecode.sardine.impl.handler.VoidResponseHandler;
import com.googlecode.sardine.impl.methods.HttpPropFind;

/**
 * @version $Id$
 */
public class DAVSession extends HttpSession<DAVClient> {
    private static final Logger log = Logger.getLogger(DAVSession.class);

    public DAVSession(Host h) {
        super(h);
    }

    @Override
    public DAVClient connect(final HostKeyController key) throws BackgroundException {
        return new DAVClient(host, super.connect());
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        client.setCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword(),
                // Windows credentials. Provide empty string for NTLM domain by default.
                Preferences.instance().getProperty("webdav.ntlm.workstation"),
                Preferences.instance().getProperty("webdav.ntlm.domain"));
        if(host.getCredentials().validate(host.getProtocol(), new LoginOptions())) {
            if(Preferences.instance().getBoolean("webdav.basic.preemptive")) {
                // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
                client.enablePreemptiveAuthentication(this.getHost().getHostname());
            }
        }
        try {
            try {
                client.execute(new HttpHead(new DAVPathEncoder().encode(this.home())), new VoidResponseHandler());
            }
            catch(SardineException e) {
                if(e.getStatusCode() == HttpStatus.SC_FORBIDDEN
                        || e.getStatusCode() == HttpStatus.SC_NOT_FOUND
                        || e.getStatusCode() == HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE
                        || e.getStatusCode() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                    // Possibly only HEAD requests are not allowed
                    client.execute(new HttpPropFind(new DAVPathEncoder().encode(this.home())), new VoidResponseHandler());
                }
                else {
                    throw new DAVExceptionMappingService().map(e);
                }
            }
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean alert() throws BackgroundException {
        return Preferences.instance().getBoolean("webdav.basic.preemptive");
    }

    @Override
    public String toHttpURL(final Path path) {
        return this.toURL(path);
    }

    @Override
    public boolean exists(final Path path) throws BackgroundException {
        if(super.exists(path)) {
            return true;
        }
        try {
            if(path.attributes().isDirectory()) {
                // Parent directory may not be accessible. Issue #5662
                try {
                    return this.getClient().exists(new DAVPathEncoder().encode(path));
                }
                catch(SardineException e) {
                    throw new DAVExceptionMappingService().map("Cannot read file attributes", e, path);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e, path);
                }
            }
        }
        catch(NotfoundException e) {
            return false;
        }
        return false;
    }

    @Override
    public void delete(final List<Path> files, final LoginController prompt) throws BackgroundException {
        new DAVDeleteFeature(this).delete(files);
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        return new DAVListService(this).list(file);
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            this.getClient().createDirectory(new DAVPathEncoder().encode(file));
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public void rename(final Path file, final Path renamed) throws BackgroundException {
        try {
            this.getClient().move(new DAVPathEncoder().encode(file), new DAVPathEncoder().encode(renamed));
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        Map<String, String> headers = new HashMap<String, String>();
        if(status.isResume()) {
            headers.put(HttpHeaders.RANGE, "bytes=" + status.getCurrent() + "-");
        }
        try {
            return this.getClient().get(new DAVPathEncoder().encode(file), headers);
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Download failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, file);
        }
    }

    @Override
    public void download(final Path file, final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status) throws BackgroundException {
        OutputStream out = null;
        InputStream in = null;
        try {
            in = this.read(file, status);
            out = file.getLocal().getOutputStream(status.isResume());
            this.download(file, in, out, throttle, listener, status);
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Download failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, file);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public void upload(final Path file, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) throws BackgroundException {
        try {
            InputStream in = null;
            ResponseOutputStream<Void> out = null;
            try {
                in = file.getLocal().getInputStream();
                if(status.isResume()) {
                    long skipped = in.skip(status.getCurrent());
                    log.info(String.format("Skipping %d bytes", skipped));
                    if(skipped < status.getCurrent()) {
                        throw new IOResumeException(String.format("Skipped %d bytes instead of %d", skipped, status.getCurrent()));
                    }
                }
                out = this.write(file, status);
                this.upload(file, out, in, throttle, listener, status);
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
            throw new DAVExceptionMappingService().map("Upload failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }

    @Override
    public ResponseOutputStream<Void> write(final Path file, final TransferStatus status) throws BackgroundException {
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
        return this.write(file, headers, status);
    }

    private ResponseOutputStream<Void> write(final Path file, final Map<String, String> headers, final TransferStatus status)
            throws BackgroundException {
        // Submit store call to background thread
        final DelayedHttpEntityCallable<Void> command = new DelayedHttpEntityCallable<Void>() {
            /**
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public Void call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    getClient().put(new DAVPathEncoder().encode(file), entity, headers);
                }
                catch(SardineException e) {
                    if(e.getStatusCode() == HttpStatus.SC_EXPECTATION_FAILED) {
                        // Retry with the Expect header removed
                        headers.remove(HTTP.EXPECT_DIRECTIVE);
                        return this.call(entity);
                    }
                    else {
                        throw new DAVExceptionMappingService().map("Upload failed", e, file);
                    }
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
                }
                return null;
            }

            @Override
            public long getContentLength() {
                return status.getLength() - status.getCurrent();
            }
        };
        return this.write(file, command);
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Delete.class) {
            return (T) new DAVDeleteFeature(this);
        }
        if(type == Headers.class) {
            return (T) new DAVHeadersFeature(this);
        }
        if(type == Copy.class) {
            return (T) new DAVCopyFeature(this);
        }
        return super.getFeature(type, prompt);
    }
}