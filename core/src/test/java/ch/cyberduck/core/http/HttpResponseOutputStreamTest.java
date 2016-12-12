package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HttpResponseOutputStreamTest {

    @Test(expected = IOException.class)
    public void testClose() throws Exception {
        try {
            new HttpResponseOutputStream<Void>(new NullOutputStream()) {
                @Override
                public Void getResponse() throws BackgroundException {
                    throw new InteroperabilityException("d");
                }
            }.close();
        }
        catch(IOException e) {
            assertEquals("d. Please contact your web hosting service provider for assistance.", e.getMessage());
            throw e;
        }
    }
}