package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;

import java.util.Objects;

public class StatefulDefaultCopyFeature extends DefaultCopyFeature {

    private Session<?> from;
    private Session<?> to;

    public StatefulDefaultCopyFeature(final Session<?> source) {
        super(source);
        this.from = source;
    }

    @Override
    public DefaultCopyFeature withTarget(final Session<?> session) {
        super.withTarget(session);
        to = session;
        return this;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(null == to) {
            return false;
        }
        return !Objects.equals(from, to);
    }
}
