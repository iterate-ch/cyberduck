package ch.cyberduck.core.vault.registry;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.AbstractChecksumCompute;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import java.io.InputStream;

public class VaultRegistryChecksumCompute extends AbstractChecksumCompute implements ChecksumCompute {
    private final DefaultVaultRegistry registry;
    private final Session<?> session;
    private final ChecksumCompute proxy;

    public VaultRegistryChecksumCompute(final Session<?> session, final ChecksumCompute proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws ChecksumException {
        return proxy.compute(in, status);
    }
}
