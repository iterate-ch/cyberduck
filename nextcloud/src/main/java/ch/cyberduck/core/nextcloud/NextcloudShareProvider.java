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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractResponseHandler;

import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class NextcloudShareProvider implements PromptUrlProvider {

    private final NextcloudSession session;

    public NextcloudShareProvider(final NextcloudSession session) {
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

    /**
     * int) 0 = user; 1 = group; 3 = public link; 6 = federated cloud share
     */
    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Object options, final PasswordCallback callback) throws BackgroundException {
        final HttpPost resource = new HttpPost(String.format("https://%s/ocs/v2.php/apps/files_sharing/api/v1/shares?path=%s&shareType=%d",
            session.getHost().getHostname(),
            URIEncoder.encode(StringUtils.substringAfter(file.getAbsolute(), session.getHost().getDefaultPath())),
            3 // Public link
        ));
        resource.setHeader("OCS-APIRequest", "true");
        resource.setHeader(HttpHeaders.ACCEPT, "application/xml");
        try {
            return new DescriptiveUrl(session.getClient().execute(resource, new AbstractResponseHandler<URI>() {
                @Override
                public URI handleEntity(final HttpEntity entity) throws IOException {
                    final XmlMapper mapper = new XmlMapper();
                    ocs value = mapper.readValue(entity.getContent(), ocs.class);
                    return URI.create(value.data.url);
                }
            }), DescriptiveUrl.Type.http);
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }


    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Object options, final PasswordCallback callback) throws BackgroundException {
        final HttpPost resource = new HttpPost(String.format("https://%s/ocs/v2.php/apps/files_sharing/api/v1/shares?path=%s&shareType=%d&publicUpload=true",
            session.getHost().getHostname(),
            URIEncoder.encode(StringUtils.substringAfter(file.getAbsolute(), session.getHost().getDefaultPath())),
            3 // Public link
        ));
        resource.setHeader("OCS-APIRequest", "true");
        resource.setHeader(HttpHeaders.ACCEPT, "application/xml");
        try {
            return new DescriptiveUrl(session.getClient().execute(resource, new AbstractResponseHandler<URI>() {
                @Override
                public URI handleEntity(final HttpEntity entity) throws IOException {
                    final XmlMapper mapper = new XmlMapper();
                    ocs value = mapper.readValue(entity.getContent(), ocs.class);
                    return URI.create(value.data.url);
                }
            }), DescriptiveUrl.Type.http);
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
        public data data;

        @JsonIgnoreProperties(ignoreUnknown = true)

        private static final class data {
            public int id;
            public String url;
        }
    }
}
