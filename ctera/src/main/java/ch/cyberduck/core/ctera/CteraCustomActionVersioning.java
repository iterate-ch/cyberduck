package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.ctera.auth.CteraTokens;
import ch.cyberduck.core.ctera.model.Attachment;
import ch.cyberduck.core.ctera.model.Device;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class CteraCustomActionVersioning {
    private static final Logger log = LogManager.getLogger(CteraCustomActionVersioning.class);

    private final CteraSession session;
    private final Path file;

    public CteraCustomActionVersioning(final CteraSession session, final Path file) {
        this.session = session;
        this.file = file;
    }

    public void run() throws BackgroundException {
        final String token = this.getSessionToken();
        final Local html = this.writeTemporaryHtml(token);
        BrowserLauncherFactory.get().open(html.toURL());
    }

    private Local writeTemporaryHtml(final String token) throws BackgroundException {
        try {
            final String content =
                    String.format("<!doctype html>\n" +
                                    "<html> <head> <script>\n" +
                                    "function _onload(){document.getElementById(\"sForm\").submit();}\n" +
                                    "</script> </head> <body onload = \"_onload()\">\n" +
                                    "<form id=\"sForm\" method=\"POST\" action=\"%s/ServicesPortal/sso\">\n" +
                                    "<input type=\"hidden\" name=\"targeturi\" value=\"?GUI_openFmFolder=%s&GUI_fmVersions=true\"/>\n" +
                                    "<input type=\"hidden\" name=\"ctera_ticket\" value=\"%s\"/>\n" +
                                    "</form> </body> </html>",
                            new HostUrlProvider().withUsername(false).get(session.getHost()),
                            URLEncoder.encode(String.format("/%s",
                                    PathRelativizer.relativize(session.getHost().getProtocol().getDefaultPath(), file.getAbsolute())), StandardCharsets.UTF_8.name()),
                            token);
            final Local file = TemporaryFileServiceFactory.get().create(String.format("%s.html", new AlphanumericRandomStringService().random()));
            try (final OutputStream out = file.getOutputStream(false)) {
                IOUtils.write(content, out, StandardCharsets.UTF_8);
            }
            return file;
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Error writing temporary file", e);
        }
    }

    protected String getSessionToken() throws BackgroundException {
        try {
            final String device = this.getDeviceName();
            if(log.isDebugEnabled()) {
                log.debug("Using device {} to request a session token", device);
            }
            final HttpPost post = new HttpPost(String.format("/ServicesPortal/api/devices/%s?format=jsonext", device));
            post.setEntity(new StringEntity(getSessionTokenPayloadAsString(), ContentType.APPLICATION_JSON));
            return session.getClient().execute(post, response -> StringUtils.remove(EntityUtils.toString(response.getEntity()), "\""));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(String.format("Unable to redirect to the portal for %s", file), e);
        }
    }

    private String getDeviceName() throws BackgroundException {
        final String token = this.getCteraTokens();
        if(StringUtils.isBlank(token)) {
            throw new InteroperabilityException("No token found");
        }
        try {
            final CteraTokens tokens = CteraTokens.parse(token);
            final String id = tokens.getDeviceId();
            final HttpGet request = new HttpGet("/ServicesPortal/api/devices?format=jsonext");
            final Device[] devices = session.getClient().execute(request, new AbstractResponseHandler<Device[]>() {
                @Override
                public Device[] handleEntity(final HttpEntity entity) throws IOException {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(entity.getContent(), Device[].class);
                }
            });
            for(Device device : devices) {
                if(device.uid.equals(id)) {
                    return device.name;
                }
            }
            throw new InteroperabilityException(String.format("No device for token %s found", token));
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    protected String getCteraTokens() {
        final HostPasswordStore keychain = PasswordStoreFactory.get();
        return keychain.findLoginToken(session.getHost());
    }

    private static String getSessionTokenPayloadAsString() throws JsonProcessingException {
        final Attachment attachment = new Attachment();
        final ArrayList<Attachment.Attribute> attributes = new ArrayList<>();
        final Attachment.Attribute type = new Attachment.Attribute();
        type.setId("type");
        type.setVal("user-defined");
        attributes.add(type);
        final Attachment.Attribute name = new Attachment.Attribute();
        name.setId("name");
        name.setVal("getSessionToken");
        attributes.add(name);
        attachment.setAttributes(attributes);

        final XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writeValueAsString(attachment);
    }
}
