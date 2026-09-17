package ch.cyberduck.core.ocs;

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

import ch.cyberduck.core.ocs.model.User;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Parse user identifier of the authenticated account
 */
public class OcsUserResponseHandler extends OcsResponseHandler<String> {
    private static final Logger log = LogManager.getLogger(OcsUserResponseHandler.class);

    /**
     * @return User identifier or null if not found in response
     */
    @Override
    public String handleEntity(final HttpEntity entity) throws IOException {
        if(isXml(entity)) {
            final XmlMapper mapper = new XmlMapper();
            final User value = mapper.readValue(entity.getContent(), User.class);
            if(value.data != null) {
                log.debug("Determined user {} from response", value.data.id);
                return value.data.id;
            }
        }
        else {
            log.warn("Ignore entity {}", entity);
        }
        return null;
    }
}
