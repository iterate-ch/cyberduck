package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

public class DAVTouchFeature extends DefaultTouchFeature<Void> {

    private final AttributesFinder attributes;

    public DAVTouchFeature(final DAVSession session) {
        this(new DAVWriteFeature(session), new DAVAttributesFinderFeature(session));
    }

    public DAVTouchFeature(final Write<Void> writer, final AttributesFinder attributes) {
        super(writer);
        this.attributes = attributes;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        return super.touch(file, status).withAttributes(attributes.find(file));
    }
}
