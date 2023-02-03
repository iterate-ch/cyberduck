package ch.cyberduck.core.sds;

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
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptReadFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;

public class SDSDelegatingReadFeature implements Read {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final SDSReadFeature proxy;

    public SDSDelegatingReadFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final SDSReadFeature proxy) {
        this.session = session;
        this.nodeid = nodeid;
        this.proxy = proxy;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(file)) {
            return new TripleCryptReadFeature(session, nodeid, proxy).read(file, status, callback);
        }
        else {
            return proxy.read(file, status, callback);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        if(new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(file)) {
            return new TripleCryptReadFeature(session, nodeid, proxy).offset(file);
        }
        else {
            return proxy.offset(file);
        }
    }
}
