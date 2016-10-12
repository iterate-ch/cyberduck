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
import ch.cyberduck.core.accelerate.AccelerationTransferOption;
import ch.cyberduck.core.accelerate.DisabledAccelerationTransferOption;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
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

    private Long udtThreshold
            = preferences.getLong("s3.upload.udt.threshold");

    private final AccelerationTransferOption<S3Session> accelerateTransferOption;

    private final X509TrustManager trust;

    private final X509KeyManager key;

    private final S3SingleUploadService singleUploadService;

    private final S3MultipartUploadService multipartUploadService;

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key) {
        this(session, trust, key, PreferencesFactory.get().getLong("s3.upload.multipart.threshold"));
    }

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key,
                                    final Long multipartThreshold) {
        this(session, trust, key, multipartThreshold, new DisabledAccelerationTransferOption<S3Session>());
    }

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key,
                                    final AccelerationTransferOption<S3Session> accelerateTransferOption) {
        this(session, trust, key, PreferencesFactory.get().getLong("s3.upload.multipart.threshold"), accelerateTransferOption);
    }

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key,
                                    final Long multipartThreshold,
                                    final AccelerationTransferOption<S3Session> accelerateTransferOption) {
        this.session = session;
        this.trust = trust;
        this.key = key;
        this.multipartThreshold = multipartThreshold;
        this.accelerateTransferOption = accelerateTransferOption;
        this.singleUploadService = new S3SingleUploadService(session);
        this.multipartUploadService = new S3MultipartUploadService(session);
    }

    @Override
    public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        return new S3WriteFeature(session).append(file, length, cache);
    }

    @Override
    public StorageObject upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        final Host bookmark = session.getHost();
        if(bookmark.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
            // Only for AWS given threshold
            if(status.getLength() > udtThreshold) {
                // Prompt user
                if(accelerateTransferOption.prompt(bookmark, status, prompt)) {
                    final S3Session tunneled = accelerateTransferOption.open(bookmark, file, trust, key);
                    if(!preferences.getBoolean("s3.upload.multipart")) {
                        // Disabled by user
                        if(status.getLength() < preferences.getLong("s3.upload.multipart.required.threshold")) {
                            log.warn("Multipart upload is disabled with property s3.upload.multipart");
                            final S3SingleUploadService single = new S3SingleUploadService(tunneled);
                            return single.upload(file, local, throttle, listener, status, prompt);
                        }
                    }
                    final Upload<StorageObject> service = new S3MultipartUploadService(tunneled);
                    return service.upload(file, local, throttle, listener, status, prompt);
                }
            }
        }
        if(status.getLength() > multipartThreshold) {
            if(!preferences.getBoolean("s3.upload.multipart")) {
                // Disabled by user
                if(status.getLength() < preferences.getLong("s3.upload.multipart.required.threshold")) {
                    log.warn("Multipart upload is disabled with property s3.upload.multipart");
                    return singleUploadService.upload(file, local, throttle, listener, status, prompt);
                }
            }
            try {
                return multipartUploadService.upload(file, local, throttle, listener, status, prompt);
            }
            catch(NotfoundException | InteroperabilityException e) {
                log.warn(String.format("Failure using multipart upload %s. Fallback to single upload.", e.getMessage()));
            }
        }
        return singleUploadService.upload(file, local, throttle, listener, status, prompt);
    }

    public S3ThresholdUploadService withMultipartThreshold(final Long threshold) {
        this.multipartThreshold = threshold;
        return this;
    }

    public S3ThresholdUploadService withUdtThreshold(final Long threshold) {
        this.udtThreshold = threshold;
        return this;
    }
}