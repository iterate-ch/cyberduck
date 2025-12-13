package ch.cyberduck.core.transfer.upload.features;

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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.browser.SearchFilterFactory;

import java.util.Optional;

public class HiddenFeatureFilter implements FeatureFilter {

    private final Filter<Path> hidden;

    public HiddenFeatureFilter() {
        this(SearchFilterFactory.HIDDEN_FILTER);
    }

    public HiddenFeatureFilter(final Filter<Path> hidden) {
        this.hidden = hidden;
    }

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        status.setHidden(!hidden.accept(file));
        return status;
    }
}
