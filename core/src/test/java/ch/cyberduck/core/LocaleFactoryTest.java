package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import org.junit.Test;

import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;

public class LocaleFactoryTest {

    @Test
    public void testFormat() throws Exception {
        assertEquals("La clé d'hôte fournie est 1.",
                MessageFormat.format(LocaleFactory.localizedString("La clé d'hôte fournie est {0}."), "1"));
    }

    @Test
    public void testLocalizedString() throws Exception {
        assertEquals("La clé d''hôte fournie est {0}.",
                LocaleFactory.localizedString("La clé d'hôte fournie est {0}.", "Localizable"));
    }
}