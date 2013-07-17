package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.iterate.openstack.swift.AuthenticationResponse;
import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.Region;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class SwiftSession extends HttpSession<Client> {
    private static final Logger log = Logger.getLogger(SwiftSession.class);

    private Map<String, Region> regions
            = new HashMap<String, Region>();

    private Map<Path, Distribution> distributions
            = new HashMap<Path, Distribution>();

    private PathContainerService containerService = new PathContainerService();

    public SwiftSession(Host h) {
        super(h);
    }

    @Override
    public Client connect(final HostKeyController key) throws BackgroundException {
        return new Client(super.connect());
    }

    protected Region getRegion(final Path container) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Lookup region for container %s", container));
        }
        return this.getRegion(container.attributes().getRegion());
    }

    protected Region getRegion(final String location)
            throws ConnectionCanceledException {
        if(regions.containsKey(location)) {
            return regions.get(location);
        }
        log.warn(String.format("Unknown region %s in authentication context", location));
        if(regions.containsKey(null)) {
            final Region region = regions.get(null);
            log.info(String.format("Use default region %s", region));
            return region;
        }
        if(regions.isEmpty()) {
            throw new ConnectionCanceledException("No default region in authentication context");
        }
        final Region region = regions.values().iterator().next();
        log.warn(String.format("Fallback to first region found %s", region));
        return region;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        try {
            final AuthenticationResponse authentication = client.authenticate(
                    new SwiftAuthenticationService().getRequest(host, prompt));
            for(Region region : authentication.getRegions()) {
                regions.put(region.getRegionId(), region);
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        super.logout();
        regions.clear();
        distributions.clear();
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isUploadResumable() {
        return false;
    }

    @Override
    public boolean isRenameSupported(final Path file) {
        return !file.attributes().isVolume();
    }

    @Override
    public boolean isCreateFileSupported(final Path workdir) {
        // Creating files is only possible inside a container.
        return !workdir.isRoot();
    }

    /**
     * @return Publicy accessible URL of given object
     */
    @Override
    public String toHttpURL(final Path file) {
        if(distributions.containsKey(containerService.getContainer(file))) {
            return distributions.get(containerService.getContainer(file)).getURL(file);
        }
        return null;
    }

    @Override
    public Set<DescriptiveUrl> getURLs(final Path path) {
        // Storage URL is not accessible
        return this.getHttpURLs(path);
    }

    @Override
    public boolean exists(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                try {
                    return this.getClient().containerExists(this.getRegion(containerService.getContainer(file)),
                            file.getName());
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Cannot read file attributes", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, file);
                }
            }
            return super.exists(file);
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return new AttributedList<Path>(new SwiftContainerListService().list(this));
        }
        else {
            return new SwiftObjectListService(this).list(file);
        }
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(status.isResume()) {
                return this.getClient().getObject(this.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        status.getCurrent(), status.getLength());
            }
            return this.getClient().getObject(this.getRegion(containerService.getContainer(file)),
                    containerService.getContainer(file).getName(), containerService.getKey(file));
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Download failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public void download(final Path file, final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status) throws BackgroundException {
        OutputStream out = null;
        InputStream in = null;
        try {
            in = this.read(file, status);
            out = file.getLocal().getOutputStream(status.isResume());
            this.download(file, in, out, throttle, listener, status);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, file);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public void upload(final Path file, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) throws BackgroundException {
        try {
            String md5sum = null;
            if(Preferences.instance().getBoolean("openstack.upload.metadata.md5")) {
                this.message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        file.getName()));
                md5sum = file.getLocal().attributes().getChecksum();
            }
            MessageDigest digest = null;
            if(!Preferences.instance().getBoolean("openstack.upload.metadata.md5")) {
                try {
                    digest = MessageDigest.getInstance("MD5");
                }
                catch(NoSuchAlgorithmException e) {
                    log.error("Failure loading MD5 digest", e);
                }
            }
            InputStream in = null;
            ResponseOutputStream<String> out = null;
            try {
                if(null == digest) {
                    log.warn("MD5 calculation disabled");
                    in = file.getLocal().getInputStream();
                }
                else {
                    in = new DigestInputStream(file.getLocal().getInputStream(), digest);
                }
                out = this.write(file, status, md5sum);
                this.upload(file, out, in, throttle, listener, status);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
            if(null != digest && null != out) {
                this.message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"), file.getName()));
                // Obtain locally-calculated MD5 hash.
                String expectedETag = ServiceUtils.toHex(digest.digest());
                // Compare our locally-calculated hash with the ETag returned.
                final String result = out.getResponse();
                if(!expectedETag.equals(result)) {
                    throw new IOException("Mismatch between MD5 hash of uploaded data ("
                            + expectedETag + ") and ETag returned ("
                            + result + ") for object key: "
                            + containerService.getKey(file));
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug("Object upload was automatically verified, the calculated MD5 hash " +
                                "value matched the ETag returned: " + containerService.getKey(file));
                    }
                }
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Upload failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        return this.write(file, status, null);
    }

    private ResponseOutputStream<String> write(final Path file, final TransferStatus status, final String md5sum)
            throws BackgroundException {
        final HashMap<String, String> metadata = new HashMap<String, String>();
        // Default metadata for new files
        for(String m : Preferences.instance().getList("openstack.metadata.default")) {
            if(StringUtils.isBlank(m)) {
                continue;
            }
            if(!m.contains("=")) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            int split = m.indexOf('=');
            String name = m.substring(0, split);
            if(StringUtils.isBlank(name)) {
                log.warn(String.format("Missing key in %s", m));
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn(String.format("Missing value in %s", m));
                continue;
            }
            metadata.put(name, value);
        }
        // Submit store run to background thread
        final DelayedHttpEntityCallable<String> command = new DelayedHttpEntityCallable<String>() {
            /**
             *
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public String call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    return getClient().storeObject(
                            getRegion(containerService.getContainer(file)), containerService.getContainer(file).getName(),
                            containerService.getKey(file), entity,
                            metadata, md5sum);
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Upload failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength() - status.getCurrent();
            }
        };
        return this.write(file, command);
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                // Create container at top level
                this.getClient().createContainer(this.getRegion(region), file.getName());
            }
            else {
                // Create virtual directory. Use region of parent container.
                this.getClient().createPath(this.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }

    @Override
    public void rename(final Path file, final Path renamed) throws BackgroundException {
        try {
            if(file.attributes().isFile()) {
                this.getClient().copyObject(this.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        containerService.getContainer(renamed).getName(), containerService.getKey(renamed));
                this.getClient().deleteObject(this.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
            }
            else if(file.attributes().isDirectory()) {
                for(Path i : this.list(file)) {
                    this.rename(i, new Path(renamed, i.getName(), i.attributes().getType()));
                }
                try {
                    this.getClient().deleteObject(this.getRegion(containerService.getContainer(file)),
                            containerService.getContainer(file).getName(), containerService.getKey(file));
                }
                catch(NotFoundException e) {
                    // No real placeholder but just a delimiter returned in the object listing.
                    log.warn(e.getMessage());
                }
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Delete.class) {
            return (T) new SwiftDeleteFeature(this);
        }
        if(type == Headers.class) {
            return (T) new SwiftMetadataFeature(this);
        }
        if(type == Location.class) {
            return (T) new Location() {
                @Override
                public String getLocation(final Path container) throws BackgroundException {
                    return container.attributes().getRegion();
                }
            };
        }
        if(type == AnalyticsProvider.class) {
            return (T) new QloudstatAnalyticsProvider();
        }
        if(type == IdentityConfiguration.class) {
            return (T) new DefaultCredentialsIdentityConfiguration(host);
        }
        if(type == DistributionConfiguration.class) {
            for(Region region : client.getRegions()) {
                if(null != region.getCDNManagementUrl()) {
                    return (T) new SwiftDistributionConfiguration(this) {
                        @Override
                        public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
                            final Distribution distribution = super.read(container, method);
                            distributions.put(container, distribution);
                            return distribution;
                        }
                    };
                }
            }
            return null;
        }
        return super.getFeature(type, prompt);
    }
}