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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UserDateFormatterFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id:$
 */
public class SwiftHpUrlProvider extends SwiftUrlProvider {
    private static final Logger log = Logger.getLogger(SwiftUrlProvider.class);

    private PathContainerService containerService
            = new PathContainerService();

    private HostPasswordStore store;

    private SwiftSession session;

    public SwiftHpUrlProvider(final SwiftSession session) {
        this(session, PasswordStoreFactory.get());
    }

    public SwiftHpUrlProvider(final SwiftSession session, final HostPasswordStore store) {
        super(session, Collections.<Region, AccountInfo>emptyMap(), store);
        this.session = session;
        this.store = store;
    }

    @Override
    protected DescriptiveUrl createTempUrl(final Region region, final Path file, final int seconds) {
        final String path = region.getStorageUrl(
                containerService.getContainer(file).getName(), containerService.getKey(file)).getRawPath();
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.SECOND, seconds);
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
                return DescriptiveUrl.EMPTY;
            }
            final String signature = String.format("%s:%s:%s",
                    tenant, accesskey,
                    ServiceUtils.signWithHmacSha1(secret,
                            String.format("GET\n%d\n%s", expiry.getTimeInMillis() / 1000, path))
            );
            //Compile the temporary URL
            return new DescriptiveUrl(URI.create(String.format("https://%s%s?temp_url_sig=%s&temp_url_expires=%d",
                    region.getStorageUrl().getHost(), path, signature, expiry.getTimeInMillis() / 1000)),
                    DescriptiveUrl.Type.signed,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Signed", "S3"))
                            + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                            UserDateFormatterFactory.get().getShortFormat(expiry.getTimeInMillis()))
            );
        }
        else {
            log.warn("Missing tenant in user credentials to sign temporary URL");
            return DescriptiveUrl.EMPTY;
        }
    }
}
