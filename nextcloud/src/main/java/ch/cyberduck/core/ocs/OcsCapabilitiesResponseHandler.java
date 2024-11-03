package ch.cyberduck.core.ocs;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ocs.model.Capabilities;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class OcsCapabilitiesResponseHandler extends OcsResponseHandler<OcsCapabilities> {
    private static final Logger log = LogManager.getLogger(OcsCapabilitiesResponseHandler.class);

    private final OcsCapabilities capabilities;

    public OcsCapabilitiesResponseHandler(final OcsCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public OcsCapabilities handleEntity(final HttpEntity entity) throws IOException {
        final XmlMapper mapper = new XmlMapper();
        final Capabilities value = mapper.readValue(entity.getContent(), Capabilities.class);
        if(value.data != null) {
            if(value.data.capabilities != null) {
                if(value.data.capabilities.core != null) {
                    capabilities.withWebdav(value.data.capabilities.core.webdav);
                }
                if(value.data.capabilities.files != null) {
                    if(value.data.capabilities.files.locking != null) {
                        try {
                            capabilities.withLocking(1 == Double.parseDouble(value.data.capabilities.files.locking));
                        }
                        catch(NumberFormatException e) {
                            log.warn("Failure parsing {}", value.data.capabilities.files.locking);
                        }
                    }
                    if(value.data.capabilities.files.versioning != null) {
                        try {
                            capabilities.withVersioning(1 == Integer.parseInt(value.data.capabilities.files.versioning));
                        }
                        catch(NumberFormatException e) {
                            log.warn("Failure parsing {}", value.data.capabilities.files.versioning);
                        }
                    }
                }
            }
        }
        if(log.isDebugEnabled()) {
            log.debug("Determined OCS capabilities {}", capabilities);
        }
        return capabilities;
    }
}
