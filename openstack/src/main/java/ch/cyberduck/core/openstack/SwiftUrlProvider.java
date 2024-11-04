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

import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.HostWebUrlProvider;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionUrlProvider;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.Constants;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

public class SwiftUrlProvider implements UrlProvider {
    private static final Logger log = LogManager.getLogger(SwiftUrlProvider.class);

    private final PathContainerService containerService
            = new DefaultPathContainerService();

    private final SwiftSession session;
    private final Map<Path, Set<Distribution>> distributions;
    private final Map<Region, AccountInfo> accounts;

    public SwiftUrlProvider(final SwiftSession session) {
        this(session, Collections.emptyMap());
    }

    public SwiftUrlProvider(final SwiftSession session, final Map<Region, AccountInfo> accounts) {
        this(session, accounts, Collections.emptyMap());
    }

    public SwiftUrlProvider(final SwiftSession session, final Map<Region, AccountInfo> accounts,
                            final Map<Path, Set<Distribution>> distributions) {
        this.session = session;
        this.accounts = accounts;
        this.distributions = distributions;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.isFile()) {
            Optional<Region> optional = accounts.keySet().stream().filter(r -> StringUtils.equals(r.getRegionId(),
                    file.attributes().getRegion())).findAny();
            if(!optional.isPresent()) {
                list.addAll(new DefaultUrlProvider(session.getHost()).toUrl(file));
            }
            else {
                final Region region = optional.get();
                list.addAll(new HostWebUrlProvider(session.getHost()).toUrl(file));
                list.add(new DescriptiveUrl(
                        URI.create(region.getStorageUrl(containerService.getContainer(file).getName(),
                                containerService.getKey(file)).toString()),
                        DescriptiveUrl.Type.provider,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                                session.getHost().getProtocol().getScheme().name().toUpperCase(Locale.ROOT))
                ));
                // In one hour
                list.add(this.toSignedUrl(region, file, (int) TimeUnit.HOURS.toSeconds(1)));
                // Default signed URL expiring in 24 hours.
                list.add(this.toSignedUrl(region, file, (int) TimeUnit.SECONDS.toSeconds(
                        new HostPreferences(session.getHost()).getInteger("s3.url.expire.seconds"))));
                // 1 Week
                list.add(this.toSignedUrl(region, file, (int) TimeUnit.DAYS.toSeconds(7)));
                // 1 Month
                list.add(this.toSignedUrl(region, file, (int) TimeUnit.DAYS.toSeconds(30)));
                // 1 Year
                list.add(this.toSignedUrl(region, file, (int) TimeUnit.DAYS.toSeconds(365)));
            }
        }
        // Filter by matching container name
        final Optional<Set<Distribution>> filtered = distributions.entrySet().stream().filter(entry ->
                        new SimplePathPredicate(containerService.getContainer(file)).test(entry.getKey()))
                .map(Map.Entry::getValue).findFirst();
        if(filtered.isPresent()) {
            // Add CloudFront distributions
            for(Distribution distribution : filtered.get()) {
                list.addAll(new DistributionUrlProvider(distribution).toUrl(file));
            }
        }
        return list;
    }

    protected DescriptiveUrl toSignedUrl(final Region region, final Path file, final int seconds) {
        if(!accounts.containsKey(region)) {
            log.warn("No account info for region {} available required to sign temporary URL", region);
            return DescriptiveUrl.EMPTY;
        }
        // OpenStack Swift Temporary URLs (TempURL) required the X-Account-Meta-Temp-URL-Key header be set on the Swift account. Used to sign.
        final AccountInfo info = accounts.get(region);
        final String tempUrlKey = info.getTempUrlKey();
        if(StringUtils.isBlank(tempUrlKey)) {
            log.warn("Missing X-Account-Meta-Temp-URL-Key header value to sign temporary URL");
            return DescriptiveUrl.EMPTY;
        }
        log.info("Using X-Account-Meta-Temp-URL-Key header value {} to sign", tempUrlKey);
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.SECOND, seconds);
        return new PresignedUrl(file, region, tempUrlKey, expiry);
    }

    private final class PresignedUrl extends DescriptiveUrl {
        private final Path file;
        private final Region region;
        private final String tempUrlKey;
        private final Calendar expiry;

        public PresignedUrl(final Path file, final Region region, final String tempUrlKey, final Calendar expiry) {
            super(EMPTY);
            this.file = file;
            this.region = region;
            this.tempUrlKey = tempUrlKey;
            this.expiry = expiry;
        }

        @Override
        public String getUrl() {
            final long epoch = expiry.getTimeInMillis() / 1000;
            final String signature;
            try {
                signature = this.sign(tempUrlKey,
                        String.format("GET\n%d\n%s", epoch, String.format("%s/%s/%s",
                                region.getStorageUrl().getRawPath(),
                                URIEncoder.encode(containerService.getContainer(file).getName()),
                                containerService.getKey(file))));
            }
            catch(NoSuchAlgorithmException | InvalidKeyException e) {
                return DescriptiveUrl.EMPTY.getUrl();
            }
            Scheme scheme = Scheme.valueOf(region.getStorageUrl().getScheme());
            final int port = region.getStorageUrl().getPort();
            return String.format("%s://%s%s%s?temp_url_sig=%s&temp_url_expires=%d",
                    scheme.name(), region.getStorageUrl().getHost(),
                    port == -1 ? StringUtils.EMPTY : port == scheme.getPort() ? StringUtils.EMPTY : String.format(":%d", port),
                    region.getStorageUrl(containerService.getContainer(file).getName(), containerService.getKey(file)).getRawPath(), signature,
                    epoch);
        }

        private String sign(final String secret, final String body) throws NoSuchAlgorithmException, InvalidKeyException {
            // Acquire an HMAC/SHA1 from the raw key bytes.
            final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                    Constants.HMAC_SHA1_ALGORITHM);
            // Acquire the MAC instance and initialize with the signing key.
            final Mac mac = Mac.getInstance(Constants.HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            return Hex.encodeHexString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
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
            final PresignedUrl that = (PresignedUrl) o;
            return Objects.equals(file, that.file) && Objects.equals(expiry, that.expiry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), file, expiry);
        }
    }
}
