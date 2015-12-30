package ch.cyberduck.core.i18n;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.WorkdirPrefixer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegexLocaleTest {

    @Test
    public void testLocalize() throws Exception {
        final RegexLocale locale = new RegexLocale(new Local(new WorkdirPrefixer().normalize("../i18n/src/main/resources")));
        assertEquals("Download failed", locale.localize("Download failed", "Status"));
        locale.setDefault("fr");
        assertEquals("Échec du téléchargement", locale.localize("Download failed", "Status"));
    }
}