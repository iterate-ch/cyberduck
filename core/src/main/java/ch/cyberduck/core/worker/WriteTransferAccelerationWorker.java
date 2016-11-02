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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.TransferAcceleration;

import java.text.MessageFormat;
import java.util.List;

public class WriteTransferAccelerationWorker extends Worker<Boolean> {

    /**
     * Selected files.
     */
    private final List<Path> files;

    private final boolean enabled;

    public WriteTransferAccelerationWorker(final List<Path> files, final boolean enabled) {
        this.files = files;
        this.enabled = enabled;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final TransferAcceleration feature = session.getFeature(TransferAcceleration.class);
        for(Path file : files) {
            this.write(feature, file);
        }
        return true;
    }

    private void write(final TransferAcceleration feature, final Path file) throws BackgroundException {
        feature.setStatus(file, enabled);
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"),
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
        final WriteTransferAccelerationWorker that = (WriteTransferAccelerationWorker) o;
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
        final StringBuilder sb = new StringBuilder("WriteTransferAccelerationWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
