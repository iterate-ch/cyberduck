package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ctera.model.APICredentials;
import ch.cyberduck.core.ctera.model.PortalSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.ObjectMapper;

import static ch.cyberduck.core.ctera.CteraSession.CURRENT_SESSION_PATH;

public class CteraDirectIOInterceptor implements HttpRequestInterceptor {

    private static final Logger log = LogManager.getLogger(CteraDirectIOInterceptor.class);

    public static final String API_PATH = "/ServicesPortal/api?format=jsonext";

    private final ReentrantLock lock = new ReentrantLock();
    private final HostPasswordStore keychain = PasswordStoreFactory.get();
    private final Host bookmark;
    private final CteraSession session;

    public CteraDirectIOInterceptor(final CteraSession session) {
        this.bookmark = session.getHost();
        this.session = session;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext httpContext) throws HttpException, IOException {
        if(httpRequest.getRequestLine().getUri().startsWith("/directio/")) {
            try {
                httpRequest.addHeader("Authorization", "Bearer " + this.getToken());
            }
            catch(BackgroundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getToken() throws BackgroundException {
        final String key = keychain.getPassword(toServiceNameForAccessKey(bookmark), toAccountName(bookmark));
        if(StringUtils.isBlank(key)) {
            final HttpGet request = new HttpGet(CURRENT_SESSION_PATH);
            try {
                final String userId = session.getClient().execute(request, new AbstractResponseHandler<PortalSession>() {
                    @Override
                    public PortalSession handleEntity(final HttpEntity entity) throws IOException {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(entity.getContent(), PortalSession.class);
                    }
                }).getUserIdFromUserRef();
                final APICredentials apiKey = this.createAPIKey(userId);
                keychain.addPassword(toServiceNameForAccessKey(bookmark), toAccountName(bookmark), apiKey.accessKey);
                keychain.addPassword(toServiceNameForSecretKey(bookmark), toAccountName(bookmark), apiKey.secretKey);
                return apiKey.accessKey;
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e);
            }
        }
        return key;
    }

    private APICredentials createAPIKey(final String userId) throws BackgroundException {
        lock.lock();
        try {
            final HttpPost post = new HttpPost(API_PATH);
            try {
                post.setEntity(
                        new StringEntity(String.format("<obj><att id=\"type\"><val>user-defined</val></att><att id=\"name\"><val>createApiKey</val></att><att id=\"param\"><val>%s</val></att></obj>",
                                userId), ContentType.TEXT_XML
                        )
                );
                final APICredentials credentials = session.getClient().execute(post, new AbstractResponseHandler<APICredentials>() {
                    @Override
                    public APICredentials handleEntity(final HttpEntity entity) throws IOException {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(entity.getContent(), APICredentials.class);
                    }
                });
                return credentials;
            }
            catch(HttpResponseException e) {
                throw new DefaultHttpResponseExceptionMappingService().map(e);
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e);
            }
        }
        finally {
            lock.unlock();
        }
    }

    protected static String toAccountName(final Host bookmark) {
        return new DefaultUrlProvider(bookmark).toUrl(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)),
                EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl();
    }

    protected static String toServiceNameForAccessKey(final Host bookmark) {
        return String.format("API Access Key (%s)", bookmark.getCredentials().getUsername());
    }

    protected static String toServiceNameForSecretKey(final Host bookmark) {
        return String.format("API Secret Key (%s)", bookmark.getCredentials().getUsername());
    }
}
