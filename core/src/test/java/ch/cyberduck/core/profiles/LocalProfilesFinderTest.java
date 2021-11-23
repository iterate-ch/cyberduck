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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertFalse;

public class LocalProfilesFinderTest {

    @Test
    public void find() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new TestProtocol() {
            @Override
            public Type getType() {
                return Type.s3;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        })));
        final Profile profile = reader.read(
            new Local("src/test/resources/Test S3 (HTTP).cyberduckprofile")
        );
        final LocalProfilesFinder finder = new LocalProfilesFinder(ProtocolFactory.get(), new Local("src/test/resources/"));
        final Set<ProfileDescription> stream = finder.find();
        assertFalse(stream.isEmpty());
    }
}
