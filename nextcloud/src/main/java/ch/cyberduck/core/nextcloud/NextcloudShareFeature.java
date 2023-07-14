package ch.cyberduck.core.nextcloud;

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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.AbstractResponseHandler;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sardine.impl.handler.VoidResponseHandler;

public class NextcloudShareFeature implements Share {

    private final DAVSession session;

    /**
     * Public link
     */
    private static final int SHARE_TYPE_PUBLIC_LINK = 3;
    private static final int SHARE_TYPE_USER = 0;
    private static final int SHARE_TYPE_EMAIL = 4;

    /**
     * File drop only
     */
    private static final int SHARE_PERMISSIONS_CREATE = 4;

    public NextcloudShareFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case upload:
                return file.isDirectory();
        }
        return true;
    }

    @Override
    public Set<Sharee> getSharees(final Type type) throws BackgroundException {
        switch(type) {
            case upload:
                return Collections.singleton(Sharee.world);
            default:
                final Host bookmark = session.getHost();
                final StringBuilder request = new StringBuilder(String.format("https://%s/ocs/v2.php/apps/files_sharing/api/v1/sharees?lookup=true&shareType=%d&itemType=file",
                        bookmark.getHostname(),
                        SHARE_TYPE_USER // User
                ));
                final HttpGet resource = new HttpGet(request.toString());
                resource.setHeader("OCS-APIRequest", "true");
                resource.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
                final Set<Sharee> sharees = new LinkedHashSet<>(
                        Collections.singletonList(Sharee.world)
                );
                try {
                    sharees.addAll(session.getClient().execute(resource, new AbstractResponseHandler<List<Sharee>>() {
                                @Override
                                public List<Sharee> handleResponse(final HttpResponse response) throws IOException {
                                    final StatusLine statusLine = response.getStatusLine();
                                    final HttpEntity entity = response.getEntity();
                                    if(statusLine.getStatusCode() >= 300) {
                                        final StringAppender message = new StringAppender();
                                        message.append(statusLine.getReasonPhrase());
                                        final ocs error = new XmlMapper().readValue(entity.getContent(), ocs.class);
                                        message.append(error.meta.message);
                                        throw new HttpResponseException(statusLine.getStatusCode(), message.toString());
                                    }
                                    return super.handleResponse(response);
                                }

                                @Override
                                public List<Sharee> handleEntity(final HttpEntity entity) throws IOException {
                                    final XmlMapper mapper = new XmlMapper();
                                    final ocs value = mapper.readValue(entity.getContent(), ocs.class);
                                    if(value.data != null) {
                                        if(value.data.users != null) {
                                            final List<Sharee> sharees = new ArrayList<>();
                                            for(ocs.user user : value.data.users) {
                                                final String id = user.value.shareWith;
                                                final String label = String.format("%s (%s)", user.label, user.shareWithDisplayNameUnique);
                                                sharees.add(new Sharee(id, label));
                                            }
                                            return sharees;
                                        }
                                    }
                                    return Collections.emptyList();
                                }
                            }
                    ));
                }
                catch(HttpResponseException e) {
                    throw new DefaultHttpResponseExceptionMappingService().map(e);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
                return sharees;
        }
    }

    /**
     * int) 0 = user; 1 = group; 3 = public link; 6 = federated cloud share
     */
    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Object options, final PasswordCallback callback) throws BackgroundException {
        final Host bookmark = session.getHost();
        final StringBuilder request = new StringBuilder(String.format("https://%s/ocs/v2.php/apps/files_sharing/api/v1/shares?path=%s&shareType=%d&shareWith=%s",
                bookmark.getHostname(),
                URIEncoder.encode(PathRelativizer.relativize(new NextcloudHomeFeature(bookmark).find().getAbsolute(), file.getAbsolute())),
                Sharee.world == sharee ? SHARE_TYPE_PUBLIC_LINK : SHARE_TYPE_USER,
                Sharee.world == sharee ? StringUtils.EMPTY : sharee.getIdentifier()
        ));
        final Credentials password = callback.prompt(bookmark,
                LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                new LoginOptions().anonymous(true).keychain(false).icon(bookmark.getProtocol().disk()));
        if(password.isPasswordAuthentication()) {
            request.append(String.format("&password=%s", password.getPassword()));
        }
        final HttpPost resource = new HttpPost(request.toString());
        resource.setHeader("OCS-APIRequest", "true");
        resource.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
        try {
            return session.getClient().execute(resource, new AbstractResponseHandler<DescriptiveUrl>() {
                @Override
                public DescriptiveUrl handleResponse(final HttpResponse response) throws IOException {
                    final StatusLine statusLine = response.getStatusLine();
                    final HttpEntity entity = response.getEntity();
                    if(statusLine.getStatusCode() >= 300) {
                        final StringAppender message = new StringAppender();
                        message.append(statusLine.getReasonPhrase());
                        final ocs error = new XmlMapper().readValue(entity.getContent(), ocs.class);
                        message.append(error.meta.message);
                        throw new HttpResponseException(statusLine.getStatusCode(), message.toString());
                    }
                    return super.handleResponse(response);
                }

                @Override
                public DescriptiveUrl handleEntity(final HttpEntity entity) throws IOException {
                    final XmlMapper mapper = new XmlMapper();
                    final ocs value = mapper.readValue(entity.getContent(), ocs.class);
                    if(null != value.data) {
                        if(null != value.data.url) {
                            return new DescriptiveUrl(URI.create(value.data.url), DescriptiveUrl.Type.http);
                        }
                    }
                    return DescriptiveUrl.EMPTY;
                }
            });
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }


    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Object options, final PasswordCallback callback) throws BackgroundException {
        final Host bookmark = session.getHost();
        final StringBuilder request = new StringBuilder(String.format("https://%s/ocs/v2.php/apps/files_sharing/api/v1/shares",
                bookmark.getHostname()
        ));
        final Credentials password = callback.prompt(bookmark,
                LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                new LoginOptions().anonymous(true).keychain(false).icon(bookmark.getProtocol().disk()));
        if(password.isPasswordAuthentication()) {
            request.append(String.format("?password=%s", password.getPassword()));
        }
        final HttpPost resource = new HttpPost(request.toString());
        resource.setEntity(EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON).setText(String.format("{\"path\":\"%s\",\"shareType\":%d,\"permissions\":%d}",
                URIEncoder.encode(PathRelativizer.relativize(new NextcloudHomeFeature(bookmark).find().getAbsolute(), file.getAbsolute())),
                Sharee.world == sharee ? SHARE_TYPE_PUBLIC_LINK : SHARE_TYPE_USER,
                SHARE_PERMISSIONS_CREATE // Create
        )).build());
        resource.setHeader("OCS-APIRequest", "true");
        resource.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
        try {
            return session.getClient().execute(resource, new AbstractResponseHandler<DescriptiveUrl>() {
                @Override
                public DescriptiveUrl handleResponse(final HttpResponse response) throws IOException {
                    final StatusLine statusLine = response.getStatusLine();
                    final HttpEntity entity = response.getEntity();
                    if(statusLine.getStatusCode() >= 300) {
                        final StringAppender message = new StringAppender();
                        message.append(statusLine.getReasonPhrase());
                        final ocs error = new XmlMapper().readValue(entity.getContent(), ocs.class);
                        message.append(error.meta.message);
                        throw new HttpResponseException(statusLine.getStatusCode(), message.toString());
                    }
                    return super.handleResponse(response);
                }

                @Override
                public DescriptiveUrl handleEntity(final HttpEntity entity) throws IOException {
                    final XmlMapper mapper = new XmlMapper();
                    final ocs value = mapper.readValue(entity.getContent(), ocs.class);
                    // Additional request, because permissions are ignored in POST
                    final HttpPut put = new HttpPut(String.format("https://%s/ocs/v2.php/apps/files_sharing/api/v1/shares/%d",
                            bookmark.getHostname(),
                            value.data.id
                    ));
                    put.setEntity(EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON).setText(String.format(
                            "{\"permissions\":\"%d\"}", SHARE_PERMISSIONS_CREATE)).build());
                    put.setHeader("OCS-APIRequest", "true");
                    put.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
                    session.getClient().execute(put, new VoidResponseHandler());
                    if(null != value.data) {
                        if(null != value.data.url) {
                            return new DescriptiveUrl(URI.create(value.data.url), DescriptiveUrl.Type.http);
                        }
                    }
                    return DescriptiveUrl.EMPTY;
                }
            });
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    /*
    <ocs>
     <meta>
      <status>ok</status>
      <statuscode>200</statuscode>
      <message>OK</message>
     </meta>
     <data>
      <id>36</id>
      <share_type>3</share_type>
      <uid_owner>dkocher</uid_owner>
      <displayname_owner>David Kocher</displayname_owner>
      <permissions>1</permissions>
      <stime>1559218292</stime>
      <parent/>
      <expiration/>
      <token>79NKo6JxmsxxGBb</token>
      <uid_file_owner>dkocher</uid_file_owner>
      <note></note>
      <label></label>
      <displayname_file_owner>David Kocher</displayname_file_owner>
      <path>/sandbox/example.png</path>
      <item_type>file</item_type>
      <mimetype>image/png</mimetype>
      <storage_id>home::dkocher</storage_id>
      <storage>3</storage>
      <item_source>36285</item_source>
      <file_source>36285</file_source>
      <file_parent>36275</file_parent>
      <file_target>/Monte Panarotta.png</file_target>
      <share_with/>
      <share_with_displayname/>
      <password/>
      <send_password_by_talk></send_password_by_talk>
      <url>https://example.net/s/67hgsdfjkds67</url>
      <mail_send>1</mail_send>
      <hide_download>0</hide_download>
     </data>
    </ocs>
    */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class ocs {
        public meta meta;
        public data data;

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class meta {
            public String status;
            public String statuscode;
            public String message;
            public int itemsperpage;
            public int totalitems;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class data {
            public int id;
            public String url;
            public user[] users;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class users {
            public user[] element;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class user {
            public String label;
            public String icon;
            public String shareWithDisplayNameUnique;
            public value value;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class value {
            public int shareType;
            public String shareWith;
            public String shareWithAdditionalInfo;
        }
    }
}
