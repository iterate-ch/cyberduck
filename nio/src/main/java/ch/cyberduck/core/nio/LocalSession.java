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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;

public class LocalSession extends Session<FileSystem> {
    private static final Logger log = LogManager.getLogger(LocalSession.class);

    private Object lock;

    public LocalSession(final Host h) {
        super(h);
    }

    public LocalSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h);
    }

    public java.nio.file.Path toPath(final Path file) throws LocalAccessDeniedException {
        return this.toPath(file.getAbsolute());
    }

    public java.nio.file.Path toPath(final String path) throws LocalAccessDeniedException {
        try {
            /*
            This matches:
            ^ - Start of string
            / - Slash
            ( - Capture group which replaces this regex
                . - Any character (i.e. drive letter)
                : - A colon
                (?: - non-capture group for easier usage of following pattern
                    / - Slash
                    | - or
                    \ - Backspace (escaped escaping characters)
                )? - Zero or one of the above non-capture group
            )
            Following are matches:
            - /X:
            - /X:/
            - /X:\
            and get replaced by
            - X:
            - X:/
            - X:\
            respectively.
            This does not affect paths after the drive letter.
            - /X:/Test -> X:/Test
            - /X:\Test\Test -> X:\Test\Test
             */
            return client.getPath(path.replaceFirst("^/(.:(?:/|\\\\)?)", "$1"));
        }
        catch(InvalidPathException e) {
            throw new LocalAccessDeniedException(e.getReason(), e);
        }
    }

    @Override
    protected FileSystem connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
        return FileSystems.getDefault();
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Path home = new LocalHomeFinderFeature().find();
        try {
            lock = LocalFactory.get(this.toPath(home).toString()).lock(true);
        }
        catch(LocalAccessDeniedException e) {
            log.debug("Ignore failure obtaining lock for {}", home);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        final Path home = new LocalHomeFinderFeature().find();
        LocalFactory.get(this.toPath(home).toString()).release(lock);
    }

    protected boolean isPosixFilesystem() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new LocalListService(this);
        }
        if(type == Touch.class) {
            return (T) new LocalTouchFeature(this);
        }
        if(type == Find.class) {
            return (T) new LocalFindFeature(this);
        }
        if(type == AttributesFinder.class) {
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
        if(type == Copy.class) {
            return (T) new LocalCopyFeature(this);
        }
        if(type == Directory.class) {
            return (T) new LocalDirectoryFeature(this);
        }
        if(type == Symlink.class) {
            if(this.isPosixFilesystem()) {
                return (T) new LocalSymlinkFeature(this);
            }
        }
        if(type == UnixPermission.class) {
            if(this.isPosixFilesystem()) {
                return (T) new LocalUnixPermissionFeature(this);
            }
        }
        if(type == Home.class) {
            return (T) new DelegatingHomeFeature(new WorkdirHomeFeature(host), new DefaultPathHomeFeature(host), new LocalHomeFinderFeature());
        }
        if(type == Quota.class) {
            return (T) new LocalQuotaFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new LocalTimestampFeature(this);
        }
        if(type == Upload.class) {
            return (T) new LocalUploadFeature(this);
        }
        return super._getFeature(type);
    }
}
