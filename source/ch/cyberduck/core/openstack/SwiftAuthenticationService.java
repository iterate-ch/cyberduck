package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.URI;

import ch.iterate.openstack.swift.method.Authentication10UsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication11UsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20AccessKeySecretKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20RAXUsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20UsernamePasswordRequest;
import ch.iterate.openstack.swift.method.AuthenticationRequest;

/**
 * @version $Id$
 */
public class SwiftAuthenticationService {
    private static final Logger log = Logger.getLogger(SwiftAuthenticationService.class);

    /**
     * Default authentication version.
     */
    private String version;

    public SwiftAuthenticationService() {
        this(Preferences.instance().getProperty("openstack.authentication.context"));
    }

    public SwiftAuthenticationService(final String version) {
        this.version = version;
    }

    public AuthenticationRequest getRequest(final Host host, final LoginController prompt) throws LoginCanceledException {
        final Credentials credentials = host.getCredentials();
        final StringBuilder url = new StringBuilder();
        url.append(host.getProtocol().getScheme().toString()).append("://");
        url.append(host.getHostname());
        if(!(host.getProtocol().getScheme().getPort() == host.getPort())) {
            url.append(":").append(host.getPort());
        }
        if(host.getHostname().endsWith("identity.api.rackspacecloud.com")) {
            // Fix access to *.identity.api.rackspacecloud.com
            url.append("/v2.0/tokens");
            return new Authentication20RAXUsernameKeyRequest(
                    URI.create(url.toString()),
                    credentials.getUsername(), credentials.getPassword(), null
            );
        }
        final String context;
        if(StringUtils.isBlank(host.getProtocol().getContext())) {
            // Default to 1.0
            context = PathNormalizer.normalize(version);
        }
        else {
            context = PathNormalizer.normalize(host.getProtocol().getContext());
        }
        // Custom authentication context
        url.append(context);
        if(context.contains("1.0")) {
            return new Authentication10UsernameKeyRequest(URI.create(url.toString()),
                    credentials.getUsername(), credentials.getPassword());
        }
        else if(context.contains("1.1")) {
            return new Authentication11UsernameKeyRequest(URI.create(url.toString()),
                    credentials.getUsername(), credentials.getPassword());
        }
        else if(context.contains("2.0")) {
            // Prompt for tenant
            final String user;
            String tenant = null;
            if(StringUtils.contains(credentials.getUsername(), ':')) {
                tenant = StringUtils.split(credentials.getUsername(), ':')[0];
                user = StringUtils.split(credentials.getUsername(), ':')[1];
            }
            else {
                user = credentials.getUsername();
                final Credentials tenantCredentials = new TenantCredentials();
                final LoginOptions options = new LoginOptions();
                options.user = false;
                options.password = false;
                try {
                    prompt.prompt(host.getProtocol(), tenantCredentials,
                            LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                            LocaleFactory.localizedString("Tenant or project identifier", "Mosso"), options);
                    if(StringUtils.isNotBlank(tenantCredentials.getUsername())) {
                        tenant = tenantCredentials.getUsername();
                        // Save tenant in username
                        credentials.setUsername(String.format("%s:%s", tenant, credentials.getUsername()));
                    }
                }
                catch(LoginCanceledException e) {
                    // No tenant provided. Continue anyway.
                }
            }
            if(host.getHostname().endsWith("identity.hpcloudsvc.com")) {
                return new Authentication20AccessKeySecretKeyRequest(
                        URI.create(url.toString()),
                        user, credentials.getPassword(), tenant
                );
            }
            return new Authentication20UsernamePasswordRequest(
                    URI.create(url.toString()),
                    user, host.getCredentials().getPassword(), tenant
            );
        }
        else {
            log.warn(String.format("Unknown context version in %s. Default to v1 authentication.", context));
            // Default to 1.0
            return new Authentication10UsernameKeyRequest(URI.create(url.toString()),
                    credentials.getUsername(), credentials.getPassword());
        }
    }
}