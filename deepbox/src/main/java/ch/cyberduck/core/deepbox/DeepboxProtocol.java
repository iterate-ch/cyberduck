package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Scheme;

public class DeepboxProtocol extends AbstractProtocol {
    @Override
    public String getIdentifier() {
        return "deepbox";
    }

    @Override
    public String getName() {
        return "DeepBox";
    }

    @Override
    public String getDescription() {
        return "DeepBox";
    }

    @Override
    public Type getType() {
        return Type.deepbox;
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", DeepboxProtocol.class.getPackage().getName(), "Deepbox");
    }

    @Override
    public boolean isUsernameConfigurable() {
        return false;
    }

    @Override
    public boolean isOAuthPKCE() {
        return true;
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public Case getCaseSensitivity() {
        return Case.sensitive;
    }
}
