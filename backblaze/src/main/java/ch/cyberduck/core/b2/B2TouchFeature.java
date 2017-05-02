package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.serializer.PathAttributesDictionary;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;

import java.util.Collections;
import java.util.EnumSet;

import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.BaseB2Response;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2TouchFeature implements Touch<BaseB2Response> {

    private Write<BaseB2Response> writer;

    public B2TouchFeature(final B2Session session) {
        this.writer = new B2WriteFeature(session);
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        status.setChecksum(writer.checksum().compute(new NullInputStream(0L), status));
        status.setMime(new MappingMimeTypeService().getMime(file.getName()));
        status.setMetadata(Collections.singletonMap(
                X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(System.currentTimeMillis()))
        );
        final StatusOutputStream<BaseB2Response> out = writer.write(file, status, new DisabledConnectionCallback());
        new DefaultStreamCloser().close(out);
        final Path p = new Path(file.getParent(), file.getName(), EnumSet.of(Path.Type.file),
                new PathAttributesDictionary().deserialize(file.attributes().serialize(SerializerFactory.get())));
        p.attributes().setVersionId(((B2FileResponse) out.getStatus()).getFileId());
        return p;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }

    @Override
    public B2TouchFeature withWriter(final Write<BaseB2Response> writer) {
        this.writer = writer;
        return this;
    }
}
