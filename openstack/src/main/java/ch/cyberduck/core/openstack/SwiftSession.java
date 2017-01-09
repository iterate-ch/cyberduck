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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
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
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.method.AuthenticationRequest;
import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

public class SwiftSession extends HttpSession<Client> {
    private static final Logger log = Logger.getLogger(SwiftSession.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final SwiftRegionService regionService
            = new SwiftRegionService(this);

    private final SwiftDistributionConfiguration cdn
            = new SwiftDistributionConfiguration(this, regionService);

    protected final Map<Region, AccountInfo> accounts
            = new HashMap<Region, AccountInfo>();

    /**
     * Preload account info
     */
    private boolean accountPreload = preferences.getBoolean("openstack.account.preload");

    /**
     * Preload CDN configuration
     */
    private boolean cdnPreload = preferences.getBoolean("openstack.cdn.preload");

    /**
     * Preload container size
     */
    private boolean containerPreload = preferences.getBoolean("openstack.container.size.preload");

    public SwiftSession(final Host host) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), host.getHostname()), new DefaultX509KeyManager());
    }

    public SwiftSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    public SwiftSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, proxy);
    }

    public SwiftSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final SocketFactory socketFactory) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, socketFactory);
    }

    @Override
    public Client connect(final HostKeyCallback key) throws BackgroundException {
        // Always inject new pool to builder on connect because the pool is shutdown on disconnect
        final HttpClientBuilder pool = builder.build(this);
        pool.disableContentCompression();
        return new Client(pool.build());
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.disconnect();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            super.logout();
        }
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
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
            if(host.getCredentials().isPassed()) {
                log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
                return;
            }
            if(accountPreload) {
                final ThreadPool<AccountInfo> pool = new DefaultThreadPool<AccountInfo>("accounts");
                try {
                    for(Region region : client.getRegions()) {
                        pool.execute(new Callable<AccountInfo>() {
                            @Override
                            public AccountInfo call() throws IOException {
                                final AccountInfo info = client.getAccountInfo(region);
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Signing key is %s", info.getTempUrlKey()));
                                }
                                accounts.put(region, info);
                                return info;
                            }
                        });
                    }
                }
                finally {
                    // Shutdown gracefully
                    pool.shutdown(true);
                }
            }
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
            return new SwiftContainerListService(this, regionService,
                    new SwiftLocationFeature.SwiftRegion(host.getRegion()), cdnPreload, containerPreload).list(directory, listener);
        }
        else {
            return new SwiftObjectListService(this, regionService).list(directory, listener);
        }
    }

    public SwiftSession withAccountPreload(final boolean preload) {
        this.accountPreload = preload;
        return this;
    }

    public SwiftSession withCdnPreload(final boolean preload) {
        this.cdnPreload = preload;
        return this;
    }

    public SwiftSession withContainerPreload(final boolean preload) {
        this.containerPreload = preload;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Read.class) {
            return (T) new SwiftReadFeature(this, regionService);
        }
        if(type == Write.class) {
            return (T) new SwiftLargeUploadWriteFeature(this, regionService, new SwiftSegmentService(this, regionService));
        }
        if(type == Upload.class) {
            return (T) new SwiftThresholdUploadService(this, regionService, new SwiftWriteFeature(this, regionService));
        }
        if(type == Directory.class) {
            return (T) new SwiftDirectoryFeature(this, regionService, new SwiftWriteFeature(this, regionService));
        }
        if(type == Delete.class) {
            return (T) new SwiftMultipleDeleteFeature(this, new SwiftSegmentService(this, regionService), regionService);
        }
        if(type == Headers.class) {
            return (T) new SwiftMetadataFeature(this, regionService);
        }
        if(type == Copy.class) {
            return (T) new SwiftCopyFeature(this, regionService);
        }
        if(type == Move.class) {
            return (T) new SwiftMoveFeature(this, regionService);
        }
        if(type == Touch.class) {
            return (T) new SwiftTouchFeature(this, regionService);
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
            return (T) new SwiftUrlProvider(this, accounts, regionService);
        }
        if(type == AttributesFinder.class) {
            return (T) new SwiftAttributesFinderFeature(this, regionService);
        }
        if(type == Home.class) {
            return (T) new SwiftHomeFinderService(this);
        }
        return super._getFeature(type);
    }
}
