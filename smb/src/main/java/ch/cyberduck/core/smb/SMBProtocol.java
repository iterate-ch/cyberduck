package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

import org.apache.commons.lang3.StringUtils;

public class SMBProtocol extends AbstractProtocol {

    @Override
    public String getIdentifier() {
        return this.getScheme().name();
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", SMBProtocol.class.getPackage().getName(), StringUtils.upperCase(this.getType().name()));
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("SMB (Server Message Block)");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.smb;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public boolean isUTCTimezone() {
        return false;
    }

    @Override
    public String getUsernamePlaceholder() {
        return "username@domain/share";
    }
}
