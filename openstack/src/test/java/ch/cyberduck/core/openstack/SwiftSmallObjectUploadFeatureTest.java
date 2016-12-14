package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.MessageDigest;
import java.util.EnumSet;

import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftSmallObjectUploadFeatureTest {

    @Test
    public void testDecorate() throws Exception {
        final NullInputStream n = new NullInputStream(1L);
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol()));
        assertSame(NullInputStream.class, new SwiftSmallObjectUploadFeature(new SwiftWriteFeature(
                session, new SwiftRegionService(session))).decorate(n, null).getClass());
    }

    @Test
    public void testDigest() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol()));
        assertNotNull(new SwiftSmallObjectUploadFeature(new SwiftWriteFeature(
                session, new SwiftRegionService(session))).digest());
    }

    @Test(expected = ChecksumException.class)
    public void testPostChecksumFailure() throws Exception {
        final StorageObject o = new StorageObject("f");
        o.setMd5sum("d41d8cd98f00b204e9800998ecf8427f");
        try {
            final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol()));
            new SwiftSmallObjectUploadFeature(new SwiftWriteFeature(
                    session, new SwiftRegionService(session))).post(
                    new Path("/f", EnumSet.of(Path.Type.file)), MessageDigest.getInstance("MD5"), o
            );
        }
        catch(ChecksumException e) {
            assertEquals("Upload f failed", e.getMessage());
            assertEquals("Mismatch between MD5 hash d41d8cd98f00b204e9800998ecf8427e of uploaded data and ETag d41d8cd98f00b204e9800998ecf8427f returned by the server.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testPostChecksum() throws Exception {
        final StorageObject o = new StorageObject("f");
        o.setMd5sum("d41d8cd98f00b204e9800998ecf8427e");
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol()));
        new SwiftSmallObjectUploadFeature(new SwiftWriteFeature(
                session, new SwiftRegionService(session))).post(
                new Path("/f", EnumSet.of(Path.Type.file)), MessageDigest.getInstance("MD5"), o
        );
    }
}
