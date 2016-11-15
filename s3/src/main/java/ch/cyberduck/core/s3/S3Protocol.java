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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class S3Protocol extends AbstractProtocol {
    private static final Logger log = Logger.getLogger(S3Protocol.class);

    @Override
    public String getName() {
        return "S3";
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("S3 (Amazon Simple Storage Service)", "S3");
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
    public String[] getSchemes() {
        return new String[]{this.getScheme().name(), "s3"};
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
    public Set<Location.Name> getRegions() {
        return new HashSet<Location.Name>(Arrays.asList(
                new S3LocationFeature.S3Region("us-east-1"),
                new S3LocationFeature.S3Region("us-east-2"),
                new S3LocationFeature.S3Region("us-west-1"),
                new S3LocationFeature.S3Region("us-west-2"),
                new S3LocationFeature.S3Region("eu-west-1"),
                new S3LocationFeature.S3Region("eu-central-1"),
                new S3LocationFeature.S3Region("ap-southeast-1"),
                new S3LocationFeature.S3Region("ap-southeast-2"),
                new S3LocationFeature.S3Region("ap-northeast-1"),
                new S3LocationFeature.S3Region("ap-northeast-2"),
                new S3LocationFeature.S3Region("ap-south-1"),
                new S3LocationFeature.S3Region("sa-east-1")
        ));
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
    public String favicon() {
        // Return static icon as endpoint has no favicon configured
        return this.icon();
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
                log.warn(String.format("Unsupported authentication context %s", protocol.getAuthorization()));
                return S3Protocol.AuthenticationHeaderSignatureVersion.valueOf(
                        PreferencesFactory.get().getProperty("s3.signature.version"));
            }
        }

        public abstract HashAlgorithm getHashAlgorithm();
    }

    @Override
    public String getAuthorization() {
        return PreferencesFactory.get().getProperty("s3.signature.version");
    }
}
