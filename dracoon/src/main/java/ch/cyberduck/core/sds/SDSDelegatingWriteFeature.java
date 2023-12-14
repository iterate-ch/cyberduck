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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class SDSDelegatingWriteFeature implements MultipartWrite<Node> {
    private static final Logger log = LogManager.getLogger(SDSDelegatingWriteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final Write<Node> proxy;

    private final PathContainerService containerService
            = new SDSPathContainerService();

    public SDSDelegatingWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final Write<Node> proxy) {
        this.session = session;
        this.nodeid = nodeid;
        this.proxy = proxy;
    }

    @Override
    public StatusOutputStream<Node> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(containerService.getContainer(file))) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return encrypting writer for %s", file));
            }
            // File key is set in encryption bulk feature if container is encrypted
            return new TripleCryptWriteFeature(session, nodeid, proxy).write(file, status, callback);
        }
        return proxy.write(file, status, callback);
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        if(SDSAttributesAdapter.isEncrypted(containerService.getContainer(file).attributes())) {
            return new TripleCryptWriteFeature(session, nodeid, proxy).append(file, status);
        }
        return proxy.append(file, status);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        if(SDSAttributesAdapter.isEncrypted(containerService.getContainer(file).attributes())) {
            return new TripleCryptWriteFeature(session, nodeid, proxy).checksum(file, status);
        }
        return proxy.checksum(file, status);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        new SDSTouchFeature(session, nodeid).preflight(file.getParent(), file.getName());
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return proxy.features(file);
    }
}
