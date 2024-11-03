package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.DriveItemVersion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class GraphReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(GraphReadFeature.class);

    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public GraphReadFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(file.getType().contains(Path.Type.placeholder)) {
                final DescriptiveUrl link = new GraphUrlProvider().toUrl(file).find(DescriptiveUrl.Type.http);
                if(DescriptiveUrl.EMPTY.equals(link)) {
                    log.warn("Missing web link for file {}", file);
                    return new NullInputStream(file.attributes().getSize());
                }
                // Write web link file
                return IOUtils.toInputStream(UrlFileWriterFactory.get().write(link), Charset.defaultCharset());
            }
            else {
                final DriveItem target = session.getItem(file);
                if(status.isAppend()) {
                    final HttpRange range = HttpRange.withStatus(status);
                    final String header;
                    if(TransferStatus.UNKNOWN_LENGTH == range.getEnd()) {
                        header = String.format("%d-", range.getStart());
                    }
                    else {
                        header = String.format("%d-%d", range.getStart(), range.getEnd());
                    }
                    log.debug("Add range header {} for file {}", header, file);
                    if(file.attributes().isDuplicate()) {
                        return Files.downloadVersion(target, file.attributes().getVersionId(), header);
                    }
                    else {
                        return Files.download(target, header);
                    }
                }
                if(file.attributes().isDuplicate()) {
                    return Files.downloadVersion(target, file.attributes().getVersionId());
                }
                else {
                    return Files.download(target);
                }
            }
        }
        catch(OneDriveAPIException e) {
            switch(e.getResponseCode()) {
                case HttpStatus.SC_NOT_FOUND:
                    fileid.cache(file, null);
            }
            throw new GraphExceptionMappingService(fileid).map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }
}
