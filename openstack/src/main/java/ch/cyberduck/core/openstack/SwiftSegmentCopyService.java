package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.List;

public class SwiftSegmentCopyService implements Copy {

    private final PathContainerService containerService
        = new DefaultPathContainerService();

    private final SwiftSession session;
    private final SwiftRegionService regionService;

    public SwiftSegmentCopyService(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftSegmentCopyService(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        final SwiftSegmentService segmentService = new SwiftSegmentService(session);
        final List<Path> segments = segmentService.list(source);
        if(segments.isEmpty()) {
            return new SwiftDefaultCopyFeature(session, regionService).copy(source, target, status, callback, listener);
        }
        else {
            return new SwiftLargeObjectCopyFeature(session, regionService, segmentService)
                .copy(source, segments, target, status, callback, listener);
        }
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(containerService.isContainer(source)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source.getName())).withFile(source);
        }
        if(containerService.isContainer(target)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source.getName())).withFile(target);
        }
    }
}
