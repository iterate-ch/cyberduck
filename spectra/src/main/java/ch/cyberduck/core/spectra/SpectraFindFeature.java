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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

import java.io.IOException;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.HeadBucketRequest;
import com.spectralogic.ds3client.commands.HeadBucketResponse;
import com.spectralogic.ds3client.commands.HeadObjectRequest;
import com.spectralogic.ds3client.commands.HeadObjectResponse;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class SpectraFindFeature implements Find {

    private final SpectraSession session;
    private final PathContainerService containerService;

    public SpectraFindFeature(final SpectraSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            final Ds3Client client = new SpectraClientBuilder().wrap(session.getClient(), session.getHost());
            if(containerService.isContainer(file)) {
                final HeadBucketResponse response = client.headBucket(new HeadBucketRequest(containerService.getContainer(file).getName()));
                switch(response.getStatus()) {
                    case DOESNTEXIST:
                        return false;
                }
                return true;
            }
            else {
                final HeadObjectResponse response = client.headObject(new HeadObjectRequest(containerService.getContainer(file).getName(), containerService.getKey(file)));
                switch(response.getStatus()) {
                    case DOESNTEXIST:
                        return false;
                }
                return true;
            }
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }
}
