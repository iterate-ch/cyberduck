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

import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class WorkspaceSchemeHandlerTest {

    @Test
    public void testGetAllHandlers() {
        final List<Application> list = new WorkspaceSchemeHandler(new LaunchServicesApplicationFinder()).getAllHandlers("http:/");
        assertFalse(list.isEmpty());
        for(Application application : list) {
            assertNotNull(application.getIdentifier());
        }
    }

    @Test
    public void testSetDefaultHandler() {
        final Application application = new Application("ch.sudo.cyberduck");
        final WorkspaceSchemeHandler handler = new WorkspaceSchemeHandler(new LaunchServicesApplicationFinder());
        final String scheme = "com.googleusercontent.apps.996125414232-30v0nuldk4p54spra0k6gg3b8c8c9kib";
        handler.setDefaultHandler(application, Collections.singletonList(scheme));
        assertTrue(handler.getAllHandlers(scheme).contains(application));
        assertEquals(application, handler.getDefaultHandler(scheme));
    }
}