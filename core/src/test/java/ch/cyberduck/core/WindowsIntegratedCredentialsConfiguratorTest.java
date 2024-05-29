package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class WindowsIntegratedCredentialsConfiguratorTest {

    @Test
    public void testConfigure() {
        assumeTrue(Factory.Platform.getDefault().equals(Factory.Platform.Name.linux));
        final Host bookmark = new Host(new TestProtocol());
        assertSame(bookmark.getCredentials(), new WindowsIntegratedCredentialsConfigurator().configure(bookmark));
    }

    @Test
    public void testConfigureWindows() {
        assumeTrue(Factory.Platform.getDefault().equals(Factory.Platform.Name.windows));
        final Host bookmark = new Host(new TestProtocol());
        final Credentials configured = new WindowsIntegratedCredentialsConfigurator().configure(bookmark);
        assertNotSame(bookmark.getCredentials(), configured);
        assertFalse(configured.getUsername().isEmpty());
    }
}