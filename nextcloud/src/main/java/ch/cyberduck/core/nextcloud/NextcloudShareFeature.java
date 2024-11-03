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
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.ocs.OcsDownloadShareResponseHandler;
import ch.cyberduck.core.ocs.OcsShareeResponseHandler;
import ch.cyberduck.core.ocs.OcsUploadShareResponseHandler;
import ch.cyberduck.core.ocs.model.Share;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sardine.impl.handler.VoidResponseHandler;

public class NextcloudShareFeature implements ch.cyberduck.core.features.Share {
    private static final Logger log = LogManager.getLogger(NextcloudShareFeature.class);

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
                final StringBuilder request = new StringBuilder(String.format("https://%s%s/apps/files_sharing/api/v1/sharees?lookup=true&shareType=%d&itemType=file",
                        bookmark.getHostname(), new NextcloudHomeFeature(bookmark).find(NextcloudHomeFeature.Context.ocs).getAbsolute(),
                        SHARE_TYPE_USER // User
                ));
                final HttpGet resource = new HttpGet(request.toString());
                resource.setHeader("OCS-APIRequest", "true");
                resource.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
                final Set<Sharee> sharees = new LinkedHashSet<>(
                        Collections.singletonList(Sharee.world)
                );
                try {
                    sharees.addAll(session.getClient().execute(resource, new OcsShareeResponseHandler()));
                }
                catch(HttpResponseException e) {
                    log.warn("Failure {} retrieving sharees", e.getMessage());
                    return Collections.emptySet();
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
        final StringBuilder request = new StringBuilder(String.format("https://%s%s/apps/files_sharing/api/v1/shares?path=%s&shareType=%d&shareWith=%s",
                bookmark.getHostname(), new NextcloudHomeFeature(bookmark).find(NextcloudHomeFeature.Context.ocs).getAbsolute(),
                URIEncoder.encode(PathRelativizer.relativize(NextcloudHomeFeature.Context.files.home(bookmark).find().getAbsolute(), file.getAbsolute())),
                Sharee.world.equals(sharee) ? SHARE_TYPE_PUBLIC_LINK : SHARE_TYPE_USER,
                Sharee.world.equals(sharee) ? StringUtils.EMPTY : sharee.getIdentifier()
        ));
        final Credentials password = callback.prompt(bookmark,
                LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                new LoginOptions().anonymous(true).keychain(false).icon(bookmark.getProtocol().disk()));
        if(password.isPasswordAuthentication()) {
            request.append(String.format("&password=%s", URIEncoder.encode(password.getPassword())));
        }
        final HttpPost resource = new HttpPost(request.toString());
        resource.setHeader("OCS-APIRequest", "true");
        resource.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
        try {
            return session.getClient().execute(resource, new OcsDownloadShareResponseHandler());
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
        final StringBuilder request = new StringBuilder(String.format("https://%s%s/apps/files_sharing/api/v1/shares?path=%s&shareType=%d&permissions=%d",
                bookmark.getHostname(), new NextcloudHomeFeature(bookmark).find(NextcloudHomeFeature.Context.ocs).getAbsolute(),
                URIEncoder.encode(PathRelativizer.relativize(NextcloudHomeFeature.Context.files.home(bookmark).find().getAbsolute(), file.getAbsolute())),
                Sharee.world.equals(sharee) ? SHARE_TYPE_PUBLIC_LINK : SHARE_TYPE_USER,
                SHARE_PERMISSIONS_CREATE
        ));
        final Credentials password = callback.prompt(bookmark,
                LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                new LoginOptions().anonymous(true).keychain(false).icon(bookmark.getProtocol().disk()));
        if(password.isPasswordAuthentication()) {
            request.append(String.format("&password=%s", URIEncoder.encode(password.getPassword())));
        }
        final HttpPost resource = new HttpPost(request.toString());
        resource.setHeader("OCS-APIRequest", "true");
        resource.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
        try {
            return session.getClient().execute(resource, new OcsUploadShareResponseHandler() {
                @Override
                public DescriptiveUrl handleEntity(final HttpEntity entity) throws IOException {
                    final XmlMapper mapper = new XmlMapper();
                    final Share value = mapper.readValue(entity.getContent(), Share.class);
                    // Additional request, because permissions are ignored in POST
                    final StringBuilder request = new StringBuilder(String.format("https://%s/ocs/v1.php/apps/files_sharing/api/v1/shares/%s?permissions=%d",
                            bookmark.getHostname(),
                            value.data.id,
                            SHARE_PERMISSIONS_CREATE
                    ));
                    final HttpPut put = new HttpPut(request.toString());
                    put.setHeader("OCS-APIRequest", "true");
                    put.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType());
                    session.getClient().execute(put, new VoidResponseHandler());
                    return super.handleEntity(entity);
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

}
