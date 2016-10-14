package ch.cyberduck.core.webloc;

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

import ch.cyberduck.core.DescriptiveUrl;

public class InternetShortcutFileWriter implements UrlFileWriter {

    @Override
    public String write(final DescriptiveUrl link) {
        // Each line terminates with CR and LF characters
        return String.format("[InternetShortcut]\r\nURL=%s", link.getUrl());
    }

    @Override
    public String getExtension() {
        return "url";
    }
}
