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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

import org.apache.commons.lang3.StringUtils;

public final class DAVSSLProtocol extends AbstractProtocol {
    @Override
    public String getName() {
        return "WebDAV (HTTPS)";
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("WebDAV (HTTP/SSL)");
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", DAVSSLProtocol.class.getPackage().getName(), StringUtils.upperCase(this.getType().name()));
    }

    @Override
    public String getIdentifier() {
        return "davs";
    }

    @Override
    public Type getType() {
        return Type.dav;
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String[] getSchemes() {
        return new String[]{this.getScheme().name(), "davs"};
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }
}
