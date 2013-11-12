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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id$
 */
public class SwiftUrlProvider implements UrlProvider {
    private static final Logger log = Logger.getLogger(SwiftUrlProvider.class);

    private PathContainerService containerService
            = new PathContainerService();

    private SwiftSession session;

    private HostPasswordStore store;

    private Map<Region, AccountInfo> accounts;

    public SwiftUrlProvider(final SwiftSession session) {
        this(session, Collections.<Region, AccountInfo>emptyMap());
    }

    public SwiftUrlProvider(final SwiftSession session, final Map<Region, AccountInfo> accounts) {
        this(session, accounts, PasswordStoreFactory.get());
    }

    public SwiftUrlProvider(final SwiftSession session, final Map<Region, AccountInfo> accounts, final HostPasswordStore store) {
        this.session = session;
        this.accounts = accounts;
        this.store = store;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.attributes().isFile()) {
            Region region = null;
            try {
                region = new SwiftRegionService(session).lookup(containerService.getContainer(file));
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure looking up region for %s %s", file, e.getMessage()));
            }
            if(null == region) {
                list.addAll(new DefaultUrlProvider(session.getHost()).toUrl(file));
            }
            else {
                list.add(new DescriptiveUrl(
                        URI.create(region.getStorageUrl(containerService.getContainer(file).getName(), containerService.getKey(file)).toString()),
                        DescriptiveUrl.Type.provider,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                                session.getHost().getProtocol().getScheme().name().toUpperCase(Locale.ROOT))
                ));
                list.add(this.createTempUrl(region, file, 60 * 60));
                // Default signed URL expiring in 24 hours.
                list.add(this.createTempUrl(region, file, Preferences.instance().getInteger("s3.url.expire.seconds")));
                // Week
                list.add(this.createTempUrl(region, file, 7 * 24 * 60 * 60));
            }
        }
        return list;
    }

    protected DescriptiveUrl createTempUrl(final Region region, final Path file, final int seconds) {
        final String path = region.getStorageUrl(
                containerService.getContainer(file).getName(), containerService.getKey(file)).getRawPath();
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.SECOND, seconds);
        if(!accounts.containsKey(region)) {
            log.warn(String.format("No account info for region %s available required to sign temporary URL", region));
            return DescriptiveUrl.EMPTY;
        }
        // OpenStack Swift Temporary URLs (TempURL) required the X-Account-Meta-Temp-URL-Key header
        // be set on the Swift account. Used to sign.
        final AccountInfo info = accounts.get(region);
        if(StringUtils.isBlank(info.getTempUrlKey())) {
            log.warn("Missing X-Account-Meta-Temp-URL-Key header value to sign temporary URL");
            return DescriptiveUrl.EMPTY;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Using X-Account-Meta-Temp-URL-Key header value %s to sign", info.getTempUrlKey()));
        }
        final String signature = ServiceUtils.signWithHmacSha1(info.getTempUrlKey(),
                String.format("GET\n%d\n%s", expiry.getTimeInMillis() / 1000, path));
        //Compile the temporary URL
        return new DescriptiveUrl(URI.create(String.format("https://%s%s?temp_url_sig=%s&temp_url_expires=%d",
                region.getStorageUrl().getHost(), path, signature, expiry.getTimeInMillis() / 1000)),
                DescriptiveUrl.Type.signed,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Signed", "S3"))
                        + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getShortFormat(expiry.getTimeInMillis()))
        );
    }
}