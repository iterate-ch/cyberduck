package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Search;
import ch.cyberduck.core.preferences.HostPreferences;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2SearchFeature implements Search {

    private final PathContainerService containerService
            = new DefaultPathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2SearchFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> regex, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> list = new AttributedList<>();
            String prefix = null;
            final AttributedList<Path> containers;
            if(workdir.isRoot()) {
                containers = new B2BucketListService(session, fileid).list(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), listener);
            }
            else {
                containers = new AttributedList<>(Collections.singletonList(containerService.getContainer(workdir)));
                if(!containerService.isContainer(workdir)) {
                    prefix = containerService.getKey(workdir) + Path.DELIMITER;
                }
            }
            for(Path container : containers) {
                String startFilename = prefix;
                do {
                    final B2ListFilesResponse response = session.getClient().listFileNames(
                            fileid.getVersionId(container),
                            startFilename,
                            new HostPreferences(session.getHost()).getInteger("b2.listing.chunksize"),
                            prefix, null);
                    for(B2FileInfoResponse info : response.getFiles()) {
                        final Path f = new Path(String.format("%s%s%s", container.getAbsolute(), Path.DELIMITER, info.getFileName()), EnumSet.of(Path.Type.file));
                        if(regex.accept(f)) {
                            list.add(f.withAttributes(new B2AttributesFinderFeature(session, fileid).toAttributes(info)));
                        }
                    }
                    startFilename = response.getNextFileName();
                }
                while(startFilename != null);
            }
            return list;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}
