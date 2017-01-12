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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class LocalWriteFeature extends AppendWriteFeature<Void> {


    protected LocalWriteFeature(final Session<?> session) {
        super(session);
    }

    @Override
    public StatusOutputStream<Void> write(final Path path, final TransferStatus status) throws BackgroundException {
        try {
            final java.nio.file.Path p = Paths.get(path.getAbsolute());
            final Set<OpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.WRITE);
            if(status.isAppend()) {
                if(status.isExists()) {
                    options.add(StandardOpenOption.APPEND);
                }
                else {
                    options.add(StandardOpenOption.CREATE);
                }
            }
            else {
                if(status.isExists()) {
                    if(path.isSymbolicLink()) {
                        Files.delete(p);
                        options.add(StandardOpenOption.CREATE);
                    }
                    else {
                        if(status.getRename().remote != null) {
                            options.add(StandardOpenOption.CREATE);
                        }
                        else {
                            options.add(StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                }
                else {
                    options.add(StandardOpenOption.CREATE_NEW);
                }
            }
            final FileChannel channel = FileChannel.open(Paths.get(path.getAbsolute()), options.stream().toArray(OpenOption[]::new));
            channel.position(status.getOffset());
            return new VoidStatusOutputStream(Channels.newOutputStream(channel));
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Upload {0} failed", e, path);
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

    @Override
    public ChecksumCompute checksum() {
        return new DisabledChecksumCompute();
    }
}
