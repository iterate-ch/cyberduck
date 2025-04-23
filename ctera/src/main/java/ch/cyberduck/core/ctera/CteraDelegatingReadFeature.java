package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.EnumSet;

public class CteraDelegatingReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(CteraDelegatingReadFeature.class);

    private final CteraSession session;

    public CteraDelegatingReadFeature(final CteraSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(StringUtils.isNotBlank(status.getUrl())) {
            return new CteraDirectIOReadFeature(session).read(file, status, callback);
        }
        log.warn("No URL found in status {} for {}", status, file);
        return new CteraReadFeature(session).read(file, status, callback);
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return new CteraReadFeature(session).offset(file);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        new CteraReadFeature(session).preflight(file);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return new CteraReadFeature(session).features(file);
    }
}
