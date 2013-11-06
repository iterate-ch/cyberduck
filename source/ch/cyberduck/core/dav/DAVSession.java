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
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.http.RedirectCallback;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.log4j.Logger;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.SardineRedirectStrategy;
import com.github.sardine.impl.handler.VoidResponseHandler;

/**
 * @version $Id$
 */
public class DAVSession extends HttpSession<DAVClient> {
    private static final Logger log = Logger.getLogger(DAVSession.class);

    private RedirectCallback redirect
            = new PreferencesRedirectCallback();

    public DAVSession(Host h) {
        super(h);
    }

    public DAVSession(final Host host, final X509TrustManager manager) {
        super(host, manager);
    }

    public DAVSession(final Host host, final X509TrustManager manager, final RedirectCallback redirect) {
        super(host, manager);
        this.redirect = redirect;
    }

    @Override
    public DAVClient connect(final HostKeyController key) throws BackgroundException {
        final AbstractHttpClient client = super.connect();
        client.setRedirectStrategy(new SardineRedirectStrategy() {
            @Override
            protected boolean isRedirectable(final String method) {
                return redirect.redirect(method);
            }
        });
        return new DAVClient(new HostUrlProvider(false).get(host), client);
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt, final Cache cache) throws BackgroundException {
        client.setCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword(),
                // Windows credentials. Provide empty string for NTLM domain by default.
                Preferences.instance().getProperty("webdav.ntlm.workstation"),
                Preferences.instance().getProperty("webdav.ntlm.domain"));
        if(host.getCredentials().validate(host.getProtocol(), new LoginOptions())) {
            if(Preferences.instance().getBoolean("webdav.basic.preemptive")) {
                // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
                client.enablePreemptiveAuthentication(this.getHost().getHostname());
            }
            else {
                client.disablePreemptiveAuthentication();
            }
        }
        try {
            final Path home = new DefaultHomeFinderService(this).find();
            try {
                client.execute(new HttpHead(new DAVPathEncoder().encode(home)), new VoidResponseHandler());
            }
            catch(SardineException e) {
                if(e.getStatusCode() == HttpStatus.SC_FORBIDDEN
                        || e.getStatusCode() == HttpStatus.SC_NOT_FOUND
                        || e.getStatusCode() == HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE
                        || e.getStatusCode() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                    log.warn(String.format("Failed HEAD request to %s with %s. Retry with PROPFIND.",
                            host, e.getResponsePhrase()));
                    // Possibly only HEAD requests are not allowed
                    cache.put(home.getReference(), this.list(home, new DisabledListProgressListener()));
                }
                else if(e.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                    if(Preferences.instance().getBoolean("webdav.basic.preemptive")) {
                        log.warn(String.format("Disable preemptive authentication for %s due to failure %s",
                                host, e.getResponsePhrase()));
                        client.disablePreemptiveAuthentication();
                        client.execute(new HttpHead(new DAVPathEncoder().encode(home)), new VoidResponseHandler());
                    }
                    else {
                        throw new DAVExceptionMappingService().map(e);
                    }
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
        if(super.alert()) {
            return Preferences.instance().getBoolean("webdav.basic.preemptive");
        }
        return false;
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        return new DAVListService(this).list(file, listener);
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Touch.class) {
            return (T) new DefaultTouchFeature(this);
        }
        if(type == Directory.class) {
            return (T) new DAVDirectoryFeature(this);
        }
        if(type == Read.class) {
            return (T) new DAVReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new DAVWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DAVUploadFeature(this);
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
        if(type == Find.class) {
            return (T) new DAVFindFeature(this);
        }
        return super.getFeature(type);
    }
}