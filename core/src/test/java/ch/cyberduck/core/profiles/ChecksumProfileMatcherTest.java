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
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChecksumProfileMatcherTest {

    @Test
    public void compare() throws Exception {
        // Local only profile
        assertFalse(new ChecksumProfileMatcher(Stream.of(new ProfileDescription("Profile.cyberduckprofile", Checksum.NONE, null)).collect(Collectors.toList()))
            .compare(new ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null)).isPresent());
        // Managed profile
        assertFalse(new ChecksumProfileMatcher(Stream.of(new ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null)).collect(Collectors.toList()))
            .compare(new ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null)).isPresent());
        assertTrue(new ChecksumProfileMatcher(Stream.of(new ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null) {
            @Override
            public boolean isLatest() {
                return false;
            }

            @Override
            public Local getProfile() {
                return new NullLocal("Profile.cyberduckprofile");
            }
        }).collect(Collectors.toList()))
            .compare(new ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), null)).isPresent());
    }
}
