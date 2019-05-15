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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Collections;
import java.util.EnumSet;

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
    public DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt) throws BackgroundException {
        String token = host.getCredentials().getToken();
        if(StringUtils.isEmpty(token)) {
            host.getCredentials().setToken(new UUIDRandomStringService().random());
        }
        return super.connect(proxy, key, prompt);
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            do {
                final String token = new BrickCredentialsConfigurator().configure(host).getToken();
                // Query status
                final Host resource = new HostParser(new ProtocolFactory(Collections.singleton(new DAVSSLProtocol() {
                    @Override
                    public boolean isEnabled() {
                        return true;
                    }
                }))).get(String.format("https://app.files.com/api/rest/v1/sessions/pairing_key/%s", token));
                if(log.isInfoEnabled()) {
                    log.info(String.format("Fetch credentials for paring key %s from %s", token, resource));
                }
                final DAVSession connection = new DAVSession(resource, trust, key);
                connection.open(proxy, new DisabledHostKeyCallback(), new DisabledLoginCallback());
                final Read read = connection.getFeature(Read.class);
                final TransferStatus options = new TransferStatus();
                try {
                    final InputStream in = read.read(new Path(resource.getDefaultPath(), EnumSet.of(AbstractPath.Type.file)), options, new DisabledConnectionCallback());
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    final TransferStatus status = new TransferStatus().length(-1L);
                    new StreamCopier(status, status).transfer(in, out);
                    connection.close();
                    final JsonParser parser = new JsonParser();
                    final JsonObject json = parser.parse(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()))).getAsJsonObject();
                    if(json.has("username")) {
                        host.getCredentials().setUsername(json.getAsJsonPrimitive("username").getAsString());
                    }
                    else {
                        throw new LoginFailureException(String.format("Invalid response for pairing key %s", token));
                    }
                    if(json.has("password")) {
                        host.getCredentials().setPassword(json.getAsJsonPrimitive("password").getAsString());
                    }
                    else {
                        throw new LoginFailureException(String.format("Invalid response for pairing key %s", token));
                    }
                    break;
                }
                catch(NotfoundException e) {
                    log.warn(String.format("Missing login for pairing key %s. %s", token, e.getDetail(false)));
                    if(!BrowserLauncherFactory.get().open(
                        String.format("https://app.files.com/Login_For_Desktop?Pairing_Key=%s&Platform=%s&Computer=%s", token,
                            new PreferencesUseragentProvider().get(), InetAddress.getLocalHost().getHostName())
                    )) {
                        throw new LoginCanceledException();
                    }
                    cancel.verify();
                }
            }
            while(true);
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
        if(type == Copy.class) {
            return (T) new BrickCopyFeature(this);
        }
        if(type == Write.class) {
            return (T) new BrickWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DAVUploadFeature(new BrickWriteFeature(this));
        }
        return super._getFeature(type);
    }
}
