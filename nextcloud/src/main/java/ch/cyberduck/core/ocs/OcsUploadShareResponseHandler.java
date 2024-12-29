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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.ocs.model.Share;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class OcsUploadShareResponseHandler extends OcsResponseHandler<DescriptiveUrl> {
    private static final Logger log = LogManager.getLogger(OcsUploadShareResponseHandler.class);

    @Override
    public DescriptiveUrl handleEntity(final HttpEntity entity) throws IOException {
        if(isXml(entity)) {
            final XmlMapper mapper = new XmlMapper();
            final Share value = mapper.readValue(entity.getContent(), Share.class);
            if(null != value.data) {
                if(null != value.data.url) {
                    return new DescriptiveUrl(value.data.url, DescriptiveUrl.Type.http);
                }
            }
        }
        else {
            log.warn("Ignore entity {}", entity);
        }
        return DescriptiveUrl.EMPTY;
    }
}
