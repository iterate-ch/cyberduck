package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.sds.io.swagger.client.JSON;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import static org.junit.Assert.*;

public class TripleCryptConverterTest {

    @Test
    public void testFileKey() throws Exception {
        final JSON json = new JSON();
        final FileKey fileKey = TripleCryptConverter.toSwaggerFileKey(Crypto.generateFileKey(PlainFileKey.Version.AES256GCM));
        assertNotNull(fileKey.getIv());
        assertNotNull(fileKey.getKey());
        assertNull(fileKey.getTag());
        assertNotNull(fileKey.getVersion());
        final ObjectWriter writer = json.getContext(null).writerFor(FileKey.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeValue(out, fileKey);
        final ByteBuffer buffer = ByteBuffer.wrap(out.toByteArray());
        final ObjectReader reader = json.getContext(null).readerFor(FileKey.class);
        assertEquals(fileKey, reader.readValue(buffer.array()));
    }
}
