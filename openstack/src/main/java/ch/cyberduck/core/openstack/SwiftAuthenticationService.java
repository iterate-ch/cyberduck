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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ch.iterate.openstack.swift.method.Authentication10UsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication11UsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20AccessKeySecretKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20RAXUsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20UsernamePasswordRequest;
import ch.iterate.openstack.swift.method.Authentication20UsernamePasswordTenantIdRequest;
import ch.iterate.openstack.swift.method.Authentication3UsernamePasswordProjectRequest;
import ch.iterate.openstack.swift.method.AuthenticationRequest;

public class SwiftAuthenticationService {
    private static final Logger log = LogManager.getLogger(SwiftAuthenticationService.class);

    public Set<? extends AuthenticationRequest> getRequest(final Host bookmark, final LoginCallback prompt)
            throws LoginCanceledException {
        final StringBuilder url = new StringBuilder();
        url.append(bookmark.getProtocol().getScheme().toString()).append("://");
        url.append(bookmark.getHostname());
        if(!(bookmark.getProtocol().getScheme().getPort() == bookmark.getPort())) {
            url.append(":").append(bookmark.getPort());
        }
        final String context = PathNormalizer.normalize(bookmark.getProtocol().getContext());
        // Custom authentication context
        url.append(context);
        if(bookmark.getProtocol().getDefaultHostname().endsWith("identity.api.rackspacecloud.com")
            || bookmark.getHostname().endsWith("identity.api.rackspacecloud.com")) {
            return Collections.singleton(new Authentication20RAXUsernameKeyRequest(
                    URI.create(url.toString()),
                bookmark.getCredentials().getUsername(), bookmark.getCredentials().getPassword(), null)
            );
        }
        final LoginOptions options = new LoginOptions(bookmark.getProtocol()).password(false).anonymous(false).publickey(false);
        if(context.contains("1.0")) {
            return Collections.singleton(new Authentication10UsernameKeyRequest(URI.create(url.toString()),
                bookmark.getCredentials().getUsername(), bookmark.getCredentials().getPassword()));
        }
        else if(context.contains("1.1")) {
            return Collections.singleton(new Authentication11UsernameKeyRequest(URI.create(url.toString()),
                bookmark.getCredentials().getUsername(), bookmark.getCredentials().getPassword()));
        }
        else if(context.contains("2.0")) {
            // Prompt for tenant
            final String user;
            final String tenant;
            if(StringUtils.contains(bookmark.getCredentials().getUsername(), ':')) {
                final String[] parts = StringUtils.splitPreserveAllTokens(bookmark.getCredentials().getUsername(), ':');
                tenant = parts[0];
                user = parts[1];
            }
            else {
                user = bookmark.getCredentials().getUsername();
                tenant = prompt.prompt(bookmark, bookmark.getCredentials().getUsername(),
                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                        LocaleFactory.localizedString("Tenant Name", "Mosso"), options
                                .usernamePlaceholder(LocaleFactory.localizedString("Tenant Name", "Mosso"))).getUsername();
                // Save tenant in username
                bookmark.getCredentials().setUsername(String.format("%s:%s", tenant, bookmark.getCredentials().getUsername()));
            }
            final Set<AuthenticationRequest> requests = new LinkedHashSet<>();
            requests.add(new Authentication20UsernamePasswordRequest(
                    URI.create(url.toString()),
                user, bookmark.getCredentials().getPassword(), tenant)
            );
            requests.add(new Authentication20UsernamePasswordTenantIdRequest(
                    URI.create(url.toString()),
                user, bookmark.getCredentials().getPassword(), tenant)
            );
            requests.add(new Authentication20AccessKeySecretKeyRequest(
                    URI.create(url.toString()),
                user, bookmark.getCredentials().getPassword(), tenant));
            return requests;
        }
        else if(context.contains("3")) {
            // Prompt for project
            final String user;
            final String project;
            final String domain;
            if(StringUtils.contains(bookmark.getCredentials().getUsername(), ':')) {
                final String[] parts = StringUtils.splitPreserveAllTokens(bookmark.getCredentials().getUsername(), ':');
                if(parts.length == 3) {
                    project = parts[0];
                    domain = parts[1];
                    user = parts[2];
                }
                else {
                    project = parts[0];
                    user = parts[1];
                    domain = prompt.prompt(bookmark, bookmark.getCredentials().getUsername(),
                            LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                            LocaleFactory.localizedString("Project Domain Name", "Mosso"), options
                                    .usernamePlaceholder(LocaleFactory.localizedString("Project Domain Name", "Mosso"))).getUsername();
                    // Save project name and domain in username
                    bookmark.getCredentials().setUsername(String.format("%s:%s:%s", project, domain, bookmark.getCredentials().getUsername()));
                }
            }
            else {
                user = bookmark.getCredentials().getUsername();
                final Credentials projectName = prompt.prompt(bookmark, bookmark.getCredentials().getUsername(),
                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                        LocaleFactory.localizedString("Project Name", "Mosso"), options
                                .usernamePlaceholder(LocaleFactory.localizedString("Project Name", "Mosso")));
                if(StringUtils.contains(bookmark.getCredentials().getUsername(), ':')) {
                    final String[] parts = StringUtils.splitPreserveAllTokens(projectName.getUsername(), ':');
                    project = parts[0];
                    domain = parts[1];
                }
                else {
                    project = projectName.getUsername();
                    domain = prompt.prompt(bookmark, bookmark.getCredentials().getUsername(),
                            LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                            LocaleFactory.localizedString("Project Domain Name", "Mosso"), options
                                    .usernamePlaceholder(LocaleFactory.localizedString("Project Domain Name", "Mosso"))).getUsername();
                }
                // Save project name and domain in username
                bookmark.getCredentials().setUsername(String.format("%s:%s:%s", project, domain, bookmark.getCredentials().getUsername()));
            }
            final Set<AuthenticationRequest> requests = new LinkedHashSet<>();
            requests.add(new Authentication3UsernamePasswordProjectRequest(
                    URI.create(url.toString()),
                user, bookmark.getCredentials().getPassword(), project, domain)
            );
            return requests;
        }
        else {
            log.warn(String.format("Unknown context version in %s. Default to v1 authentication.", context));
            // Default to 1.0
            return Collections.singleton(new Authentication10UsernameKeyRequest(URI.create(url.toString()),
                bookmark.getCredentials().getUsername(), bookmark.getCredentials().getPassword()));
        }
    }
}
