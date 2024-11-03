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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DelegatingSchedulerFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.method.AuthenticationRequest;
import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

public class SwiftSession extends HttpSession<Client> {
    private static final Logger log = LogManager.getLogger(SwiftSession.class);

    private final SwiftRegionService regionService
        = new SwiftRegionService(this);

    private final Map<Region, AccountInfo> accounts = new ConcurrentHashMap<>();
    private final Map<Path, Set<Distribution>> distributions = new ConcurrentHashMap<>();

    public SwiftSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected Client connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws ConnectionCanceledException {
        // Always inject new pool to builder on connect because the pool is shutdown on disconnect
        final HttpClientBuilder pool = builder.build(proxy, this, prompt);
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
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final Set<? extends AuthenticationRequest> options = new SwiftAuthenticationService().getRequest(host, prompt);
            for(Iterator<? extends AuthenticationRequest> iter = options.iterator(); iter.hasNext(); ) {
                try {
                    final AuthenticationRequest auth = iter.next();
                    if(log.isInfoEnabled()) {
                        log.info("Attempt authentication with {}", auth);
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
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SwiftListService(this, regionService);
        }
        if(type == Read.class) {
            return (T) new SwiftReadFeature(this, regionService);
        }
        if(type == MultipartWrite.class) {
            return (T) new SwiftLargeUploadWriteFeature(this, regionService, new SwiftSegmentService(this, regionService));
        }
        if(type == Write.class) {
            return (T) new SwiftWriteFeature(this, regionService);
        }
        if(type == Upload.class) {
            return (T) new SwiftThresholdUploadService(this, regionService, new SwiftWriteFeature(this, regionService));
        }
        if(type == Directory.class) {
            return (T) new SwiftDirectoryFeature(this, regionService, new SwiftWriteFeature(this, regionService));
        }
        if(type == Delete.class) {
            return (T) new SwiftThresholdDeleteFeature(this, new SwiftSegmentService(this, regionService), regionService);
        }
        if(type == Headers.class) {
            return (T) new SwiftMetadataFeature(this, regionService);
        }
        if(type == Metadata.class) {
            return (T) new SwiftMetadataFeature(this, regionService);
        }
        if(type == Copy.class) {
            return (T) new SwiftSegmentCopyService(this, regionService);
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
        if(type == DistributionConfiguration.class) {
            for(Region region : accounts.keySet()) {
                if(null == region.getCDNManagementUrl()) {
                    log.warn("Missing CDN Management URL for region {}", region.getRegionId());
                    return null;
                }
            }
            return (T) new SwiftDistributionConfiguration(this, regionService) {
                @Override
                public Distribution read(final Path container, final Distribution.Method method, final LoginCallback prompt) throws BackgroundException {
                    final Distribution distribution = super.read(container, method, prompt);
                    if(log.isDebugEnabled()) {
                        log.debug("Cache distribution {}", distribution);
                    }
                    // Replace previously cached value
                    final Set<Distribution> cached = distributions.getOrDefault(container, new HashSet<>());
                    cached.add(distribution);
                    distributions.put(container, cached);
                    return distribution;
                }
            };
        }
        if(type == UrlProvider.class) {
            return (T) new SwiftUrlProvider(this, accounts, distributions);
        }
        if(type == Find.class) {
            return (T) new SwiftFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new SwiftAttributesFinderFeature(this, regionService);
        }
        if(type == Scheduler.class) {
            if(new HostPreferences(host).getBoolean("openstack.accounts.preload")) {
                return (T) new DelegatingSchedulerFeature(
                        new SwiftAccountLoader(this) {
                            @Override
                            public Map<Region, AccountInfo> operate(final PasswordCallback callback, final Path container) throws BackgroundException {
                                final Map<Region, AccountInfo> result = super.operate(callback, container);
                                // Only executed single time
                                accounts.putAll(result);
                                return result;
                            }
                        },
                        new SwiftDistributionConfigurationLoader(this));
            }
            return null;
        }
        return super._getFeature(type);
    }
}
