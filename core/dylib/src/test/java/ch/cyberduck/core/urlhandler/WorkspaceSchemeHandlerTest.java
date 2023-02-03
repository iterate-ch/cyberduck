package ch.cyberduck.core.urlhandler;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class WorkspaceSchemeHandlerTest {

    @Test
    public void testGetAllHandlers() {
        assumeTrue(Factory.Platform.osversion.matches("12\\..*"));
        final List<Application> list = new WorkspaceSchemeHandler(new LaunchServicesApplicationFinder()).getAllHandlers("http:/");
        assertFalse(list.isEmpty());
        for(Application application : list) {
            assertNotNull(application.getIdentifier());
        }
    }

    @Test
    @Ignore
    public void testSetDefaultHandler() {
        assumeTrue(Factory.Platform.osversion.matches("12\\..*"));
        final Application application = new Application("com.apple.finder");
        final WorkspaceSchemeHandler handler = new WorkspaceSchemeHandler(new LaunchServicesApplicationFinder());
        final String scheme = new AlphanumericRandomStringService().random();
        handler.setDefaultHandler(application, Collections.singletonList(scheme));
        assertTrue(handler.getAllHandlers(scheme).contains(application));
        assertEquals(application, handler.getDefaultHandler(scheme));
    }
}