package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.EnumSet;

import com.joyent.manta.client.MantaMetadata;
import com.joyent.manta.client.MantaObject;
import com.joyent.manta.client.MantaObjectResponse;
import com.joyent.manta.http.MantaHttpHeaders;

import static org.junit.Assert.*;

/**
 * Created by tomascelaya on 5/24/17.
 */
public class MantaPathMapperTest {

    private MantaPathMapper buildMapperWithUserAndDefaultPath(final String username, final String homePath) {
        return new MantaSession(
                        new Host(
                                new MantaProtocol(),
                                "fake.manta.test",
                                443,
                                homePath,
                                new Credentials(username))).pathMapper;
    }

    @Test
    public void testCheckingRootFolderPermissions() {
        final MantaPathMapper pm = buildMapperWithUserAndDefaultPath("account", "");

        assertTrue(pm.isUserWritable(new MantaObjectResponse("/account/public")));
        assertTrue(pm.isWorldReadable(new MantaObjectResponse("/account/public")));

        assertTrue(pm.isUserWritable(new MantaObjectResponse("/account/stor")));
        assertFalse(pm.isWorldReadable(new MantaObjectResponse("/account/stor")));
    }

    @Test
    public void testNormalizingHomePathsForNormalAccounts() {
        assertEquals("/account", buildMapperWithUserAndDefaultPath("account", "").getNormalizedHomePath());
        assertEquals("/account", buildMapperWithUserAndDefaultPath("account", "~").getNormalizedHomePath());
        assertEquals("/account", buildMapperWithUserAndDefaultPath("account", "~~").getNormalizedHomePath());
        assertEquals("/account", buildMapperWithUserAndDefaultPath("account", "/~~").getNormalizedHomePath());
        assertEquals("/account", buildMapperWithUserAndDefaultPath("account", "~~/").getNormalizedHomePath());
        assertEquals("/account", buildMapperWithUserAndDefaultPath("account", "/~~/").getNormalizedHomePath());
        assertEquals("/account/public", buildMapperWithUserAndDefaultPath("account", "~/public").getNormalizedHomePath());
        assertEquals("/account/stor", buildMapperWithUserAndDefaultPath("account", "/~~/stor").getNormalizedHomePath());
    }

    @Test
    public void testNormalizingHomePathsForSubAccounts() {
        assertEquals("/account", buildMapperWithUserAndDefaultPath("account/sub", "").getNormalizedHomePath());
    }
}
