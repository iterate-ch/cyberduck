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

import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.serializer.Deserializer;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChecksumProfileMatcherTest {

    @Test
    public void compare() throws Exception {
        // Local only profile
        assertFalse(new ChecksumProfileMatcher(Stream.of(new ProfilesFinder.ProfileDescription("Profile.cyberduckprofile", Checksum.NONE, () -> null)))
            .compare(new ProfilesFinder.ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), () -> null)).isPresent());
        // Managed profile
        assertFalse(new ChecksumProfileMatcher(Stream.of(new ProfilesFinder.ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), () -> null)))
            .compare(new ProfilesFinder.ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), () -> null)).isPresent());
        assertTrue(new ChecksumProfileMatcher(Stream.of(new ProfilesFinder.ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), () -> null) {
            @Override
            public boolean isLatest() {
                return false;
            }

            @Override
            public Supplier<Profile> getProfile() {
                return new Supplier<Profile>() {
                    @Override
                    public Profile get() {
                        return new Profile(new TestProtocol(Scheme.dav), new Deserializer<String>() {
                            @Override
                            public String stringForKey(final String key) {
                                return null;
                            }

                            @Override
                            public String objectForKey(final String key) {
                                return null;
                            }

                            @Override
                            public <L> List<L> listForKey(final String key) {
                                return null;
                            }

                            @Override
                            public Map<String, String> mapForKey(final String key) {
                                return null;
                            }

                            @Override
                            public boolean booleanForKey(final String key) {
                                return false;
                            }

                            @Override
                            public List<String> keys() {
                                return null;
                            }
                        });
                    }
                };
            }
        }))
            .compare(new ProfilesFinder.ProfileDescription("Profile.cyberduckprofile", new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"), () -> null)).isPresent());
    }
}
