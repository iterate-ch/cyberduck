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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class LocalSession extends Session<FileSystem> {

    private LocalListService listService;

    protected LocalSession(final Host h) {
        super(h);
    }

    public LocalSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h);
    }

    @Override
    protected FileSystem connect(final HostKeyCallback key) throws BackgroundException {
        client = FileSystems.getDefault();
        return client;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return listService.list(directory, listener);
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache cache) throws BackgroundException {
        listService = new LocalListService(this);
    }

    @Override
    public <T> T _getFeature(final Class<T> type) {
        if(type == Attributes.class) {
            return (T) new LocalAttributesFinderFeature(this);
        }
        if(type == Read.class) {
            return (T) new LocalReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new LocalWriteFeature(this);
        }
        if(type == Delete.class) {
            return (T) new LocalDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new LocalMoveFeature(this);
        }
        return super._getFeature(type);
    }
}
