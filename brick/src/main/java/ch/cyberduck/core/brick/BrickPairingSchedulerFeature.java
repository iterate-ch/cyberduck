package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.threading.ScheduledThreadPool;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class BrickPairingSchedulerFeature {
    private static final Logger log = LogManager.getLogger(BrickPairingSchedulerFeature.class);

    private final BrickSession session;
    private final String token;
    private final Host host;
    private final CancelCallback cancel;
    private final ScheduledThreadPool scheduler = new ScheduledThreadPool();

    public BrickPairingSchedulerFeature(final BrickSession session, final String token, final Host host, final CancelCallback cancel) {
        this.session = session;
        this.token = token;
        this.host = host;
        this.cancel = cancel;
    }

    public void repeat(final PasswordCallback callback) {
        final long timeout = new HostPreferences(session.getHost()).getLong("brick.pairing.interrupt.ms");
        final long start = System.currentTimeMillis();
        scheduler.repeat(() -> {
            try {
                if(System.currentTimeMillis() - start > timeout) {
                    throw new ConnectionCanceledException(String.format("Interrupt polling for pairing key after %d", timeout));
                }
                this.operate(callback);
            }
            catch(ConnectionCanceledException e) {
                log.warn(String.format("Cancel processing scheduled task. %s", e.getMessage()));
                callback.close(null);
                this.shutdown();
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure processing scheduled task. %s", e.getMessage()));
                callback.close(null);
                this.shutdown();
            }
        }, new HostPreferences(session.getHost()).getLong("brick.pairing.interval.ms"), TimeUnit.MILLISECONDS);
    }

    /**
     * Pool for pairing key from service
     *
     * @param callback Callback when service returns 200
     */
    private void operate(final PasswordCallback callback) throws BackgroundException {
        try {
            final HttpPost resource = new HttpPost(String.format("%s/api/rest/v1/sessions/pairing_key/%s",
                new HostUrlProvider().withUsername(false).withPath(false).get(session.getHost()), token));
            resource.setHeader(HttpHeaders.ACCEPT, "application/json");
            resource.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            log.info("Fetch credentials for paring key {} from {}", token, resource);
            final JsonObject json = session.getClient().execute(resource, new AbstractResponseHandler<JsonObject>() {
                @Override
                public JsonObject handleEntity(final HttpEntity entity) throws IOException {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    IOUtils.copy(entity.getContent(), out);
                    return JsonParser.parseReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()))).getAsJsonObject();
                }
            });
            if(json.has("nickname")) {
                if(new HostPreferences(session.getHost()).getBoolean("brick.pairing.nickname.configure")) {
                    final JsonPrimitive nickname = json.getAsJsonPrimitive("nickname");
                    if(StringUtils.isNotBlank(host.getNickname())) {
                        if(!StringUtils.equals(host.getNickname(), nickname.getAsString())) {
                            log.warn("Mismatch of nickname. Previously authorized as {} and now paired as {}", host.getNickname(), nickname.getAsString());
                            callback.close(null);
                            throw new LoginCanceledException();
                        }
                    }
                    host.setNickname(nickname.getAsString());
                }
            }
            final Credentials credentials = host.getCredentials();
            if(json.has("user_username")) {
                credentials.setUsername(json.getAsJsonPrimitive("user_username").getAsString());
            }
            else {
                throw new LoginFailureException(String.format("Invalid response for pairing key %s", token));
            }
            if(json.has("password")) {
                credentials.setPassword(json.getAsJsonPrimitive("password").getAsString());
            }
            else {
                throw new LoginFailureException(String.format("Invalid response for pairing key %s", token));
            }
            if(json.has("server")) {
                if(new HostPreferences(session.getHost()).getBoolean("brick.pairing.hostname.configure")) {
                    final String server = json.getAsJsonPrimitive("server").getAsString();
                    try {
                        host.setHostname(new URI(server).getHost());
                    }
                    catch(URISyntaxException e) {
                        log.warn("Failure{} to parse server value {} as URI", e, server);
                    }
                }
            }
            callback.close(credentials.getUsername());
        }
        catch(JsonParseException e) {
            throw new DefaultIOExceptionMappingService().map(new IOException(e.getMessage(), e));
        }
        catch(HttpResponseException e) {
            switch(e.getStatusCode()) {
                case HttpStatus.SC_NOT_FOUND:
                    log.warn("Missing login for pairing key {}", token);
                    cancel.verify();
                    break;
                default:
                    throw new DefaultHttpResponseExceptionMappingService().map(e);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
