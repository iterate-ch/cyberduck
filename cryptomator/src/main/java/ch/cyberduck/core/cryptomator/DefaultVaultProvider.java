package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.impl.uvf.DefaultUVFVaultMetadataProvider;
import ch.cyberduck.core.cryptomator.impl.uvf.UVFVault;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.impl.v8.MasterkeyVaultMetadataProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.core.vault.VaultProvider;
import ch.cyberduck.core.vault.VaultUnlockException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.JWEObject;

/**
 * Cryptomator vault implementation
 */
public class DefaultVaultProvider implements VaultProvider {
    private static final Logger log = LogManager.getLogger(DefaultVaultProvider.class);

    /**
     * List of known metadata filenames.
     */
    private final Map<String, VaultMetadata.Type> markers;

    public DefaultVaultProvider(final Session<?> session) {
        this.markers = ImmutableMap.of(
                HostPreferencesFactory.get(session.getHost()).getProperty("cryptomator.vault.masterkey.filename"), VaultMetadata.Type.V8,
                HostPreferencesFactory.get(session.getHost()).getProperty("cryptomator.vault.config.filename.uvf"), VaultMetadata.Type.UVF
        );
    }

    @Override
    public VaultMetadata matches(final Path file) {
        if(markers.keySet().stream().anyMatch(marker -> file.getName().equals(marker))) {
            return new VaultMetadata(markers.get(file.getName()));
        }
        return null;
    }

    @Override
    public VaultMetadata find(final Path directory, final Find find, final ListProgressListener listener) throws BackgroundException {
        for(String marker : markers.keySet()) {
            final Path m = new Path(directory, marker, EnumSet.of(Path.Type.file));
            if(find.find(m, listener)) {
                return new VaultMetadata(markers.get(marker));
            }
        }
        return null;
    }

    @Override
    public AbstractVault load(final Session<?> session, final Path directory, final VaultMetadata metadata, final VaultCredentials credentials) throws BackgroundException {
        final AbstractVault vault;
        switch(metadata.type) {
            case V8:
                vault = new CryptomatorVault(directory);
                vault.load(session, new MasterkeyVaultMetadataProvider(credentials));
                break;
            case UVF:
                vault = new UVFVault(directory);
                try {
                    vault.load(session, new DefaultUVFVaultMetadataProvider(JWEObject.parse(
                            new String(new ContentReader(session).readBytes(vault.getMasterkeyPath()), StandardCharsets.US_ASCII)), credentials));
                }
                catch(ParseException e) {
                    throw new VaultUnlockException(e.getMessage());
                }
                break;
            default:
                log.error("Unknown vault type {}", metadata.type);
                throw new UnsupportedException(metadata.type.toString());
        }
        log.debug("Read UVF metadata {}", vault.getMasterkeyPath());
        return vault;
    }

    @Override
    public AbstractVault create(final Session<?> session, final String region, final Path directory, final VaultMetadata metadata, final VaultCredentials credentials) throws BackgroundException {
        final AbstractVault vault;
        switch(metadata.type) {
            case V8:
                vault = new CryptomatorVault(directory);
                final MasterkeyVaultMetadataProvider masterkey = new MasterkeyVaultMetadataProvider(credentials);
                vault.create(session, region, masterkey);
                vault.load(session, masterkey);
                break;
            case UVF:
                vault = new UVFVault(directory);
                vault.create(session, region, new DefaultUVFVaultMetadataProvider(credentials));
                try {
                    vault.load(session, new DefaultUVFVaultMetadataProvider(JWEObject.parse(
                            new String(new ContentReader(session).readBytes(vault.getMasterkeyPath()), StandardCharsets.US_ASCII)), credentials));
                }
                catch(ParseException e) {
                    throw new VaultUnlockException(e.getMessage());
                }
                break;
            default:
                log.error("Unknown vault type {}", metadata.type);
                throw new UnsupportedException(metadata.type.toString());
        }
        return vault;
    }
}