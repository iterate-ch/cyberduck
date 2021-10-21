package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ChunkListSHA256ChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        assertEquals("07_pjo8c6JFhSFRWmiD19FCNarQCW4crezqCgu91sSo",
                new ChunkListSHA256ChecksumCompute().compute(new NullInputStream(0), new TransferStatus()).hash);
    }

    @Test
    public void testDigest() throws Exception {
        final byte[] bytes = "1".getBytes(StandardCharsets.UTF_8);
        assertEquals("HbkcOE7OuVb-kpfNRqNanXMu9LKEVu2cIVIB0Me6Q_Y", new ChunkListSHA256ChecksumCompute()
                .compute(new ByteArrayInputStream(bytes), new TransferStatus().withLength(bytes.length)).hash);
        assertEquals("07_pjo8c6JFhSFRWmiD19FCNarQCW4crezqCgu91sSo", new ChunkListSHA256ChecksumCompute()
                .compute(new ByteArrayInputStream(new byte[0]), new TransferStatus().withLength(0L)).hash);
    }
}