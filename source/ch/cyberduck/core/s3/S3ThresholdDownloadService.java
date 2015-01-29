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
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.udt.UDTExceptionMappingService;
import ch.cyberduck.core.udt.UDTProxy;
import ch.cyberduck.core.udt.UDTProxyProvider;
import ch.cyberduck.core.udt.UDTTransferOption;
import ch.cyberduck.core.udt.qloudsonic.QloudsonicProxyProvider;
import ch.cyberduck.core.udt.qloudsonic.QloudsonicTransferOption;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.Constants;

import com.barchart.udt.ExceptionUDT;

/**
 * @version $Id$
 */
public class S3ThresholdDownloadService extends DefaultDownloadFeature {
    private static final Logger log = Logger.getLogger(S3ThresholdDownloadService.class);

    private Preferences preferences
            = PreferencesFactory.get();

    private S3Session session;

    private Long udtThreshold = preferences.getLong("s3.download.udt.threshold");

    private UDTTransferOption udtTransferOption;

    private UDTProxyProvider udtProxyProvider;

    public S3ThresholdDownloadService(final S3Session session) {
        super(new S3ReadFeature(session));
        this.session = session;
        this.udtTransferOption = new QloudsonicTransferOption();
        this.udtProxyProvider = new QloudsonicProxyProvider();
    }

    public S3ThresholdDownloadService(final S3Session session, final UDTTransferOption udtTransferOption) {
        super(new S3ReadFeature(session));
        this.session = session;
        this.udtTransferOption = udtTransferOption;
        this.udtProxyProvider = new QloudsonicProxyProvider();
    }

    public S3ThresholdDownloadService(final Read reader, final S3Session session,
                                      final UDTTransferOption udtTransferOption, final UDTProxyProvider udtProxyProvider) {
        super(new S3ReadFeature(session));
        this.session = session;
        this.udtTransferOption = udtTransferOption;
        this.udtProxyProvider = udtProxyProvider;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
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
                    final S3Session proxy = new UDTProxy<S3Session>(location, udtProxyProvider)
                            .proxy(new S3Session(session.getHost()), session);
                    proxy.open(new DisabledHostKeyCallback(), session);
                    // Swap credentials. No login required
                    proxy.getClient().setProviderCredentials(session.getClient().getProviderCredentials());
                    try {
                        new DefaultDownloadFeature(new S3ReadFeature(proxy)).download(file, local, throttle,
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
