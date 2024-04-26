package ch.cyberduck.core.tus;

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

import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.tus.TusCapabilities.Extension;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TusCapabilitiesResponseHandler implements ResponseHandler<TusCapabilities> {
    private static final Logger log = LogManager.getLogger(TusCapabilitiesResponseHandler.class);

    private final TusCapabilities capabilities;

    public TusCapabilitiesResponseHandler(final TusCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public TusCapabilities handleResponse(final HttpResponse response) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_NO_CONTENT:
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Support for resumable uploads detected from %s", response));
                }
                if(response.containsHeader(TusCapabilities.TUS_HEADER_RESUMABLE)) {
                    if(response.containsHeader(TusCapabilities.TUS_HEADER_VERSION)) {
                        capabilities.withVersions(StringUtils.split(response.getFirstHeader(TusCapabilities.TUS_HEADER_VERSION).getValue(), ","));
                    }
                }
                if(response.containsHeader(TusCapabilities.TUS_HEADER_EXTENSION)) {
                    for(String extension : StringUtils.split(response.getFirstHeader(TusCapabilities.TUS_HEADER_EXTENSION).getValue(), ",")) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Support extension %s", extension));
                        }
                        if(StringUtils.equals(Extension.checksum.name(), StringUtils.strip(extension))) {
                            if(response.containsHeader(TusCapabilities.TUS_HEADER_CHECKSUM_ALGORITHM)) {
                                for(String algorithm : StringUtils.split(response.getFirstHeader(TusCapabilities.TUS_HEADER_CHECKSUM_ALGORITHM).getValue(), ",")) {
                                    try {
                                        capabilities.withExtension(Extension.checksum).withHashAlgorithm(HashAlgorithm.valueOf(algorithm));
                                        break;
                                    }
                                    catch(IllegalArgumentException e) {
                                        log.warn(String.format("No support for checksum algorithm %s", algorithm));
                                    }
                                }
                            }
                        }
                        else {
                            try {
                                capabilities.withExtension(Extension.valueOf(StringUtils.remove(extension, '-')));
                            }
                            catch(IllegalArgumentException e) {
                                log.warn(String.format("No support for extension %s", extension));
                            }
                        }
                    }
                }
        }
        return capabilities;
    }
}
