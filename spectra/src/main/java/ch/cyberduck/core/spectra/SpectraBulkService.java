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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.SignatureException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetAvailableJobChunksRequest;
import com.spectralogic.ds3client.commands.GetAvailableJobChunksResponse;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.serializer.XmlProcessingException;

public class SpectraBulkService implements Bulk<Set<UUID>> {
    private static final Logger log = Logger.getLogger(SpectraBulkService.class);

    private final SpectraSession session;

    private final Delete delete;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private static final String JOBID_IDENTIFIER = "jobid";

    public SpectraBulkService(final SpectraSession session) {
        this(session, new S3DefaultDeleteFeature(session));
    }

    public SpectraBulkService(final SpectraSession session, final Delete delete) {
        this.session = session;
        this.delete = delete;
    }

    /**
     * Deletes the file if it already exists for upload type
     *
     * @param type  Transfer type
     * @param files Files and status
     * @return Job status identifier list
     * @throws BackgroundException
     */
    @Override
    public Set<UUID> pre(final Transfer.Type type, final Map<Path, TransferStatus> files) throws BackgroundException {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(new SpectraClientBuilder().wrap(session));
        final Map<Path, List<Ds3Object>> objects = new HashMap<Path, List<Ds3Object>>();
        for(Map.Entry<Path, TransferStatus> item : files.entrySet()) {
            final Path file = item.getKey();
            final Path container = containerService.getContainer(file);
            if(!objects.containsKey(container)) {
                objects.put(container, new ArrayList<Ds3Object>());
            }
            if(file.isFile()) {
                final TransferStatus status = item.getValue();
                switch(type) {
                    case upload:
                        if(status.isExists()) {
                            delete.delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.Callback() {
                                @Override
                                public void delete(final Path file) {
                                    //
                                }
                            });
                        }
                        break;
                }
                objects.get(container).add(new Ds3Object(containerService.getKey(file), status.getLength()));
            }
        }
        try {
            final Set<UUID> jobs = new HashSet<UUID>();
            for(Map.Entry<Path, List<Ds3Object>> container : objects.entrySet()) {
                if(container.getValue().isEmpty()) {
                    continue;
                }
                switch(type) {
                    case download:
                        final Ds3ClientHelpers.Job read = helper.startReadJob(
                                container.getKey().getName(), container.getValue(),
                                ReadJobOptions.create().withPriority(Priority.URGENT));
                        jobs.add(read.getJobId());
                        for(Map.Entry<Path, TransferStatus> item : files.entrySet()) {
                            if(container.getKey().equals(containerService.getContainer(item.getKey()))) {
                                item.getValue().parameters(Collections.singletonMap(JOBID_IDENTIFIER, read.getJobId().toString()));
                            }
                        }
                        break;
                    case upload:
                        final Ds3ClientHelpers.Job write = helper.startWriteJob(
                                container.getKey().getName(), container.getValue(),
                                WriteJobOptions.create().withPriority(Priority.URGENT));
                        jobs.add(write.getJobId());
                        for(Map.Entry<Path, TransferStatus> item : files.entrySet()) {
                            if(container.getKey().equals(containerService.getContainer(item.getKey()))) {
                                item.getValue().parameters(Collections.singletonMap(JOBID_IDENTIFIER, write.getJobId().toString()));
                            }
                        }
                        break;
                    default:
                        throw new BackgroundException();
                }
            }
            return jobs;
        }
        catch(XmlProcessingException | SignatureException e) {
            throw new BackgroundException(e);
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    /**
     * Query status of file in cache and set job id in status
     *
     * @param file   File
     * @param status Write job id into status parameters
     * @throws RetriableAccessDeniedException File is not yet in cache
     */
    public void query(final Transfer.Type type, final Path file, final TransferStatus status) throws BackgroundException {
        // This will respond with which job chunks have been loaded into cache and are ready for download.
        try {
            final String job;
            if(status.getParameters().containsKey(JOBID_IDENTIFIER)) {
                job = status.getParameters().get(JOBID_IDENTIFIER);
            }
            else {
                log.warn(String.format("Missing job id parameter in status for %s", file.getAbsolute()));
                final Set<UUID> id = this.pre(type, Collections.singletonMap(file, status));
                job = id.iterator().next().toString();
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Query job status %s of %s", job, file));
            }
            final Ds3Client client = new SpectraClientBuilder().wrap(session);
            final GetAvailableJobChunksResponse availableJobChunks =
                    client.getAvailableJobChunks(new GetAvailableJobChunksRequest(UUID.fromString(job)));
            if(log.isInfoEnabled()) {
                log.info(String.format("Job status %s for %s", availableJobChunks.getStatus(), file));
            }
            switch(availableJobChunks.getStatus()) {
                case RETRYLATER: {
                    final Duration delay = Duration.ofSeconds((availableJobChunks.getRetryAfterSeconds()));
                    throw new RetriableAccessDeniedException(String.format("Job %s not yet loaded into cache", job), delay);
                }
            }
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        catch(SignatureException e) {
            throw new BackgroundException(e);
        }
    }
}
