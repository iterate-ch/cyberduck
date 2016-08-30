package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Assert;
import org.junit.Test;

import com.github.sardine.impl.SardineException;

import static org.junit.Assert.assertEquals;

public class DAVExceptionMappingServiceTest {

    @Test
    public void testMap() throws Exception {
        Assert.assertEquals(LoginFailureException.class,
                new DAVExceptionMappingService().map(new SardineException("m", 401, "r")).getClass());
        assertEquals(AccessDeniedException.class,
                new DAVExceptionMappingService().map(new SardineException("m", 403, "r")).getClass());
        assertEquals(NotfoundException.class,
                new DAVExceptionMappingService().map(new SardineException("m", 404, "r")).getClass());
    }
}
