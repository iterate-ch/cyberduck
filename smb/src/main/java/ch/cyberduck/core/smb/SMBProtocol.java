package ch.cyberduck.core.smb;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
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
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.implicit;
    }

    @Override
    public Statefulness getStatefulness() {
        return Statefulness.stateful;
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
    public boolean isUTCTimezone() {
        return false;
    }

    @Override
    public boolean isEncodingConfigurable() {
        return true;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return true;
    }
}
