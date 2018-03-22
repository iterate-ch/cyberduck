package ch.cyberduck.core.privasphere;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

public class PrivasphereWriteFeature implements Write<Void> {

    private final PrivasphereSession session;
    private final Write<Void> proxy;

    public PrivasphereWriteFeature(final PrivasphereSession session, final Write<Void> proxy) {
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        //TODO handle resume
//            return new SMimeOutputStream<Void>(proxy.write(file, status, callback),
//                file.attributes().getCustom().get("certificate"),
//                file.attributes().getCustom().get("sessionKey"),
//                file.attributes().getCustom().get("securityParameters"),
//                file.attributes().getCustom().get("mimeType"),
//                file.attributes().getCustom().get("sha256"));
        return null;
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return proxy.append(file, length, cache);
    }

    @Override
    public boolean temporary() {
        return proxy.temporary();
    }

    @Override
    public boolean random() {
        return proxy.random();
    }

    @Override
    public ChecksumCompute checksum(final Path file) {
        return new DisabledChecksumCompute();
    }
}
