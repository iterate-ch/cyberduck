package ch.cyberduck.core.vault.registry;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultCopyFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryCopyFeature implements Copy {

    private final Session<?> session;
    private final Copy proxy;
    private final DefaultVaultRegistry registry;

    public VaultRegistryCopyFeature(final Session<?> session, final Copy proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        if(registry.find(session, source).equals(registry.find(session, target))) {
            // Move files inside vault. May use server side copy.
            registry.find(session, source).getFeature(session, Copy.class, proxy).copy(source, target, status);
        }
        else {
            // Use default copy feature. Will need to transfer using read and write features with encryption
            new DefaultCopyFeature(
                    registry.find(session, source).getFeature(session, Read.class, session._getFeature(Read.class)),
                    registry.find(session, target).getFeature(session, Write.class, session._getFeature(Write.class))
            ).copy(source, target, status);
        }
    }
}
