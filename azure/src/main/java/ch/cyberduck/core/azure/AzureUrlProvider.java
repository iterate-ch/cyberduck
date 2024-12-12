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
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.preferences.HostPreferences;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

public class AzureUrlProvider implements UrlProvider {

    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    private final AzureSession session;

    public AzureUrlProvider(final AzureSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file, final EnumSet<DescriptiveUrl.Type> types) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        // In one hour
        list.add(this.toSignedUrl(file, (int) TimeUnit.HOURS.toSeconds(1)));
        // Default signed URL expiring in 24 hours.
        list.add(this.toSignedUrl(file, (int) TimeUnit.SECONDS.toSeconds(
                new HostPreferences(session.getHost()).getInteger("s3.url.expire.seconds"))));
        // 1 Week
        list.add(this.toSignedUrl(file, (int) TimeUnit.DAYS.toSeconds(7)));
        // 1 Month
        list.add(this.toSignedUrl(file, (int) TimeUnit.DAYS.toSeconds(30)));
        // 1 Year
        list.add(this.toSignedUrl(file, (int) TimeUnit.DAYS.toSeconds(365)));
        return list;
    }

    private DescriptiveUrl toSignedUrl(final Path file, int seconds) {
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
            try {
                if(!session.isConnected()) {
                    return DescriptiveUrl.EMPTY.getUrl();
                }
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                final String token;
                if(containerService.isContainer(file)) {
                    token = container.generateSharedAccessSignature(this.getPolicy(expiry), null);
                }
                else {
                    final CloudBlob blob = container.getBlobReferenceFromServer(containerService.getKey(file));
                    token = blob.generateSharedAccessSignature(this.getPolicy(expiry), null);
                }
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
        public String getHelp() {
            return MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3"),
                    UserDateFormatterFactory.get().getMediumFormat(expiry.getTimeInMillis()));
        }

        @Override
        public Type getType() {
            return DescriptiveUrl.Type.signed;
        }

        @Override
        public String getPreview() {
            return MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                    LocaleFactory.localizedString("Pre-Signed", "S3"));
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            if(!super.equals(o)) {
                return false;
            }
            final SharedAccessSignatureUrl that = (SharedAccessSignatureUrl) o;
            return Objects.equals(file, that.file) && Objects.equals(expiry, that.expiry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), file, expiry);
        }
    }
}
