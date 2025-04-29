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
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveRequest;

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
            final DescriptiveUrl link = new DriveUrlProvider().toUrl(file, EnumSet.of(DescriptiveUrl.Type.http)).find(DescriptiveUrl.Type.http);
            if(DescriptiveUrl.EMPTY.equals(link)) {
                log.warn("Missing web link for file {}", file);
                return new NullInputStream(file.attributes().getSize());
            }
            // Write web link file
            return IOUtils.toInputStream(UrlFileWriterFactory.get().write(link), Charset.defaultCharset());
        }
        else {
            final List<Header> headers = new ArrayList<>();
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                final String header;
                if(TransferStatus.UNKNOWN_LENGTH == range.getEnd()) {
                    header = String.format("bytes=%d-", range.getStart());
                }
                else {
                    header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
                }
                log.debug("Add range header {} for file {}", header, file);
                headers.add(new BasicHeader(HttpHeaders.RANGE, header));
                // Disable compression
                headers.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "identity"));
            }
            if(file.attributes().isDuplicate()) {
                // Read previous version
                try {
                    final Drive.Revisions.Get request = session.getClient().revisions().get(fileid.getFileId(file), file.attributes().getVersionId());
                    return this.getStream(file, status, request, headers);
                }
                catch(IOException e) {
                    throw new DriveExceptionMappingService(fileid).map("Download {0} failed", e, file);
                }
            }
            else {
                try {
                    try {
                        final Drive.Files.Get request = session.getClient().files().get(fileid.getFileId(file));
                        request.setSupportsTeamDrives(HostPreferencesFactory.get(session.getHost()).getBoolean("googledrive.teamdrive.enable"));
                        return this.getStream(file, status, request, headers);
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
                        request.setSupportsTeamDrives(HostPreferencesFactory.get(session.getHost()).getBoolean("googledrive.teamdrive.enable"));
                        request.setAcknowledgeAbuse(true);
                        return this.getStream(file, status, request, headers);
                    }
                    catch(IOException f) {
                        throw new DriveExceptionMappingService(fileid).map("Download {0} failed", f, file);
                    }
                }
            }
        }
    }

    private InputStream getStream(final Path file, final TransferStatus status, final DriveRequest<?> request, final List<Header> headers) throws IOException, BackgroundException {
        request.put("alt", "media");
        final HttpGet get = new HttpGet(request.buildHttpRequestUrl().build());
        for(Header header : headers) {
            get.addHeader(header);
        }
        final HttpResponse response = session.getHttpClient().execute(get);
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_PARTIAL_CONTENT:
                return new HttpMethodReleaseInputStream(response, status);
            default:
                throw new DefaultHttpResponseExceptionMappingService().map("Download {0} failed", new HttpResponseException(
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file);
        }
    }
}
