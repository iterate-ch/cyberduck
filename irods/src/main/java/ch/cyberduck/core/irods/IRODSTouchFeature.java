package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IRODSTouchFeature implements Touch {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final IRODSSession session;

    public IRODSTouchFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path touch(final Write writer, final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();

            Map<String, Object> input = new HashMap<>();
            input.put("logical_path", file.getAbsolute());

            String jsonInput = mapper.writeValueAsString(input);

            int ec = IRODSApi.rcTouch(conn.getRcComm(), jsonInput);
            if(ec < 0) {
                throw new IRODSException(ec, "rcTouch error");
            }

            return file;
        }
        catch(IRODSException e) {
            throw new IRODSExceptionMappingService().map("Cannot create {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        return true;
    }
}
