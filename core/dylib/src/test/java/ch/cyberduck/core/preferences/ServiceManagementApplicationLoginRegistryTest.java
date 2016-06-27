package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.local.Application;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ServiceManagementApplicationLoginRegistryTest {

    @Test
    public void testRegister() throws Exception {
        assertFalse(new ServiceManagementApplicationLoginRegistry().register(
                new Application("bundle.helper")));
    }

    @Test
    public void testUnregister() throws Exception {
        assertFalse(new ServiceManagementApplicationLoginRegistry().unregister(
                new Application("bundle.helper")));
    }
}