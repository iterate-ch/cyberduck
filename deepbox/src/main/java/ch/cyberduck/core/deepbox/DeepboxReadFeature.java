package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.DownloadRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Download;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DownloadAdd;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class DeepboxReadFeature implements Read {
    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxReadFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final DownloadRestControllerApi boxApi = new DownloadRestControllerApi(session.getClient());
            final String fileId = fileid.getFileId(file);
            if(fileId == null) {
                throw new NotfoundException(file.getAbsolute());
            }
            final UUID boxNodeId = UUID.fromString(fileId);
            Download download = boxApi.requestDownload(new DownloadAdd().addNodesItem(boxNodeId));
            // TODO right way to wait for download to be ready?
            while(download.getStatus() != Download.StatusEnum.READY) {
                Thread.sleep(200);
                download = boxApi.downloadStatus(download.getDownloadId(), null);
            }
            return new URL(download.getDownloadUrl()).openStream();

            // TODO fails with 400 due to authorization header from OAuth2requestinterceptor
            /*final HttpUriRequest request = new HttpGet(URI.create(download.getDownloadUrl()));


            final HttpResponse response = session.getClient().getClient().execute(request);
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_PARTIAL_CONTENT:
                    return new HttpMethodReleaseInputStream(response, status);
                case HttpStatus.SC_NOT_FOUND:
                    fileid.cache(file, null);
                    // Break through
                default:
                    System.out.println(download.getDownloadUrl());
                    throw new DefaultHttpResponseExceptionMappingService().map("Download {0} failed", new HttpResponseException(
                            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file);
            }*/
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map(e);
        }
        catch(InterruptedException e) {
            throw new DefaultExceptionMappingService().map(e);
        }
    }
}
