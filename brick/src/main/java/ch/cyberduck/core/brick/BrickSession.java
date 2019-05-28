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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.date.RemainingPeriodFormatter;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.BackgroundActionPauser;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.text.MessageFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class BrickSession extends DAVSession {
    private static final Logger log = Logger.getLogger(BrickSession.class);

    public BrickSession(final Host host) {
        super(host);
    }

    public BrickSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final Credentials credentials = host.getCredentials();
            if(!credentials.isPasswordAuthentication()) {
                final String token = new BrickCredentialsConfigurator().configure(host).getToken();
                if(!BrowserLauncherFactory.get().open(
                    String.format("https://app.files.com/login_from_desktop?pairing_key=%s&platform=%s&computer=%s", token,
                        URIEncoder.encode(new PreferencesUseragentProvider().get()), URIEncoder.encode(InetAddress.getLocalHost().getHostName()))
                )) {
                    throw new LoginCanceledException();
                }
                do {
                    // Query status
                    try {
                        final HttpPost resource = new HttpPost(String.format("https://app.files.com/api/rest/v1/sessions/pairing_key/%s", token));
                        resource.setHeader(HttpHeaders.ACCEPT, "application/json");
                        resource.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Fetch credentials for paring key %s from %s", token, resource));
                        }
                        final JsonObject json = client.execute(resource, new AbstractResponseHandler<JsonObject>() {
                            @Override
                            public JsonObject handleEntity(final HttpEntity entity) throws IOException {
                                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                                IOUtils.copy(entity.getContent(), out);
                                final JsonParser parser = new JsonParser();
                                return parser.parse(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()))).getAsJsonObject();
                            }
                        });
                        if(json.has("username")) {
                            credentials.setUsername(json.getAsJsonPrimitive("username").getAsString());
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
                        if(json.has("nickname")) {
                            if(PreferencesFactory.get().getBoolean("brick.pairing.nickname.configure")) {
                                host.setNickname(json.getAsJsonPrimitive("nickname").getAsString());
                            }
                        }
                        if(json.has("server")) {
                            if(PreferencesFactory.get().getBoolean("brick.pairing.hostname.configure")) {
                                host.setHostname(URI.create(json.getAsJsonPrimitive("server").getAsString()).getHost());
                            }
                        }
                        break;
                    }
                    catch(HttpResponseException e) {
                        switch(e.getStatusCode()) {
                            case HttpStatus.SC_NOT_FOUND:
                                log.warn(String.format("Missing login for pairing key %s", token));
                                final BackgroundActionPauser pause = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
                                    @Override
                                    public boolean isCanceled() {
                                        try {
                                            cancel.verify();
                                            return false;
                                        }
                                        catch(ConnectionCanceledException ex) {
                                            return true;
                                        }
                                    }

                                    @Override
                                    public void progress(final Integer seconds) {
                                        if(log.isInfoEnabled()) {
                                            log.info(MessageFormat.format(LocaleFactory.localizedString("Retry again in {0}", "Status"),
                                                new RemainingPeriodFormatter().format(seconds)));
                                        }
                                    }
                                }, 1);
                                pause.await();
                                cancel.verify();
                                break;
                            default:
                                throw new DefaultHttpResponseExceptionMappingService().map(e);
                        }
                    }
                }
                while(true);
            }
        }
        catch(JsonParseException e) {
            throw new DefaultIOExceptionMappingService().map(new IOException(e.getMessage(), e));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        super.login(proxy, prompt, cancel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Write.class) {
            return (T) new BrickWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DAVUploadFeature(new BrickWriteFeature(this));
        }
        if(type == UrlProvider.class) {
            return (T) new BrickUrlProvider(host);
        }
        return super._getFeature(type);
    }
}
