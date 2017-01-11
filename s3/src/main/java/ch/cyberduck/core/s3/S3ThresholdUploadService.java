package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.TransferAcceleration;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.model.StorageObject;

public class S3ThresholdUploadService implements Upload<StorageObject> {
    private static final Logger log = Logger.getLogger(S3ThresholdUploadService.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final S3Session session;

    private Long multipartThreshold
            = preferences.getLong("s3.upload.multipart.threshold");

    private final TransferAcceleration<S3Session> accelerateTransferOption;

    private final X509TrustManager trust;
    private final X509KeyManager key;

    private Write<StorageObject> writer;

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key,
                                    final TransferAcceleration<S3Session> accelerateTransferOption) {
        this(session, trust, key, accelerateTransferOption, PreferencesFactory.get().getLong("s3.upload.multipart.threshold"));
    }

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key,
                                    final TransferAcceleration<S3Session> accelerateTransferOption, final Long multipartThreshold) {
        this.session = session;
        this.trust = trust;
        this.key = key;
        this.multipartThreshold = multipartThreshold;
        this.accelerateTransferOption = accelerateTransferOption;
        this.writer = new S3WriteFeature(session, new S3DisabledMultipartService());
    }

    @Override
    public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        return writer.append(file, length, cache);
    }

    @Override
    public StorageObject upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        final Host bookmark = session.getHost();
        try {
            if(this.accelerate(file, status, prompt, bookmark)) {
                final S3Session tunneled = accelerateTransferOption.open(bookmark, file, trust, key);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Tunnel upload for file %s through accelerated endpoint %s", file, tunneled));
                }
                if(status.getLength() > multipartThreshold) {
                    if(!preferences.getBoolean("s3.upload.multipart")) {
                        log.warn("Multipart upload is disabled with property s3.upload.multipart");
                        // Disabled by user
                        if(status.getLength() < preferences.getLong("s3.upload.multipart.required.threshold")) {
                            // Use single upload service with accelerate proxy
                            final S3SingleUploadService single = new S3SingleUploadService(tunneled, new S3WriteFeature(tunneled, new S3DisabledMultipartService()));
                            return single.upload(file, local, throttle, listener, status, prompt);
                        }
                    }
                    // Use multipart upload service with accelerate proxy
                    final Upload<StorageObject> service = new S3MultipartUploadService(tunneled, new S3WriteFeature(tunneled, new S3DisabledMultipartService()));
                    return service.upload(file, local, throttle, listener, status, prompt);
                }
                // Use single upload service with accelerate proxy
                final S3SingleUploadService service = new S3SingleUploadService(tunneled, new S3WriteFeature(tunneled, new S3DisabledMultipartService()));
                return service.upload(file, local, throttle, listener, status, prompt);
            }
            else {
                log.warn(String.format("Transfer acceleration disabled for %s", file));
            }
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Ignore failure reading S3 accelerate configuration. %s", e.getMessage()));
        }
        if(status.getLength() > multipartThreshold) {
            if(!preferences.getBoolean("s3.upload.multipart")) {
                log.warn("Multipart upload is disabled with property s3.upload.multipart");
                // Disabled by user
                if(status.getLength() < preferences.getLong("s3.upload.multipart.required.threshold")) {
                    // Use single upload service
                    return new S3SingleUploadService(session, writer).upload(file, local, throttle, listener, status, prompt);
                }
            }
            try {
                return new S3MultipartUploadService(session, writer).upload(file, local, throttle, listener, status, prompt);
            }
            catch(NotfoundException | InteroperabilityException e) {
                log.warn(String.format("Failure using multipart upload %s. Fallback to single upload.", e.getMessage()));
            }
        }
        // Use single upload service
        return new S3SingleUploadService(session, writer).upload(file, local, throttle, listener, status, prompt);
    }

    protected boolean accelerate(final Path file, final TransferStatus status, final ConnectionCallback prompt, final Host bookmark) throws BackgroundException {
        switch(session.getSignatureVersion()) {
            case AWS2:
                return false;
        }
        if(file.getType().contains(Path.Type.encrypted)) {
            return false;
        }
        if(accelerateTransferOption.getStatus(file)) {
            log.info(String.format("S3 transfer acceleration enabled for file %s", file));
            return true;
        }
        if(preferences.getBoolean("s3.accelerate.prompt")) {
            if(accelerateTransferOption.prompt(bookmark, file, status, prompt)) {
                log.info(String.format("S3 transfer acceleration enabled for file %s", file));
                return true;
            }
        }
        return false;
    }

    public S3ThresholdUploadService withMultipartThreshold(final Long threshold) {
        this.multipartThreshold = threshold;
        return this;
    }

    @Override
    public Upload<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}