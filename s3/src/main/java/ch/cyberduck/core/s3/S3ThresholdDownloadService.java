package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.udt.DisabledUDTTransferOption;
import ch.cyberduck.core.udt.UDTExceptionMappingService;
import ch.cyberduck.core.udt.UDTProxyConfigurator;
import ch.cyberduck.core.udt.UDTTransferOption;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.barchart.udt.ExceptionUDT;

public class S3ThresholdDownloadService extends DefaultDownloadFeature {
    private static final Logger log = Logger.getLogger(S3ThresholdDownloadService.class);

    private Preferences preferences
            = PreferencesFactory.get();

    private S3Session session;

    private Long udtThreshold = preferences.getLong("s3.download.udt.threshold");

    private UDTTransferOption udtTransferOption
            = new DisabledUDTTransferOption();

    private X509TrustManager trust;

    private X509KeyManager key;

    public S3ThresholdDownloadService(final S3Session session, final X509TrustManager trust, final X509KeyManager key) {
        super(new S3ReadFeature(session));
        this.session = session;
        this.trust = trust;
        this.key = key;
    }

    public S3ThresholdDownloadService(final S3Session session,
                                      final X509TrustManager trust,
                                      final X509KeyManager key,
                                      final UDTTransferOption udtTransferOption) {
        super(new S3ReadFeature(session));
        this.session = session;
        this.trust = trust;
        this.key = key;
        this.udtTransferOption = udtTransferOption;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        final Host bookmark = session.getHost();
        if(bookmark.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
            // Only for AWS given threshold
            if(status.getLength() > udtThreshold) {
                // Prompt user
                if(udtTransferOption.prompt(bookmark, status, prompt)) {
                    final Location.Name location = session.getFeature(Location.class).getLocation(file);
                    if(Location.unknown.equals(location)) {
                        throw new AccessDeniedException("Cannot read bucket location");
                    }
                    final S3Session tunneled = new S3Session(session.getHost(), trust, key);
                    final UDTProxyConfigurator configurator = new UDTProxyConfigurator(location, udtTransferOption.provider(), trust, key);
                    configurator.configure(tunneled);
                    final RequestEntityRestStorageService client = tunneled.open(new DisabledHostKeyCallback(), session);
                    // Swap credentials. No login required
                    client.setProviderCredentials(session.getClient().getProviderCredentials());
                    try {
                        new DefaultDownloadFeature(new S3ReadFeature(tunneled)).download(file, local, throttle,
                                listener, status, prompt);
                    }
                    catch(BackgroundException e) {
                        final Throwable cause = ExceptionUtils.getRootCause(e);
                        if(cause instanceof ExceptionUDT) {
                            throw new UDTExceptionMappingService().map((ExceptionUDT) cause);
                        }
                        throw e;
                    }
                    return;
                }
            }
        }
        super.download(file, local, throttle, listener, status, prompt);
    }

    public S3ThresholdDownloadService withUdtThreshold(final Long threshold) {
        this.udtThreshold = threshold;
        return this;
    }
}
