package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Free-form key/value metadata stored in the global <code>properties</code> metadata template instance of a file or folder
 */
public class BoxMetadataFeature implements Metadata {
    private static final Logger log = LogManager.getLogger(BoxMetadataFeature.class);

    private static final String SCOPE = "global";
    private static final String TEMPLATE_KEY = "properties";
    private static final String TEMPLATE = String.format("%s/%s", SCOPE, TEMPLATE_KEY);
    /**
     * Field to request metadata template instance with file or folder
     */
    public static final String FIELD = String.format("metadata.%s.%s", SCOPE, TEMPLATE_KEY);
    private static final String SYSTEM_KEY_PREFIX = "$";
    private static final ContentType JSON_PATCH = ContentType.create("application/json-patch+json");

    private final BoxSession session;
    private final BoxFileidProvider fileid;
    private final ObjectMapper mapper = new JSON().getContext(null);

    public BoxMetadataFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Map<String, String> getDefault(final Path file) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return Collections.emptyMap();
        }
        try {
            final Map<String, String> metadata = this.read(file);
            if(null == metadata) {
                return Collections.emptyMap();
            }
            return metadata;
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        if(file.isRoot()) {
            log.warn("Skip writing metadata for root folder");
            return;
        }
        try {
            final Map<String, String> current = this.read(file);
            final Map<String, String> update = status.getMetadata();
            if(null == current) {
                if(update.isEmpty()) {
                    log.debug("No metadata instance to update for {}", file);
                    return;
                }
                // Create template instance
                final HttpPost request = new HttpPost(this.toUrl(file));
                request.setEntity(new ByteArrayEntity(mapper.writeValueAsBytes(update), ContentType.APPLICATION_JSON));
                this.execute(request);
                return;
            }
            if(update.isEmpty()) {
                // Remove template instance
                this.execute(new HttpDelete(this.toUrl(file)));
                return;
            }
            final List<Map<String, String>> operations = toPatch(current, update);
            if(operations.isEmpty()) {
                log.debug("No change in metadata for {}", file);
                return;
            }
            final HttpPut request = new HttpPut(this.toUrl(file));
            request.setEntity(new ByteArrayEntity(mapper.writeValueAsBytes(operations), JSON_PATCH));
            this.execute(request);
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    /**
     * @return Null if no metadata template instance is attached to file
     */
    private Map<String, String> read(final Path file) throws BackgroundException, IOException {
        try {
            final Map<String, Object> instance = session.getClient().execute(new HttpGet(this.toUrl(file)),
                    new BoxClientErrorResponseHandler<Map<String, Object>>() {
                        @Override
                        public Map<String, Object> handleEntity(final HttpEntity entity) throws IOException {
                            return mapper.readValue(entity.getContent(), new TypeReference<Map<String, Object>>() {
                            });
                        }
                    });
            return toMetadata(instance);
        }
        catch(HttpResponseException e) {
            if(HttpStatus.SC_NOT_FOUND == e.getStatusCode()) {
                log.debug("No metadata instance found for {}", file);
                return null;
            }
            throw e;
        }
    }

    /**
     * @param instance Metadata template instance
     * @return Key/value pairs excluding system keys
     */
    protected static Map<String, String> toMetadata(final Map<String, Object> instance) {
        final Map<String, String> metadata = new HashMap<>();
        for(Map.Entry<String, Object> entry : instance.entrySet()) {
            if(StringUtils.startsWith(entry.getKey(), SYSTEM_KEY_PREFIX)) {
                continue;
            }
            metadata.put(entry.getKey(), null == entry.getValue() ? null : String.valueOf(entry.getValue()));
        }
        return metadata;
    }

    /**
     * @param metadata Metadata template instances by scope and template key as returned for {@link #FIELD}
     * @return Null when no metadata was requested for item
     */
    protected static Map<String, String> fromInstances(final Map<String, Map<String, Map<String, Object>>> metadata) {
        if(null == metadata) {
            return null;
        }
        final Map<String, Map<String, Object>> scope = metadata.get(SCOPE);
        if(null == scope || null == scope.get(TEMPLATE_KEY)) {
            return Collections.emptyMap();
        }
        return toMetadata(scope.get(TEMPLATE_KEY));
    }

    /**
     * @return Metadata template instances by scope and template key as returned for {@link #FIELD}
     */
    protected static Map<String, Map<String, Map<String, Object>>> toInstance(final Map<String, String> metadata) {
        return Collections.singletonMap(SCOPE, Collections.singletonMap(TEMPLATE_KEY, new HashMap<>(metadata)));
    }

    private void execute(final HttpRequestBase request) throws IOException {
        session.getClient().execute(request, new BoxClientErrorResponseHandler<Void>() {
            @Override
            public Void handleEntity(final HttpEntity entity) {
                return null;
            }
        });
    }

    private String toUrl(final Path file) throws BackgroundException {
        return String.format("%s/%s/%s/metadata/%s", new BoxApiClient(session.getClient()).getBasePath(),
                file.isDirectory() ? "folders" : "files", fileid.getFileId(file), TEMPLATE);
    }

    /**
     * @return JSON Patch operations to transform current into update
     */
    protected static List<Map<String, String>> toPatch(final Map<String, String> current, final Map<String, String> update) {
        final List<Map<String, String>> operations = new ArrayList<>();
        for(String key : current.keySet()) {
            if(!update.containsKey(key)) {
                operations.add(toOperation("remove", key, null));
            }
        }
        for(Map.Entry<String, String> entry : update.entrySet()) {
            if(current.containsKey(entry.getKey())) {
                if(!StringUtils.equals(current.get(entry.getKey()), entry.getValue())) {
                    operations.add(toOperation("replace", entry.getKey(), entry.getValue()));
                }
            }
            else {
                operations.add(toOperation("add", entry.getKey(), entry.getValue()));
            }
        }
        return operations;
    }

    private static Map<String, String> toOperation(final String op, final String key, final String value) {
        final Map<String, String> operation = new LinkedHashMap<>();
        operation.put("op", op);
        // Escape according to RFC 6901
        operation.put("path", String.format("/%s", key.replace("~", "~0").replace("/", "~1")));
        if(null != value) {
            operation.put("value", value);
        }
        return operation;
    }
}
