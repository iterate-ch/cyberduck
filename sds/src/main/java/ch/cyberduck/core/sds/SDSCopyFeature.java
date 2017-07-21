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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.shared.DefaultCopyFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.ObjectWriter;
import eu.ssp_europe.sds.crypto.Crypto;

public class SDSCopyFeature extends DefaultCopyFeature {

    private final SDSSession session;

    private final PathContainerService containerService = new PathContainerService();

    public SDSCopyFeature(final SDSSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        try {
            final Path srcContainer = containerService.getContainer(source);
            final Path targetContainer = containerService.getContainer(target);
            if(srcContainer.getType().contains(Path.Type.vault) || targetContainer.getType().contains(Path.Type.vault)) {
                if(!srcContainer.equals(targetContainer)) {
                    final FileKey fileKey = TripleCryptConverter.toSwaggerFileKey(Crypto.generateFileKey());
                    final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    writer.writeValue(out, fileKey);
                    status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
                }
            }
            super.copy(source, target, status);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
