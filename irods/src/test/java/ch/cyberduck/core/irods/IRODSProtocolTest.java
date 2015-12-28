/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.irods;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IRODSProtocolTest {

    @Test
    public void testGetPrefix() throws Exception {
        assertEquals("ch.cyberduck.core.irods.IRODS", new IRODSProtocol().getPrefix());
    }
}