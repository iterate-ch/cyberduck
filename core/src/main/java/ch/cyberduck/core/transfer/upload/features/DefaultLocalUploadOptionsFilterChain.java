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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.transfer.ChainedFeatureFilter;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

public final class DefaultLocalUploadOptionsFilterChain extends ChainedFeatureFilter {

    public DefaultLocalUploadOptionsFilterChain(final Session<?> session, final UploadFilterOptions options) {
        super(
                new MimeFeatureFilter(),
                new HiddenFeatureFilter(),
                options.temporary ? new TemporaryFeatureFilter(session) : noop,
                options.permissions ? new PermissionFeatureFilter(session) : noop,
                options.acl ? new AclFeatureFilter(session) : noop,
                options.timestamp ? new TimestampFeatureFilter(session) : noop,
                options.metadata ? new MetadataFeatureFilter(session) : noop,
                options.encryption ? new EncryptionFeatureFilter(session) : noop,
                options.redundancy ? new RedundancyClassFeatureFilter(session) : noop,
                options.checksum ? new ChecksumFeatureFilter(session) : noop,
                options.versioning ? new VersioningFeatureFilter(session) : noop
        );
    }
}
