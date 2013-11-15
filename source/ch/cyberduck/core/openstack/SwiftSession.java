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

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.log4j.Logger;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class SwiftSession extends HttpSession<Client> {
    private static final Logger log = Logger.getLogger(SwiftSession.class);

    private SwiftDistributionConfiguration cdn
            = new SwiftDistributionConfiguration(this);

    protected Map<Region, AccountInfo> accounts
            = new HashMap<Region, AccountInfo>();

    private final ThreadFactory threadFactory
            = new NamedThreadFactory("account");

    public SwiftSession(Host h) {
        super(h);
    }

    public SwiftSession(final Host host, final X509TrustManager manager) {
        super(host, manager);
    }

    @Override
    public Client connect(final HostKeyController key) throws BackgroundException {
        return new Client(super.connect());
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt, final Cache cache) throws BackgroundException {
        try {
            client.authenticate(new SwiftAuthenticationService().getRequest(host, prompt));
            threadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    for(Region region : client.getRegions()) {
                        try {
                            final AccountInfo info = client.getAccountInfo(region);
                            accounts.put(region, info);
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Signing key is %s", info.getTempUrlKey()));
                            }
                        }
                        catch(IOException e) {
                            log.warn(String.format("Failure loading account info for region %s", region));
                        }
                    }
                }
            }).start();
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return new AttributedList<Path>(new SwiftContainerListService(this).list(listener));
        }
        else {
            return new SwiftObjectListService(this).list(file, listener);
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
            return (T) cdn;
        }
        if(type == UrlProvider.class) {
            if(host.getHostname().endsWith("identity.hpcloudsvc.com")) {
                return (T) new SwiftHpUrlProvider(this);
            }
            return (T) new SwiftUrlProvider(this, accounts);
        }
        if(type == Find.class) {
            return (T) new SwiftFindFeature(this);
        }
        if(type == Home.class) {
            return (T) new SwiftHomeFinderService(this);
        }
        return super.getFeature(type);
    }

}
