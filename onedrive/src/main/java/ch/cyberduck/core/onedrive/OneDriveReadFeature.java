package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class OneDriveReadFeature implements Read {
    private static final Logger log = Logger.getLogger(OneDriveReadFeature.class);

    private final OneDriveSession session;

    public OneDriveReadFeature(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(file.getType().contains(Path.Type.placeholder)) {
                final DescriptiveUrl link = new OneDriveUrlProvider().toUrl(file).find(DescriptiveUrl.Type.http);
                if(DescriptiveUrl.EMPTY.equals(link)) {
                    log.warn(String.format("Missing web link for file %s", file));
                    return new NullInputStream(file.attributes().getSize());
                }
                // Write web link file
                return IOUtils.toInputStream(UrlFileWriterFactory.get().write(link), Charset.defaultCharset());
            }
            else {
                final OneDriveItem item = session.toItem(file);
                if(null == item) {
                    throw new NotfoundException(String.format("Did not find %s", file));
                }
                if(!(item instanceof OneDriveFile)) {
                    throw new NotfoundException(String.format("%s is no file", file));
                }
                final OneDriveFile target = (OneDriveFile) item;

                if(status.isAppend()) {
                    final HttpRange range = HttpRange.withStatus(status);
                    final String header;
                    if(-1 == range.getEnd()) {
                        header = String.format("%d-", range.getStart());
                    }
                    else {
                        header = String.format("%d-%d", range.getStart(), range.getEnd());
                    }
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Add range header %s for file %s", header, file));
                    }
                    return target.download(header);
                }
                return target.download();
            }
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return true;
    }
}
