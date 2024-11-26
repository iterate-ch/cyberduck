package ch.cyberduck.core.transfer.download.features;

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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.transfer.ChainedFeatureFilter;
import ch.cyberduck.core.transfer.download.DownloadFilterOptions;

public class DefaultDownloadOptionsFilterChain extends ChainedFeatureFilter {

    public DefaultDownloadOptionsFilterChain(final Session<?> session, final DownloadFilterOptions options) {
        super(
                options.timestamp ? new TimestampFeatureFilter() : noop,
                options.permissions ? new PermissionFeatureFilter(session) : noop,
                options.checksum ? new ChecksumFeatureFilter() : noop,
                new TemporaryFeatureFilter(),
                options.icon ? new IconFilter() : noop,
                options.quarantine ? new QuarantineFilter(session) : noop,
                options.open ? new LauncherFilter() : noop
        );
    }
}
