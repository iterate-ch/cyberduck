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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.s3.S3PathContainerService;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.HeadObjectRequest;
import com.spectralogic.ds3client.commands.HeadObjectResponse;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class SpectraAttributesFinderFeature implements AttributesFinder, AttributesAdapter<HeadObjectResponse> {

    private final SpectraSession session;
    private final PathContainerService containerService;

    public SpectraAttributesFinderFeature(final SpectraSession session) {
        this.session = session;
        this.containerService = new S3PathContainerService(session.getHost());
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        if(containerService.isContainer(file)) {
            return PathAttributes.EMPTY;
        }
        return this.toAttributes(this.details(file));
    }

    protected HeadObjectResponse details(final Path file) throws BackgroundException {
        try {
            final Ds3Client client = new SpectraClientBuilder().wrap(session.getClient(), session.getHost());
            final HeadObjectResponse response = client.headObject(new HeadObjectRequest(containerService.getContainer(file).getName(), containerService.getKey(file)));
            switch(response.getStatus()) {
                case DOESNTEXIST:
                    throw new NotfoundException(file.getAbsolute());
            }
            return response;
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final HeadObjectResponse object) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(object.getObjectSize());
        final Map<String, String> metadata = new HashMap<>();
        for(String key : object.getMetadata().keys()) {
            for(String value : object.getMetadata().get(key)) {
                metadata.put(key, value);
            }
        }
        attributes.setMetadata(metadata);
        for(Map.Entry<Long, String> checksum : object.getBlobChecksums().entrySet()) {
            attributes.setChecksum(Checksum.parse(Hex.encodeHexString(Base64.decodeBase64(checksum.getValue()))));
        }
        return attributes;
    }
}
