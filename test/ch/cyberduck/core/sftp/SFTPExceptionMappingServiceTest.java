package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class SFTPExceptionMappingServiceTest extends AbstractTestCase {

    @Test
    public void testMapReadFailure() throws Exception {
        assertEquals(SocketException.class,
                new SFTPExceptionMappingService().map(new SocketException("Unexpected end of sftp stream.")).getCause().getClass());
    }
}
