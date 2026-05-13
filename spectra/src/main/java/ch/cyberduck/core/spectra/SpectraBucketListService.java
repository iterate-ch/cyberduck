package ch.cyberduck.core.spectra;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.util.EnumSet;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetServiceRequest;
import com.spectralogic.ds3client.commands.GetServiceResponse;
import com.spectralogic.ds3client.models.BucketDetails;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class SpectraBucketListService implements ListService {

    private final SpectraSession session;

    public SpectraBucketListService(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> buckets = new AttributedList<Path>();
            final Ds3Client client = new SpectraClientBuilder().wrap(session, session.getHost());
            final GetServiceResponse response = client.getService(new GetServiceRequest());
            for(final BucketDetails b : response.getListAllMyBucketsResult().getBuckets()) {
                final Path bucket = new Path(PathNormalizer.normalize(b.getName()), EnumSet.of(Path.Type.volume, Path.Type.directory));
                bucket.attributes().setCreationDate(b.getCreationDate().getTime());
                buckets.add(bucket);
            }
            return buckets;
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
