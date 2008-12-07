/*
 * See COPYING for license information.
 */

package com.mosso.client.cloudfiles;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A client for Cloud Files.  Here follows a basic example of logging in, creating a container and an
 * object, retrieving the object, and then deleting both the object and container.  For more examples,
 * see the code in com.mosso.client.cloudfiles.sample, which contains a series of examples.
 * <p/>
 * <pre>
 * <p/>
 *  //  Create the client object for username "jdoe", password "johnsdogsname".
 * 	FilesClient myClient = FilesClient("jdoe", "johnsdogsname");
 * <p/>
 *  // Log in (<code>login()</code> will return false if the login was unsuccessful.
 *  assert(myClient.login());
 * <p/>
 *  // Make sure there are no containers in the account
 *  assert(myClient.listContainers.length() == 0);
 * <p/>
 *  // Create the container
 *  assert(FilesConstants.CONTAINER_CREATED == myClient.createContainer("myContainer"));
 * <p/>
 *  // Now we should have one
 *  assert(myClient.listContainers.length() == 1);
 * <p/>
 *  // Upload the file "alpaca.jpg"
 *  assert(FilesConstants.OBJECT_CREATED == myClient.storeObject("myContainer", new File("alapca.jpg"), "image/jpeg"));
 * <p/>
 *  // Download "alpaca.jpg"
 *  FilesObject obj = myClient.getObject("myContainer", "alpaca.jpg");
 *  byte data[] = obj.getObject();
 * <p/>
 *  // Clean up after ourselves.
 *  // Note:  Order here is important, you can't delete non-empty containers.
 *  assert(FilesConstants.OBJECT_DELETED == myClient.deleteObject("myContainer", "alpaca.jpg"));
 *  assert(FilesConstants.CONTAINER_DELETED == myClient.deleteContainer("myContainer");
 * </pre>
 *
 * @author lvaughn
 * @see com.mosso.client.cloudfiles.sample.FilesCli
 * @see com.mosso.client.cloudfiles.sample.FilesAuth
 * @see com.mosso.client.cloudfiles.sample.FilesCopy
 * @see com.mosso.client.cloudfiles.sample.FilesList
 * @see com.mosso.client.cloudfiles.sample.FilesRemove
 * @see com.mosso.client.cloudfiles.sample.FilesMakeContainer
 */
public class FilesClient {
    public static final String VERSION = "v1";

    private String username = null;
    private String password = null;
    private String account = null;
    private String authenticationURL;
    private int connectionTimeOut;
    private String storageURL = null;
    private String cdnManagementURL = null;
    private String authToken = null;
    private boolean isLoggedin = false;
    private boolean useETag = true;

    private HttpClient client = new HttpClient();

    private static Logger logger = Logger.getLogger(FilesClient.class);

    /**
     * @param username          The username to log in to
     * @param password          The password
     * @param account           The Cloud Files account to use
     * @param connectionTimeOut The connection timeout, in ms.
     */
    public FilesClient(String username, String password, String account, int connectionTimeOut) {
        this.username = username;
        this.password = password;
        this.account = account;
        if(account != null && account.length() > 0) {
            this.authenticationURL = FilesUtil.getProperty("auth_url") + VERSION + "/" + account + FilesUtil.getProperty("auth_url_post");
        }
        else {
            this.authenticationURL = FilesUtil.getProperty("auth_url");
        }
        this.connectionTimeOut = connectionTimeOut;

        client.getParams().setParameter("http.socket.timeout", this.connectionTimeOut);
        setUserAgent(FilesConstants.USER_AGENT);

        if(logger.isDebugEnabled()) {
            logger.debug("UserName: " + this.username);
            logger.debug("AuthenticationURL: " + this.authenticationURL);
            logger.debug("ConnectionTimeOut: " + this.connectionTimeOut);
        }
    }

    /**
     * This method uses the default connection time out of CONNECTON_TIMEOUT.  If <code>account</code>
     * is null, "Mosso Style" authentication is assumed, otherwise standard Cloud Files authentication is used.
     *
     * @param username
     * @param password
     * @param account
     */
    public FilesClient(String username, String password, String account) {
        this(username, password, account, FilesUtil.getIntProperty("connection_timeout"));
    }

    /**
     * Mosso-style authentication (No accounts).
     *
     * @param username     Your CloudFiles username
     * @param apiAccessKey Your CloudFiles API Access Key
     */
    public FilesClient(String username, String apiAccessKey) {
        this(username, apiAccessKey, null, FilesUtil.getIntProperty("connection_timeout"));
    }

    /**
     * This method uses the default connection time out of CONNECTON_TIMEOUT and username, password,
     * and account from FilesUtil
     */
    public FilesClient() {
        this(FilesUtil.getProperty("username"),
                FilesUtil.getProperty("password"),
                FilesUtil.getProperty("account"),
                FilesUtil.getIntProperty("connection_timeout"));
    }

    /**
     * @param config
     */
    public void setHostConfiguration(HostConfiguration config) {
        client.setHostConfiguration(config);
    }

    /**
     * Returns the Account associated with the URL
     *
     * @return The account name
     */
    public String getAccount() {
        return account;
    }

    /**
     * Set the Account value and reassemble the Authentication URL.
     *
     * @param account
     */
    public void setAccount(String account) {
        this.account = account;
        if(account != null && account.length() > 0) {
            this.authenticationURL = FilesUtil.getProperty("auth_url") + VERSION + "/" + account + FilesUtil.getProperty("auth_url_post");
        }
        else {
            this.authenticationURL = FilesUtil.getProperty("auth_url");
        }
    }

    /**
     * Log in to Cloud FS.  This method performs the authentication and sets up the client's internal state.
     *
     * @return true if the login was successful, false otherwise.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public boolean login() throws IOException, HttpException {
        GetMethod method = new GetMethod(authenticationURL);
        method.getParams().setSoTimeout(connectionTimeOut);

        method.setRequestHeader(FilesConstants.X_STORAGE_USER, username);
        method.setRequestHeader(FilesConstants.X_STORAGE_PASS, password);

        logger.debug("Logging in user: " + username + " using URL: " + authenticationURL);
        client.executeMethod(method);

        FilesResponse response = new FilesResponse(method);

        if(response.loginSuccess()) {
            isLoggedin = true;
            storageURL = response.getStorageURL();
            cdnManagementURL = response.getCDNManagementURL();
            authToken = response.getAuthToken();
            logger.debug("storageURL: " + storageURL);
            logger.debug("authToken: " + authToken);
            logger.debug("cdnManagementURL:" + cdnManagementURL);
            if(cdnManagementURL == null) {
                logger.warn("CDN Management URL was null!!!");
                cdnManagementURL = storageURL.replace("storage", "cdn");
                logger.warn("Replaced with: " + cdnManagementURL);
            }
        }
        method.releaseConnection();

        return this.isLoggedin;
    }

    /**
     * List the containers available in an account.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.
     *         if there are no containers in the account, the list will be zero length.
     */
    public List<FilesContainer> listContainers() throws IOException, HttpException, FilesAuthorizationException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        GetMethod method = new GetMethod(storageURL);
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                StrTokenizer tokenize = new StrTokenizer(inputStreamToString(response.getResponseBodyAsStream(), method.getResponseCharSet()));
                tokenize.setDelimiterString("\n");
                String[] containers = tokenize.getTokenArray();
                logger.debug("Total Containers in Account are: " + containers.length);
                ArrayList<FilesContainer> containerList = new ArrayList<FilesContainer>();
                for(String containerName : containers) {
                    FilesContainer tmpCont = new FilesContainer(containerName, this);
                    containerList.add(tmpCont);
                }
                return containerList;
            }
            else if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                logger.debug("Account has no Containers");
                return new ArrayList<FilesContainer>();
            }
            else if(response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                logger.warn("User not Authorized !");
                throw new FilesAuthorizationException("User not Authorized !", response.getResponseHeaders(), response.getStatusLine());
            }
            else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.warn("The URL used for the account was not found !");
                logger.warn("HTTP Status: " + response.getStatusLine());
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * List of of the objects in a container with the given starting string.
     *
     * @param container  The container name
     * @param startsWith The string to start with
     * @return A list of FSObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public List<FilesObject> listObjectsStaringWith(String container, String startsWith) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        GetMethod method = new GetMethod(storageURL + "/" + sanitizeForURI(container));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

            NameValuePair[] queryParams = {new NameValuePair(FilesConstants.LIST_CONTAINER_NAME_QUERY, startsWith)};
            method.setQueryString(queryParams);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                StrTokenizer tokenize = new StrTokenizer(inputStreamToString(response.getResponseBodyAsStream(), method.getResponseCharSet()));
                tokenize.setDelimiterString("\n");
                String[] containers = tokenize.getTokenArray();
                logger.debug("Total Containers in Account are: " + containers.length);
                ArrayList<FilesObject> containerList = new ArrayList<FilesObject>();
                for(String containerName : containers) {
                    FilesObject tmpObj = new FilesObject(containerName, container, this);
                    containerList.add(tmpObj);
                }

                return containerList;
            }
            else if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                logger.debug("Account has no Containers");
                return new ArrayList<FilesObject>();
            }
            else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.info("Either the Container could not be found or the account information is incorrect !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Lists the contents of a container
     *
     * @param container The name of the container
     * @return A list of all of the objects in that container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public List<FilesObject> listObjects(String container) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        GetMethod method = new GetMethod(storageURL + "/" + sanitizeForURI(container));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                StrTokenizer tokenize = new StrTokenizer(inputStreamToString(response.getResponseBodyAsStream(), method.getResponseCharSet()));
                tokenize.setDelimiterString("\n");
                String[] objects = tokenize.getTokenArray();
                logger.debug("Total Objects in Container: " + container + " are: " + objects.length);
                ArrayList<FilesObject> objectList = new ArrayList<FilesObject>();
                for(String objectName : objects) {
                    FilesObject tmpObj = new FilesObject(objectName, container, this);
                    objectList.add(tmpObj);
                }
                return objectList;
            }
            else if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                logger.info("Container " + container + " has no Objects");
                return new ArrayList<FilesObject>();
            }
            else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.info("Either the Container could not be found or the account information is incorrect !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Convenience method to test for the existence of a container in Cloud Files.
     *
     * @param container
     * @return true if the container exists.  false otherwise.
     * @throws IOException
     * @throws HttpException
     */
    public boolean containerExists(String container) throws IOException, HttpException {
        return this.getContainerInfo(container) != null;

    }

    /**
     * Gets information for to given account.
     *
     * @return The FilesAccountInfo with information about the number of containers and number of bytes used
     *         by the given account.
     * @throws IOException
     * @throws HttpException
     */
    public FilesAccountInfo getAccountInfo() throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        HeadMethod method = new HeadMethod(storageURL);
        method.getParams().setSoTimeout(connectionTimeOut);
        method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
        client.executeMethod(method);

        FilesResponse response = new FilesResponse(method);

        if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            logger.debug("Info Returned ");

            int nContainers = response.getAccountContainerCount();
            long totalSize = response.getAccountBytesUsed();
            return new FilesAccountInfo(totalSize, nContainers);
        }
        throw new HttpException(response.getStatusMessage());
    }

    /**
     * Get basic information on a container (number of items and the total size).
     *
     * @param container
     * @return ContainerInfo object of the container is present or null if its not present
     * @throws IOException
     * @throws HttpException
     */
    public FilesContainerInfo getContainerInfo(String container) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        HeadMethod method = new HeadMethod(storageURL + "/" + sanitizeForURI(container));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                logger.debug("Container Returned ");

                int objCount = response.getContainerObjectCount();
                long objSize = response.getContainerBytesUsed();
                return new FilesContainerInfo(objCount, objSize);
            }
            else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.info("Either the Container could not be found or the account information is incorrect !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }


    /**
     * Creates a container
     *
     * @param name The name of the container to be created
     * @return If the container was created : FilesConstants.CONTAINER_CREATED. If the container already existed FilesConstants.CONTAINER_EXISTED
     *         or -1 if something was wrong. Check the logs for exact details of what wet wrong.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public int createContainer(String name) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        if(name.length() > 64) {
            logger.warn("Container Name : " + name + " is too long.");
            name = name.substring(0, 64);
            logger.warn("Container Name truncated to max allowed: " + name);
        }

        // logger.warn(name + ":" + sanitizeForURI(name));
        PutMethod method = new PutMethod(storageURL + "/" + sanitizeForURI(name));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_CREATED) {
                logger.debug("Container Created : " + name);
                return response.getStatusCode();
            }
            if(response.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                logger.info("Container already exists !");
            }
            else {
                logger.warn("Unexpected return code " + response.getStatusCode() + ": " + response.getStatusMessage());
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Deletes a container
     *
     * @param name The name of the container
     * @return FilesConstants.CONTAINER_DELETED if it was successfully deleted, -1 if there was a error
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public int deleteContainer(String name) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        DeleteMethod method = new DeleteMethod(storageURL + "/" + sanitizeForURI(name));
        try {
            logger.debug("Deleting container with the following URL:\n" + method.getURI().toString());
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                logger.info("Container Deleted : " + name);
                return response.getStatusCode();
            }
            else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.info("Container does not exist !");
            }
            else if(response.getStatusCode() == HttpStatus.SC_CONFLICT) {
                logger.info("Container is not empty, can not delete a none empty container !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Enables access of files in this container via the Content Delivery Network.
     *
     * @param name The name of the container to enable
     * @return The CDN Url of the container
     * @throws IOException                 There was an IO error doing network communication
     * @throws HttpException               There was an error with the http protocol
     * @throws FilesAuthorizationException Authentication failed
     */
    public String cdnEnableContainer(String name) throws IOException, HttpException, FilesAuthorizationException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        PutMethod method = new PutMethod(cdnManagementURL + "/" + sanitizeForURI(name));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                return response.getCdnUrl();
            }
            else if(response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                logger.warn("Unauthorized access");
                throw new FilesAuthorizationException("User not Authorized!", response.getResponseHeaders(), response.getStatusLine());
            }
            logger.warn("Unexpected return code: " + response.getStatusLine());
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Enables access of files in this container via the Content Delivery Network.
     *
     * @param name    The name of the container to enable
     * @param ttl     How long the CDN can use the content before checking for an update.  A negative value will result in this not being changed.
     * @param enabled True if this folder should be accesible, false otherwise
     * @return The CDN Url of the container
     * @throws IOException                 There was an IO error doing network communication
     * @throws HttpException               There was an error with the http protocol
     * @throws FilesAuthorizationException Authentication failed
     */
    /*
     * @param referrerAcl Unused for now
     * @param userAgentACL Unused for now
     */
//    private String cdnUpdateContainer(String name, int ttl, boolean enabled, String referrerAcl, String userAgentACL) 
    public String cdnUpdateContainer(String name, int ttl, boolean enabled)
            throws IOException, HttpException, FilesAuthorizationException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        PostMethod method = new PostMethod(cdnManagementURL + "/" + sanitizeForURI(name));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            // TTL
            if(ttl > 0) {
                method.setRequestHeader(FilesConstants.X_CDN_TTL, Integer.toString(ttl));
            }
            // Enabled
            method.setRequestHeader(FilesConstants.X_CDN_ENABLED, Boolean.toString(enabled));

//    			// Referrer ACL
//    			if(referrerAcl != null) {
//    				method.setRequestHeader(FilesConstants.X_CDN_REFERRER_ACL, referrerAcl);
//    			}
//    			
//    			// User Agent ACL
//    			if(userAgentACL != null) {
//    				method.setRequestHeader(FilesConstants.X_CDN_USER_AGENT_ACL, userAgentACL);
//    			}
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                return response.getCdnUrl();
            }
            else if(response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                logger.warn("Unauthorized access");
                throw new FilesAuthorizationException("User not Authorized!", response.getResponseHeaders(), response.getStatusLine());
            }
            logger.warn("Unexpected return code: " + response.getStatusLine());
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Gets current sharing status of the container
     *
     * @param name The name of the container to enable
     * @return Information on the container or null if not found
     * @throws IOException                 There was an IO error doing network communication
     * @throws HttpException               There was an error with the http protocol
     * @throws FilesAuthorizationException Authentication failed
     */
    public FilesCDNContainer getCDNContainerInfo(String container) throws IOException, HttpException, FilesAuthorizationException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        HeadMethod method = new HeadMethod(cdnManagementURL + "/" + sanitizeForURI(container));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                FilesCDNContainer result = new FilesCDNContainer(response.getCdnUrl());
                for(Header hdr : response.getResponseHeaders()) {
                    String name = hdr.getName().toLowerCase();
                    // logger.warn("Inbound Header: " + name + ":" + hdr.getValue());
                    if("x-cdn-enabled".equals(name)) {
                        result.setEnabled(Boolean.valueOf(hdr.getValue()));
                    }
                    else if("x-ttl".equals(name)) {
                        result.setTtl(Integer.parseInt(hdr.getValue()));
                    }
//       					else if ("x-user-agent-acl".equals(name)) {
//    						result.setUserAgentACL(hdr.getValue());
//    					}
//       					else if ("x-referrer-acl".equals(name)) {
//    						result.setReferrerACL(hdr.getValue());
//    					}
                }
                return result;
            }
            else if(response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                logger.warn("Unauthorized access");
                throw new FilesAuthorizationException("User not Authorized!", response.getResponseHeaders(), response.getStatusLine());
            }
            else {
                logger.warn("Unexpected return code: " + response.getStatusLine());
            }
        }
        finally {
            method.releaseConnection();
        }
        return null;
    }

    /**
     * Gets the names of all of the containers associated with this account.
     *
     * @param name The name of the container to enable
     * @return Information on the container
     * @throws IOException                 There was an IO error doing network communication
     * @throws HttpException               There was an error with the http protocol
     * @throws FilesAuthorizationException Authentication failed
     */
    public List<String> listCdnContainers() throws IOException, HttpException, FilesAuthorizationException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        GetMethod method = new GetMethod(cdnManagementURL);
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                StrTokenizer tokenize = new StrTokenizer(inputStreamToString(response.getResponseBodyAsStream(), method.getResponseCharSet()));
                tokenize.setDelimiterString("\n");
                String[] containers = tokenize.getTokenArray();
                List<String> returnValue = new ArrayList<String>();
                for(String containerName : containers) {
                    returnValue.add(containerName);
                }
                return returnValue;
            }
            else if(response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                logger.warn("Unauthorized access");
                throw new FilesAuthorizationException("User not Authorized!", response.getResponseHeaders(), response.getStatusLine());
            }
            else {
                logger.warn("Unexpected return code: " + response.getStatusLine());
                throw new HttpException(response.getStatusMessage());
            }
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Copies the file to Cloud Files, keeping the original file name in Cloud Files.
     *
     * @param container   The name of the container to place the file in
     * @param obj         The File to transfer
     * @param contentType The file's MIME type
     * @return FilesConstants.OBJECT_CREATED if successful
     * @throws IOException              There was an IO error doing network communication
     * @throws HttpException            There was an error with the http protocol
     * @throws NoSuchAlgorithmException
     */
    public int storeObject(String container, File obj, String contentType) throws IOException, HttpException, NoSuchAlgorithmException {
        return storeObjectAs(container, obj.getName(), obj, contentType);
    }

    /**
     * Store a file on the server
     *
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @return FilesConstants.OBJECT_CREATED if the object was created
     * @throws IOException              There was an IO error doing network communication
     * @throws HttpException            There was an error with the http protocol
     * @throws NoSuchAlgorithmException
     */
    public int storeObjectAs(String container, String name, File obj, String contentType) throws IOException, HttpException, NoSuchAlgorithmException {
        return storeObjectAs(container, name, new FileRequestEntity(obj, contentType), new HashMap<String, String>(), md5sum(obj));
    }

    /**
     * Store a file on the server, including metadata
     *
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return FilesConstants.OBJECT_CREATED if the object was created
     * @throws IOException              There was an IO error doing network communication
     * @throws HttpException            There was an error with the http protocol
     * @throws NoSuchAlgorithmException
     */
    public int storeObjectAs(String container, String name, byte obj[], String contentType, Map<String, String> metadata) throws IOException, HttpException, NoSuchAlgorithmException {
        return this.storeObjectAs(container, name, new ByteArrayRequestEntity(obj, contentType), metadata, md5sum(obj));
    }

    /**
     * @param container   The name of the container
     * @param in          The input stream to write to the server
     * @param md5sum      Checksum
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return FilesConstants.OBJECT_CREATED if the object was created
     * @throws IOException              There was an IO error doing network communication
     * @throws HttpException            There was an error with the http protocol
     * @throws NoSuchAlgorithmException
     */
    public int storeObjectAs(String container, String name, InputStream in, long contentLength, String contentType, String md5sum) throws IOException, HttpException {
        return this.storeObjectAs(container, name, new InputStreamRequestEntity(in, contentLength, contentType), new HashMap<String, String>(), md5sum);
    }

    /**
     * @param container   The name of the container
     * @param in          The input stream to write to the server
     * @param md5sum      Checksum
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return FilesConstants.OBJECT_CREATED if the object was created
     * @throws IOException              There was an IO error doing network communication
     * @throws HttpException            There was an error with the http protocol
     * @throws NoSuchAlgorithmException
     */
    public int storeObjectAs(String container, String name, InputStream in, long contentLength, String contentType, Map<String, String> metadata, String md5sum) throws IOException, HttpException {
        return this.storeObjectAs(container, name, new InputStreamRequestEntity(in, contentLength, contentType), metadata, md5sum);
    }

    /**
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return FilesConstants.OBJECT_CREATED if the object was created
     * @throws IOException              There was an IO error doing network communication
     * @throws HttpException            There was an error with the http protocol
     * @throws NoSuchAlgorithmException
     */
    public int storeObjectAs(String container, String name, RequestEntity entity, Map<String, String> metadata, String md5sum) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        String objName = name;
        if(objName.length() > 128) {
            logger.warn("Object Name supplied was truncated to Max allowed of 128 characters !");
            objName = objName.substring(0, 127);
            logger.warn("Truncated Object Name is: " + objName);
        }

        PutMethod method = new PutMethod(storageURL + "/" + sanitizeForURI(container) + "/" + sanitizeForURI(objName));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            if(useETag) {
                if(StringUtils.isNotBlank(md5sum)) {
                    method.setRequestHeader(FilesConstants.E_TAG, md5sum);
                }
            }
            method.setRequestEntity(entity);
            for(String key : metadata.keySet()) {
                // logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
                method.setRequestHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
            }

            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_CREATED) {
                logger.debug("Object stored : ");
                return response.getStatusCode();
            }
            if(response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
                logger.warn("Missing ETag header and Content-Type !");
            }
            if(response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED) {
                logger.warn("Missing Content-Type !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Delete the given object from it's container.
     *
     * @param container The container name
     * @param objName   The object name
     * @return FilesConstants.OBJECT_DELETED
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public int deleteObject(String container, String objName) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        if(objName.length() > 128) {
            logger.warn("Object Name supplied was truncated to Max allowed of 128 characters !");
            objName = objName.substring(0, 127);
            logger.warn("Truncated Object Name is: " + objName);
        }

        DeleteMethod method = new DeleteMethod(storageURL + "/" + sanitizeForURI(container) + "/" + sanitizeForURI(objName));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                logger.info("Object Deleted : " + objName);
                return response.getStatusCode();
            }
            if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.info("Object " + objName + " was not found  !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Get an object's metadata
     *
     * @param container The name of the container
     * @param objName   The name of the object
     * @return The object's metadata
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public FilesObjectMetaData getObjectMetaData(String container, String objName) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        FilesObjectMetaData metaData;
        if(objName.length() > 128) {
            logger.warn("Object Name supplied was truncated to Max allowed of 128 characters !");
            objName = objName.substring(0, 127);
            logger.warn("Truncated Object Name is: " + objName);
        }

        HeadMethod method = new HeadMethod(storageURL + "/" + sanitizeForURI(container) + "/" + sanitizeForURI(objName));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                logger.debug("Object metadata retreived  : " + objName);
                String mimeType = response.getContentType();
                String lastModified = response.getLastModified();
                String eTag = response.getETag();
                String contentLength = response.getContentLength();

                metaData = new FilesObjectMetaData(mimeType, contentLength, eTag, lastModified);

                Header[] headers = response.getResponseHeaders();
                HashMap<String, String> headerMap = new HashMap<String, String>();

                for(Header h : headers) {
                    if(h.getName().startsWith(FilesConstants.X_OBJECT_META)) {
                        headerMap.put(h.getName().substring(FilesConstants.X_OBJECT_META.length()), unencodeURI(h.getValue()));
                    }
                }
                if(headerMap.size() > 0) {
                    metaData.setMetaData(headerMap);
                }

                return metaData;
            }
            if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.info("Object " + objName + " was not found  !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }


    /**
     * Get the content of the given object
     *
     * @param container The name of the container
     * @param objName   The name of the object
     * @return The content of the object
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public byte[] getObject(String container, String objName) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        if(objName.length() > 128) {
            logger.warn("Object Name supplied was truncated to Max allowed of 128 characters !");
            objName = objName.substring(0, 127);
            logger.warn("Truncated Object Name is: " + objName);
        }

        GetMethod method = new GetMethod(storageURL + "/" + sanitizeForURI(container) + "/" + sanitizeForURI(objName));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                logger.debug("Object data retreived  : " + objName);
                return response.getResponseBody();
            }
            if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.warn("Object " + objName + " was not found  !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Get's the given object's content as a stream
     *
     * @param container The name of the container
     * @param objName   The name of the object
     * @return An input stream that will give the objects content when read from.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public InputStream getObjectAsStream(String container, String objName) throws IOException, HttpException {
        if(!this.isLoggedin()) {
            throw new IOException("You need to first login to the account before you perform this operation.");
        }
        if(objName.length() > 128) {
            logger.warn("Object Name supplied was truncated to Max allowed of 128 characters !");
            objName = objName.substring(0, 127);
            logger.warn("Truncated Object Name is: " + objName);
        }

        GetMethod method = new GetMethod(storageURL + "/" + sanitizeForURI(container) + "/" + sanitizeForURI(objName));
        try {
            method.getParams().setSoTimeout(connectionTimeOut);
            method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

            client.executeMethod(method);

            FilesResponse response = new FilesResponse(method);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                logger.info("Object data retreived  : " + objName);
                return response.getResponseBodyAsStream();
            }
            if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                logger.info("Object " + objName + " was not found  !");
            }
            throw new HttpException(response.getStatusMessage());
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Utility function to write an InputStream to a file
     *
     * @param is
     * @param f
     * @throws IOException
     */
    static void writeInputStreamToFile(InputStream is, File f) throws IOException {
        BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(f));
        byte[] buffer = new byte[1024];
        int read = 0;

        while((read = is.read(buffer)) > 0) {
            bf.write(buffer, 0, read);
        }

        is.close();
        bf.flush();
        bf.close();
    }

    /**
     * Reads an input stream into a stream
     *
     * @param is The input stream
     * @return The contents of the stream stored in a string.
     * @throws IOException
     */
    public static String inputStreamToString(InputStream stream, String encoding) throws IOException {
        char buffer[] = new char[4096];
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(stream, encoding);

        int nRead = 0;
        while((nRead = isr.read(buffer)) >= 0) {
            sb.append(buffer, 0, nRead);
        }
        isr.close();

        return sb.toString();
    }

    /**
     * Calculates the MD5 checksum of a file, returned as a hex encoded string
     *
     * @param f The file
     * @return The MD5 checksum, as a base 16 encoded string
     * @throws IOException
     * @throws NoSuchAlgorithmException The Java installation doesn't have the MD5 digest algorithm installed.
     */
    public static String md5sum(File f) throws IOException, NoSuchAlgorithmException {
        InputStream is = new FileInputStream(f);
        return md5sum(is);
    }

    /**
     * @param is
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String md5sum(InputStream is) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        byte[] buffer = new byte[1024];
        int read = 0;

        while((read = is.read(buffer)) > 0) {
            digest.update(buffer, 0, read);
        }

        is.close();

        byte[] md5sum = digest.digest();
        BigInteger bigInt = new BigInteger(1, md5sum);

        // Front load any zeros cut off by BigInteger
        String md5 = bigInt.toString(16);
        while(md5.length() != 32) {
            md5 = "0" + md5;
        }
        return md5;
    }

    /**
     * Calculates the MD5 checksum of an array of data
     *
     * @param data The data to checksum
     * @return The checksum, represented as a base 16 encoded string.
     * @throws IOException
     * @throws NoSuchAlgorithmException The Java installation doesn't have the MD5 digest algorithm installed.
     */
    public static String md5sum(byte[] data) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        byte[] md5sum = digest.digest(data);
        BigInteger bigInt = new BigInteger(1, md5sum);

        // Front load any zeros cut off by BigInteger
        String md5 = bigInt.toString(16);
        while(md5.length() != 32) {
            md5 = "0" + md5;
        }
        return md5;
    }

    /**
     * Encode any unicode characters that will cause us problems.
     *
     * @param str
     * @return The string encoded for a URI
     */
    public static String sanitizeForURI(String str) {
        URLCodec codec = new URLCodec();
        try {
            return codec.encode(str).replaceAll("\\+", "%20");
        }
        catch(EncoderException ee) {
            logger.warn("Error trying to encode string for URI", ee);
            return str;
        }
    }

    public static String unencodeURI(String str) {
        URLCodec codec = new URLCodec();
        try {
            return codec.decode(str);
        }
        catch(DecoderException ee) {
            logger.warn("Error trying to encode string for URI", ee);
            return str;
        }

    }

    /**
     * @return The connection timeout used for communicating with the server (in milliseconds)
     */
    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    /**
     * The timeout we will use for communicating with the server (in milliseconds)
     *
     * @param connectionTimeOut The new timeout for this connection
     */
    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    /**
     * @return The storage URL on the other end of the ReST api
     */
    public String getStorageURL() {
        return storageURL;
    }

    /**
     * @return Get's our storage token.
     */
    public String getStorageToken() {
        return authToken;
    }

    /**
     * Has this instance of the client authenticated itself?
     *
     * @return True if we logged in, false otherwise.
     */
    public boolean isLoggedin() {
        return isLoggedin;
    }

    /**
     * The username we are logged in with.
     *
     * @return The username
     */
    public String getUserName() {
        return username;
    }

    /**
     * Set's the username for this client. Note, setting this after login has no real impact unless the <code>login()</code>
     * method is called again.
     *
     * @param userName the username
     */
    public void setUserName(String userName) {
        this.username = userName;
    }

    /**
     * The password the client will use for the login.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set's the password for this client. Note, setting this after login has no real impact unless the <code>login()</code>
     * method is called again.
     *
     * @param password The new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * The URL we will use for Authentication
     *
     * @return The URL (represented as a string)
     */
    public String getAuthenticationURL() {
        return authenticationURL;
    }

    /**
     * Changes the URL of the authentication service.  Note, if one is logged in, this doesn't have an effect unless one calls login again.
     *
     * @param authenticationURL The new authentication URL
     */
    public void setAuthenticationURL(String authenticationURL) {
        this.authenticationURL = authenticationURL;
    }

    /**
     * @return the useETag
     */
    public boolean getUseETag() {
        return useETag;
    }

    /**
     * @param useETag the useETag to set
     */
    public void setUseETag(boolean useETag) {
        this.useETag = useETag;
    }

    public void setUserAgent(String userAgent) {
        client.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
    }

    public String getUserAgent() {
        return client.getParams().getParameter(HttpMethodParams.USER_AGENT).toString();
    }
}
