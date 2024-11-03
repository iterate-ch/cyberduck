package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.auth.AWSCredentialsConfigurator;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.ETagComparisonService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class S3Protocol extends AbstractProtocol {
    private static final Logger log = LogManager.getLogger(S3Protocol.class);

    private final AWSCredentialsConfigurator credentials = new AWSCredentialsConfigurator(
            new AWSCredentialsProviderChain(
                    new ProfileCredentialsProvider(),
                    new EnvironmentVariableCredentialsProvider()
            )
    );

    @Override
    public String getName() {
        return "S3";
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("Amazon S3", "S3");
    }

    @Override
    public String getIdentifier() {
        return "s3";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return true;
    }

    @Override
    public boolean isPortConfigurable() {
        return true;
    }

    @Override
    public String getDefaultHostname() {
        return "s3.amazonaws.com";
    }

    @Override
    public String getSTSEndpoint() {
        return "https://sts.amazonaws.com/";
    }

    @Override
    public Set<Location.Name> getRegions(final List<String> regions) {
        return regions.stream().map(S3LocationFeature.S3Region::new).collect(Collectors.toSet());
    }

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Access Key ID", "S3");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Secret Access Key", "S3");
    }

    @Override
    public String getTokenPlaceholder() {
        return LocaleFactory.localizedString("MFA Authentication Code", "S3");
    }

    @Override
    public String favicon() {
        // Return static icon as endpoint has no favicon configured
        return this.icon();
    }

    @Override
    public String getAuthorization() {
        return PreferencesFactory.get().getProperty("s3.signature.version");
    }

    public enum AuthenticationHeaderSignatureVersion {
        AWS2 {
            @Override
            public HashAlgorithm getHashAlgorithm() {
                return HashAlgorithm.sha1;
            }
        },
        AWS4HMACSHA256 {
            @Override
            public HashAlgorithm getHashAlgorithm() {
                return HashAlgorithm.sha256;
            }

            @Override
            public String toString() {
                return "AWS4-HMAC-SHA256";
            }
        };

        public static AuthenticationHeaderSignatureVersion getDefault(final Protocol protocol) {
            try {
                return S3Protocol.AuthenticationHeaderSignatureVersion.valueOf(protocol.getAuthorization());
            }
            catch(IllegalArgumentException e) {
                log.warn("Unsupported authentication context {}", protocol.getAuthorization());
                return S3Protocol.AuthenticationHeaderSignatureVersion.valueOf(
                        PreferencesFactory.get().getProperty("s3.signature.version"));
            }
        }

        public abstract HashAlgorithm getHashAlgorithm();
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.storage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == PathContainerService.class) {
            return (T) new DirectoryDelimiterPathContainerService();
        }
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new ETagComparisonService(), ComparisonService.disabled);
        }
        if(type == CredentialsConfigurator.class) {
            return (T) credentials;
        }
        return super.getFeature(type);
    }
}
