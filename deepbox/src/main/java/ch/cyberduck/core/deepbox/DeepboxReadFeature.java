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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.DownloadRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Download;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DownloadAdd;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.LoggingUncaughtExceptionHandler;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANDOWNLOAD;

public class DeepboxReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(DeepboxReadFeature.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxReadFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    protected void poll(final String downloadId) throws BackgroundException {
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicReference<BackgroundException> failure = new AtomicReference<>();
        final ScheduledThreadPool scheduler = new ScheduledThreadPool(new LoggingUncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                super.uncaughtException(t, e);
                failure.set(new BackgroundException(e));
                signal.countDown();
            }
        }, "download");
        final long timeout = new HostPreferences(session.getHost()).getLong("deepbox.download.interrupt.ms");
        final long start = System.currentTimeMillis();
        try {
            final ScheduledFuture<?> f = scheduler.repeat(() -> {
                try {
                    if(System.currentTimeMillis() - start > timeout) {
                        failure.set(new ConnectionCanceledException(String.format("Interrupt polling for download URL after %d", timeout)));
                        signal.countDown();
                        return;
                    }
                    // Poll status
                    final DownloadRestControllerApi rest = new DownloadRestControllerApi(session.getClient());
                    final Download.StatusEnum status = rest.downloadStatus(downloadId, null).getStatus();
                    switch(status) {
                        case READY:
                        case READY_WITH_ISSUES:
                            signal.countDown();
                            return;
                        default:
                            log.warn("Wait for download URL to become ready with current status {}", status);
                            break;
                    }
                }
                catch(ApiException e) {
                    log.warn(String.format("Failure processing scheduled task. %s", e.getMessage()));
                    failure.set(new DeepboxExceptionMappingService(fileid).map(e));
                    signal.countDown();
                }
            }, new HostPreferences(session.getHost()).getLong("deepbox.download.interval.ms"), TimeUnit.MILLISECONDS);
            while(!Uninterruptibles.awaitUninterruptibly(signal, Duration.ofSeconds(1))) {
                try {
                    if(f.isDone()) {
                        Uninterruptibles.getUninterruptibly(f);
                    }
                }
                catch(ExecutionException e) {
                    Throwables.throwIfInstanceOf(Throwables.getRootCause(e), BackgroundException.class);
                    throw new DefaultExceptionMappingService().map(Throwables.getRootCause(e));
                }
            }
            if(null != failure.get()) {
                throw failure.get();
            }
        }
        finally {
            scheduler.shutdown(true);
        }
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            // https://apidocs.deepcloud.swiss/deepbox-api-docs/index.html#download
            final DownloadRestControllerApi rest = new DownloadRestControllerApi(session.getClient());
            final String boxNodeId = fileid.getFileId(file);
            final Download download = rest.requestDownload(new DownloadAdd().addNodesItem(boxNodeId));
            this.poll(download.getDownloadId());
            final HttpUriRequest request = new HttpGet(URI.create(rest.downloadStatus(download.getDownloadId(), null).getDownloadUrl()));
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
                request.addHeader(new BasicHeader(HttpHeaders.RANGE, header));
                // Disable compression
                request.addHeader(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "identity"));
            }

            final HttpResponse response = session.getClient().getClient().execute(request);
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_PARTIAL_CONTENT:
                    return new HttpMethodReleaseInputStream(response, status);
                case HttpStatus.SC_NOT_FOUND:
                    fileid.cache(file, null);
                    // Break through
                default:
                    throw new DefaultHttpResponseExceptionMappingService().map("Download {0} failed", new HttpResponseException(
                            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map("Download {0} failed", e, file);
        }
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        final Acl acl = file.attributes().getAcl();
        if(Acl.EMPTY == acl) {
            // Missing initialization
            log.warn("Unknown ACLs on {}", file);
            return;
        }
        if(!acl.get(new Acl.CanonicalUser()).contains(CANDOWNLOAD)) {
            log.warn("ACL {} for {} does not include {}", acl, file, CANDOWNLOAD);
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot download {0}", "Error"), file.getName())).withFile(file);
        }
    }
}
