package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.i18n.RegexLocale;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SDSExceptionMappingServiceTest {

    @Test
    public void testMap() throws Exception {
        final BackgroundException e = new SDSExceptionMappingService().map(new ApiException("m", 403, Collections.emptyMap(),
            "{\"errorCode\" = -40761}"));
        assertEquals("-40761. Please contact your web hosting service provider for assistance.", e.getDetail());
    }

    @Test
    public void testCode() throws Exception {
        assertEquals("For now, the file can't be decrypted. Please ask another authorized user to grant you access to this file.", new RegexLocale(LocalFactory.get("../i18n/src/main/resources")).localize("-40761", "SDS"));
    }
}
