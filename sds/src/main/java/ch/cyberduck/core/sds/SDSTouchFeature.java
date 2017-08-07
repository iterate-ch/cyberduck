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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.ObjectWriter;
import eu.ssp_europe.sds.crypto.Crypto;

public class SDSTouchFeature implements Touch<VersionId> {

    private final SDSSession session;
    private Write<VersionId> writer;

    public SDSTouchFeature(final SDSSession session) {
        this.session = session;
        this.writer = new SDSDelegatingWriteFeature(session, new SDSWriteFeature(session));
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(session.userAccount().getIsEncryptionEnabled()) {
                final FileKey fileKey = TripleCryptConverter.toSwaggerFileKey(Crypto.generateFileKey());
                final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                writer.writeValue(out, fileKey);
                status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
            }
            final StatusOutputStream<VersionId> out = writer.write(file, status, new DisabledConnectionCallback());
            out.close();
            return new Path(file.getParent(), file.getName(), file.getType(),
                    new PathAttributes(file.attributes()).withVersionId(out.getStatus().toString()));
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Cannot create file {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return !workdir.isRoot();
    }

    @Override
    public Touch<VersionId> withWriter(final Write<VersionId> writer) {
        this.writer = writer;
        return this;
    }
}
