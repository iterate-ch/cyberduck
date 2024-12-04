package ch.cyberduck.core.spectra;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.DeleteObjectsRequest;
import com.spectralogic.ds3client.commands.spectrads3.DeleteBucketSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.DeleteFolderRecursivelySpectraS3Request;
import com.spectralogic.ds3client.networking.FailedRequestException;

public class SpectraDeleteFeature implements Delete {

    private final SpectraSession session;
    private final PathContainerService containerService;

    public SpectraDeleteFeature(final SpectraSession session) {
        this.session = session;
        this.containerService = new S3PathContainerService(session.getHost());
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        try {
            final Ds3Client client = new SpectraClientBuilder().wrap(session.getClient(), session.getHost());
            final Map<Path, TransferStatus> filtered = new LinkedHashMap<>(files);
            for(Iterator<Map.Entry<Path, TransferStatus>> iter = filtered.entrySet().iterator(); iter.hasNext(); ) {
                final Map.Entry<Path, TransferStatus> file = iter.next();
                if(containerService.isContainer(file.getKey())) {
                    client.deleteBucketSpectraS3(
                            new DeleteBucketSpectraS3Request(containerService.getContainer(file.getKey()).getName()).withForce(true));
                    iter.remove();
                }
                else if(file.getKey().isDirectory()) {
                    client.deleteFolderRecursivelySpectraS3(
                            new DeleteFolderRecursivelySpectraS3Request(
                                    containerService.getContainer(file.getKey()).getName(),
                                    containerService.getKey(file.getKey())));
                    iter.remove();
                }
            }
            final Map<Path, List<Path>> containers = new HashMap<>();
            for(Path file : filtered.keySet()) {
                final Path bucket = containerService.getContainer(file);
                if(containers.containsKey(bucket)) {
                    containers.get(bucket).add(file);
                }
                else {
                    containers.put(bucket, new ArrayList<>(Collections.singletonList(file)));
                }
            }
            for(Map.Entry<Path, List<Path>> entry : containers.entrySet()) {
                final List<String> keys = entry.getValue().stream().map(containerService::getKey).collect(Collectors.toList());
                client.deleteObjects(new DeleteObjectsRequest(containerService.getContainer(entry.getKey()).getName(), keys));
            }
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map(e);
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
