package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.comparison.DefaultAttributesComparison;
import ch.cyberduck.core.comparison.DisabledAttributesComparison;
import ch.cyberduck.core.comparison.TimestampAttributesComparison;
import ch.cyberduck.core.features.AttributesComparison;

public class StoregateProtocol extends AbstractProtocol {
    @Override
    public String getIdentifier() {
        return "storegate";
    }

    @Override
    public String getName() {
        return "Storegate";
    }

    @Override
    public String getDescription() {
        return "Storegate";
    }

    @Override
    public Type getType() {
        return Type.storegate;
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String getContext() {
        return "/api";
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", StoregateProtocol.class.getPackage().getName(), "Storegate");
    }

    @Override
    public boolean isUsernameConfigurable() {
        return false;
    }

    @Override
    public boolean isOAuthPKCE() {
        return false;
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public Case getCaseSensitivity() {
        return Case.insensitive;
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == AttributesComparison.class) {
            return (T) new DefaultAttributesComparison(new TimestampAttributesComparison(), new DisabledAttributesComparison());
        }
        return super.getFeature(type);
    }
}
