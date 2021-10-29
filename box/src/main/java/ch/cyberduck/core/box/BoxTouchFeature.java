package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

public class BoxTouchFeature implements Touch<Files> {

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    private Write<Files> writer;

    public BoxTouchFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.writer = new BoxWriteFeature(session, fileid);
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        final StatusOutputStream<Files> out = writer.write(file, status, new DisabledConnectionCallback());
        new DefaultStreamCloser().close(out);
        if(out.getStatus().getEntries().stream().findFirst().isPresent()) {
            return new Path(file).withAttributes(new BoxAttributesFinderFeature(session, fileid).toAttributes(out.getStatus().getEntries().stream().findFirst().get()));
        }
        throw new NotfoundException(file.getAbsolute());
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        // Max Length 255
        if(StringUtils.length(name) > 255) {
            return false;
        }
        // Names containing non-printable ASCII characters, forward and backward slashes (/, \), as well as names with trailing spaces are prohibited.
        if(!StringUtils.isAsciiPrintable(name)) {
            return false;
        }
        if(StringUtils.contains(name, "/")) {
            return false;
        }
        if(StringUtils.contains(name, "\\")) {
            return false;
        }
        if(StringUtils.endsWith(name, StringUtils.SPACE)) {
            return false;
        }
        // Additionally, the names . and .. are not allowed either.
        if(StringUtils.equals(name, ".")) {
            return false;
        }
        if(StringUtils.equals(name, "..")) {
            return false;
        }
        return true;
    }

    @Override
    public Touch<Files> withWriter(final Write<Files> writer) {
        this.writer = writer;
        return this;
    }
}
