package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.collections.Partition;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Trash;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.model.File;

public class DriveBatchTrashFeature implements Trash {
    private static final Logger log = LogManager.getLogger(DriveBatchTrashFeature.class);

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveBatchTrashFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        // Must split otherwise 413 Request Entity Too Large is returned
        for(List<Path> partition : new Partition<>(new ArrayList<>(files.keySet()),
                new HostPreferences(session.getHost()).getInteger("googledrive.delete.multiple.partition"))) {
            final BatchRequest batch = session.getClient().batch();
            final List<BackgroundException> failures = new CopyOnWriteArrayList<>();
            for(Path f : partition) {
                try {
                    if(new SimplePathPredicate(DriveHomeFinderService.SHARED_DRIVES_NAME).test(f.getParent())) {
                        session.getClient().teamdrives().delete(fileid.getFileId(f))
                                .queue(batch, new DeleteBatchCallback<>(f, failures, callback));
                    }
                    else {
                        if(f.attributes().isHidden()) {
                            log.warn("Delete file {} already in trash", f);
                            new DriveBatchDeleteFeature(session, fileid).queue(f, batch, callback, failures);
                            continue;
                        }
                        final File properties = new File();
                        properties.setTrashed(true);
                        session.getClient().files().update(fileid.getFileId(f), properties)
                                .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable"))
                                .queue(batch, new DeleteBatchCallback<>(f, failures, callback));
                    }
                }
                catch(IOException e) {
                    throw new DriveExceptionMappingService(fileid).map("Cannot delete {0}", e, f);
                }
            }
            if(!partition.isEmpty()) {
                try {
                    batch.execute();
                }
                catch(IOException e) {
                    throw new DriveExceptionMappingService(fileid).map(e);
                }
                for(BackgroundException e : failures) {
                    throw e;
                }
            }
        }
    }

    private class DeleteBatchCallback<V> extends JsonBatchCallback<V> {
        private final Path file;
        private final List<BackgroundException> failures;
        private final Callback callback;

        public DeleteBatchCallback(final Path file, final List<BackgroundException> failures, final Callback callback) {
            this.file = file;
            this.failures = failures;
            this.callback = callback;
        }

        @Override
        public void onFailure(final GoogleJsonError e, final HttpHeaders responseHeaders) {
            log.warn("Failure trashing {}. {}", file, e.getMessage());
            failures.add(new DefaultHttpResponseExceptionMappingService().map(
                    new HttpResponseException(e.getCode(), e.getMessage())));
        }

        @Override
        public void onSuccess(final V aVoid, final HttpHeaders responseHeaders) {
            fileid.cache(file, null);
            callback.delete(file);
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}