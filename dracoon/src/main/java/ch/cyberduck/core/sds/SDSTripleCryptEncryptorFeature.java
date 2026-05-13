package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.sds.io.swagger.client.JSON;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.fasterxml.jackson.databind.ObjectWriter;

public class SDSTripleCryptEncryptorFeature {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public static ByteBuffer generateFileKey() throws BackgroundException {
        return toBuffer(TripleCryptConverter.toSwaggerFileKey(Crypto.generateFileKey(PlainFileKey.Version.AES256GCM)));
    }

    private static ByteBuffer toBuffer(final FileKey fileKey) throws BackgroundException {
        final ObjectWriter writer = new JSON().getContext(null).writerFor(FileKey.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writer.writeValue(out, fileKey);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return ByteBuffer.wrap(out.toByteArray());
    }

    public SDSTripleCryptEncryptorFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    public boolean isEncrypted(final Path file) throws BackgroundException {
        final PathAttributes attr = file.attributes();
        if(attr.getCustom().containsKey(SDSAttributesFinderFeature.KEY_ENCRYPTED)) {
            return SDSAttributesAdapter.isEncrypted(attr);
        }
        return SDSAttributesAdapter.isEncrypted(new SDSAttributesFinderFeature(session, nodeid).find(file));
    }
}
