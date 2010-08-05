package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.log4j.Logger;
import org.soyatec.windows.azure.blob.*;
import org.soyatec.windows.azure.blob.internal.BlobContents;
import org.soyatec.windows.azure.blob.internal.BlobProperties;
import org.soyatec.windows.azure.blob.internal.ContainerAccessControl;
import org.soyatec.windows.azure.error.StorageException;
import org.soyatec.windows.azure.internal.SignedIdentifier;
import org.soyatec.windows.azure.util.NameValueCollection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * @version $Id$
 */
public class AzurePath extends CloudPath {
    private static Logger log = Logger.getLogger(AzurePath.class);

    private static class Factory extends PathFactory<AzureSession> {
        @Override
        protected Path create(AzureSession session, String path, int type) {
            return new AzurePath(session, path, type);
        }

        @Override
        protected Path create(AzureSession session, String parent, String name, int type) {
            return new AzurePath(session, parent, name, type);
        }

        @Override
        protected Path create(AzureSession session, String parent, Local file) {
            return new AzurePath(session, parent, file);
        }

        @Override
        protected <T> Path create(AzureSession session, T dict) {
            return new AzurePath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
    }

    private final AzureSession session;

    protected AzurePath(AzureSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected AzurePath(AzureSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected AzurePath(AzureSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected <T> AzurePath(AzureSession s, T dict) {
        super(dict);
        this.session = s;
    }

    @Override
    public AzureSession getSession() {
        return session;
    }

    @Override
    public void readMetadata() {
        try {
            this.getSession().check();

            IBlobContainer container = this.getSession().getContainer(this.getContainerName());
            if(this.isContainer()) {
                final IContainerProperties properties = container.getContainerProperties();
                if(null == properties.getMetadata()) {
                    log.warn("No container properties for " + this.getAbsolute());
                    return;
                }
                Map<String, String> metadata = new HashMap<String, String>();
                for(Object key : properties.getMetadata().keySet()) {
                    metadata.put(key.toString(), properties.getMetadata().getSingleValue(key.toString()));
                }
                this.attributes().setMetadata(metadata);
            }
            else {
                Map<String, String> metadata = new HashMap<String, String>();
                final IBlobProperties properties = container.getBlobProperties(this.getKey());
                if(null == properties.getMetadata()) {
                    log.warn("No blob metadata for " + this.getAbsolute());
                }
                else {
                    for(Object key : properties.getMetadata().keySet()) {
                        metadata.put(key.toString(), properties.getMetadata().getSingleValue(key.toString()));
                    }
                }
                this.attributes().setMetadata(metadata);
            }
        }
        catch(StorageException e) {
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void writeMetadata(Map<String, String> meta) {
        try {
            this.getSession().check();

            final NameValueCollection collection = new NameValueCollection();
            collection.putAll(meta);

            IBlobContainer container = this.getSession().getContainer(this.getContainerName());
            if(this.isContainer()) {
                container.setContainerMetadata(collection);
            }
            else {
                BlobProperties properties = new BlobProperties(this.getKey());
                properties.setMetadata(collection);
                container.updateBlobMetadata(properties);
            }
            this.attributes().clear(false, false, false, true);
        }
        catch(StorageException e) {
            this.error("Cannot write file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot write file attributes", e);
        }
    }

    /**
     * Only supported for containers.
     */
    @Override
    public void readAcl() {
        try {
            IBlobContainer container = this.getSession().getContainer(this.getContainerName());
            final ContainerAccessControl acl = container.getContainerAccessControl();
            this.attributes().setAcl(this.convert(acl));
        }
        catch(StorageException e) {
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    public static final Acl.UserAndRole PUBLIC_ACL = new Acl.UserAndRole(
            new Acl.CanonicalUser("anonymous", Locale.localizedString("Anonymous", "Azure"), false) {
                @Override
                public String getPlaceholder() {
                    return this.getDisplayName();
                }
            },
            new Acl.Role("public")
    );

    public static final Acl.UserAndRole PRIVATE_ACL = new Acl.UserAndRole(
            new Acl.CanonicalUser("anonymous", Locale.localizedString("Anonymous", "Azure"), false) {
                @Override
                public String getPlaceholder() {
                    return this.getDisplayName();
                }
            },
            new Acl.Role("private")
    );

    protected Acl convert(final ContainerAccessControl list) {
        Acl acl = new Acl();
        acl.addAll(list.isPublic() ? PUBLIC_ACL : PRIVATE_ACL);
//        for(ISignedIdentifier identifier : list.getSigendIdentifiers()) {
//            acl.addAll(new Acl.UserAndRole(new Acl.CanonicalUser(identifier.getId(), false),
//                    new Acl.Role(SharedAccessPermissions.toString(identifier.getPolicy().getPermission()))));
//        }
        return acl;
    }

    /**
     * Only supported for containers.
     * <p/>
     * Specifying a Container-Level Access Policy.
     * Establishing a container-level access policy serves to group Shared Access Signatures and to provide
     * additional restrictions for signatures that are bound by the policy. You can use a container-level
     * access policy to change the start time, expiry time, or permissions for a signature, or to
     * revoke it, after it has been issued.
     *
     * @param acl       The permissions to apply
     * @param recursive Include subdirectories and files
     */
    @Override
    public void writeAcl(Acl acl, boolean recursive) {
        try {
            IBlobContainer container = this.getSession().getContainer(this.getContainerName());
            final ContainerAccessControl list;
            if(acl.asList().contains(PUBLIC_ACL)) {
                list = new ContainerAccessControl(true);
            }
            else {
                list = new ContainerAccessControl(false);
            }
            for(Acl.UserAndRole ur : acl.asList()) {
                list.addSignedIdentifier(new SignedIdentifier(ur.getUser().getIdentifier(),
                        SharedAccessPermissions.valueOf(ur.getRole().getName()),
                        null, null));
            }
            container.setContainerAccessControl(list);
        }
        catch(StorageException e) {
            this.error("Cannot change permissions", e);
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
        finally {
            this.attributes().clear(false, false, true, false);
        }
    }


    @Override
    public boolean exists() {
        if(this.isRoot()) {
            return true;
        }
        try {
            final IBlobContainer container = this.getSession().getClient().getBlobContainer(this.getContainerName());
            if(this.isContainer()) {
                return container.isContainerExist();
            }
            else {
                return container.isBlobExist(this.getKey());
            }
        }
        catch(IOException e) {
            return false;
        }
        catch(StorageException e) {
            return false;
        }
        catch(IllegalArgumentException e) {
            log.error(e.getMessage());
            return false;
        }
    }


    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            if(this.isRoot()) {
                // List all containers
                for(IBlobContainer container : this.getSession().getContainers(true)) {
                    Path p = PathFactory.createPath(this.getSession(), this.getAbsolute(), container.getContainerName(),
                            Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                    p.attributes().setOwner(container.getAccountName());
                    p.attributes().setModificationDate(container.getLastModifiedTime().getTime());
                    childs.add(p);
                }
            }
            else {
                IBlobContainer container = this.getSession().getContainer(this.getContainerName());
                final Iterator<IBlobProperties> blobs = container.listBlobs(this.getKey(), true);
                while(blobs.hasNext()) {
                    final IBlobProperties object = blobs.next();
                    final Path file = PathFactory.createPath(this.getSession(), this.getContainerName(), object.getName(),
                            "application/directory".equals(object.getContentType()) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                    if(file.getParent().equals(this)) {
                        file.setParent(this);
                        file.attributes().setSize(object.getContentLength());
                        file.attributes().setChecksum(object.getETag());
                        file.attributes().setModificationDate(object.getLastModifiedTime().getTime());
                        file.attributes().setOwner(this.attributes().getOwner());
                        childs.add(file);
                    }
                }
            }
            this.getSession().setWorkdir(this);
        }
        catch(StorageException e) {
            childs.attributes().setReadable(false);
        }
        catch(IOException e) {
            childs.attributes().setReadable(false);
        }
        return childs;
    }

    @Override
    public void readSize() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                    this.getName()));

            IBlobContainer container = this.getSession().getContainer(this.getContainerName());
            if(this.isContainer()) {
                ;
            }
            else if(this.attributes().isFile()) {
                attributes().setSize(container.getBlobProperties(this.getKey()).getContentLength());
            }
        }
        catch(StorageException e) {
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void readTimestamp() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                    this.getName()));

            IBlobContainer container = this.getSession().getContainer(this.getContainerName());
            if(this.isContainer()) {
                attributes().setModificationDate(container.getLastModifiedTime().getTime());
            }
            else if(this.attributes().isFile()) {
                attributes().setModificationDate(container.getBlobProperties(this.getKey()).getLastModifiedTime().getTime());
            }
        }
        catch(StorageException e) {
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    protected void download(BandwidthThrottle throttle, StreamListener listener, boolean check) {
        if(attributes().isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    this.getSession().check();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                        this.getName()));

                AzureSession.AzureContainer container = this.getSession().getContainer(this.getContainerName());
                in = container.getBlob(this.getKey());
                if(null == in) {
                    throw new IOException("Unable opening data stream");
                }
                final Status status = this.status();
                status.setResume(false);
                out = this.getLocal().getOutputStream(status.isResume());
                this.download(in, out, throttle, listener);
            }
            catch(StorageException e) {
                this.error("Download failed", e);
            }
            catch(IOException e) {
                this.error("Download failed", e);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    @Override
    protected void upload(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        if(attributes().isFile()) {
            try {
                if(check) {
                    this.getSession().check();
                }

                final Status status = this.status();
                status.setResume(false);
                final InputStream in = this.getLocal().getInputStream();
                try {
                    AzureSession.AzureContainer container = this.getSession().getContainer(this.getContainerName());
                    final BlobProperties properties = new BlobProperties(this.getKey());
                    properties.setContentType(this.getLocal().getMimeType());
                    boolean blob = container.createBlob(properties, new HttpEntity() {
                        public boolean isRepeatable() {
                            return false;
                        }

                        public boolean isChunked() {
                            return false;
                        }

                        public long getContentLength() {
                            return getLocal().attributes().getSize();
                        }

                        public Header getContentType() {
                            return null;
                        }

                        public Header getContentEncoding() {
                            return null;
                        }

                        public InputStream getContent() throws IOException, IllegalStateException {
                            return getLocal().getInputStream();
                        }

                        public void writeTo(OutputStream out) throws IOException {
                            upload(out, in, throttle, listener);
                        }

                        public boolean isStreaming() {
                            return true;
                        }

                        public void consumeContent() throws IOException {
                            ;
                        }
                    });
                    if(!blob) {
                        this.status().setComplete(false);
                    }
                }
                finally {
                    IOUtils.closeQuietly(in);
                }
            }
            catch(StorageException e) {
                this.error("Upload failed", e);
            }
            catch(IOException e) {
                this.error("Upload failed", e);
            }
        }
    }

    @Override
    public void mkdir() {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                        this.getName()));

                if(this.isContainer()) {
                    // Create container at top level
                    this.getSession().getClient().createContainer(this.getContainerName(),
                            new NameValueCollection(), IContainerAccessControl.Private);
                    this.getSession().getContainers(true);
                }
                else {
                    BlobProperties properties = new BlobProperties(this.getKey());
                    properties.setContentType("application/directory");
                    // Create virtual directory
                    this.getSession().getContainer(this.getContainerName()).createBlob(properties,
                            new BlobContents(new byte[]{}), true);
                }
                this.cache().put(this.getReference(), AttributedList.<Path>emptyList());
                // The directory listing is no more current
                this.getParent().invalidate();
            }
            catch(StorageException e) {
                this.error("Cannot create folder", e);
            }
            catch(IOException e) {
                this.error("Cannot create folder", e);
            }
        }
    }

    @Override
    public void delete() {
        try {
            this.getSession().check();
            if(!this.isContainer()) {
                this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                IBlobContainer container = this.getSession().getContainer(this.getContainerName());
                container.deleteBlob(this.getKey());
            }
            else if(attributes().isDirectory()) {
                for(AbstractPath i : this.children()) {
                    if(!this.getSession().isConnected()) {
                        break;
                    }
                    i.delete();
                }
                if(this.isContainer()) {
                    this.getSession().getClient().deleteContainer(this.getContainerName());
                }
            }
            // The directory listing is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            if(this.attributes().isFile()) {
                this.error("Cannot delete file", e);
            }
            if(this.attributes().isDirectory()) {
                this.error("Cannot delete folder", e);
            }
        }
    }

    @Override
    public void readUnixPermission() {
        try {
            if(this.isContainer()) {
                IBlobContainer container = this.getSession().getContainer(this.getContainerName());
                ContainerAccessControl acl = container.getContainerAccessControl();
                Permission permission = new Permission();
                permission.getOtherPermissions()[Permission.READ] = acl.isPublic();
                this.attributes().setPermission(permission);
            }
        }
        catch(StorageException e) {
            this.error("Cannot change permissions", e);
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
    }

    @Override
    public void writeUnixPermission(Permission perm, boolean recursive) {
        try {
            if(this.isContainer()) {
                IBlobContainer container = this.getSession().getContainer(this.getContainerName());
                final ContainerAccessControl acl;
                if(perm.getOtherPermissions()[Permission.READ]) {
                    acl = new ContainerAccessControl(true);
                }
                else {
                    acl = new ContainerAccessControl(false);
                }
                container.setContainerAccessControl(acl);
            }
        }
        catch(StorageException e) {
            this.error("Cannot change permissions", e);
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
    }

    @Override
    public void writeTimestamp(long millis) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(AbstractPath renamed) {
        this.copy(renamed);
        renamed.getParent().invalidate();
        this.delete();
    }

    @Override
    public void copy(AbstractPath copy) {
        if(attributes().isFile()) {
            final NameValueCollection metadata = new NameValueCollection();
            if(this.attributes().getMetadata().isEmpty()) {
                this.readMetadata();
            }
            metadata.putAll(this.attributes().getMetadata());
            try {
                this.getSession().getContainer(this.getContainerName()).copyBlob(((AzurePath) copy).getContainerName(),
                        ((AzurePath) copy).getKey(), this.getKey(), metadata, null);
            }
            catch(IOException e) {
                this.error(this.attributes().isFile() ? "Cannot copy file" : "Cannot copy folder", e);
            }
        }
        else {
            for(AbstractPath i : this.children()) {
                if(!this.getSession().isConnected()) {
                    break;
                }
                i.copy(PathFactory.createPath(this.getSession(), copy.getAbsolute(),
                        i.getName(), i.attributes().getType()));
            }
        }
    }

    @Override
    public String toURL() {
        return this.toHttpURL();
    }

    @Override
    public String toHttpURL() {
        try {
            IBlobContainer container = this.getSession().getContainer(this.getContainerName());
            if(this.isContainer()) {
                return container.getContainerUri().toString();
            }
            return container.getBlobProperties(this.getKey()).getUri().toString();
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        return super.toHttpURL();
    }

    @Override
    public String createSignedUrl() {
        ResourceType type;
        if(this.isContainer()) {
            type = ResourceType.Container;
        }
        else {
            type = ResourceType.Blob;
        }
        Date now = new Date();
        Calendar expiry = Calendar.getInstance();
        expiry.setTime(now);
        // If a signed identifier is not specified as part of the Shared Access Signature, the maximum
        // permissible interval over which the signature is valid is one hour. This limit ensures
        // that a signature that is not bound to a container-level access policy is valid for a
        // short duration.
        expiry.add(Calendar.HOUR, 1);
        try {
            final String signedidentifier = null; // Optional. A unique value that correlates to an access policy
            // specified at the container level. The signed identifier may have a maximum size of 64 characters.
            this.getSession().getClient().createSharedAccessUrl(this.getContainerName(),
                    this.getKey(), type, SharedAccessPermissions.RL,
                    null, // If the signature does not provide a value for the signedstart field, the
                    // start time is assumed to be the time when the request reaches the Blob service.
                    new DateTime(expiry.getTime()), signedidentifier);
        }
        catch(ConnectionCanceledException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
