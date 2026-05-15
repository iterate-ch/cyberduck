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
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.EnumSet;

public class CteraDelegatingReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(CteraDelegatingReadFeature.class);

    private final CteraSession session;
    private final boolean directio;

    public CteraDelegatingReadFeature(final CteraSession session) {
        this.session = session;
        this.directio = HostPreferencesFactory.get(session.getHost()).getBoolean("ctera.download.directio.enable");
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(directio) {
            try {
                return new CteraDirectIOReadFeature(session).read(file, status, callback);
            }
            catch(BackgroundException e) {
                log.warn("Ignore DirectIO retrieval failure {} for {}", e, file);
            }
        }
        return new CteraReadFeature(session).read(file, status, callback);
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        if(directio) {
            return new CteraDirectIOReadFeature(session).offset(file);
        }
        return new CteraReadFeature(session).offset(file);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        if(directio) {
            new CteraDirectIOReadFeature(session).preflight(file);
            return;
        }
        new CteraReadFeature(session).preflight(file);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        if(directio) {
            return new CteraDirectIOReadFeature(session).features(file);
        }
        return new CteraReadFeature(session).features(file);
    }
}
