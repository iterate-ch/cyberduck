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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.text.MessageFormat;
import java.util.List;

public class DistributionPurgeWorker extends Worker<Boolean> {

    /**
     * Selected files.
     */
    private final List<Path> files;

    private final LoginCallback prompt;

    private final Distribution.Method method;

    public DistributionPurgeWorker(final List<Path> files, final LoginCallback prompt, final Distribution.Method method) {
        this.files = files;
        this.prompt = prompt;
        this.method = method;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
        final Purge feature = cdn.getFeature(Purge.class, method);
        for(Path file : this.getContainers(files)) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            feature.invalidate(file, method, files, prompt);
        }
        return true;
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Writing CDN configuration of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final DistributionPurgeWorker that = (DistributionPurgeWorker) o;
        if(files != null ? !files.equals(that.files) : that.files != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return files != null ? files.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DistributionPurgeWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
