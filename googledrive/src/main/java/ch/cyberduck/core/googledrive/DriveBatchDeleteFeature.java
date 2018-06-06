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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;

public class DriveBatchDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(DriveBatchDeleteFeature.class);

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveBatchDeleteFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final List<Path> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final BatchRequest batch = session.getClient().batch();
        final List<BackgroundException> failures = new ArrayList<>();
        for(Path file : files) {
            try {
                if(DriveHomeFinderService.TEAM_DRIVES_NAME.equals(file.getParent())) {
                    session.getClient().teamdrives().delete(fileid.getFileid(file, new DisabledListProgressListener()))
                        .queue(batch, new DeleteBatchCallback(file, failures, callback));
                }
                else {
                    session.getClient().files().delete(fileid.getFileid(file, new DisabledListProgressListener()))
                        .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable"))
                        .queue(batch, new DeleteBatchCallback(file, failures, callback));
                }
            }
            catch(IOException e) {
                throw new DriveExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
        if(!files.isEmpty()) {
            try {
                batch.execute();
            }
            catch(IOException e) {
                throw new DriveExceptionMappingService().map(e);
            }
            for(BackgroundException e : failures) {
                throw e;
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }

    @Override
    public boolean isRecursive() {
        return false;
    }

    private static class DeleteBatchCallback extends JsonBatchCallback<Void> {
        private final Path file;
        private final List<BackgroundException> failures;
        private final Callback callback;

        public DeleteBatchCallback(final Path file, final List<BackgroundException> failures, final Callback callback) {
            this.file = file;
            this.failures = failures;
            this.callback = callback;
        }

        @Override
        public void onFailure(final GoogleJsonError e, final HttpHeaders responseHeaders) throws IOException {
            log.warn(String.format("Failure deleting %s. %s", file, e.getMessage()));
            failures.add(new HttpResponseExceptionMappingService().map(
                new HttpResponseException(e.getCode(), e.getMessage())));
        }

        @Override
        public void onSuccess(final Void aVoid, final HttpHeaders responseHeaders) throws IOException {
            callback.delete(file);
        }
    }
}
