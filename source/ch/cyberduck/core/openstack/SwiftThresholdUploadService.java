package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;

/**
 * @author Joel Wright <joel.wright@sohonet.com>
 * @version $Id$
 */
public class SwiftThresholdUploadService implements Upload {

    private SwiftSession session;

    private Long threshold;

    public SwiftThresholdUploadService(final SwiftSession session) {
        this(session, Preferences.instance().getLong("openstack.upload.largeobject.threshold"));
    }

    public SwiftThresholdUploadService(SwiftSession session, Long threshold) {
        this.session = session;
        this.threshold = threshold;
    }

    @Override
    public void upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                       final TransferStatus status) throws BackgroundException {
        final Upload feature;
        if(status.getLength() > threshold) {
            feature = new SwiftLargeObjectUploadFeature(session, threshold);
        }
        else {
            feature = new DefaultUploadFeature(session);
        }
        feature.upload(file, local, throttle, listener, status);
    }
}
