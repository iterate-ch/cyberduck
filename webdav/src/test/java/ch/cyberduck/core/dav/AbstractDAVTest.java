package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.local.FlatTemporaryFileService;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.Auth;
import io.milton.http.HttpManager;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.ResourceFactory;
import io.milton.http.SecurityManager;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.fs.FileSystemResourceFactory;
import io.milton.http.fs.FsDirectoryResource;
import io.milton.http.fs.FsFileResource;
import io.milton.http.fs.SimpleFileContentService;
import io.milton.http.fs.SimpleSecurityManager;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.property.PropertySource;
import io.milton.resource.CollectionResource;
import io.milton.resource.MultiNamespaceCustomPropertyResource;
import io.milton.resource.Resource;
import io.milton.simpleton.SimpletonServer;

import static org.junit.Assert.fail;

public class AbstractDAVTest extends VaultTest {

    protected DAVSession session;

    private SimpletonServer server;
    private static final int PORT_NUMBER = ThreadLocalRandom.current().nextInt(2000, 3000);

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{CryptoVault.VAULT_VERSION_DEPRECATED, CryptoVault.VAULT_VERSION};
    }

    @Parameterized.Parameter
    public int vaultVersion;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new DAVProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/DAV.cyberduckprofile"));
        final Host host = new Host(profile, "localhost", PORT_NUMBER, new Credentials("cyberduck"));
        session = new DAVSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }

            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(), new TestPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            return "n";
        }
    }

    @After
    public void stop() {
        server.stop();
    }

    @Before
    public void start() throws Exception {
        final HttpManagerBuilder b = new HttpManagerBuilder();
        b.setEnableFormAuth(false);
        b.setEnableDigestAuth(false);
        b.setEnableOptionsAuth(false);

        final Local directory = new FlatTemporaryFileService().create(new AlphanumericRandomStringService().random());
        directory.mkdir();

        SecurityManager sm = new SimpleSecurityManager() {
            @Override
            public Object authenticate(final String user, final String password) {
                return user;
            }

            @Override
            public Object authenticate(final DigestResponse digestRequest) {
                return "ok";
            }

            @Override
            public String getRealm(final String host) {
                return "realm";
            }
        };
        final FileSystemResourceFactory resourceFactory = new FileSystemResourceFactory(new File(directory.getAbsolute()), sm, "/");
        resourceFactory.setAllowDirectoryBrowsing(true);
        final Map<QName, Object> properties = new HashMap<>();
        b.setResourceFactory(new ResourceFactory() {
            @Override
            public Resource getResource(final String host, final String path) {
                final Resource resource = resourceFactory.getResource(host, path);
                if(resource instanceof FsFileResource) {
                    return new MultiNamespaceCustomPropertyFsFileResource(properties, (FsFileResource) resource, host, resourceFactory, ((FsFileResource) resource).getFile());
                }
                if(resource instanceof FsDirectoryResource) {
                    return new MultiNamespaceCustomPropertyFsDirectoryResource(properties, (FsDirectoryResource) resource, host, resourceFactory, ((FsDirectoryResource) resource).getFile());
                }
                return resource;
            }
        });
        final HttpManager httpManager = b.buildHttpManager();
        server = new SimpletonServer(httpManager, b.getOuterWebdavResponseHandler(), 100, 10);
        server.setHttpPort(PORT_NUMBER);
        server.start();
    }

    private static class MultiNamespaceCustomPropertyFsFileResource extends FsFileResource implements MultiNamespaceCustomPropertyResource {
        private final Map<QName, Object> properties;
        private final FsFileResource proxy;

        public MultiNamespaceCustomPropertyFsFileResource(final Map<QName, Object> properties, final FsFileResource proxy, final String host, final FileSystemResourceFactory factory, final File dir) {
            super(host, factory, dir, new SimpleFileContentService());
            this.properties = properties;
            this.proxy = proxy;
        }

        @Override
        public Object getProperty(final QName name) {
            return properties.get(name);
        }

        @Override
        public void setProperty(final QName name, final Object value) throws PropertySource.PropertySetException {
            properties.put(name, value);
        }

        @Override
        public PropertySource.PropertyMetaData getPropertyMetaData(final QName name) {
            if(name.getNamespaceURI().equals("DAV:")) {
                return PropertySource.PropertyMetaData.UNKNOWN;
            }
            return new PropertySource.PropertyMetaData(PropertySource.PropertyAccessibility.WRITABLE, String.class);
        }

        @Override
        public List<QName> getAllPropertyNames() {
            return new ArrayList<>(properties.keySet());
        }

        @Override
        public String getUniqueId() {
            return proxy.getUniqueId();
        }

        @Override
        public String getName() {
            return proxy.getName();
        }

        @Override
        public Object authenticate(final String user, final String password) {
            return proxy.authenticate(user, password);
        }

        @Override
        public boolean authorise(final Request request, final Request.Method method, final Auth auth) {
            return proxy.authorise(request, method, auth);
        }

        @Override
        public String getRealm() {
            return proxy.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return proxy.getModifiedDate();
        }

        @Override
        public String checkRedirect(final Request request) {
            return proxy.checkRedirect(request);
        }


        @Override
        public void copyTo(final CollectionResource toCollection, final String name) throws NotAuthorizedException {
            proxy.copyTo(toCollection, name);
        }

        @Override
        public void delete() {
            proxy.delete();
        }

        @Override
        public void sendContent(final OutputStream out, final Range range, final Map<String, String> params, final String contentType) throws IOException, NotFoundException {
            proxy.sendContent(out, range, params, contentType);
        }

        @Override
        public Long getMaxAgeSeconds(final Auth auth) {
            return proxy.getMaxAgeSeconds(auth);
        }

        @Override
        public String getContentType(final String accepts) {
            return proxy.getContentType(accepts);
        }

        @Override
        public Long getContentLength() {
            return proxy.getContentLength();
        }

        @Override
        public void moveTo(final CollectionResource rDest, final String name) {
            proxy.moveTo(rDest, name);
        }

        @Override
        public Date getCreateDate() {
            return proxy.getCreateDate();
        }

        @Override
        public void replaceContent(final InputStream in, final Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
            proxy.replaceContent(in, length);
        }
    }

    private static class MultiNamespaceCustomPropertyFsDirectoryResource extends FsDirectoryResource implements MultiNamespaceCustomPropertyResource {
        private final Map<QName, Object> properties;
        private final FsDirectoryResource proxy;
        private final String host;
        private final FileSystemResourceFactory factory;

        public MultiNamespaceCustomPropertyFsDirectoryResource(final Map<QName, Object> properties, final FsDirectoryResource proxy, final String host, final FileSystemResourceFactory factory, final File dir) {
            super(host, factory, dir, new SimpleFileContentService());
            this.properties = properties;
            this.proxy = proxy;
            this.host = host;
            this.factory = factory;
        }

        @Override
        public Object getProperty(final QName name) {
            return properties.get(name);
        }

        @Override
        public void setProperty(final QName name, final Object value) throws PropertySource.PropertySetException {
            properties.put(name, value);
        }

        @Override
        public PropertySource.PropertyMetaData getPropertyMetaData(final QName name) {
            if(name.getNamespaceURI().equals("DAV:")) {
                return PropertySource.PropertyMetaData.UNKNOWN;
            }
            return new PropertySource.PropertyMetaData(PropertySource.PropertyAccessibility.WRITABLE, String.class);
        }

        @Override
        public List<QName> getAllPropertyNames() {
            return new ArrayList<>(properties.keySet());
        }

        @Override
        public String getUniqueId() {
            return proxy.getUniqueId();
        }

        @Override
        public String getName() {
            return proxy.getName();
        }

        @Override
        public Object authenticate(final String user, final String password) {
            return proxy.authenticate(user, password);
        }

        @Override
        public boolean authorise(final Request request, final Request.Method method, final Auth auth) {
            return proxy.authorise(request, method, auth);
        }

        @Override
        public String getRealm() {
            return proxy.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return proxy.getModifiedDate();
        }

        @Override
        public String checkRedirect(final Request request) {
            return proxy.checkRedirect(request);
        }

        @Override
        public void copyTo(final CollectionResource toCollection, final String name) throws NotAuthorizedException {
            proxy.copyTo(toCollection, name);
        }

        @Override
        public void delete() {
            proxy.delete();
        }

        @Override
        public void moveTo(final CollectionResource rDest, final String name) {
            proxy.moveTo(rDest, name);
        }

        @Override
        public Date getCreateDate() {
            return proxy.getCreateDate();
        }

        @Override
        public LockToken createAndLock(final String name, final LockTimeout timeout, final LockInfo lockInfo) throws NotAuthorizedException {
            return proxy.createAndLock(name, timeout, lockInfo);
        }

        @Override
        public CollectionResource createCollection(final String newName) {
            return proxy.createCollection(newName);
        }

        @Override
        public Resource createNew(final String newName, final InputStream inputStream, final Long length, final String contentType) throws IOException {
            return proxy.createNew(newName, inputStream, length, contentType);
        }

        @Override
        public Resource child(final String childName) {
            return proxy.child(childName);
        }

        @Override
        public List<? extends Resource> getChildren() {
            return proxy.getChildren().stream().map(resource -> resource instanceof FsFileResource ? new MultiNamespaceCustomPropertyFsFileResource(properties, (FsFileResource) resource, host, factory, ((FsFileResource) resource).getFile()) :
                    resource instanceof FsDirectoryResource ? new MultiNamespaceCustomPropertyFsDirectoryResource(properties, (FsDirectoryResource) resource, host, factory, ((FsDirectoryResource) resource).getFile()) : resource).collect(Collectors.toList());
        }

        @Override
        public void sendContent(final OutputStream out, final Range range, final Map<String, String> params, final String contentType) throws IOException, NotAuthorizedException {
            proxy.sendContent(out, range, params, contentType);
        }

        @Override
        public Long getMaxAgeSeconds(final Auth auth) {
            return proxy.getMaxAgeSeconds(auth);
        }

        @Override
        public String getContentType(final String accepts) {
            return proxy.getContentType(accepts);
        }

        @Override
        public Long getContentLength() {
            return proxy.getContentLength();
        }

        @Override
        public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo) throws NotAuthorizedException {
            return proxy.lock(timeout, lockInfo);
        }

        @Override
        public LockResult refreshLock(final String token, final LockTimeout timeout) throws NotAuthorizedException {
            return proxy.refreshLock(token, timeout);
        }

        @Override
        public void unlock(final String tokenId) throws NotAuthorizedException {
            proxy.unlock(tokenId);
        }

        @Override
        public LockToken getCurrentLock() {
            return proxy.getCurrentLock();
        }
    }
}
