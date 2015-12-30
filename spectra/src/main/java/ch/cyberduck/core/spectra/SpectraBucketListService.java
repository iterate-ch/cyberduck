/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetServiceRequest;
import com.spectralogic.ds3client.models.Bucket;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class SpectraBucketListService implements RootListService {
    private static final Logger log = Logger.getLogger(SpectraBucketListService.class);

    private final SpectraSession session;

    public SpectraBucketListService(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public List<Path> list(final ListProgressListener listener) throws BackgroundException {
        final Ds3Client client = new SpectraClientBuilder().wrap(session);
        try {
            final List<Path> buckets = new ArrayList<Path>();
            for(Bucket b : client.getService(new GetServiceRequest()).getResult().getBuckets()) {
                final Path bucket = new Path(b.getName(), EnumSet.of(Path.Type.volume, Path.Type.directory));
                buckets.add(bucket);
                listener.chunk(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)),
                        new AttributedList<Path>(buckets));
            }
            return buckets;
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map("Listing directory {0} failed", e,
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        }
        catch(HttpResponseException e) {
            throw new HttpResponseExceptionMappingService().map("Listing directory {0} failed", e,
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Listing directory {0} failed", e,
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        }
        catch(SignatureException e) {
            throw new BackgroundException(e);
        }
    }
}
