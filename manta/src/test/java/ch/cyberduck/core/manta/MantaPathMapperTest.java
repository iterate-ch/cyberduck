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

import ch.cyberduck.core.Credentials;

import org.junit.Test;

import com.joyent.manta.client.MantaObjectResponse;

import static org.junit.Assert.*;

public class MantaPathMapperTest {

    private MantaPathMapper buildMapper(final String username, final String homePath) {
        return SessionFactory.create(new Credentials(username), homePath).pathMapper;
    }

    @Test
    public void testCheckingRootFolderPermissions() {
        final MantaPathMapper pm = buildMapper("account", "");

        assertTrue(pm.isUserWritable(new MantaObjectResponse("/account/public")));
        assertTrue(pm.isWorldReadable(new MantaObjectResponse("/account/public")));

        assertTrue(pm.isUserWritable(new MantaObjectResponse("/account/stor")));
        assertFalse(pm.isWorldReadable(new MantaObjectResponse("/account/stor")));
    }

    @Test
    public void testNormalizingHomePathsForNormalAccounts() {
        assertEquals("/account", buildMapper("account", "").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account", "~").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account", "~~").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account", "/~~").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account", "~~/").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account", "/~~/").getNormalizedHomePath().getAbsolute());
        assertEquals("/account/public", buildMapper("account", "~/public").getNormalizedHomePath().getAbsolute());
        assertEquals("/account/public", buildMapper("account", "/~/public").getNormalizedHomePath().getAbsolute());
        assertEquals("/account/public", buildMapper("account", "~~/public").getNormalizedHomePath().getAbsolute());
        assertEquals("/account/public", buildMapper("account", "/~~/public").getNormalizedHomePath().getAbsolute());
    }

    @Test
    public void testNormalizingHomePathsForSubAccounts() {
        assertEquals("/account", buildMapper("account/sub", "").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account/sub", "~").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account/sub", "~~").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account/sub", "/~~").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account/sub", "~~/").getNormalizedHomePath().getAbsolute());
        assertEquals("/account", buildMapper("account/sub", "/~~/").getNormalizedHomePath().getAbsolute());
        assertEquals("/account/public", buildMapper("account/sub", "~/public").getNormalizedHomePath().getAbsolute());
        assertEquals("/account/public", buildMapper("account/sub", "/~/public").getNormalizedHomePath().getAbsolute());
        assertEquals("/account/public", buildMapper("account/sub", "~~/public").getNormalizedHomePath().getAbsolute());
        assertEquals("/account/public", buildMapper("account/sub", "/~~/public").getNormalizedHomePath().getAbsolute());
    }
}
