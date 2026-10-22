package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVLockFeature;
import ch.cyberduck.core.dav.DAVPathMapper;
import ch.cyberduck.core.dav.DAVQuotaFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.nextcloud.NextcloudDeleteFeature;
import ch.cyberduck.core.nextcloud.NextcloudListService;
import ch.cyberduck.core.nextcloud.NextcloudShareFeature;
import ch.cyberduck.core.nextcloud.NextcloudUrlProvider;
import ch.cyberduck.core.nextcloud.NextcloudWriteFeature;
import ch.cyberduck.core.ocs.OcsCapabilities;
import ch.cyberduck.core.ocs.OcsCapabilitiesRequest;
import ch.cyberduck.core.ocs.OcsCapabilitiesResponseHandler;
import ch.cyberduck.core.ocs.OcsUserRequest;
import ch.cyberduck.core.ocs.OcsUserResponseHandler;
import ch.cyberduck.core.owncloud.model.Drives;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.tus.TusCapabilities;
import ch.cyberduck.core.tus.TusCapabilitiesRequest;
import ch.cyberduck.core.tus.TusCapabilitiesResponseHandler;
import ch.cyberduck.core.tus.TusWriteFeature;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static ch.cyberduck.core.tus.TusCapabilities.TUS_VERSION;

public class OwncloudSession extends DAVSession {
    private static final Logger log = LogManager.getLogger(OwncloudSession.class);

    protected final TusCapabilities tus = new TusCapabilities();
    protected final OcsCapabilities ocs = new OcsCapabilities();
    protected final OwncloudSpaces spaces = new OwncloudSpaces();

    /**
     * Show storage spaces in virtual folders
     */
    private boolean virtual;

    public OwncloudSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected DAVClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final DAVClient client = super.connect(proxy, key, prompt, cancel);
        try {
            final TusCapabilitiesRequest request = new TusCapabilitiesRequest(host, new OwncloudHomeFeature(host).find(OwncloudHomeFeature.Context.tus));
            log.debug("Query Tus capabilities at {}", request);
            client.execute(request, new TusCapabilitiesResponseHandler(tus));
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return client;
    }

    @Override
    protected void authorized() throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        try {
            final OcsUserRequest request = new OcsUserRequest(host);
            log.debug("Query OCS user at {}", request);
            final String id = client.execute(request, new OcsUserResponseHandler());
            if(StringUtils.isNotBlank(id) && !StringUtils.equals(credentials.getUsername(), id)) {
                log.debug("Set username to {} from server", id);
                credentials.setUsername(id);
            }
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        // Session is reused when reconnecting. Verify login with default home and discover spaces again
        virtual = false;
        super.login(prompt, cancel);
        try {
            client.execute(new OcsCapabilitiesRequest(host), new OcsCapabilitiesResponseHandler(ocs));
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        if(ocs.spaces && preferences.getBoolean("owncloud.spaces.enable")) {
            try {
                this.drives();
                virtual = true;
            }
            catch(BackgroundException e) {
                log.warn("Failure {} retrieving spaces. Disable virtual folders", e.getMessage());
            }
        }
    }

    /**
     * Retrieve spaces of user and update mapping to WebDAV root
     *
     * @return Project spaces
     */
    public List<Drives.Drive> drives() throws BackgroundException {
        try {
            final OwncloudDrivesRequest request = new OwncloudDrivesRequest(host);
            log.debug("Query spaces at {}", request);
            return spaces.withDrives(client.execute(request, new OwncloudDrivesResponseHandler()));
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public DAVPathMapper getMapper() {
        if(virtual) {
            return spaces;
        }
        return super.getMapper();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(virtual) {
            if(type == Home.class) {
                return (T) new OwncloudSpacesHomeFeature(host, spaces);
            }
            if(type == ListService.class) {
                return (T) new OwncloudSpacesListService(this, new NextcloudListService(this));
            }
            if(type == AttributesFinder.class) {
                return (T) new OwncloudSpacesAttributesFinderFeature(this);
            }
            if(type == Find.class) {
                return (T) new OwncloudSpacesFindFeature(this);
            }
            if(type == Directory.class) {
                return (T) new OwncloudSpacesDirectoryFeature(this);
            }
            if(type == Touch.class) {
                return (T) new OwncloudSpacesTouchFeature(this);
            }
            if(type == Delete.class) {
                return (T) new OwncloudSpacesDeleteFeature(this);
            }
            if(type == Move.class) {
                return (T) new OwncloudSpacesMoveFeature(this);
            }
            if(type == Copy.class) {
                return (T) new OwncloudSpacesCopyFeature(this);
            }
            if(type == Share.class) {
                return (T) new OwncloudSpacesShareFeature(this);
            }
            if(type == Write.class) {
                return (T) new OwncloudSpacesWriteFeature<>(this, this.write());
            }
            if(type == Quota.class) {
                if(null == spaces.getPersonal()) {
                    return (T) (Quota) () -> Quota.unknown;
                }
                // Home is root with virtual folders
                return (T) new DAVQuotaFeature(this, () -> OwncloudSpaces.PERSONAL_FOLDER);
            }
        }
        if(type == Home.class) {
            return (T) new DelegatingHomeFeature(new WorkdirHomeFeature(host), new OwncloudHomeFeature(host));
        }
        if(type == ListService.class) {
            return (T) new NextcloudListService(this);
        }
        if(type == Touch.class) {
            return (T) new DAVTouchFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new OwncloudAttributesFinderFeature(this);
        }
        if(type == Lock.class) {
            if(!ocs.locking) {
                return null;
            }
            return (T) new DAVLockFeature(this);
        }
        if(type == Upload.class) {
            if(ArrayUtils.contains(tus.versions, TUS_VERSION) && tus.extensions.contains(TusCapabilities.Extension.creation)) {
                return (T) new OcisUploadFeature(this, tus);
            }
            return (T) new HttpUploadFeature<>();
        }
        if(type == Write.class) {
            return (T) this.write();
        }
        if(type == UrlProvider.class) {
            return (T) new NextcloudUrlProvider(this);
        }
        if(type == Share.class) {
            return (T) new NextcloudShareFeature(this);
        }
        if(type == Versioning.class) {
            if(!ocs.versioning) {
                return null;
            }
            return (T) new OwncloudVersioningFeature(this);
        }
        if(type == Delete.class) {
            return (T) new NextcloudDeleteFeature(this);
        }
        if(type == Read.class) {
            return (T) new OwncloudReadFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new OwncloudTimestampFeature(this);
        }
        return super._getFeature(type);
    }

    private Write<Void> write() {
        if(ArrayUtils.contains(tus.versions, TUS_VERSION) && tus.extensions.contains(TusCapabilities.Extension.creation)) {
            return new TusWriteFeature(this, tus);
        }
        return new NextcloudWriteFeature(this);
    }
}
