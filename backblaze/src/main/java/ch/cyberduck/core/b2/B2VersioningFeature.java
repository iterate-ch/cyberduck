package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.transfer.TransferStatus;

public class B2VersioningFeature implements Versioning {

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2VersioningFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) throws BackgroundException {
        final LifecycleConfiguration configuration = new B2LifecycleFeature(session, fileid).getConfiguration(container);
        if(configuration.getTransition() == null && configuration.getExpiration() != null && configuration.getExpiration() == 1) {
            return new VersioningConfiguration(false);
        }
        return new VersioningConfiguration(true);
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        if(configuration.isEnabled()) {
            new B2LifecycleFeature(session, fileid).setConfiguration(container, LifecycleConfiguration.empty());
        }
        else {
            new B2LifecycleFeature(session, fileid).setConfiguration(container, new LifecycleConfiguration(null, 1));
        }
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        new B2CopyFeature(session, fileid).copy(file, file, new TransferStatus(), new DisabledLoginCallback(), new DisabledStreamListener());
    }

    @Override
    public boolean isRevertable(final Path file) {
        return true;
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        return new B2ObjectListService(session, fileid).list(file, listener).filter(new NullFilter<Path>() {
            @Override
            public boolean accept(final Path f) {
                return f.attributes().isDuplicate();
            }
        });
    }
}
