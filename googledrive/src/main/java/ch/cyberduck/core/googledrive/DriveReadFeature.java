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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;

import static com.google.api.client.json.Json.MEDIA_TYPE;

public class DriveReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(DriveReadFeature.class);

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveReadFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(file.isPlaceholder()) {
            final DescriptiveUrl link = new DriveUrlProvider().toUrl(file).find(DescriptiveUrl.Type.http);
            if(DescriptiveUrl.EMPTY.equals(link)) {
                log.warn(String.format("Missing web link for file %s", file));
                return new NullInputStream(file.attributes().getSize());
            }
            // Write web link file
            return IOUtils.toInputStream(UrlFileWriterFactory.get().write(link), Charset.defaultCharset());
        }
        else {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MEDIA_TYPE);
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                final String header;
                if(TransferStatus.UNKNOWN_LENGTH == range.getEnd()) {
                    header = String.format("bytes=%d-", range.getStart());
                }
                else {
                    header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
                }
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Add range header %s for file %s", header, file));
                }
                headers.setRange(header);
                // Disable compression
                headers.setAcceptEncoding("identity");
            }
            if(file.attributes().isDuplicate()) {
                // Read previous version
                try {
                    final Drive.Revisions.Get request = session.getClient().revisions().get(fileid.getFileId(file), file.attributes().getVersionId());
                    request.setRequestHeaders(headers);
                    return request.executeMediaAsInputStream();
                }
                catch(IOException e) {
                    throw new DriveExceptionMappingService(fileid).map("Download {0} failed", e, file);
                }
            }
            else {
                try {
                    try {
                        final Drive.Files.Get request = session.getClient().files().get(fileid.getFileId(file));
                        request.setRequestHeaders(headers);
                        request.setSupportsTeamDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable"));
                        return request.executeMediaAsInputStream();
                    }
                    catch(IOException e) {
                        throw new DriveExceptionMappingService(fileid).map("Download {0} failed", e, file);
                    }
                }
                catch(RetriableAccessDeniedException e) {
                    throw e;
                }
                catch(AccessDeniedException e) {
                    if(!PreferencesFactory.get().getBoolean(String.format("connection.unsecure.download.%s", session.getHost().getHostname()))) {
                        // Not previously dismissed
                        callback.warn(session.getHost(),
                                MessageFormat.format(LocaleFactory.localizedString("Download {0} failed", "Error"), file.getName()),
                                "Acknowledge the risk of downloading known malware or other abusive file.",
                                LocaleFactory.localizedString("Continue", "Credentials"), LocaleFactory.localizedString("Cancel", "Localizable"),
                                String.format("connection.unsecure.download.%s", session.getHost().getHostname()));
                    }
                    try {
                        final Drive.Files.Get request = session.getClient().files().get(fileid.getFileId(file));
                        request.setAcknowledgeAbuse(true);
                        request.setRequestHeaders(headers);
                        request.setSupportsTeamDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable"));
                        return request.executeMediaAsInputStream();
                    }
                    catch(IOException f) {
                        throw new DriveExceptionMappingService(fileid).map("Download {0} failed", f, file);
                    }
                }
            }
        }
    }
}
