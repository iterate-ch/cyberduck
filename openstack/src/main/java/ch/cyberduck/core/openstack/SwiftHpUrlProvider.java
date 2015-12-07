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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.UserDateFormatterFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;

import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id$
 */
public class SwiftHpUrlProvider extends SwiftUrlProvider {
    private static final Logger log = Logger.getLogger(SwiftUrlProvider.class);

    private PathContainerService containerService
            = new SwiftPathContainerService();

    private HostPasswordStore store;

    private SwiftSession session;

    public SwiftHpUrlProvider(final SwiftSession session) {
        this(session, PasswordStoreFactory.get());
    }

    public SwiftHpUrlProvider(final SwiftSession session, final HostPasswordStore store) {
        super(session, Collections.<Region, AccountInfo>emptyMap());
        this.session = session;
        this.store = store;
    }

    @Override
    protected DescriptiveUrlBag sign(final Region region, final Path file, final long expiry) {
        final String path = region.getStorageUrl(
                containerService.getContainer(file).getName(), containerService.getKey(file)).getRawPath();
        final Credentials credentials = session.getHost().getCredentials();
        if(StringUtils.contains(credentials.getUsername(), ':')) {
            if(log.isInfoEnabled()) {
                log.info("Using account secret key to sign");
            }
            final String tenant = StringUtils.split(credentials.getUsername(), ':')[0];
            final String accesskey = StringUtils.split(credentials.getUsername(), ':')[1];
            // HP Cloud Object Storage Temporary URLs require the user's Tenant ID and Access Key ID
            // to be prepended to the signature. Using the secret key to sign.
            final String secret = store.find(session.getHost());
            if(StringUtils.isBlank(secret)) {
                log.warn("No secret found in keychain required to sign temporary URL");
                return DescriptiveUrlBag.empty();
            }
            final String body = String.format("GET\n%d\n%s", expiry, path);
            final String signature = String.format("%s:%s:%s", tenant, accesskey, this.sign(secret, body));
            //Compile the temporary URL
            final DescriptiveUrlBag list = new DescriptiveUrlBag();
            for(Scheme scheme : Arrays.asList(Scheme.http, Scheme.https)) {
                list.add(new DescriptiveUrl(URI.create(String.format("%s://%s%s?temp_url_sig=%s&temp_url_expires=%d",
                        scheme.name(), region.getStorageUrl().getHost(), path, signature, expiry)),
                        DescriptiveUrl.Type.signed,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Signed", "S3"))
                                + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                                UserDateFormatterFactory.get().getShortFormat(expiry))
                ));
            }
            return list;
        }
        else {
            log.warn("Missing tenant in user credentials to sign temporary URL");
            return DescriptiveUrlBag.empty();
        }
    }
}
