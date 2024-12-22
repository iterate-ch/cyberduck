package ch.cyberduck.core.manta;

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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Optional;

import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaException;

public class MantaMoveFeature implements Move {

    private final MantaSession session;

    public MantaMoveFeature(MantaSession session) {
        this.session = session;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status,
                     final Delete.Callback deleteCallback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            session.getClient().move(file.getAbsolute(), renamed.getAbsolute());
            // Copy original file attributes
            return renamed.withAttributes(file.attributes());
        }
        catch(MantaException e) {
            throw new MantaExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(MantaClientHttpResponseException e) {
            throw new MantaHttpExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        if(target.isPresent()) {
            if(!session.isUserWritable(target.get())) {
                throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
            }
        }
    }
}
