package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.local.Application;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeFalse;

public class SMAppServiceApplicationLoginRegistryTest {

    @Test
    public void testRegister() {
        assumeFalse(Factory.Platform.osversion.matches("(10|11|12)\\..*"));
        assertFalse(new SMAppServiceApplicationLoginRegistry().register(
                new Application("bundle.helper")));
    }

    @Test
    public void testUnregister() {
        assumeFalse(Factory.Platform.osversion.matches("(10|11|12)\\..*"));
        assertFalse(new SMAppServiceApplicationLoginRegistry().unregister(
                new Application("bundle.helper")));
    }
}