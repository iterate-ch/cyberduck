package ch.cyberduck.core.exception;

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

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import com.googlecode.sardine.impl.SardineException;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class SardineExceptionMappingServiceTest extends AbstractTestCase {

    @Test
    public void testMap() throws Exception {
        assertEquals(LoginFailureException.class,
                new SardineExceptionMappingService().map(new SardineException("m", 401, "r")).getClass());
        assertEquals(LoginFailureException.class,
                new SardineExceptionMappingService().map(new SardineException("m", 403, "r")).getClass());
        assertEquals(NotfoundException.class,
                new SardineExceptionMappingService().map(new SardineException("m", 404, "r")).getClass());
    }
}
