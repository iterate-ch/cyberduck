package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Versioning;

import java.util.List;

public class WriteVersioningWorker extends Worker<Boolean> {

    /**
     * Selected files.
     */
    private final List<Path> files;

    private final LoginCallback prompt;

    private final VersioningConfiguration configuration;

    public WriteVersioningWorker(final List<Path> files, final LoginCallback prompt, final VersioningConfiguration configuration) {
        this.files = files;
        this.prompt = prompt;
        this.configuration = configuration;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final Versioning feature = session.getFeature(Versioning.class);
        for(Path file : files) {
            this.write(feature, file);
        }
        return true;
    }

    private void write(final Versioning feature, final Path file) throws BackgroundException {
        feature.setConfiguration(file, prompt, configuration);
    }
}
