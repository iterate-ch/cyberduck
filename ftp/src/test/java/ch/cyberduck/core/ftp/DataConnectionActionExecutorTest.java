package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertThrows;

@Category(IntegrationTest.class)
public class DataConnectionActionExecutorTest extends AbstractFTPTest {

    @Test
    public void testServerError() throws Exception {
        final DataConnectionAction<Void> action = new DataConnectionAction<Void>() {
            @Override
            public Void execute() throws BackgroundException {
                throw new FTPExceptionMappingService().map(new FTPException(500, "m"));
            }
        };
        final DataConnectionActionExecutor f = new DataConnectionActionExecutor(session);
        assertThrows(InteroperabilityException.class, () -> f.open(action));
    }
}
