package ch.cyberduck.core;

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

import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;

public class NullTransferSession extends NullSession {
    public NullTransferSession(final Host h) {
        super(h);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final T feature) {
        if(type == Find.class) {
            return (T) new DefaultFindFeature(this) {
                @Override
                protected Path search(final Path file) {
                    return file;
                }
            };
        }
        if(type == AttributesFinder.class) {
            return (T) new DefaultAttributesFinderFeature(this) {
                @Override
                public PathAttributes find(final Path file) {
                    return file.attributes();
                }
            };
        }
        return super.getFeature(type, feature);
    }
}
