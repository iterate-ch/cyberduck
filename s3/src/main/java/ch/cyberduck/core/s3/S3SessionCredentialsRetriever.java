package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.dav.DAVReadFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

public class S3SessionCredentialsRetriever {

    private final TranscriptListener transcript;

    private final ProtocolFactory factory;

    private final String url;

    public S3SessionCredentialsRetriever(final TranscriptListener transcript, final String url) {
        this(ProtocolFactory.global, transcript, url);
    }

    public S3SessionCredentialsRetriever(final ProtocolFactory factory, final TranscriptListener transcript, final String url) {
        this.factory = factory;
        this.transcript = transcript;
        this.url = url;
    }

    public AWSCredentials get() throws BackgroundException {
        final Host address = new HostParser(factory).get(url);
        final Path access = new Path(address.getDefaultPath(), EnumSet.of(Path.Type.file));
        address.setDefaultPath(String.valueOf(Path.DELIMITER));
        final DAVSession connection = new DAVSession(address);
        connection.open(new DisabledHostKeyCallback(), transcript);
        final InputStream in = new DAVReadFeature(connection).read(access, new TransferStatus());
        return this.parse(in);
    }

    protected AWSCredentials parse(final InputStream in) throws BackgroundException {
        try {
            final JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
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
            return new AWSSessionCredentials(key, secret, token);
        }
        catch(UnsupportedEncodingException e) {
            throw new BackgroundException(e);
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