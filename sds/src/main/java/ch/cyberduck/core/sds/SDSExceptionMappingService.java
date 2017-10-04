package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultSocketExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.PartialLoginFailureException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class SDSExceptionMappingService extends AbstractExceptionMappingService<ApiException> {
    private static final Logger log = Logger.getLogger(SDSExceptionMappingService.class);

    @Override
    public BackgroundException map(final ApiException failure) {
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SocketException) {
                // Map Connection has been shutdown: javax.net.ssl.SSLException: java.net.SocketException: Broken pipe
                return new DefaultSocketExceptionMappingService().map((SocketException) cause);
            }
            if(cause instanceof HttpResponseException) {
                return new HttpResponseExceptionMappingService().map((HttpResponseException) cause);
            }
            if(cause instanceof IOException) {
                return new DefaultIOExceptionMappingService().map((IOException) cause);
            }
        }
        final StringBuilder buffer = new StringBuilder();
        if(null != failure.getResponseBody()) {
            final JsonParser parser = new JsonParser();
            try {
                final JsonObject json = parser.parse(new StringReader(failure.getResponseBody())).getAsJsonObject();
                if(json.has("errorCode")) {
                    if(json.get("errorCode").isJsonPrimitive()) {
                        final int errorCode = json.getAsJsonPrimitive("errorCode").getAsInt();
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Failure with errorCode %s", errorCode));
                        }
                        final String key = String.format("Error %d", errorCode);
                        final String localized = LocaleFactory.get().localize(key, "SDS");
                        this.append(buffer, localized);
                        if(StringUtils.equals(localized, key)) {
                            log.warn(String.format("Missing user message for error code %d", errorCode));
                            if(json.has("debugInfo")) {
                                if(json.get("debugInfo").isJsonPrimitive()) {
                                    this.append(buffer, json.getAsJsonPrimitive("debugInfo").getAsString());
                                }
                            }
                        }
                        switch(failure.getCode()) {
                            case HttpStatus.SC_NOT_FOUND:
                                switch(errorCode) {
                                    case -70501:
                                        // [-70501] User not found
                                        return new AccessDeniedException(buffer.toString(), failure);
                                    case -40761:
                                        // [-40761] Filekey not found for encrypted file
                                        return new AccessDeniedException(buffer.toString(), failure);
                                }
                            case HttpStatus.SC_PRECONDITION_FAILED:
                                switch(errorCode) {
                                    case -10108:
                                        // [-10108] Radius Access-Challenge required.
                                        if(json.has("replyMessage")) {
                                            if(json.get("replyMessage").isJsonPrimitive()) {
                                                final JsonPrimitive replyMessage = json.getAsJsonPrimitive("replyMessage");
                                                if(log.isDebugEnabled()) {
                                                    log.debug(String.format("Failure with replyMessage %s", replyMessage));
                                                }
                                                buffer.append(replyMessage.getAsString());
                                            }
                                        }
                                        return new PartialLoginFailureException(buffer.toString(), failure);
                                }
                        }
                    }
                }
                else {
                    switch(failure.getCode()) {
                        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                            break;
                        default:
                            if(json.has("debugInfo")) {
                                log.warn(String.format("Missing error code for failure %s", json));
                                if(json.get("debugInfo").isJsonPrimitive()) {
                                    this.append(buffer, json.getAsJsonPrimitive("debugInfo").getAsString());
                                }
                            }
                    }
                }
            }
            catch(JsonParseException e) {
                // Ignore
                this.append(buffer, failure.getMessage());
            }
        }
        switch(failure.getCode()) {
            case HttpStatus.SC_PRECONDITION_FAILED:
                // [-10103] EULA must be accepted
                // [-10104] Password must be changed
                // [-10106] Username must be changed
                return new LoginFailureException(buffer.toString(), failure);
        }
        return new HttpResponseExceptionMappingService().map(failure, buffer, failure.getCode());
    }
}
