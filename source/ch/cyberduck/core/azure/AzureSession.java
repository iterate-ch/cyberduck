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
import ch.cyberduck.core.cloud.CloudHTTP4Session;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.soyatec.windows.azure.authenticate.IAccessPolicy;
import org.soyatec.windows.azure.authenticate.SharedKeyCredentials;
import org.soyatec.windows.azure.blob.*;
import org.soyatec.windows.azure.blob.internal.*;
import org.soyatec.windows.azure.constants.HeaderValues;
import org.soyatec.windows.azure.constants.XmlElementNames;
import org.soyatec.windows.azure.error.StorageErrorCode;
import org.soyatec.windows.azure.error.StorageException;
import org.soyatec.windows.azure.error.StorageServerException;
import org.soyatec.windows.azure.internal.AccessPolicy;
import org.soyatec.windows.azure.internal.OutParameter;
import org.soyatec.windows.azure.internal.ResourceUriComponents;
import org.soyatec.windows.azure.internal.SignedIdentifier;
import org.soyatec.windows.azure.internal.constants.*;
import org.soyatec.windows.azure.util.HttpUtilities;
import org.soyatec.windows.azure.util.NameValueCollection;
import org.soyatec.windows.azure.util.TimeSpan;
import org.soyatec.windows.azure.util.xml.XPathQueryHelper;
import org.soyatec.windows.azure.util.xml.XmlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @version $Id$
 */
public class AzureSession extends CloudHTTP4Session {
    private static Logger log = Logger.getLogger(AzureSession.class);

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new AzureSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    protected AzureSession(Host h) {
        super(h);
    }

    public List<String> getSupportedStorageClasses() {
        return Collections.emptyList();
    }

    private BlobStorageRest client;

    @Override
    protected BlobStorageRest getClient() throws ConnectionCanceledException {
        if(null == client) {
            throw new ConnectionCanceledException();
        }
        return client;
    }

    @Override
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();
        // Prompt the login credentials first
        this.login();
        this.fireConnectionDidOpenEvent();
    }

    @Override
    protected void login(LoginController controller, Credentials credentials) throws IOException {
        // http://*.blob.core.windows.net
        try {
            client = new BlobStorageRest(new URI(host.getProtocol().getScheme() + "://" + host.getHostname()),
                    false,
                    credentials.getUsername(),
                    credentials.getPassword());
        }
        catch(URISyntaxException e) {
            log.error("Failure parsing URI:" + e.getMessage());
            throw new IOException(e.getMessage());
        }
        client.setTimeout(TimeSpan.fromMilliseconds(this.timeout()));
        try {
            this.getContainers(true);
        }
        catch(StorageServerException e) {
            if(this.isLoginFailure(e)) {
                this.message(Locale.localizedString("Login failed", "Credentials"));
                controller.fail(host.getProtocol(), credentials);
                this.login();
            }
            else {
                throw new IOException(e.getCause().getMessage());
            }
        }
    }

    /**
     * Check for Invalid Access ID or Invalid Secret Key
     *
     * @param e
     * @return True if the error code of the S3 exception is a login failure
     */
    protected boolean isLoginFailure(StorageServerException e) {
        Throwable cause = e.getCause();
        if(cause instanceof StorageServerException) {
            if(403 == ((StorageServerException) cause).getStatusCode()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
                super.close();
            }
        }
        finally {
            // No logout required
            client = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    /**
     * Caching the uses's buckets
     */
    private Map<String, AzureContainer> containers = new HashMap<String, AzureContainer>();

    /**
     * Extending REST container to support IO Streams for GET and PUT.
     */
    protected class AzureContainer extends BlobContainerRest {
        public AzureContainer(String containerName, Timestamp lastModified)
                throws ConnectionCanceledException {
            super(getClient().getBaseUri(), getClient().isUsePathStyleUris(), getClient().getAccountName(), containerName,
                    getClient().getBase64Key(), lastModified, getClient().getTimeout(), getClient().getRetryPolicy());
        }

        /**
         * Create a new blob or overwrite an existing blob.
         *
         * @throws StorageException
         */
        public boolean createBlob(BlobProperties blobProperties,
                                  HttpEntity entity)
                throws StorageException {
            try {
                return putBlobImpl(blobProperties, entity);
            }
            catch(Exception e) {
                throw HttpUtilities.translateWebException(e);
            }
        }

        private boolean putBlobImpl(final BlobProperties blobProperties,
                                    final HttpEntity entity)
                throws Exception {
            boolean retval;
            IRetryPolicy policy = RetryPolicies.noRetry();
            retval = (Boolean) policy.execute(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return uploadData(blobProperties, entity);
                }

            });
            return retval;
        }

        private boolean uploadData(BlobProperties blobProperties, HttpEntity entity)
                throws Exception {
            boolean retval;
            ResourceUriComponents uriComponents = new ResourceUriComponents(
                    getAccountName(), getContainerName(), blobProperties.getName());
            URI blobUri = HttpUtilities.createRequestUri(getBaseUri(), this
                    .isUsePathStyleUris(), getAccountName(), getContainerName(),
                    blobProperties.getName(), getTimeout(), new NameValueCollection(),
                    uriComponents);

            HttpRequest request = createHttpRequestForPutBlob(blobUri,
                    HttpMethod.Put, blobProperties);

            SharedKeyCredentials credentials = getClient().getCredentials();
            credentials.signRequest(request, uriComponents);
            ((HttpEntityEnclosingRequest) request).setEntity(entity);
            HttpWebResponse response = new HttpWebResponse(http().execute((HttpUriRequest) request));
            if(response.getStatusCode() == HttpStatus.SC_CREATED) {
                retval = true;
            }
            else {
                retval = false;
                HttpUtilities.processUnexpectedStatusCode(response);
            }

            blobProperties.setLastModifiedTime(response.getLastModified());
            blobProperties.setETag(response.getHeader(HeaderNames.ETag));
            return retval;
        }

        private HttpRequest createHttpRequestForPutBlob(URI blobUri, String httpMethod,
                                                        IBlobProperties blobProperties) {
            HttpRequest request = HttpUtilities.createHttpRequestWithCommonHeaders(
                    blobUri, httpMethod, getTimeout());
            if(blobProperties.getContentEncoding() != null) {
                request.addHeader(HeaderNames.ContentEncoding, blobProperties
                        .getContentEncoding());
            }
            if(blobProperties.getContentLanguage() != null) {
                request.addHeader(HeaderNames.ContentLanguage, blobProperties
                        .getContentLanguage());
            }
            if(blobProperties.getContentType() != null) {
                request.addHeader(HeaderNames.ContentType, blobProperties
                        .getContentType());
            }
            if(blobProperties.getMetadata() != null
                    && blobProperties.getMetadata().size() > 0) {
                HttpUtilities.addMetadataHeaders(request, blobProperties
                        .getMetadata());
            }
            return request;
        }

        /**
         * @param blobName
         * @return
         * @throws StorageException
         */
        public InputStream getBlob(final String blobName)
                throws StorageException {
            try {
                HttpWebResponse response = getBlobImpl(HttpMethod.Get,
                        blobName, new OutParameter<Boolean>(false));
                return response.getStream();
            }
            catch(Exception e) {
                throw HttpUtilities.translateWebException(e);
            }
        }

        private HttpWebResponse getBlobImpl(final String httpMethod, final String blobName,
                                            OutParameter<Boolean> modified) {
            final HttpWebResponse[] httpWebResponses = new HttpWebResponse[1];
            final OutParameter<Boolean> localModified = new OutParameter<Boolean>(true);
            // Reset the stop flag
            stopFetchProgress(Boolean.FALSE);

            modified.setValue(localModified.getValue());

            IRetryPolicy rp = RetryPolicies.noRetry();
            final long originalPosition = 0;
            rp.execute(new Callable<Object>() {
                public Object call() throws Exception {
                    HttpWebResponse response = downloadData(httpMethod, blobName,
                            0, 0, new NameValueCollection(),
                            localModified);
                    httpWebResponses[0] = response;
                    return response;
                }
            });
            modified.setValue(localModified.getValue());
            return httpWebResponses[0];
        }

        private HttpWebResponse downloadData(String httpMethod, String blobName,
                                             long offset,
                                             long length, NameValueCollection nvc,
                                             OutParameter<Boolean> localModified) throws StorageException, ConnectionCanceledException {
            ResourceUriComponents uriComponents = new ResourceUriComponents(
                    getAccountName(), getContainerName(), blobName);
            URI blobUri = HttpUtilities.createRequestUri(getBaseUri(),
                    isUsePathStyleUris(), getAccountName(), getContainerName(),
                    blobName, getTimeout(), nvc, uriComponents);
            HttpRequest request = createHttpRequestForGetBlob(blobUri, httpMethod);

            if(offset != 0 || length != 0) {
                // Use the blob storage custom header for range since the standard
                // HttpWebRequest.
                // AddRange accepts only 32 bit integers and so does not work for
                // large blobs.
                String rangeHeaderValue = MessageFormat
                        .format(HeaderValues.RangeHeaderFormat, offset, offset
                                + length - 1);
                request.addHeader(HeaderNames.StorageRange, rangeHeaderValue);
            }

            final SharedKeyCredentials credentials = getClient().getCredentials();
            credentials.signRequest(request, uriComponents);
            BlobProperties blobProperties;

            try {
                HttpWebResponse response = new HttpWebResponse(http().execute((HttpUriRequest) request));
                if(response.getStatusCode() == HttpStatus.SC_OK
                        || response.getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {

                    return response;
                }
                else {
                    HttpUtilities.processUnexpectedStatusCode(response);
                    return null;
                }
            }
            catch(Exception we) {
                throw HttpUtilities.translateWebException(we);
            }
        }

        private HttpRequest createHttpRequestForGetBlob(URI blobUri,
                                                        String httpMethod) {
            return HttpUtilities.createHttpRequestWithCommonHeaders(
                    blobUri, httpMethod, getTimeout());
        }

        @Override
        public ContainerAccessControl getContainerAccessControl()
                throws StorageException {
            ContainerAccessControl accessControl;
            try {
                accessControl = (ContainerAccessControl) getRetryPolicy().execute(
                        new Callable<ContainerAccessControl>() {
                            public ContainerAccessControl call() throws Exception {
                                NameValueCollection queryParams = new NameValueCollection();
                                queryParams.put(QueryParams.QueryParamComp,
                                        CompConstants.Acl);
                                // New version container ACL
                                queryParams.put(QueryParams.QueryRestType,
                                        CompConstants.Container);

                                ResourceUriComponents uriComponents = new ResourceUriComponents(
                                        getAccountName(), getContainerName(), null);
                                URI uri = HttpUtilities.createRequestUri(
                                        getBaseUri(), isUsePathStyleUris(),
                                        getAccountName(), getContainerName(), null,
                                        getTimeout(), queryParams, uriComponents);
                                HttpRequest request = HttpUtilities
                                        .createHttpRequestWithCommonHeaders(uri,
                                                HttpMethod.Get, getTimeout());
                                request.addHeader(HeaderNames.ApiVersion,
                                        XmsVersion.VERSION_2009_07_17);

                                getClient().getCredentials().signRequest(request, uriComponents);
                                HttpWebResponse response = new HttpWebResponse(http().execute((HttpUriRequest) request));
                                if(response.getStatusCode() == HttpStatus.SC_OK) {
                                    String acl = response
                                            .getHeader(HeaderNames.PublicAccess);
                                    boolean publicAcl = false;
                                    if(acl != null) {
                                        publicAcl = Boolean.parseBoolean(acl);
                                        List<SignedIdentifier> identifiers = getSignedIdentifiersFromResponse(response);
                                        ContainerAccessControl aclEntity;
                                        if(identifiers != null
                                                && identifiers.size() > 0) {
                                            aclEntity = new ContainerAccessControl(publicAcl);
                                            aclEntity.setSigendIdentifiers(identifiers);
                                        }
                                        else {
                                            aclEntity = publicAcl ? IContainerAccessControl.Public
                                                    : IContainerAccessControl.Private;
                                        }
                                        return aclEntity;
                                    }
                                    else {
                                        throw new StorageServerException(
                                                StorageErrorCode.ServiceBadResponse,
                                                "The server did not respond with expected container access control header",
                                                response.getStatusCode(), null);
                                    }
                                }
                                else {
                                    HttpUtilities.processUnexpectedStatusCode(response);
                                    return null;
                                }
                            }

                        });
            }
            catch(Exception e) {
                throw HttpUtilities.translateWebException(e);

            }
            return accessControl;
        }

        @SuppressWarnings("unchecked")
        private List<SignedIdentifier> getSignedIdentifiersFromResponse(
                HttpWebResponse response) {
            InputStream stream = response.getStream();
            if(stream == null) {
                return Collections.EMPTY_LIST;
            }
            try {
                Document doc = XmlUtil.load(stream,
                        "Container access control parsed error.");
                List selectNodes = doc
                        .selectNodes(XPathQueryHelper.SignedIdentifierListQuery);
                List<SignedIdentifier> result = new ArrayList<SignedIdentifier>();
                if(selectNodes.size() > 0) {
                    for(Object selectNode : selectNodes) {
                        Element element = (Element) selectNode;
                        SignedIdentifier identifier = new SignedIdentifier();
                        identifier
                                .setId(XPathQueryHelper
                                        .loadSingleChildStringValue(
                                                element,
                                                XmlElementNames.ContainerSignedIdentifierId,
                                                true));
                        IAccessPolicy policy = new AccessPolicy();
                        Element accesPlocy = (Element) element
                                .selectSingleNode(XmlElementNames.ContainerAccessPolicyName);
                        if(accesPlocy != null && accesPlocy.hasContent()) {
                            String start = XPathQueryHelper
                                    .loadSingleChildStringValue(
                                            accesPlocy,
                                            XmlElementNames.ContainerAccessPolicyStart,
                                            true);
                            if(StringUtils.isNotEmpty(start)) {
                                policy.setStart(new DateTime(start));
                            }
                            String end = XPathQueryHelper
                                    .loadSingleChildStringValue(
                                            accesPlocy,
                                            XmlElementNames.ContainerAccessPolicyExpiry,
                                            true);
                            if(StringUtils.isNotEmpty(end)) {
                                policy.setExpiry(new DateTime(end));
                            }
                            policy.setPermission(SharedAccessPermissions
                                    .valueOf(XPathQueryHelper
                                            .loadSingleChildStringValue(
                                                    accesPlocy,
                                                    XmlElementNames.ContainerAccessPolicyPermission,
                                                    true)));
                            identifier.setPolicy(policy);
                        }
                        result.add(identifier);
                    }
                }
                return result;
            }
            catch(Exception e) {
                // For dev local storage, Container access control may have no
                // detail.
                org.soyatec.windows.azure.util.Logger.error("Parse container accesss control error", e);
                return Collections.EMPTY_LIST;
            }
        }
    }

    /**
     * @param reload
     * @return
     */
    protected List<AzureContainer> getContainers(boolean reload) throws IOException, StorageServerException {
        if(containers.isEmpty() || reload) {
            containers.clear();
            for(final IBlobContainer container : this.getClient().listBlobContainers()) {
                containers.put(container.getContainerName(), new AzureContainer(
                        container.getContainerName(), container.getLastModifiedTime())
                );
            }
        }
        return new ArrayList<AzureContainer>(containers.values());
    }

    /**
     * @param bucketname
     * @return
     * @throws IOException
     */
    protected AzureContainer getContainer(final String bucketname) throws IOException {
        try {
            for(AzureContainer container : this.getContainers(false)) {
                if(container.getContainerName().equals(bucketname)) {
                    return container;
                }
            }
        }
        catch(StorageServerException e) {
            this.error("Cannot read file attributes", e);
        }
        log.warn("Bucket not found with name:" + bucketname);
        return new AzureContainer("$root", null);
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return Arrays.asList(AzurePath.PUBLIC_ACL.getUser());
//        List<Acl.User> l = new ArrayList<Acl.User>();
//        l.add(new Acl.CanonicalUser(""));
//        l.add(AzurePath.PUBLIC_ACL.getUser());
//        return l;
    }

    @Override
    public Acl getPrivateAcl(String container) {
        return new Acl();
    }

    @Override
    public Acl getPublicAcl(String container, boolean readable, boolean writable) {
        Acl acl = new Acl();
        if(readable) {
            acl.addAll(AzurePath.PUBLIC_ACL.getUser(), AzurePath.PUBLIC_ACL.getRole());
        }
        return acl;
    }

    /**
     * Valid permissions values are read (r), write (w), delete (d) and list (l).
     *
     * @return
     */
    @Override
    public List<Acl.Role> getAvailableAclRoles(List<Path> files) {
        return Arrays.asList(AzurePath.PUBLIC_ACL.getRole(), AzurePath.PRIVATE_ACL.getRole());
//        return Arrays.asList(new Acl.Role(SharedAccessPermissions.toString(SharedAccessPermissions.RL)),
//                new Acl.Role(SharedAccessPermissions.toString(SharedAccessPermissions.RW)),
//                new Acl.Role(SharedAccessPermissions.toString(SharedAccessPermissions.RWL)),
//                new Acl.Role(SharedAccessPermissions.toString(SharedAccessPermissions.RWDL)));
    }

    @Override
    public boolean isDownloadResumable() {
        return false;
    }

    @Override
    public boolean isUploadResumable() {
        return false;
    }

    /**
     * Creating files is only possible inside a container.
     *
     * @param workdir The workdir to create query
     * @return Fals if directory is root.
     */
    @Override
    public boolean isCreateFileSupported(Path workdir) {
        return !workdir.isRoot();
    }

    @Override
    public boolean isCDNSupported() {
        return false;
    }

    @Override
    public boolean isChecksumSupported() {
        return true;
    }
}