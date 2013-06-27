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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.SardineExceptionMappingService;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;

import java.io.IOException;

import com.googlecode.sardine.impl.SardineException;
import com.googlecode.sardine.impl.handler.VoidResponseHandler;
import com.googlecode.sardine.impl.methods.HttpPropFind;

/**
 * @version $Id$
 */
public class DAVSession extends HttpSession<DAVClient> {

    private DAVClient client;

    public DAVSession(Host h) {
        super(h);
    }

    @Override
    public DAVClient connect(final HostKeyController key) throws BackgroundException {
        client = new DAVClient(host, this.http());
        return client;
    }

    @Override
    public void login(final LoginController prompt) throws BackgroundException {
        client.setCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword(),
                // Windows credentials. Provide empty string for NTLM domain by default.
                Preferences.instance().getProperty("webdav.ntlm.workstation"),
                Preferences.instance().getProperty("webdav.ntlm.domain"));
        if(host.getCredentials().validate(host.getProtocol())) {
            // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
            client.enablePreemptiveAuthentication(this.getHost().getHostname());
        }
        try {
            try {
                client.execute(new HttpHead(this.toURL(this.home())), new VoidResponseHandler());
                this.message(Locale.localizedString("Login successful", "Credentials"));
            }
            catch(SardineException e) {
                if(e.getStatusCode() == HttpStatus.SC_FORBIDDEN
                        || e.getStatusCode() == HttpStatus.SC_NOT_FOUND
                        || e.getStatusCode() == HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE
                        || e.getStatusCode() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                    // Possibly only HEAD requests are not allowed
                    client.execute(new HttpPropFind(this.toURL(this.home())), new VoidResponseHandler());
                }
                else {
                    throw new SardineExceptionMappingService().map(e);
                }
            }
        }
        catch(SardineException e) {
            throw new SardineExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isWriteTimestampSupported() {
        return false;
    }

    @Override
    public boolean isMetadataSupported() {
        return true;
    }

    @Override
    public String toHttpURL(final Path path) {
        return this.toURL(path);
    }
}