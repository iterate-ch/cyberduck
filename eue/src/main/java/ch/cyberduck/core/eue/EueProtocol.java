package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.comparison.ChecksumAttributesComparison;
import ch.cyberduck.core.comparison.DefaultAttributesComparison;
import ch.cyberduck.core.comparison.ETagAttributesComparison;
import ch.cyberduck.core.features.AttributesComparison;

public class EueProtocol extends AbstractProtocol {

    @Override
    public Type getType() {
        return Type.eue;
    }

    @Override
    public String getIdentifier() {
        return Type.eue.name();
    }

    @Override
    public String getDescription() {
        return "GMX Cloud";
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
    public Case getCaseSensitivity() {
        return Case.insensitive;
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.implicit;
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == AttributesComparison.class) {
            return (T) new DefaultAttributesComparison(new ChecksumAttributesComparison(), new ETagAttributesComparison());
        }
        return super.getFeature(type);
    }
}
