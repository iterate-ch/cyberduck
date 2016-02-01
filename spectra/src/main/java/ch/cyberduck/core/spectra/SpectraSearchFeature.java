/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Search;

import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

public class SpectraSearchFeature implements Search {

    private final SpectraSession session;

    public SpectraSearchFeature(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> search(final Filter<Path> regex, final ListProgressListener listener) {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(new SpectraClientBuilder().wrap(session));
        return AttributedList.emptyList();
    }

    @Override
    public Search withCache(final PathCache cache) {
        return this;
    }
}
