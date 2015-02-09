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
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.udt.DisabledUDTTransferOption;
import ch.cyberduck.core.udt.UDTExceptionMappingService;
import ch.cyberduck.core.udt.UDTProxy;
import ch.cyberduck.core.udt.UDTTransferOption;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.Constants;
import org.jets3t.service.model.StorageObject;

import com.barchart.udt.ExceptionUDT;

/**
 * @version $Id$
 */
public class S3ThresholdUploadService implements Upload<StorageObject> {
    private static final Logger log = Logger.getLogger(S3ThresholdUploadService.class);

    private Preferences preferences
            = PreferencesFactory.get();

    private S3Session session;

    private Long multipartThreshold
            = preferences.getLong("s3.upload.multipart.threshold");

    private Long udtThreshold
            = preferences.getLong("s3.upload.udt.threshold");

    private UDTTransferOption udtTransferOption
            = new DisabledUDTTransferOption();

    private X509TrustManager trust;

    private X509KeyManager key;

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key) {
        this.session = session;
        this.trust = trust;
        this.key = key;
    }

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key,
                                    final Long multipartThreshold) {
        this.session = session;
        this.trust = trust;
        this.key = key;
        this.multipartThreshold = multipartThreshold;
    }

    public S3ThresholdUploadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key,
                                    final UDTTransferOption udtTransferOption) {
        this.session = session;
        this.trust = trust;
        this.key = key;
        this.udtTransferOption = udtTransferOption;
    }

    @Override
    public StorageObject upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        final Host bookmark = session.getHost();
        if(bookmark.getHostname().endsWith(Constants.S3_DEFAULT_HOSTNAME)) {
            // Only for AWS given threshold
            if(status.getLength() > udtThreshold) {
                // Prompt user
                if(udtTransferOption.prompt(bookmark, status, prompt)) {
                    final Location.Name location = session.getFeature(Location.class).getLocation(file);
                    if(Location.unknown.equals(location)) {
                        throw new AccessDeniedException("Cannot read bucket location");
                    }
                    final S3Session proxy = new UDTProxy<S3Session>(location, udtTransferOption.provider(), trust, key)
                            .proxy(new S3Session(session.getHost()), session);
                    final S3Session.RequestEntityRestStorageService client = proxy.open(new DisabledHostKeyCallback(), session);
                    // Swap credentials. No login required
                    client.setProviderCredentials(session.getClient().getProviderCredentials());
                    final Upload<StorageObject> service = new S3MultipartUploadService(proxy);
                    try {
                        return service.upload(file, local, throttle, listener, status, prompt);
                    }
                    catch(BackgroundException e) {
                        final Throwable cause = ExceptionUtils.getRootCause(e);
                        if(cause instanceof ExceptionUDT) {
                            throw new UDTExceptionMappingService().map((ExceptionUDT) cause);
                        }
                        throw e;
                    }
                }
            }
            if(status.getLength() > multipartThreshold) {
                if(!preferences.getBoolean("s3.upload.multipart")) {
                    // Disabled by user
                    if(status.getLength() < preferences.getLong("s3.upload.multipart.required.threshold")) {
                        log.warn("Multipart upload is disabled with property s3.upload.multipart");
                        final S3SingleUploadService single = new S3SingleUploadService(session);
                        return single.upload(file, local, throttle, listener, status, prompt);
                    }
                }
                final S3MultipartUploadService service = new S3MultipartUploadService(session);
                try {
                    return service.upload(file, local, throttle, listener, status, prompt);
                }
                catch(NotfoundException e) {
                    log.warn(String.format("Failure using multipart upload %s. Fallback to single upload.", e.getMessage()));
                    final S3SingleUploadService single = new S3SingleUploadService(session);
                    return single.upload(file, local, throttle, listener, status, prompt);
                }
                catch(InteroperabilityException e) {
                    log.warn(String.format("Failure using multipart upload %s. Fallback to single upload.", e.getMessage()));
                    final S3SingleUploadService single = new S3SingleUploadService(session);
                    return single.upload(file, local, throttle, listener, status, prompt);
                }
            }
        }
        final S3SingleUploadService single = new S3SingleUploadService(session);
        return single.upload(file, local, throttle, listener, status, prompt);
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