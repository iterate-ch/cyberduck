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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Resolver;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.s3.RequestEntityRestStorageService;
import ch.cyberduck.core.s3.S3ExceptionMappingService;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPut;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;

import java.io.IOException;
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
import com.spectralogic.ds3client.commands.spectrads3.GetJobChunksReadyForClientProcessingSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetJobChunksReadyForClientProcessingSpectraS3Response;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.options.ReadJobOptions;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.JobNode;
import com.spectralogic.ds3client.models.MasterObjectList;
import com.spectralogic.ds3client.models.Objects;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.serializer.XmlProcessingException;

public class SpectraBulkService implements Bulk<Set<UUID>> {
    private static final Logger log = Logger.getLogger(SpectraBulkService.class);

    private final SpectraSession session;
    private Delete delete;

    private final PathContainerService containerService
        = new S3PathContainerService();

    private static final String REQUEST_PARAMETER_JOBID_IDENTIFIER = "job";
    private static final String REQUEST_PARAMETER_OFFSET = "offset";

    public SpectraBulkService(final SpectraSession session) {
        this.session = session;
        this.delete = new SpectraDeleteFeature(session);
    }

    @Override
    public Bulk<Set<UUID>> withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        //
    }

    /**
     * Deletes the file if it already exists for upload type. Create a job to stream PUT object requests. Clients should use this before
     * putting objects to physical data stores.
     *
     * @param type     Transfer type
     * @param files    Files and status
     * @param callback Prompt
     * @return Job status identifier list
     */
    @Override
    public Set<UUID> pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        final Ds3ClientHelpers helper = Ds3ClientHelpers.wrap(new SpectraClientBuilder().wrap(session.getClient(), session.getHost()));
        final Map<Path, List<Ds3Object>> objects = new HashMap<Path, List<Ds3Object>>();
        for(Map.Entry<TransferItem, TransferStatus> item : files.entrySet()) {
            final Path file = item.getKey().remote;
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
                            delete.delete(Collections.singletonList(file), callback, new Delete.DisabledCallback());
                        }
                        break;
                }
                objects.get(container).add(new Ds3Object(containerService.getKey(file), status.getLength()));
            }
            if(file.isDirectory()) {
                switch(type) {
                    case upload:
                        objects.get(container).add(new Ds3Object(containerService.getKey(file), 0L));
                        // Do not include folders when creating a GET job
                        break;
                }
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
                            container.getKey().getName(), container.getValue(), ReadJobOptions.create());
                        break;
                    case upload:
                        job = helper.startWriteJob(
                            container.getKey().getName(), container.getValue(), WriteJobOptions.create()
                                .withMaxUploadSize(Integer.MAX_VALUE)
                                .withChecksumType(ChecksumType.Type.CRC_32));
                        break;
                    default:
                        throw new NotfoundException(String.format("Unsupported transfer type %s", type));
                }
                jobs.add(job.getJobId());
                for(Map.Entry<TransferItem, TransferStatus> item : files.entrySet()) {
                    if(container.getKey().equals(containerService.getContainer(item.getKey().remote))) {
                        final TransferStatus status = item.getValue();
                        final Map<String, String> parameters = new HashMap<>(status.getParameters());
                        parameters.put(REQUEST_PARAMETER_JOBID_IDENTIFIER, job.getJobId().toString());
                        status.withParameters(parameters);
                    }
                }
            }
            return jobs;
        }
        catch(XmlProcessingException e) {
            throw new DefaultExceptionMappingService().map(e);
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
    public List<TransferStatus> query(final Transfer.Type type, final Path file, final TransferStatus status) throws BackgroundException {
        // This will respond with which job chunks have been loaded into cache and are ready for download.
        try {
            if(!status.getParameters().containsKey(REQUEST_PARAMETER_JOBID_IDENTIFIER)) {
                throw new NotfoundException(String.format("Missing job id parameter in status for %s", file.getName()));
            }
            final String job = status.getParameters().get(REQUEST_PARAMETER_JOBID_IDENTIFIER);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Query status for job %s", job));
            }
            final MasterObjectList list = new MasterObjectList();
            list.setObjects(Collections.emptyList());
            List<TransferStatus> chunks = this.query(file, status, job, list);
            if(chunks.isEmpty()) {
                // Fetch current list from server
                final Ds3Client client = new SpectraClientBuilder().wrap(session.getClient(), session.getHost());
                // For GET, the client may need to issue multiple GET requests for a single object if it has
                // been broken up into multiple pieces due to its large size
                // For PUT, This will allocate a working window of job chunks, if possible, and return a list of
                // the job chunks that the client can upload. The client should PUT all of the object parts
                // from the list of job chunks returned and repeat this process until all chunks are transferred

                final GetJobChunksReadyForClientProcessingSpectraS3Response response = client.getJobChunksReadyForClientProcessingSpectraS3(
                    new GetJobChunksReadyForClientProcessingSpectraS3Request(UUID.fromString(job)).withPreferredNumberOfChunks(Integer.MAX_VALUE));
                if(log.isInfoEnabled()) {
                    log.info(String.format("Job status %s for job %s", response.getStatus(), job));
                }
                switch(response.getStatus()) {
                    case RETRYLATER: {
                        final Duration delay = Duration.ofSeconds(response.getRetryAfterSeconds());
                        throw new RetriableAccessDeniedException(String.format("Job %s not yet loaded into cache", job), delay);
                    }
                }
                final MasterObjectList master = response.getMasterObjectListResult();
                if(log.isInfoEnabled()) {
                    log.info(String.format("Master object list with %d objects for %s", master.getObjects().size(), file));
                    log.info(String.format("Master object list status %s for %s", master.getStatus(), file));
                }
                chunks = this.query(file, status, job, master);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Server returned %d chunks for %s", chunks.size(), file));
                }
                if(chunks.isEmpty()) {
                    log.error(String.format("File %s not found in object list for job %s", file.getName(), job));
                    // Still look for Retry-After header for non empty master object list
                    if(response.getStatus() == GetJobChunksReadyForClientProcessingSpectraS3Response.Status.RETRYLATER) {
                        final Duration delay = Duration.ofSeconds(response.getRetryAfterSeconds());
                        throw new RetriableAccessDeniedException(String.format("Cache is full for job %s", job), delay);
                    }
                }
            }
            return chunks;
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private List<TransferStatus> query(final Path file, final TransferStatus status, final String job,
                                       final MasterObjectList master) throws BackgroundException {
        final List<TransferStatus> chunks = new ArrayList<TransferStatus>();
        for(Objects objects : master.getObjects()) {
            final UUID nodeId = objects.getNodeId();
            if(null == nodeId) {
                log.warn(String.format("No node returned in master object list for file %s", file));
            }
            else {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Determined node %s for %s", nodeId, file));
                }
            }
            for(JobNode node : master.getNodes()) {
                if(node.getId().equals(nodeId)) {
                    final Host host = session.getHost();
                    // The IP address or DNS name of the BlackPearl node.
                    if(StringUtils.equals(node.getEndPoint(), host.getHostname())) {
                        break;
                    }
                    if(StringUtils.equals(node.getEndPoint(), new Resolver().resolve(host.getHostname(),
                        new DisabledCancelCallback()).getHostAddress())) {
                        break;
                    }
                    log.warn(String.format("Redirect to %s for file %s", node.getEndPoint(), file));
                }
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Object list with %d objects for job %s", objects.getObjects().size(), job));
            }
            for(BulkObject object : objects.getObjects()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Found object %s looking for %s", object, file));
                }
                if(object.getName().equals(containerService.getKey(file))) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Found chunk %s matching file %s", object, file));
                    }
                    final TransferStatus chunk = new TransferStatus()
                        .exists(status.isExists())
                        .withMetadata(status.getMetadata())
                        .withParameters(status.getParameters());
                    // Server sends multiple chunks with offsets
                    if(object.getOffset() > 0L) {
                        chunk.setAppend(true);
                    }
                    chunk.setLength(object.getLength());
                    chunk.setOffset(object.getOffset());
                    // Job parameter already present from #pre
                    final Map<String, String> parameters = new HashMap<>(chunk.getParameters());
                    // Set offset for chunk.
                    parameters.put(REQUEST_PARAMETER_OFFSET, Long.toString(chunk.getOffset()));
                    chunk.setParameters(parameters);
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Add chunk %s for file %s", chunk, file));
                    }
                    chunks.add(chunk);
                }
            }
        }
        return chunks;
    }

    /**
     * Forces a full reclaim of all caches, and waits until the reclaim completes. Cache contents that need to be retained because they are a part of an active job are retained.
     * Any cache contents that can be reclaimed will be. This operation may take a very long time to complete, depending on how much of the cache can be reclaimed and how many blobs the cache is managing.
     */
    protected void clear() throws BackgroundException {
        try {
            final RequestEntityRestStorageService client = session.getClient();
            final HttpPut request = new HttpPut(String.format("%s://%s/_rest_/cache_filesystem?reclaim", session.getHost().getProtocol().getScheme(), session.getHost().getHostname()));
            client.authorizeHttpRequest(request, null, null);
            final HttpResponse response = client.getHttpClient().execute(request);
            if(HttpStatus.SC_NO_CONTENT != response.getStatusLine().getStatusCode()) {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        }
        catch(HttpResponseException e) {
            throw new HttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map(e);
        }
    }
}
