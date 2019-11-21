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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

public class AzureUrlProvider implements PromptUrlProvider<Void, Void> {

    private final PathContainerService containerService
        = new AzurePathContainerService();

    private final AzureSession session;

    public AzureUrlProvider(final AzureSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case download:
                return file.isFile();
        }
        return false;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Void options, final PasswordCallback callback) throws BackgroundException {
        return this.createSignedUrl(file, PreferencesFactory.get().getInteger("s3.url.expire.seconds"));
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Void options, final PasswordCallback callback) throws BackgroundException {
        throw new UnsupportedException();
    }

    private DescriptiveUrl createSignedUrl(final Path file, int seconds) throws BackgroundException {
        try {
            final CloudBlob blob;
            try {
                if(!session.isConnected()) {
                    return DescriptiveUrl.EMPTY;
                }
                blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                    .getBlobReferenceFromServer(containerService.getKey(file));
            }
            catch(URISyntaxException e) {
                return DescriptiveUrl.EMPTY;
            }
            final String token;
            try {
                token = blob.generateSharedAccessSignature(this.getPolicy(seconds), null);
            }
            catch(InvalidKeyException e) {
                return DescriptiveUrl.EMPTY;
            }
            return new DescriptiveUrl(URI.create(String.format("%s://%s%s?%s",
                Scheme.https.name(), session.getHost().getHostname(), URIEncoder.encode(file.getAbsolute()), token)),
                DescriptiveUrl.Type.signed,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"))
                    + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                    UserDateFormatterFactory.get().getShortFormat(this.getExpiry(seconds))));
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map(e);
        }
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
