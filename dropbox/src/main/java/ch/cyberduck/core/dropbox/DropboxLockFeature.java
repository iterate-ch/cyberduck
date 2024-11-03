package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.LockFileArg;
import com.dropbox.core.v2.files.LockFileResultEntry;
import com.dropbox.core.v2.files.UnlockFileArg;

public class DropboxLockFeature implements Lock<String> {
    private static final Logger log = LogManager.getLogger(DropboxSession.class);

    private final DropboxSession session;
    private final PathContainerService containerService;

    public DropboxLockFeature(final DropboxSession session) {
        this.session = session;
        this.containerService = new DropboxPathContainerService(session);
    }

    @Override
    public String lock(final Path file) throws BackgroundException {
        if(!containerService.getContainer(file).getType().contains(Path.Type.shared)) {
            log.warn("Skip attempting to lock file {} not in shared folder", file);
            throw new UnsupportedException();
        }
        try {
            for(LockFileResultEntry result : new DbxUserFilesRequests(session.getClient(file)).lockFileBatch(Collections.singletonList(
                new LockFileArg(containerService.getKey(file)))).getEntries()) {
                if(result.isFailure()) {
                    throw this.failure(result);
                }
                if(result.isSuccess()) {
                    if(log.isDebugEnabled()) {
                        log.debug("Locked file {} with result {}", file, result.getSuccessValue());
                    }
                    return String.valueOf(true);
                }
            }
            return null;
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    @Override
    public void unlock(final Path file, final String token) throws BackgroundException {
        try {
            for(LockFileResultEntry result : new DbxUserFilesRequests(session.getClient(file)).unlockFileBatch(Collections.singletonList(
                new UnlockFileArg(containerService.getKey(file)))).getEntries()) {
                if(result.isFailure()) {
                    throw failure(result);
                }
                if(log.isDebugEnabled()) {
                    log.debug("Unlocked file {} with result {}", file, result.getSuccessValue());
                }
            }
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    protected BackgroundException failure(final LockFileResultEntry result) {
        log.warn("Lock failure {}", result.getFailureValue());
        switch(result.getFailureValue().tag()) {
            case PATH_LOOKUP:
                return new NotfoundException(result.getFailureValue().toString());
            case NO_WRITE_PERMISSION:
            case CANNOT_BE_LOCKED:
                return new AccessDeniedException(result.getFailureValue().toString());
            case FILE_NOT_SHARED:
                return new UnsupportedException(result.getFailureValue().toString());
            case LOCK_CONFLICT:
                return new LockedException(result.getFailureValue().getLockConflictValue().toString());
        }
        return new InteroperabilityException(result.getFailureValue().toString());
    }
}
