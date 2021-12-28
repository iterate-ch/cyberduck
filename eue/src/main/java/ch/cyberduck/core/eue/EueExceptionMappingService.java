package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultSocketExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.net.SocketException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EueExceptionMappingService extends AbstractExceptionMappingService<ApiException> {
    private static final Logger log = LogManager.getLogger(EueExceptionMappingService.class);

    @Override
    public BackgroundException map(final ApiException failure) {
        final StringBuilder buffer = new StringBuilder();
        if(StringUtils.isNotBlank(failure.getMessage())) {
            for(String s : StringUtils.split(failure.getMessage(), ",")) {
                this.append(buffer, LocaleFactory.localizedString(s, "EUE"));
            }
        }
        if(null != failure.getResponseHeaders()) {
            final List<List<String>> headers = failure.getResponseHeaders().entrySet().stream()
                    .filter(e -> "X-UI-ENHANCED-STATUS".equalsIgnoreCase(e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
            for(List<String> header : headers) {
                for(String s : header) {
                    this.append(buffer, LocaleFactory.localizedString(s, "EUE"));
                }
            }
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof ProcessingException) {
                return new InteroperabilityException(cause.getMessage(), cause);
            }
            if(cause instanceof SocketException) {
                // Map Connection has been shutdown: javax.net.ssl.SSLException: java.net.SocketException: Broken pipe
                return new DefaultSocketExceptionMappingService().map((SocketException) cause);
            }
            if(cause instanceof HttpResponseException) {
                return new DefaultHttpResponseExceptionMappingService().map((HttpResponseException) cause);
            }
            if(cause instanceof IOException) {
                return new DefaultIOExceptionMappingService().map((IOException) cause);
            }
            if(cause instanceof IllegalStateException) {
                // Caused by: ApiException: javax.ws.rs.ProcessingException: java.lang.IllegalStateException: Connection pool shut down
                return new ConnectionCanceledException(cause);
            }
        }
        switch(failure.getCode()) {
            case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                return new LockedException(buffer.toString(), failure);
            case 429:
                final Optional<Map.Entry<String, List<String>>> header
                        = failure.getResponseHeaders().entrySet().stream().filter(e -> HttpHeaders.RETRY_AFTER.equals(e.getKey())).findAny();
                if(header.isPresent()) {
                    final Optional<String> value = header.get().getValue().stream().findAny();
                    return value.map(s -> new RetriableAccessDeniedException(buffer.toString(),
                            Duration.ofSeconds(Long.parseLong(s)), failure)).orElseGet(() -> new RetriableAccessDeniedException(buffer.toString(), failure));
                }
        }
        return new DefaultHttpResponseExceptionMappingService().map(failure, buffer, failure.getCode());
    }

    public BackgroundException map(final HttpResponse response) {
        final Map<String, List<String>> responseHeaders = new HashMap<>();
        for(Header s : response.getAllHeaders()) {
            responseHeaders.put(s.getName(), Collections.singletonList(s.getValue()));
        }
        return this.map(new ApiException(response.getStatusLine().getReasonPhrase(),
                null, response.getStatusLine().getStatusCode(), responseHeaders));
    }
}
