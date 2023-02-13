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
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

public class AzureUrlProvider implements PromptUrlProvider<Void, Void> {

    private final PathContainerService containerService
        = new DirectoryDelimiterPathContainerService();

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
    public DescriptiveUrl toDownloadUrl(final Path file, final Void options, final PasswordCallback callback) {
        return this.createSignedUrl(file, new HostPreferences(session.getHost()).getInteger("s3.url.expire.seconds"));
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Void options, final PasswordCallback callback) {
        return DescriptiveUrl.EMPTY;
    }

    private DescriptiveUrl createSignedUrl(final Path file, int seconds) {
        return new SharedAccessSignatureUrl(file, this.getExpiry(seconds));
    }

    protected Calendar getExpiry(final int seconds) {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.SECOND, seconds);
        return expiry;
    }

    private final class SharedAccessSignatureUrl extends DescriptiveUrl {
        private final Path file;
        private final Calendar expiry;

        public SharedAccessSignatureUrl(final Path file, final Calendar expiry) {
            super(EMPTY);
            this.file = file;
            this.expiry = expiry;
        }

        @Override
        public String getUrl() {
            final CloudBlob blob;
            try {
                if(!session.isConnected()) {
                    return DescriptiveUrl.EMPTY.getUrl();
                }
                blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlobReferenceFromServer(containerService.getKey(file));
                final String token;
                token = blob.generateSharedAccessSignature(this.getPolicy(expiry), null);
                return String.format("%s://%s%s?%s",
                        Scheme.https.name(), session.getHost().getHostname(), URIEncoder.encode(file.getAbsolute()), token);
            }
            catch(InvalidKeyException | URISyntaxException | StorageException e) {
                return DescriptiveUrl.EMPTY.getUrl();
            }
        }

        private SharedAccessBlobPolicy getPolicy(final Calendar expiry) {
            final SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setSharedAccessExpiryTime(expiry.getTime());
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            return policy;
        }

        @Override
        public String getPreview() {
            return MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                    UserDateFormatterFactory.get().getMediumFormat(expiry.getTimeInMillis()));
        }

        @Override
        public Type getType() {
            return DescriptiveUrl.Type.signed;
        }

        @Override
        public String getHelp() {
            return MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                    LocaleFactory.localizedString("Pre-Signed", "S3"));
        }
    }
}
