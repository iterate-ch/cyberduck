package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;

public class AzureUrlProvider implements UrlProvider {
    private static final Logger log = LogManager.getLogger(AzureUrlProvider.class);

    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    private final AzureSession session;
    private final HostPasswordStore store;

    public AzureUrlProvider(final AzureSession session) {
        this(session, PasswordStoreFactory.get());
    }

    public AzureUrlProvider(final AzureSession session, final HostPasswordStore store) {
        this.session = session;
        this.store = store;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
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

    protected Long getExpiry(final int seconds) {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.SECOND, seconds);
        return expiry.getTimeInMillis();
    }

    private final class SharedAccessSignatureUrl extends DescriptiveUrl {
        private final Path file;
        private final Long expiry;

        public SharedAccessSignatureUrl(final Path file, final Long expiry) {
            super(EMPTY);
            this.file = file;
            this.expiry = expiry;
        }

        @Override
        public String getUrl() {
            final String secret = store.findLoginPassword(session.getHost());
            if(StringUtils.isBlank(secret)) {
                if(log.isWarnEnabled()) {
                    log.warn("No secret found in password store required to sign temporary URL");
                }
                return DescriptiveUrl.EMPTY.getUrl();
            }
            final String token = new BlobSasImplUtil(new BlobServiceSasSignatureValues(
                    OffsetDateTime.now().plus(Duration.ofMillis(expiry)), new BlobSasPermission().setReadPermission(true)), containerService.getContainer(file).getName())
                    .generateSas(new StorageSharedKeyCredential(session.getHost().getCredentials().getUsername(),
                            secret), null);
            return String.format("%s://%s%s?%s",
                    Scheme.https.name(), session.getHost().getHostname(), URIEncoder.encode(file.getAbsolute()), token);
        }

        @Override
        public String getHelp() {
            return MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3"),
                    UserDateFormatterFactory.get().getMediumFormat(expiry));
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
