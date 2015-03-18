package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.method.AuthenticationRequest;
import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id$
 */
public class SwiftSession extends HttpSession<Client> {
    private static final Logger log = Logger.getLogger(SwiftSession.class);

    private SwiftDistributionConfiguration cdn
            = new SwiftDistributionConfiguration(this);

    protected Map<Region, AccountInfo> accounts
            = new HashMap<Region, AccountInfo>();

    private final ThreadPool pool
            = new ThreadPool(5, "accounts");

    public SwiftSession(final Host h) {
        super(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public SwiftSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public Client connect(final HostKeyCallback key) throws BackgroundException {
        return new Client(this.builder().build());
    }

    @Override
    public HttpClientBuilder builder() {
        final HttpClientBuilder builder = super.builder();
        builder.disableContentCompression();
        return builder;
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.disconnect();
            pool.shutdown(false);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        try {
            final Set<? extends AuthenticationRequest> options = new SwiftAuthenticationService().getRequest(host, prompt);
            for(Iterator<? extends AuthenticationRequest> iter = options.iterator(); iter.hasNext(); ) {
                try {
                    final AuthenticationRequest auth = iter.next();
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Attempt authentication with %s", auth));
                    }
                    client.authenticate(auth);
                    break;
                }
                catch(GenericException failure) {
                    final BackgroundException reason = new SwiftExceptionMappingService().map(failure);
                    if(reason instanceof LoginFailureException
                            || reason instanceof AccessDeniedException
                            || reason instanceof InteroperabilityException) {
                        if(!iter.hasNext()) {
                            throw failure;
                        }
                    }
                    else {
                        throw failure;
                    }
                }
                cancel.verify();
            }
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    for(Region region : client.getRegions()) {
                        try {
                            final AccountInfo info = client.getAccountInfo(region);
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Signing key is %s", info.getTempUrlKey()));
                            }
                            accounts.put(region, info);
                        }
                        catch(IOException e) {
                            log.warn(String.format("Failure loading account info for region %s", region));
                        }
                    }
                }
            });
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            return new AttributedList<Path>(new SwiftContainerListService(this,
                    new SwiftLocationFeature.SwiftRegion(host.getRegion())).list(listener));
        }
        else {
            SwiftLocationFeature.SwiftRegion region = new SwiftLocationFeature.SwiftRegion(host.getRegion());
            if(Location.unknown.equals(region)) {
                region = new SwiftLocationFeature.SwiftRegion(directory.attributes().getRegion());
            }
            if(Location.unknown.equals(region)) {
                directory.attributes().setRegion(
                        new SwiftRegionService(this).lookup(this.getFeature(Location.class).getLocation(directory)).getRegionId());
            }
            return new SwiftObjectListService(this).list(directory, listener);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Read.class) {
            return (T) new SwiftReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new SwiftWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new SwiftThresholdUploadService(this);
        }
        if(type == Directory.class) {
            return (T) new SwiftDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new SwiftMultipleDeleteFeature(this);
        }
        if(type == Headers.class) {
            return (T) new SwiftMetadataFeature(this);
        }
        if(type == Copy.class) {
            return (T) new SwiftCopyFeature(this);
        }
        if(type == Move.class) {
            return (T) new SwiftMoveFeature(this);
        }
        if(type == Touch.class) {
            return (T) new SwiftTouchFeature(this);
        }
        if(type == Location.class) {
            return (T) new SwiftLocationFeature(this);
        }
        if(type == AnalyticsProvider.class) {
            return (T) new QloudstatAnalyticsProvider();
        }
        if(type == DistributionConfiguration.class) {
            for(Region region : accounts.keySet()) {
                if(null == region.getCDNManagementUrl()) {
                    log.warn(String.format("Missing CDN Management URL for region %s", region.getRegionId()));
                    return null;
                }
            }
            return (T) cdn;
        }
        if(type == UrlProvider.class) {
            if(host.getHostname().endsWith("identity.hpcloudsvc.com")) {
                return (T) new SwiftHpUrlProvider(this);
            }
            return (T) new SwiftUrlProvider(this, accounts);
        }
        if(type == Attributes.class) {
            return (T) new SwiftAttributesFeature(this);
        }
        if(type == Home.class) {
            return (T) new SwiftHomeFinderService(this);
        }
        return super.getFeature(type);
    }
}
