package ch.cyberduck.core.cryptomator.impl.uvf;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Seed digests observed for vaults in Universal Vault Format to reject outdated or forged vault metadata. Persisted in
 * the keychain outside of the vault storage. Pins are only ever added, never replaced or removed.
 *
 * @see <a href="https://github.com/encryption-alliance/unified-vault-format/blob/develop/implementation-notes.md#seed-pinning">Seed Pinning</a>
 */
public class UVFSeedPins {
    private static final Logger log = LogManager.getLogger(UVFSeedPins.class);

    /**
     * Length of seed digests
     */
    private static final int DIGEST_LENGTH = 32;
    private static final String DELIMITER = ",";

    /**
     * Serialize updates of pins in this process
     */
    private static final Object lock = new Object();

    private final PasswordStore keychain;

    public UVFSeedPins() {
        this(PasswordStoreFactory.get());
    }

    public UVFSeedPins(final PasswordStore keychain) {
        this.keychain = keychain;
    }

    /**
     * @param bookmark Connection
     * @param home     Vault directory
     * @return Seed digests that must be present in vault metadata
     */
    public Collection<byte[]> get(final Host bookmark, final Path home) throws AccessDeniedException {
        synchronized(lock) {
            final List<byte[]> digests = new ArrayList<>();
            for(String pin : this.read(bookmark, home)) {
                digests.add(Base64.getDecoder().decode(pin));
            }
            return digests;
        }
    }

    /**
     * Add to pins of vault. Pins added concurrently from outdated vault metadata are kept.
     *
     * @param bookmark Connection
     * @param home     Vault directory
     * @param digests  Seed digests of successfully validated vault metadata
     */
    public void add(final Host bookmark, final Path home, final Collection<byte[]> digests) throws AccessDeniedException {
        synchronized(lock) {
            final Set<String> pins = this.read(bookmark, home);
            boolean modified = false;
            for(byte[] digest : digests) {
                modified |= pins.add(Base64.getEncoder().encodeToString(digest));
            }
            if(modified) {
                log.debug("Save {} seed pins for {}", pins.size(), home);
                keychain.addPassword(service(bookmark), account(bookmark, home), String.join(DELIMITER, pins));
            }
        }
    }

    private Set<String> read(final Host bookmark, final Path home) throws AccessDeniedException {
        final Set<String> pins = new LinkedHashSet<>();
        final String value = keychain.getPassword(service(bookmark), account(bookmark, home));
        for(String pin : StringUtils.split(StringUtils.defaultString(value), DELIMITER)) {
            if(isValid(pin)) {
                pins.add(pin);
            }
            else {
                log.warn("Ignore invalid seed pin {} for {}", pin, home);
            }
        }
        return pins;
    }

    private static boolean isValid(final String pin) {
        try {
            return Base64.getDecoder().decode(pin).length == DIGEST_LENGTH;
        }
        catch(IllegalArgumentException e) {
            return false;
        }
    }

    private static String service(final Host bookmark) {
        return String.format("Cryptomator Seed Pins (%s)", bookmark.getCredentials().getUsername());
    }

    /**
     * @return Same as for the vault passphrase
     */
    private static String account(final Host bookmark, final Path home) {
        return new DefaultUrlProvider(bookmark).toUrl(home, EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl();
    }
}
