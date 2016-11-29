package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

public class AzureUrlProvider implements UrlProvider {

    private final PathContainerService containerService
            = new AzurePathContainerService();

    private final AzureSession session;

    public AzureUrlProvider(final AzureSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        list.addAll(new DefaultUrlProvider(session.getHost()).toUrl(file));
        if(file.isFile()) {
            list.add(this.createSignedUrl(file, 60 * 60));
            // Default signed URL expiring in 24 hours.
            list.add(this.createSignedUrl(file, PreferencesFactory.get().getInteger("s3.url.expire.seconds")));
            // Week
            list.add(this.createSignedUrl(file, 7 * 24 * 60 * 60));
            // Month
            list.add(this.createSignedUrl(file, 7 * 24 * 60 * 60 * 4));
        }
        return list;
    }

    private DescriptiveUrl createSignedUrl(final Path file, int seconds) {
        final CloudBlockBlob blob;
        try {
            if(!session.isConnected()) {
                return DescriptiveUrl.EMPTY;
            }
            blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                    .getBlockBlobReference(containerService.getKey(file));
        }
        catch(URISyntaxException | StorageException e) {
            return DescriptiveUrl.EMPTY;
        }
        final String token;
        try {
            token = blob.generateSharedAccessSignature(this.getPolicy(seconds), null);
        }
        catch(InvalidKeyException | StorageException e) {
            return DescriptiveUrl.EMPTY;
        }
        return new DescriptiveUrl(URI.create(String.format("%s://%s%s?%s",
                Scheme.https.name(), session.getHost().getHostname(), URIEncoder.encode(file.getAbsolute()), token)),
                DescriptiveUrl.Type.signed,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"))
                        + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getShortFormat(this.getExpiry(seconds))));
    }

    private SharedAccessBlobPolicy getPolicy(final int expiry) {
        final SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        policy.setSharedAccessExpiryTime(new Date(this.getExpiry(expiry)));
        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
        return policy;
    }

    protected Long getExpiry(final int seconds) {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.SECOND, seconds);
        return expiry.getTimeInMillis();
    }
}
