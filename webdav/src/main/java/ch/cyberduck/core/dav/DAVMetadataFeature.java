package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;
import com.github.sardine.util.SardineUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * Metadata stored as JSON object in a single dead property. Request the property by name with
 * {@link #METADATA_CUSTOM_NAMESPACE} as servers such as Nextcloud and ownCloud omit dead properties from
 * <code>allprop</code> responses.
 */
public class DAVMetadataFeature implements Headers {
    private static final Logger log = LogManager.getLogger(DAVMetadataFeature.class);

    public static final String CUSTOM_NAMESPACE_PREFIX = "cd";
    public static final String CUSTOM_NAMESPACE_URI = "https://cyberduck.io/ns";

    /**
     * Property with metadata key/value pairs serialized as JSON object
     */
    public static final QName METADATA_CUSTOM_NAMESPACE = new QName(CUSTOM_NAMESPACE_URI, "metadata", CUSTOM_NAMESPACE_PREFIX);

    private static final Gson gson = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    private final DAVSession session;

    public DAVMetadataFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getDefault(final Path file) {
        return HostPreferencesFactory.get(session.getHost()).getMap("webdav.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            final List<DavResource> resources = session.getClient().list(new DAVPathEncoder(session).encode(file), 0,
                    Collections.singleton(METADATA_CUSTOM_NAMESPACE));
            for(DavResource resource : resources) {
                return toMetadata(resource);
            }
            return Collections.emptyMap();
        }
        catch(SardineException e) {
            try {
                throw new DAVExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
            catch(InteroperabilityException | NotfoundException i) {
                log.warn("Failure to obtain attributes of {}. {}", file, i);
                // Workaround for #8902
                return Collections.emptyMap();
            }
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, file);
        }
    }

    /**
     * @return Metadata parsed from JSON in property {@link #METADATA_CUSTOM_NAMESPACE}
     */
    public static Map<String, String> toMetadata(final DavResource resource) {
        final Map<QName, String> properties = resource.getCustomPropsNS();
        if(null == properties) {
            return Collections.emptyMap();
        }
        final String value = properties.get(METADATA_CUSTOM_NAMESPACE);
        if(StringUtils.isBlank(value)) {
            return Collections.emptyMap();
        }
        try {
            final Map<String, String> metadata = gson.fromJson(value, MAP_TYPE);
            if(null == metadata) {
                return Collections.emptyMap();
            }
            return new HashMap<>(metadata);
        }
        catch(JsonParseException e) {
            log.warn("Failure parsing value {} of property {}. {}", value, METADATA_CUSTOM_NAMESPACE, e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        log.debug("Write metadata {} for file {}", status, file);
        try {
            final List<Element> setProps;
            final List<QName> removeProps;
            if(status.getMetadata().isEmpty()) {
                setProps = Collections.emptyList();
                removeProps = Collections.singletonList(METADATA_CUSTOM_NAMESPACE);
            }
            else {
                final Element element = SardineUtil.createElement(METADATA_CUSTOM_NAMESPACE);
                element.setTextContent(gson.toJson(status.getMetadata(), MAP_TYPE));
                setProps = Collections.singletonList(element);
                removeProps = Collections.emptyList();
            }
            session.getClient().patch(new DAVPathEncoder(session).encode(file), setProps, removeProps,
                    DAVLockFeature.toHeaders(session, file, status));
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, file);
        }
    }
}
