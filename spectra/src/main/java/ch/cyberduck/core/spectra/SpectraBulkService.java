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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Resolver;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.RedirectException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;
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
import com.spectralogic.ds3client.commands.AllocateJobChunkRequest;
import com.spectralogic.ds3client.commands.AllocateJobChunkResponse;
import com.spectralogic.ds3client.commands.GetAvailableJobChunksRequest;
import com.spectralogic.ds3client.commands.GetAvailableJobChunksResponse;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.models.bulk.MasterObjectList;
import com.spectralogic.ds3client.models.bulk.Node;
import com.spectralogic.ds3client.models.bulk.Objects;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.serializer.XmlProcessingException;

public class SpectraBulkService implements Bulk<Set<UUID>> {
    private static final Logger log = Logger.getLogger(SpectraBulkService.class);

    private final SpectraSession session;

    private final Delete delete;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private static final String JOBID_IDENTIFIER = "job";

    public SpectraBulkService(final SpectraSession session) {
        this(session, session.getFeature(Delete.class));
    }

    public SpectraBulkService(final SpectraSession session, final Delete delete) {
        this.session = session;
        this.delete = delete;
    }

    /**
     * Deletes the file if it already exists for upload type. Create a job to stream PUT object requests. Clients should use this before
     * putting objects to physical data stores.
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
                            log.warn(String.format("Delete existing file %s", file));
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
                final Ds3ClientHelpers.Job job;
                switch(type) {
                    case download:
                        job = helper.startReadJob(
                                container.getKey().getName(), container.getValue());
                        break;
                    case upload:
                        job = helper.startWriteJob(
                                container.getKey().getName(), container.getValue());
                        break;
                    default:
                        throw new NotfoundException(String.format("Unsupported transfer type %s", type));
                }
                jobs.add(job.getJobId());
                for(Map.Entry<Path, TransferStatus> item : files.entrySet()) {
                    if(container.getKey().equals(containerService.getContainer(item.getKey()))) {
                        final TransferStatus status = item.getValue();
                        final Map<String, String> parameters = new HashMap<>(status.getParameters());
                        parameters.put(JOBID_IDENTIFIER, job.getJobId().toString());
                        status.parameters(parameters);
                    }
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
     * Get a list of all job chunks for a given job that are ready for client processing.
     * <p>
     * For PUT jobs, this will allocate a working window of job chunks, if possible, and return the job chunks that the client can upload.
     * Any chunk returned is fully allocated, meaning that you do not have to handle HTTP 307 retries on subsequent PUTs for the chunks.
     * Retries adversely impact BlackPearl gateway performance and require you to provide the object data stream for every PUT retry.
     * <p>
     * For GET jobs, this will respond with which job chunks have been loaded into cache and are ready for download.
     *
     * @param file   File
     * @param status Write job id into status parameters
     * @throws RetriableAccessDeniedException                File is not yet in cache
     * @throws ch.cyberduck.core.exception.RedirectException Should be accessed from different node
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
            // For GET, the client may need to issue multiple GET requests for a single object if it has
            // been broken up into multiple pieces due to its large size
            // For PUT, This will allocate a working window of job chunks, if possible, and return a list of
            // the job chunks that the client can upload. The client should PUT all of the object parts
            // from the list of job chunks returned and repeat this process until all chunks are transferred
            final GetAvailableJobChunksResponse response =
                    // GetJobChunksReadyForClientProcessing
                    client.getAvailableJobChunks(new GetAvailableJobChunksRequest(UUID.fromString(job))
                            .withPreferredNumberOfChunks(1));
            if(log.isInfoEnabled()) {
                log.info(String.format("Job status %s for %s", response.getStatus(), file));
            }
            switch(response.getStatus()) {
                case RETRYLATER: {
                    final Duration delay = Duration.ofSeconds((response.getRetryAfterSeconds()));
                    throw new RetriableAccessDeniedException(String.format("Job %s not yet loaded into cache", job), delay);
                }
            }
            final MasterObjectList list = response.getMasterObjectList();
            final Objects chunk = list.getObjects().iterator().next();
            switch(type) {
                case upload:
                    this.allocate(file, chunk.getChunkId().toString());
                    break;
            }
            final UUID nodeId = chunk.getNodeId();
            if(null == nodeId) {
                log.warn(String.format("No node returned in master object list for file %s", file));
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Determined node %s for %s", nodeId, file));
            }
            for(Node node : list.getNodes()) {
                if(node.getId().equals(nodeId)) {
                    final Host host = session.getHost();
                    // The IP address or DNS name of the BlackPearl node.
                    if(!StringUtils.equals(node.getEndpoint(), new Resolver().resolve(host.getHostname()).getHostAddress())) {
                        final Host target = new Host(host.getProtocol(), node.getEndpoint(),
                                host.getProtocol().isSecure() ? node.getHttpsPort() : node.getHttpPort());
                        log.warn(String.format("Redirect to %s for file %s", target, file));
                        throw new RedirectException(target);
                    }
                    break;
                }
            }
            log.warn(String.format("Failed to determine node for id %s", nodeId));
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

    public void allocate(final Path file, final String chunkId) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Allocate job %s for file %s", chunkId, file));
        }
        try {
            boolean allocated = false;
            while(!allocated) {
                allocated = this.tryAllocateChunk(chunkId);
            }
        }
        catch(ConflictException e) {
            // Already allocated
            log.warn(String.format("Failed to allocate file %s. %s", file, e.getMessage()));
            throw e;
        }
    }

    /**
     * Allocate a specific job chunk that is part of a PUT job before beginning the PUT operation. This avoids the HTTP 307 retries on the
     * object PUTs and increases performance.
     */
    private boolean tryAllocateChunk(final String chunkId) throws BackgroundException {
        final Ds3Client client = new SpectraClientBuilder().wrap(session);
        try {
            final AllocateJobChunkResponse response = client.allocateJobChunk(new AllocateJobChunkRequest(UUID.fromString(chunkId)));
            if(log.isDebugEnabled()) {
                log.debug(String.format("Status %s for allocate job chunk request status", response.getStatus().toString()));
            }
            switch(response.getStatus()) {
                case RETRYLATER:
                    final Duration delay = Duration.ofSeconds((response.getRetryAfterSeconds()));
                    throw new RetriableAccessDeniedException(String.format("Allocate job failed for job %s", chunkId), delay);
            }
            return !response.getObjects().getObjects().isEmpty();
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
