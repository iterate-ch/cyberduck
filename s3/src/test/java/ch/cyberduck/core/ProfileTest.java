package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.s3.S3Protocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProfileTest {

    @Test
    public void testEquals() throws Exception {
        ProtocolFactory.register(new S3Protocol());
        assertEquals(ProfileReaderFactory.get().read(
                        new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile")),
                ProfileReaderFactory.get().read(
                        new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile")));
    }

    @Test
    public void testCompareTo() throws Exception {
        ProtocolFactory.register(new S3Protocol());
        assertEquals(0, ProfileReaderFactory.get().read(
                new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile")).compareTo(ProfileReaderFactory.get().read(
                new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile"))));
        assertNotEquals(0, ProfileReaderFactory.get().read(
                new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile")).compareTo(new TestProtocol()));
    }

    @Test
    public void testCompareMultipleRegions() throws Exception {
        ProtocolFactory.register(new S3Protocol());
        assertEquals(0, ProfileReaderFactory.get().read(
                new Local("../profiles/Verizon Cloud Storage (AMS1A).cyberduckprofile")).compareTo(ProfileReaderFactory.get().read(
                new Local("../profiles/Verizon Cloud Storage (IAD3A).cyberduckprofile"))));
    }
}