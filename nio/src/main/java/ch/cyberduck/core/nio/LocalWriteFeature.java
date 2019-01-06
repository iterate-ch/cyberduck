package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class LocalWriteFeature extends AppendWriteFeature<Void> {

    private final LocalSession session;

    public LocalWriteFeature(final LocalSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final java.nio.file.Path p = session.toPath(file);
            final Set<OpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.WRITE);
            if(status.isAppend()) {
                if(!status.isExists()) {
                    options.add(StandardOpenOption.CREATE);
                }
            }
            else {
                if(status.isExists()) {
                    if(file.isSymbolicLink()) {
                        Files.delete(p);
                        options.add(StandardOpenOption.CREATE);
                    }
                    else {
                        options.add(StandardOpenOption.TRUNCATE_EXISTING);
                    }
                }
                else {
                    options.add(StandardOpenOption.CREATE_NEW);
                }
            }
            final FileChannel channel = FileChannel.open(session.toPath(file), options.stream().toArray(OpenOption[]::new));
            channel.position(status.getOffset());
            return new VoidStatusOutputStream(Channels.newOutputStream(channel));
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    @Override
    public boolean temporary() {
        return true;
    }

    @Override
    public boolean random() {
        return true;
    }
}
