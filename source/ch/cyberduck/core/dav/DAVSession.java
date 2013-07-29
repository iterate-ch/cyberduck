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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.shared.DefaultTouchFeature;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;

import java.io.IOException;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.VoidResponseHandler;
import com.github.sardine.impl.methods.HttpPropFind;

/**
 * @version $Id$
 */
public class DAVSession extends HttpSession<DAVClient> {

    public DAVSession(Host h) {
        super(h);
    }

    @Override
    public DAVClient connect(final HostKeyController key) throws BackgroundException {
        return new DAVClient(new HostUrlProvider(false).get(host),
                super.connect());
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
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        return new DAVListService(this).list(file, listener);
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
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Touch.class) {
            return (T) new DefaultTouchFeature(this);
        }
        if(type == Read.class) {
            return (T) new DAVReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new DAVWriteFeature(this);
        }
        if(type == Delete.class) {
            return (T) new DAVDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new DAVMoveFeature(this);
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