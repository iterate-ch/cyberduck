package ch.cyberduck.core.auth;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.dav.DAVReadFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

public class AWSSessionCredentialsRetriever {
    private static final Logger log = LogManager.getLogger(AWSSessionCredentialsRetriever.class);

    private final TranscriptListener transcript;
    private final ProtocolFactory factory;
    private final String url;
    private final X509TrustManager trust;
    private final X509KeyManager key;

    public AWSSessionCredentialsRetriever(final X509TrustManager trust, final X509KeyManager key, final TranscriptListener transcript, final String url) {
        this(trust, key, ProtocolFactory.get(), transcript, url);
    }

    public AWSSessionCredentialsRetriever(final X509TrustManager trust, final X509KeyManager key, final ProtocolFactory factory, final TranscriptListener transcript, final String url) {
        this.trust = trust;
        this.key = key;
        this.factory = factory;
        this.transcript = transcript;
        this.url = url;
    }

    public static class Configurator implements CredentialsConfigurator {

        private final AWSSessionCredentialsRetriever retriever;
        private final String url;

        public Configurator(final X509TrustManager trust, final X509KeyManager key, final TranscriptListener transcript, final String url) {
            this.url = url;
            this.retriever = new AWSSessionCredentialsRetriever(trust, key, transcript, url);
        }

        @Override
        public Configurator reload() {
            return this;
        }

        @Override
        public Credentials configure(final Host host) {
            try {
                return retriever.get();
            }
            catch(BackgroundException e) {
                log.warn(String.format("Ignore failure %s retrieving credentials from %s", e, url));
                return host.getCredentials();
            }
        }
    }

    public Credentials get() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure credentials from %s", url));
        }
        final Host address = new HostParser(factory).get(url);
        final Path access = new Path(PathNormalizer.normalize(address.getDefaultPath()), EnumSet.of(Path.Type.file));
        address.setDefaultPath(String.valueOf(Path.DELIMITER));
        final DAVSession connection = new DAVSession(address, trust, key);
        connection.withListener(transcript).open(ProxyFactory.get().find(url), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final InputStream in = new DAVReadFeature(connection).read(access, new TransferStatus(), new DisabledConnectionCallback());
        try {
            final Credentials credentials = this.parse(in);
            connection.close();
            return credentials;
        }
        finally {
            connection.removeListener(transcript);
        }
    }

    protected Credentials parse(final InputStream in) throws BackgroundException {
        try {
            final JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            reader.beginObject();
            String key = null;
            String secret = null;
            String token = null;
            while(reader.hasNext()) {
                final String name = reader.nextName();
                final String value = reader.nextString();
                switch(name) {
                    case "AccessKeyId":
                        key = value;
                        break;
                    case "SecretAccessKey":
                        secret = value;
                        break;
                    case "Token":
                        token = value;
                        break;
                }
            }
            reader.endObject();
            final Credentials credentials = new Credentials(key, secret);
            if(StringUtils.isNotBlank(token)) {
                credentials.setToken(token);
            }
            return credentials;
        }
        catch(MalformedJsonException e) {
            throw new InteroperabilityException("Invalid JSON response", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public String getUrl() {
        return url;
    }
}
