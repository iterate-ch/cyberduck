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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.PartialLoginFailureException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;

import org.apache.http.HttpStatus;

import java.io.StringReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class SDSExceptionMappingService extends AbstractExceptionMappingService<ApiException> {

    @Override
    public BackgroundException map(final ApiException failure) {
        final StringBuilder buffer = new StringBuilder();
        if(null != failure.getResponseBody()) {
            final JsonParser parser = new JsonParser();
            try {
                final JsonObject json = parser.parse(new StringReader(failure.getMessage())).getAsJsonObject();
                if(json.get("errorCode").isJsonPrimitive()) {
                    final JsonPrimitive errorCode = json.getAsJsonPrimitive("errorCode");
                    this.append(buffer, errorCode.getAsString());
                    switch(failure.getCode()) {
                        case HttpStatus.SC_NOT_FOUND:
                            switch(errorCode.getAsInt()) {
                                case -40761:
                                    // [-40761] Filekey not found for encrypted file
                                    return new AccessDeniedException(buffer.toString(), failure);
                            }
                        case HttpStatus.SC_PRECONDITION_FAILED:
                            switch(errorCode.getAsInt()) {
                                case -10108:
                                    // [-10108] Radius Access-Challenge required.
                                    return new PartialLoginFailureException(buffer.toString(), failure);
                            }
                    }
                }
                if(json.get("debugInfo").isJsonPrimitive()) {
                    this.append(buffer, json.getAsJsonPrimitive("debugInfo").getAsString());
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