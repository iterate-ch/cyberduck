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

import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.ocs.model.Share;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public abstract class OcsResponseHandler<R> extends AbstractResponseHandler<R> {
    private static final Logger log = LogManager.getLogger(OcsResponseHandler.class);

    @Override
    public R handleResponse(final HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        if(response.getEntity() != null) {
            EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
            if(isXml(response.getEntity())) {
                if(statusLine.getStatusCode() >= 300) {
                    final StringAppender message = new StringAppender();
                    message.append(statusLine.getReasonPhrase());
                    final Share error = new XmlMapper().readValue(response.getEntity().getContent(), Share.class);
                    message.append(error.meta.message);
                    throw new HttpResponseException(statusLine.getStatusCode(), message.toString());
                }
                final Share error = new XmlMapper().readValue(response.getEntity().getContent(), Share.class);
                try {
                    if(Integer.parseInt(error.meta.statuscode) > 100) {
                        final StringAppender message = new StringAppender();
                        message.append(error.meta.message);
                        throw new HttpResponseException(Integer.parseInt(error.meta.statuscode), message.toString());
                    }
                }
                catch(NumberFormatException e) {
                    log.warn("Failure parsing status code in response {}", error);
                }
            }
            else {
                log.warn("Ignore entity {}", response.getEntity());
            }
        }
        return super.handleResponse(response);
    }

    protected static boolean isXml(final HttpEntity response) {
        return StringUtils.equals(ContentType.APPLICATION_XML.getMimeType(),
                ContentType.parse(response.getContentType().getValue()).getMimeType())
                || StringUtils.equals(ContentType.TEXT_XML.getMimeType(),
                ContentType.parse(response.getContentType().getValue()).getMimeType());
    }
}
