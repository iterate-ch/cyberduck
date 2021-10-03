package ch.cyberduck.core.profiles;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.junit.Test;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChecksumProfileMatcherTest {

    @Test
    public void testLocalOnly() throws Exception {
        // Local only profile
        final ProfileDescription local = new ProfileDescription(
                ProtocolFactory.get(), new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null);
        assertFalse(new ChecksumProfileMatcher(Stream.<ProfileDescription>empty().collect(Collectors.toSet())).compare(local).isPresent());
    }

    @Test
    public void testEqual() throws Exception {
        // Managed profile
        final ProfileDescription remote = new ProfileDescription(
                ProtocolFactory.get(), new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null) {
            @Override
            public boolean isLatest() {
                return true;
            }
        };
        final ProfileDescription local = new ProfileDescription(
                ProtocolFactory.get(), new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null);
        assertFalse(new ChecksumProfileMatcher(Stream.of(remote).collect(Collectors.toSet())).compare(local).isPresent());
    }

    @Test
    public void testNewerVersionFound() throws Exception {
        final ProfileDescription local = new ProfileDescription(ProtocolFactory.get(), new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null);
        final ProfileDescription remote = new ProfileDescription(
                ProtocolFactory.get(), new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null) {
            @Override
            public boolean isLatest() {
                return false;
            }

            @Override
            public Optional<Local> getFile() {
                return Optional.of(new NullLocal("Profile.cyberduckprofile"));
            }
        };
        assertTrue(new ChecksumProfileMatcher(Stream.of(remote).collect(Collectors.toSet())).compare(local).isPresent());
    }
}
