package ch.cyberduck.core.cf;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.FilesExceptionMappingService;
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

import com.rackspacecloud.client.cloudfiles.FilesAuthenticationResponse;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import com.rackspacecloud.client.cloudfiles.FilesRegion;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends HttpSession<FilesClient> {
    private static final Logger log = Logger.getLogger(CFSession.class);

    private Map<String, FilesRegion> regions
            = new HashMap<String, FilesRegion>();

    private Map<Path, Distribution> distributions
            = new HashMap<Path, Distribution>();

    public CFSession(Host h) {
        super(h);
    }

    @Override
    public FilesClient connect(final HostKeyController key) throws BackgroundException {
        return new FilesClient(this.http());
    }

    protected FilesRegion getRegion(final Path container) throws BackgroundException {
        final String location = container.attributes().getRegion();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Lookup region for container %s in region %s", container, location));
        }
        if(regions.containsKey(location)) {
            return regions.get(location);
        }
        log.warn(String.format("Unknown region %s in authentication context", location));
        if(regions.containsKey(null)) {
            final FilesRegion region = regions.get(null);
            log.info(String.format("Use default region %s", region));
            return region;
        }
        if(regions.isEmpty()) {
            throw new ConnectionCanceledException("No default region in authentication context");
        }
        final FilesRegion region = regions.values().iterator().next();
        log.warn(String.format("Fallback to first region found %s", region));
        return region;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        try {
            final FilesAuthenticationResponse authentication = client.authenticate(
                    new SwiftAuthenticationService().getRequest(host, prompt));
            for(FilesRegion region : authentication.getRegions()) {
                regions.put(region.getRegionId(), region);
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map(e);
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
    public String toHttpURL(final Path path) {
        if(distributions.containsKey(path.getContainer())) {
            return distributions.get(path.getContainer()).getURL(path);
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
        if(file.isContainer()) {
            try {
                return this.getClient().containerExists(this.getRegion(file.getContainer()),
                        file.getName());
            }
            catch(FilesException e) {
                throw new FilesExceptionMappingService().map("Cannot read file attributes", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, file);
            }
        }
        return super.exists(file);
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        this.message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                file.getName()));

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
                return this.getClient().getObject(this.getRegion(file.getContainer()),
                        file.getContainer().getName(), file.getKey(),
                        status.getCurrent(), status.getLength());
            }
            return this.getClient().getObject(this.getRegion(file.getContainer()),
                    file.getContainer().getName(), file.getKey());
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Download failed", e, file);
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
            if(Preferences.instance().getBoolean("cf.upload.metadata.md5")) {
                this.message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        file.getName()));
                md5sum = file.getLocal().attributes().getChecksum();
            }
            MessageDigest digest = null;
            if(!Preferences.instance().getBoolean("cf.upload.metadata.md5")) {
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
                this.message(MessageFormat.format(
                        Locale.localizedString("Compute MD5 hash of {0}", "Status"), file.getName()));
                // Obtain locally-calculated MD5 hash.
                String expectedETag = ServiceUtils.toHex(digest.digest());
                // Compare our locally-calculated hash with the ETag returned.
                final String result = out.getResponse();
                if(!expectedETag.equals(result)) {
                    throw new IOException("Mismatch between MD5 hash of uploaded data ("
                            + expectedETag + ") and ETag returned ("
                            + result + ") for object key: "
                            + file.getKey());
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug("Object upload was automatically verified, the calculated MD5 hash " +
                                "value matched the ETag returned: " + file.getKey());
                    }
                }
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Upload failed", e, file);
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
        for(String m : Preferences.instance().getList("cf.metadata.default")) {
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
                            getRegion(file.getContainer()), file.getContainer().getName(),
                            file.getKey(), entity,
                            metadata, md5sum);
                }
                catch(FilesException e) {
                    throw new FilesExceptionMappingService().map("Upload failed", e, file);
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
    public void mkdir(final Path file) throws BackgroundException {
        try {
            if(file.isContainer()) {
                // Create container at top level
                this.getClient().createContainer(this.getRegion(file.getContainer()), file.getName());
            }
            else {
                // Create virtual directory
                this.getClient().createPath(this.getRegion(file.getContainer()), file.getContainer().getName(), file.getKey());
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }

    @Override
    public void delete(final Path file, final LoginController prompt) throws BackgroundException {
        try {
            this.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    file.getName()));
            if(file.attributes().isFile()) {
                this.getClient().deleteObject(this.getRegion(file.getContainer()),
                        file.getContainer().getName(), file.getKey());
            }
            else if(file.attributes().isDirectory()) {
                for(Path i : this.list(file)) {
                    if(!this.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    this.delete(i, prompt);
                }
                if(file.isContainer()) {
                    this.getClient().deleteContainer(this.getRegion(file.getContainer()),
                            file.getContainer().getName());
                }
                else {
                    try {
                        this.getClient().deleteObject(this.getRegion(file.getContainer()),
                                file.getContainer().getName(), file.getKey());
                    }
                    catch(FilesNotFoundException e) {
                        // No real placeholder but just a delimiter returned in the object listing.
                        log.warn(e.getMessage());
                    }
                }
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot delete {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, file);
        }
    }

    @Override
    public void rename(final Path file, final Path renamed) throws BackgroundException {
        try {
            this.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    file.getName(), renamed));

            if(file.attributes().isFile()) {
                this.getClient().copyObject(this.getRegion(file.getContainer()),
                        file.getContainer().getName(), file.getKey(),
                        renamed.getContainer().getName(), renamed.getKey());
                this.getClient().deleteObject(this.getRegion(file.getContainer()),
                        file.getContainer().getName(), file.getKey());
            }
            else if(file.attributes().isDirectory()) {
                for(Path i : this.list(file)) {
                    if(!this.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    this.rename(i, new CFPath(renamed, i.getName(), i.attributes().getType()));
                }
                try {
                    this.getClient().deleteObject(this.getRegion(file.getContainer()),
                            file.getContainer().getName(), file.getKey());
                }
                catch(FilesNotFoundException e) {
                    // No real placeholder but just a delimiter returned in the object listing.
                    log.warn(e.getMessage());
                }
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Headers.class) {
            return (T) new SwiftHeadersFeature(this);
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
            for(FilesRegion region : client.getRegions()) {
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
        return null;
    }
}