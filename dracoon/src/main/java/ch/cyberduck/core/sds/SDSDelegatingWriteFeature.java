package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.sds.triplecrypt.CryptoWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

public class SDSDelegatingWriteFeature implements MultipartWrite<VersionId> {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final Write<VersionId> proxy;

    private final PathContainerService containerService
            = new SDSPathContainerService();

    public SDSDelegatingWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final Write<VersionId> proxy) {
        this.session = session;
        this.nodeid = nodeid;
        this.proxy = proxy;
    }

    @Override
    public StatusOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(nodeid.isEncrypted(file)) {
            // File key is set in encryption bulk feature if container is encrypted
            return new CryptoWriteFeature(session, proxy).write(file, status, callback);
        }
        return proxy.write(file, status, callback);
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(nodeid.isEncrypted(file)) {
            return new CryptoWriteFeature(session, proxy).append(file, length, cache);
        }
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
        if(containerService.getContainer(file).attributes().getCustom().containsKey(SDSAttributesFinderFeature.KEY_ENCRYPTED)) {
            return new CryptoWriteFeature(session, proxy).checksum(file);
        }
        return proxy.checksum(file);
    }
}
