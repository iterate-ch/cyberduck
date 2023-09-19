package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Objects;

public class WriteTimestampWorker extends Worker<Boolean> {
    private static final Logger log = LogManager.getLogger(WriteTimestampWorker.class);

    private final Path file;
    private final Long created;
    private final Long modified;

    public WriteTimestampWorker(final Path file, final Long modified) {
        this(file, null, modified);
    }


    public WriteTimestampWorker(final Path file, final Long created, final Long modified) {
        this.file = file;
        this.created = created;
        this.modified = modified;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final Timestamp feature = session.getFeature(Timestamp.class);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run with feature %s", feature));
        }
        feature.setTimestamp(file, new TransferStatus()
                .withCreated(created)
                .withModified(modified)
                .withLockId(this.getLockId(file)));
        return true;
    }

    protected String getLockId(final Path file) {
        return null;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Changing timestamp of {0} to {1}", "Status"),
            file.getName(), UserDateFormatterFactory.get().getShortFormat(modified));
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final WriteTimestampWorker that = (WriteTimestampWorker) o;
        return Objects.equals(file, that.file) && Objects.equals(created, that.created) && Objects.equals(modified, that.modified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, created, modified);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WriteTimestampWorker{");
        sb.append("file=").append(file);
        sb.append(", created=").append(created);
        sb.append(", modified=").append(modified);
        sb.append('}');
        return sb.toString();
    }

}
