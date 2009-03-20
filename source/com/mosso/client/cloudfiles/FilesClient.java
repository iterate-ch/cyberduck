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
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.mosso.client.cloudfiles.wrapper.RequestEntityWrapper;

/**
 * 
 * A client for Cloud Files.  Here follows a basic example of logging in, creating a container and an 
 * object, retrieving the object, and then deleting both the object and container.  For more examples, 
 * see the code in com.mosso.client.cloudfiles.sample, which contains a series of examples.
 * 
 * <pre>
 * 
 *  //  Create the client object for username "jdoe", password "johnsdogsname". 
 * 	FilesClient myClient = FilesClient("jdoe", "johnsdogsname");
 * 
 *  // Log in (<code>login()</code> will return false if the login was unsuccessful.
 *  assert(myClient.login());
 * 
 *  // Make sure there are no containers in the account
 *  assert(myClient.listContainers.length() == 0);
 *  
 *  // Create the container
 *  assert(myClient.createContainer("myContainer"));
 *  
 *  // Now we should have one
 *  assert(myClient.listContainers.length() == 1);
 *  
 *  // Upload the file "alpaca.jpg"
 *  assert(myClient.storeObject("myContainer", new File("alapca.jpg"), "image/jpeg"));
 *  
 *  // Download "alpaca.jpg"
 *  FilesObject obj = myClient.getObject("myContainer", "alpaca.jpg");
 *  byte data[] = obj.getObject();
 *  
 *  // Clean up after ourselves.
 *  // Note:  Order here is important, you can't delete non-empty containers.
 *  assert(myClient.deleteObject("myContainer", "alpaca.jpg"));
 *  assert(myClient.deleteContainer("myContainer");
 * </pre>
 * 
 * @see com.mosso.client.cloudfiles.sample.FilesCli
 * @see com.mosso.client.cloudfiles.sample.FilesAuth
 * @see com.mosso.client.cloudfiles.sample.FilesCopy
 * @see com.mosso.client.cloudfiles.sample.FilesList
 * @see com.mosso.client.cloudfiles.sample.FilesRemove
 * @see com.mosso.client.cloudfiles.sample.FilesMakeContainer
 * 
 * @author lvaughn
 */
public class FilesClient
{
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
     * @param username  The username to log in to 
     * @param password  The password
     * @param account   The Cloud Files account to use
     * @param connectionTimeOut  The connection timeout, in ms.
     */
    public FilesClient(String username, String password, String account, int connectionTimeOut)
    {
        this.username = username;
        this.password = password;
        this.account = account;
        if (account != null && account.length() > 0) {
        	this.authenticationURL = FilesUtil.getProperty("auth_url")+VERSION+"/"+account+FilesUtil.getProperty("auth_url_post");
        }
        else {
        	this.authenticationURL = FilesUtil.getProperty("auth_url");
        }
        this.connectionTimeOut = connectionTimeOut;

        client.getParams().setParameter("http.socket.timeout", this.connectionTimeOut );
        setUserAgent(FilesConstants.USER_AGENT);

        if (logger.isDebugEnabled()) { 
        	logger.debug("UserName: "+ this.username);
            logger.debug("AuthenticationURL: "+ this.authenticationURL);
            logger.debug("ConnectionTimeOut: "+ this.connectionTimeOut);
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
    public FilesClient(String username, String password, String account)
    {
        this (username, password, account, FilesUtil.getIntProperty("connection_timeout"));
    }

    /**
     * Mosso-style authentication (No accounts).
     * 
     * @param username     Your CloudFiles username
     * @param apiAccessKey Your CloudFiles API Access Key
     */
    public FilesClient(String username, String apiAccessKey)
    {
        this (username, apiAccessKey, null, FilesUtil.getIntProperty("connection_timeout"));
    }

    /**
     * This method uses the default connection time out of CONNECTON_TIMEOUT and username, password, 
     * and account from FilesUtil
     * 
     */
    public FilesClient()
    {
        this (FilesUtil.getProperty("username"), 
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
    public String getAccount()
    {
        return account;
    }

    /**
     * Set the Account value and reassemble the Authentication URL.
     *
     * @param account
     */
    public void setAccount(String account)
    {
        this.account = account;
        if (account != null && account.length() > 0) {
        	this.authenticationURL = FilesUtil.getProperty("auth_url")+VERSION+"/"+account+FilesUtil.getProperty("auth_url_post");
        }
        else {
        	this.authenticationURL = FilesUtil.getProperty("auth_url");
        }
    }

    /**
     * Log in to CloudFiles.  This method performs the authentication and sets up the client's internal state.
     * 
     * @return true if the login was successful, false otherwise.
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public boolean login() throws IOException, HttpException
    {
        GetMethod method = new GetMethod(authenticationURL);
        method.getParams().setSoTimeout(connectionTimeOut);

        method.setRequestHeader(FilesConstants.X_STORAGE_USER, username);
        method.setRequestHeader(FilesConstants.X_STORAGE_PASS, password);

        logger.debug ("Logging in user: "+username+" using URL: "+authenticationURL);
        client.executeMethod(method);

        FilesResponse response = new FilesResponse(method);

        if (response.loginSuccess())
        {
            isLoggedin   = true;
            storageURL   = response.getStorageURL();
            cdnManagementURL = response.getCDNManagementURL();
            authToken = response.getAuthToken();
            logger.debug("storageURL: " + storageURL);
            logger.debug("authToken: " + authToken);
            logger.debug("cdnManagementURL:" + cdnManagementURL);
        }
        method.releaseConnection();

        return this.isLoggedin;
    }
    
    /**
     * List all of the containers available in an account, ordered by container name.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainerInfo> listContainersInfo() throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	return listContainersInfo(-1, null);
    }
    	
    /**
     * List the containers available in an account, ordered by container name.
     * 
     * @param limit The maximum number of containers to return.  -1 returns an unlimited number.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainerInfo> listContainersInfo(int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	return listContainersInfo(limit, null);
    }
    	
    /**
     * List the containers available in an account, ordered by container name.
     *
     *  @param limit The maximum number of containers to return.  -1 returns an unlimited number.
	 *  @param marker Return containers that occur after this lexicographically.  
	 *  
     *  @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainerInfo> listContainersInfo(int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	if (!this.isLoggedin()) {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	GetMethod method = null;
    	try {
    		method = new GetMethod(storageURL);
    		method.getParams().setSoTimeout(connectionTimeOut);
    		method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

    		LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
       		if(limit > 0) {
    			parameters.add(new NameValuePair("limit", String.valueOf(limit)));
    		}
       		if(marker != null) {
    			parameters.add(new NameValuePair("marker", marker));
    		}
       		parameters.add(new NameValuePair("format", "xml"));
       		method.setQueryString(parameters.toArray(new NameValuePair[parameters.size()]));
			client.executeMethod(method);
    		FilesResponse response = new FilesResponse(method);
    		
    		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    			method.removeRequestHeader(FilesConstants.X_AUTH_TOKEN);
    			if(login()) {
    				method = new GetMethod(storageURL);
    	    		method.getParams().setSoTimeout(connectionTimeOut);
    	    		method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	    		method.setQueryString(parameters.toArray(new NameValuePair[parameters.size()]));
    				client.executeMethod(method);
    				response = new FilesResponse(method);
    			}
    			else {
    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		
    		if (response.getStatusCode() == HttpStatus.SC_OK)
    		{
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    			DocumentBuilder builder = factory.newDocumentBuilder();
    			Document document = builder.parse(response.getResponseBodyAsStream());

    			NodeList nodes = document.getChildNodes();
    			Node accountNode = nodes.item(0);
    			if (! "account".equals(accountNode.getNodeName())) {
    				logger.error("Got unexpected type of XML");
    				return null;
    			}
    			ArrayList <FilesContainerInfo> containerList = new ArrayList<FilesContainerInfo>();
    			NodeList containerNodes = accountNode.getChildNodes();
    			for(int i=0; i < containerNodes.getLength(); ++i) {
    				Node containerNode = containerNodes.item(i);
    				if(!"container".equals(containerNode.getNodeName())) continue;
    				String name = null;
    				int count = -1;
    				long size = -1;
    				NodeList objectData = containerNode.getChildNodes(); 
    				for(int j=0; j < objectData.getLength(); ++j) {   					
    					Node data = objectData.item(j);
    					if ("name".equals(data.getNodeName())) {
    						name = data.getTextContent();
    					}
    					else if ("bytes".equals(data.getNodeName())) {
    						size = Long.parseLong(data.getTextContent());
    					}
    					else if ("count".equals(data.getNodeName())) {
    						count = Integer.parseInt(data.getTextContent());
    					}
    					else {
    						logger.warn("Unexpected container-info tag:" + data.getNodeName());
    					}
    				}
    				if (name != null) {
    					FilesContainerInfo obj = new FilesContainerInfo(name, count, size);
    					containerList.add(obj);
    				}
    			}
    			return containerList;
    		}		
    		else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    		{	
    			return new ArrayList<FilesContainerInfo>();
    		}
    		else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    		{
    			throw new FilesNotFoundException("Account not Found", response.getResponseHeaders(), response.getStatusLine());
    		}
    		else {
    			throw new FilesException("Unexpected Return Code", response.getResponseHeaders(), response.getStatusLine());
    		}
    	}
    	catch (Exception ex) {
    		throw new FilesException("Unexpected problem, probably in parsing Server XML", ex);
    	}
    	finally {
    		if (method != null)
    			method.releaseConnection();
    	}
    }

    /**
     * List the containers available in an account.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FilesContainer with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainer> listContainers() throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	return listContainers(-1, null);
    }
   /**
    * List the containers available in an account.
    *
    * @param limit The maximum number of containers to return.  -1 denotes no limit.

    * @return null if the user is not logged in or the Account is not found.  A List of FilesContainer with all of the containers in the account.  
    *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
    */
     public List<FilesContainer> listContainers(int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	return listContainers(limit, null);
    }

    /**
     * List the containers available in an account.
     * 
     * @param limit The maximum number of containers to return.  -1 denotes no limit.
     * @param marker Only return containers after this container.  Null denotes starting at the beginning (lexicographically).  
     *
     * @return A List of FilesContainer with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainer> listContainers(int limit, String marker) throws IOException, HttpException, FilesException
    {
    	if (!this.isLoggedin()) {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	GetMethod method = null;
    	try {
    		method = new GetMethod(storageURL);
    		method.getParams().setSoTimeout(connectionTimeOut);
    		method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);   		
    		LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
    		
       		if(limit > 0) {
    			parameters.add(new NameValuePair("limit", String.valueOf(limit)));
    		}
       		if(marker != null) {
    			parameters.add(new NameValuePair("marker", marker));
    		}
       		
       		if (parameters.size() > 0) {
       			method.setQueryString(parameters.toArray(new NameValuePair[parameters.size()]));
       		}	
 			client.executeMethod(method);
    		FilesResponse response = new FilesResponse(method);
    		
       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    			method.releaseConnection();
    			if(login()) {
    				method = new GetMethod(storageURL);
    	    		method.getParams().setSoTimeout(connectionTimeOut);
    	    		method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	       		if (parameters.size() > 0) {
    	       			method.setQueryString(parameters.toArray(new NameValuePair[parameters.size()]));
    	       		}	
    				client.executeMethod(method);
    				response = new FilesResponse(method);
    			}
    			else {
    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
  		
    		if (response.getStatusCode() == HttpStatus.SC_OK)
    		{
    			StrTokenizer tokenize = new StrTokenizer(inputStreamToString(response.getResponseBodyAsStream(), method.getResponseCharSet()));
    			tokenize.setDelimiterString("\n");
    			String [] containers = tokenize.getTokenArray();    			
    			ArrayList <FilesContainer> containerList = new ArrayList<FilesContainer>();
    			for(String container : containers) { 
    				containerList.add(new FilesContainer(container, this));
    			}
    			return containerList;
    		}		
    		else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    		{	
    			return new ArrayList<FilesContainer>();
    		}
    		else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    		{
    			throw new FilesNotFoundException("Account was not found", response.getResponseHeaders(), response.getStatusLine());
    		}
    		else {
    			throw new FilesException("Unexpected resposne from server", response.getResponseHeaders(), response.getStatusLine());
    		}
    	}
    	catch (Exception ex) {
    		throw new FilesException("Unexpected error, probably parsing Server XML", ex);
    	}
    	finally {
    		if (method != null) method.releaseConnection();
    	}
    }

    /**
     * List all of the objects in a container with the given starting string.
     * 
     * @param container  The container name
     * @param startsWith The string to start with
     * @param path Only look for objects in this path
     * @param limit Return at most <code>limit</code> objects
     * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjectsStaringWith (String container, String startsWith, String path, int limit, String marker) throws IOException, HttpException, FilesException
    {
    	if (!this.isLoggedin()) {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	if (!isValidContianerName(container))  {
    		throw new FilesInvalidNameException(container);
    	}
    	GetMethod method = null;
    	try {
    		method = new GetMethod(storageURL+"/"+sanitizeForURI(container));
    		method.getParams().setSoTimeout(connectionTimeOut);
    		method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    		
    		LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
    		parameters.add(new NameValuePair ("format", "xml"));
    		if (startsWith != null) {
    			parameters.add(new NameValuePair (FilesConstants.LIST_CONTAINER_NAME_QUERY, startsWith));
    		}
       		if(path != null) {
    			parameters.add(new NameValuePair("path", path));
    		}
       		if(limit > 0) {
    			parameters.add(new NameValuePair("limit", String.valueOf(limit)));
    		}
       		if(marker != null) {
    			parameters.add(new NameValuePair("marker", marker));
    		}
       		
       		if (parameters.size() > 0) {
       			method.setQueryString(parameters.toArray(new NameValuePair[parameters.size()]));
       		}
			client.executeMethod(method);
    		FilesResponse response = new FilesResponse(method);
    		
       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    			method.removeRequestHeader(FilesConstants.X_AUTH_TOKEN);
    			if(login()) {
    				method = new GetMethod(storageURL+"/"+sanitizeForURI(container));
    	    		method.getParams().setSoTimeout(connectionTimeOut);
    	    		method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	       		if (parameters.size() > 0) {
    	       			method.setQueryString(parameters.toArray(new NameValuePair[parameters.size()]));
    	       		}	
    				client.executeMethod(method);
    				response = new FilesResponse(method);
    			}
    			else {
    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    			}
    		}

       		if (response.getStatusCode() == HttpStatus.SC_OK)
    		{
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    			DocumentBuilder builder = factory.newDocumentBuilder();
    			Document document = builder.parse(response.getResponseBodyAsStream());

    			NodeList nodes = document.getChildNodes();
    			Node containerList = nodes.item(0);
    			if (! "container".equals(containerList.getNodeName())) {
    				logger.error("Got unexpected type of XML");
    				return null;
    			}
       			ArrayList <FilesObject> objectList = new ArrayList<FilesObject>();
    			NodeList objectNodes = containerList.getChildNodes();
    			for(int i=0; i < objectNodes.getLength(); ++i) {
    				Node objectNode = objectNodes.item(i);
     				if(!"object".equals(objectNode.getNodeName())) continue;
    				String name = null;
    				String eTag = null;
    				long size = -1;
    				String mimeType = null;
    				String lastModified = null;
    				NodeList objectData = objectNode.getChildNodes(); 
    				for(int j=0; j < objectData.getLength(); ++j) {
    					Node data = objectData.item(j);
    					if ("name".equals(data.getNodeName())) {
    						name = data.getTextContent();
    					}
    					else if ("content_type".equals(data.getNodeName())) {
    						mimeType = data.getTextContent();
    					}
    					else if ("hash".equals(data.getNodeName())) {
    						eTag = data.getTextContent();
    					}
       					else if ("bytes".equals(data.getNodeName())) {
    						size = Long.parseLong(data.getTextContent());
    					}
       					else
                        if ("last_modified".equals(data.getNodeName())) {
       					    lastModified = data.getTextContent();
    					}
    						else {
    						logger.warn("Unexpected tag:" + data.getNodeName());
    					}
    				}
    				if (name != null) {
    					FilesObject obj = new FilesObject(name, container, this);
    					if (eTag != null) obj.setMd5sum(eTag);
    					if (mimeType != null) obj.setMimeType(mimeType); 
    					if (size > 0) obj.setSize(size);
    					if (lastModified != null) obj.setLastModified(lastModified);
    					objectList.add(obj);
    				}
    			}
    			return objectList;
    		}		
    		else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    		{	
    			logger.debug ("Container "+container+" has no Objects");
    			return new ArrayList<FilesObject>();
    		}
    		else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    		{
    			throw new FilesNotFoundException("Container was not found", response.getResponseHeaders(), response.getStatusLine());
    		}
    		else {
    			throw new FilesException("Unexpected Server Result", response.getResponseHeaders(), response.getStatusLine());
    		}
    	}
    	catch (Exception ex) {
    		logger.error("Error parsing xml", ex);
    		throw new FilesException("Error parsing server resposne", ex);
    	}
    	finally {
    		if (method != null) method.releaseConnection();
    	}
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjects(String container) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStaringWith(container, null, null, -1, null);
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param limit Return at most <code>limit</code> objects
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjects(String container, int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStaringWith(container, null, null, limit, null);
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param path Only look for objects in this path
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public List<FilesObject> listObjects(String container, String path) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStaringWith(container, null, path, -1, null);
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param path Only look for objects in this path
     * @param limit Return at most <code>limit</code> objects
     * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjects(String container, String path, int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStaringWith(container, null, path, limit, null);
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param path Only look for objects in this path
     * @param limit Return at most <code>limit</code> objects
     * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public List<FilesObject> listObjects(String container, String path, int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStaringWith(container, null, path, limit, marker);
    }
    
    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param limit Return at most <code>limit</code> objects
     * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjects(String container, int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStaringWith(container, null, null, limit, marker);
    }

    /**
     * Convenience method to test for the existence of a container in Cloud Files.
     * 
     * @param container
     * @return true if the container exists.  false otherwise.
     * @throws IOException
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     */
    public boolean containerExists (String container) throws IOException, HttpException
    {
        try {
        	this.getContainerInfo(container);
        	return true;
        }
        catch(FilesException fnfe) {
        	return false;
        }
     }
      
    /**
     * Gets information for the given account.
     * 
     * @return The FilesAccountInfo with information about the number of containers and number of bytes used
     *         by the given account.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
   public FilesAccountInfo getAccountInfo() throws IOException, HttpException, FilesAuthorizationException, FilesException
   {
    	if (this.isLoggedin()) {
			HeadMethod method = new HeadMethod(storageURL);
			method.getParams().setSoTimeout(connectionTimeOut);
			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
			client.executeMethod(method);

			FilesResponse response = new FilesResponse(method);
       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    			method.removeRequestHeader(FilesConstants.X_AUTH_TOKEN);
    			if(login()) {
    				method = new HeadMethod(storageURL);
    				method.getParams().setSoTimeout(connectionTimeOut);
    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				client.executeMethod(method);
    				response = new FilesResponse(method);
    			}
    			else {
    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    			}
    		}

			if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
			{
				int nContainers = response.getAccountContainerCount();
				long totalSize  = response.getAccountBytesUsed();
				return new FilesAccountInfo(totalSize,nContainers);
			}
			else {
				throw new FilesException("Unexpected return from server", response.getResponseHeaders(), response.getStatusLine());
			}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Get basic information on a container (number of items and the total size).
     *
     * @param container The container to get information for
     * @return ContainerInfo object of the container is present or null if its not present
     * @throws IOException  There was a socket level exception while talking to CloudFiles
     * @throws HttpException There was an protocol level exception while talking to Cloudfiles
     * @throws FilesNotFoundException The container was not found
     * @throws FilesAuthorizationException The client was not logged in or the log in expired.
     */
    public FilesContainerInfo getContainerInfo (String container) throws IOException, HttpException, FilesException
    {
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(container))
    		{

    			HeadMethod method = null;
    			try {
    				method = new HeadMethod(storageURL+"/"+sanitizeForURI(container));
    				method.getParams().setSoTimeout(connectionTimeOut);
    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				client.executeMethod(method);

    				FilesResponse response = new FilesResponse(method);

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.removeRequestHeader(FilesConstants.X_AUTH_TOKEN);
    					if(login()) {
    						method = new HeadMethod(storageURL+"/"+sanitizeForURI(container));
    						method.getParams().setSoTimeout(connectionTimeOut);
    						method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						client.executeMethod(method);
    						response = new FilesResponse(method);
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    				{
    					int objCount = response.getContainerObjectCount();
    					long objSize  = response.getContainerBytesUsed();
    					return new FilesContainerInfo(container, objCount,objSize);
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    				{
    					throw new FilesNotFoundException("Container not found: " + container, response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected result from server", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) method.releaseConnection();
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(container);
    		}
    	}
    	else
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    }


    /**
     * Creates a container
     *
     * @param name The name of the container to be created
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesAuthorizationException The client was not property logged in
     * @throws FilesInvalidNameException The container name was invalid
     */
    public void createContainer(String name) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(name))
    		{
    			// logger.warn(name + ":" + sanitizeForURI(name));
    			PutMethod method = new PutMethod(storageURL+"/"+sanitizeForURI(name));
    			method.getParams().setSoTimeout(connectionTimeOut);
    			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			
    			try {
    				client.executeMethod(method);

    				FilesResponse response = new FilesResponse(method);    	
    				
    	       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    	       			method.releaseConnection();
    	    			if(login()) {
    	    	   			method = new PutMethod(storageURL+"/"+sanitizeForURI(name));
    	        			method.getParams().setSoTimeout(connectionTimeOut);
    	        			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	    				client.executeMethod(method);
    	    				response = new FilesResponse(method);
    	    			}
    	    			else {
    	    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    	    			}
    	    		}

	    			if (response.getStatusCode() == HttpStatus.SC_CREATED)
	    			{
	    				return;
	    			}
	    			else if (response.getStatusCode() == HttpStatus.SC_ACCEPTED)
	    			{	
	    				throw new FilesContainerExistsException(name, response.getResponseHeaders(), response.getStatusLine());
	    			}
	    			else {
	    				throw new FilesException("Unexpected Response", response.getResponseHeaders(), response.getStatusLine());
	    			}
    			}
    			finally {
    				method.releaseConnection();
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(name);
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Deletes a container
     * 
     * @param name  The name of the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesAuthorizationException The user is not Logged in
     * @throws FilesInvalidNameException   The container name is invalid
     * @throws FilesNotFoundException      The container doesn't exist
     * @throws FilesContainerNotEmptyException The container was not empty
     */
    public boolean deleteContainer(String name) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException, FilesContainerNotEmptyException
    {
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(name))
    		{
    			DeleteMethod method = new DeleteMethod(storageURL+"/"+sanitizeForURI(name));
    			try {
    				method.getParams().setSoTimeout(connectionTimeOut);
    	   			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
        			client.executeMethod(method);
        			FilesResponse response = new FilesResponse(method);

    	       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    	       			method.releaseConnection();
    	    			if(login()) {
    	    	   			method = new DeleteMethod(storageURL+"/"+sanitizeForURI(name));
    	    				method.getParams().setSoTimeout(connectionTimeOut);
    	    	   			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	        			client.executeMethod(method);
    	    				response = new FilesResponse(method);
    	    			}
    	    			else {
    	    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    	    			}
    	    		}

    	       		if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
        			{
        				logger.debug ("Container Deleted : "+name);
        				return true;
        			}
        			else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
        			{
        				logger.debug ("Container does not exist !");
           				throw new FilesNotFoundException("You can't delete an non-empty container", response.getResponseHeaders(), response.getStatusLine());
        			}
        			else if (response.getStatusCode() == HttpStatus.SC_CONFLICT)
        			{
        				logger.debug ("Container is not empty, can not delete a none empty container !");
        				throw new FilesContainerNotEmptyException("You can't delete an non-empty container", response.getResponseHeaders(), response.getStatusLine());
        			}
    			}
    			finally {
    				method.releaseConnection();
    			}
    		}
    		else
    		{
           		throw new FilesInvalidNameException(name);
    		}
    	}
		else
		{
       		throw new FilesAuthorizationException("You must be logged in", null, null);
		}
    	return false;
    }
    
    /**
     * Enables access of files in this container via the Content Delivery Network.
     * 
     * @param name The name of the container to enable
     * @return The CDN Url of the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was an error talking to the CDN Server.
     */
    public String cdnEnableContainer(String name) throws IOException, HttpException, FilesException
    {
    	String returnValue = null;
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(name))
    		{
    			PutMethod method = null;
    			try {
    				method = new PutMethod(cdnManagementURL+"/"+sanitizeForURI(name));
    				method.getParams().setSoTimeout(connectionTimeOut);
    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				client.executeMethod(method);

    				FilesResponse response = new FilesResponse(method);

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.releaseConnection();
    					if(login()) {
    						method = new PutMethod(cdnManagementURL+"/"+sanitizeForURI(name));
    						method.getParams().setSoTimeout(connectionTimeOut);
    						method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						client.executeMethod(method);
    						response = new FilesResponse(method);
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_ACCEPTED)
    				{
    					returnValue = response.getCdnUrl();
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					logger.warn("Unauthorized access");
    					throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected Server Response",response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally	{
    				method.releaseConnection();
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(name);
    		}
    	}
    	else
		{
       		throw new FilesAuthorizationException("You must be logged in", null, null);
		}
    	return returnValue;
    }
    
    /**
     * Enables access of files in this container via the Content Delivery Network.
     * 
     * @param name The name of the container to enable
     * @param ttl How long the CDN can use the content before checking for an update.  A negative value will result in this not being changed.
     * @param enabled True if this folder should be accessible, false otherwise
      * @return The CDN Url of the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was an error talking to the CDN Service
     */
    /*
     * @param referrerAcl Unused for now
     * @param userAgentACL Unused for now
     */
//    private String cdnUpdateContainer(String name, int ttl, boolean enabled, String referrerAcl, String userAgentACL) 
    public String cdnUpdateContainer(String name, int ttl, boolean enabled) 
    throws IOException, HttpException, FilesException
    {
    	String returnValue = null;
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(name))
    		{
    			PostMethod method = null;
    			try {
    				method = new PostMethod(cdnManagementURL+"/"+sanitizeForURI(name));

    				method.getParams().setSoTimeout(connectionTimeOut);
    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				// TTL
    				if (ttl > 0) {
    					method.setRequestHeader(FilesConstants.X_CDN_TTL, Integer.toString(ttl));
    				}
    				// Enabled
    				method.setRequestHeader(FilesConstants.X_CDN_ENABLED, Boolean.toString(enabled));

//  				// Referrer ACL
//  				if(referrerAcl != null) {
//  				method.setRequestHeader(FilesConstants.X_CDN_REFERRER_ACL, referrerAcl);
//  				}

//  				// User Agent ACL
//  				if(userAgentACL != null) {
//  				method.setRequestHeader(FilesConstants.X_CDN_USER_AGENT_ACL, userAgentACL);
//  				}
    				client.executeMethod(method);

    				FilesResponse response = new FilesResponse(method);

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.releaseConnection();
    					if(login()) {
    						new PostMethod(cdnManagementURL+"/"+sanitizeForURI(name));
    						method.getParams().setSoTimeout(connectionTimeOut);
    						method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						// TTL
    						if (ttl > 0) {
    							method.setRequestHeader(FilesConstants.X_CDN_TTL, Integer.toString(ttl));
    						}
    						// Enabled
    						method.setRequestHeader(FilesConstants.X_CDN_ENABLED, Boolean.toString(enabled));

    						client.executeMethod(method);						
    						response = new FilesResponse(method);
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_ACCEPTED)
    				{
    					returnValue = response.getCdnUrl();
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					logger.warn("Unauthorized access");
    					throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected Server Response",response.getResponseHeaders(), response.getStatusLine());
    				}
    			} finally {
    				if (method != null) {
    					method.releaseConnection();
    				}
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(name);
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	return returnValue;
    }
    
   /* *
    * Enables access of files in this container via the Content Delivery Network.
    * 
    * @param name The name of the container to enable
    * @param ttl How long the CDN can use the content before checking for an update.  A negative value will result in this not being changed.
    * @param enabled True if this folder should be accesible, false otherwise
    * @return The CDN Url of the container
    * @throws IOException   There was an IO error doing network communication
    * @throws HttpException There was an error with the http protocol
    * @throws FilesAuthorizationException Authentication failed
    */
//    public String cdnUpdateContainer(String name, int ttl, boolean enabled) throws IOException, HttpException, FilesAuthorizationException
//    {
//    	return cdnUpdateContainer(name, ttl, enabled, (String) null, (String) null);
//    }
    
    /**
     * Gets current CDN sharing status of the container
     * 
     * @param name The name of the container to enable
     * @return Information on the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was an error talking to the CloudFiles Server
     */
    public FilesCDNContainer getCDNContainerInfo(String container) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin()) {
    		if (isValidContianerName(container))
    		{
    			HeadMethod method = null;
    			try {
    				method= new HeadMethod(cdnManagementURL+"/"+sanitizeForURI(container));
    				method.getParams().setSoTimeout(connectionTimeOut);
    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				client.executeMethod(method);

    				FilesResponse response = new FilesResponse(method);

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.releaseConnection();
    					if(login()) {
    						method= new HeadMethod(cdnManagementURL+"/"+sanitizeForURI(container));
    						method.getParams().setSoTimeout(connectionTimeOut);
    						method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						client.executeMethod(method);
    						response = new FilesResponse(method);
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    				{
    					FilesCDNContainer result = new FilesCDNContainer(response.getCdnUrl());
    					result.setName(container);
    					for (Header hdr : response.getResponseHeaders()) { 
    						String name = hdr.getName().toLowerCase();
    						if ("x-cdn-enabled".equals(name)) {
    							result.setEnabled(Boolean.valueOf(hdr.getValue()));
    						}
    						else if ("x-ttl".equals(name)) {
    							result.setTtl(Integer.parseInt(hdr.getValue()));
    						}
    					}
    					return result;
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					logger.warn("Unauthorized access");
    					throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected result from server: ", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) {
    					method.releaseConnection();
    				}
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(container);
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }
    
    /* *
     * Not currently used (but soon will be)
     * @param container
     */    
 //   public void purgeCDNContainer(String container) { 
///    	// Stub
//    }
    
    /* *
     * Not currently used (but soon will be)
     * @param container
     */
//   public void purgeCDNObject(String container, String object) { 
 //   	// Stub
//    }
    
    /**
     * Creates a path (but not any of the sub portions of the path)
     * 
     * @param container The name of the container.
     * @param path  The name of the Path
     * @throws HttpException There was an error at the protocol layer while talking to CloudFiles
     * @throws IOException There was an error at the socket layer while talking to CloudFiles
     * @throws FilesException There was another error while taking to the CloudFiles server
     */
    public void createPath(String container, String path) throws HttpException, IOException, FilesException {

		if (!isValidContianerName(container))
			throw new FilesInvalidNameException(container);
		if (!isValidObjectName(path))
			throw new FilesInvalidNameException(path);
		storeObject(container, new byte[0], "application/directory", path,
				new HashMap<String, String>());
	}

    /**
     * Create all of the path elements for the entire tree for a given path.  Thus, <code>createFullPath("myContainer", "foo/bar/baz")</code> 
     * creates the paths "foo", "foo/bar" and "foo/bar/baz".
     * 
     * @param container The name of the container
     * @param path The full name of the path
     * @throws HttpException There was an error at the protocol layer while talking to CloudFiles
     * @throws IOException There was an error at the socket layer while talking to CloudFiles
     * @throws FilesException There was another error while taking to the CloudFiles server
     */
    public void createFullPath(String container, String path) throws HttpException, IOException, FilesException {
    	String parts[] = path.split("/");
    	
    	for(int i=0; i < parts.length; ++i) {
    		StringBuilder sb = new StringBuilder();
    		for (int j=0; j <= i; ++j) {
    			if (sb.length() != 0) 
    				sb.append("/");
    			sb.append(parts[j]);
    		}
    		createPath(container, sb.toString());
    	}
    	
    }

    /**
     * Gets the names of all of the containers associated with this account.
     * 
     * @param limit The maximum number of container names to return
     * @return A list of container names
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public List<String> listCdnContainers(int limit) throws IOException, HttpException, FilesException
    {
    	return listCdnContainers(limit, null);
    }

    /**
     * Gets the names of all of the containers associated with this account.
     * 
     * @return A list of container names
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
   public List<String> listCdnContainers() throws IOException, HttpException, FilesException
    {
    	return listCdnContainers(-1, null);
    }

    
    /**
     * Gets the names of all of the containers associated with this account.
     * 
     * @param limit The maximum number of container names to return
     * @param marker All of the results will come after <code>marker</code> lexicographically.
     * @return A list of container names
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public List<String> listCdnContainers(int limit, String marker) throws IOException, HttpException, FilesException
    {
    	if (this.isLoggedin())
    	{
    		GetMethod method = null;
    		try {
    			method = new GetMethod(cdnManagementURL);
    			method.getParams().setSoTimeout(connectionTimeOut);
    			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
    			if (limit > 0) {
    				params.add(new NameValuePair("limit", String.valueOf(limit)));
    			}
    			if (marker != null) {
    				params.add(new NameValuePair("marker", marker));
    			}
    			if (params.size() > 0) { 
    				method.setQueryString(params.toArray(new NameValuePair[params.size()]));
    			}
    			client.executeMethod(method);
    			FilesResponse response = new FilesResponse(method);

    			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				method.releaseConnection();
    				if(login()) {
    					method = new GetMethod(cdnManagementURL);
    					method.getParams().setSoTimeout(connectionTimeOut);
    					method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	    			if (params.size() > 0) { 
    	    				method.setQueryString(params.toArray(new NameValuePair[params.size()]));
    	    			}
    					client.executeMethod(method);
    					response = new FilesResponse(method);
    				}
    				else {
    					throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}

    			if (response.getStatusCode() == HttpStatus.SC_OK)
    			{
    				StrTokenizer tokenize = new StrTokenizer(inputStreamToString(response.getResponseBodyAsStream(), method.getResponseCharSet()));
    				tokenize.setDelimiterString("\n");
    				String [] containers = tokenize.getTokenArray();
    				List<String> returnValue = new ArrayList<String>();
    				for (String containerName: containers)
    				{
    					returnValue.add(containerName);
    				}
    				return returnValue;
    			}
    			else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				logger.warn("Unauthorized access");
    				throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    			}
    			else {
    				throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		finally {
    			if (method != null) method.releaseConnection();
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Gets list of all of the containers associated with this account.
     * 
      * @return A list of containers
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public List<FilesCDNContainer> listCdnContainerInfo() throws IOException, HttpException, FilesException
    {
    	return listCdnContainerInfo(-1, null);
    }
    /**
     * Gets list of all of the containers associated with this account.
     * 
     * @param limit The maximum number of container names to return
     * @return A list of containers
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
   public List<FilesCDNContainer> listCdnContainerInfo(int limit) throws IOException, HttpException, FilesException
    {
    	return listCdnContainerInfo(limit, null);
    }
    /**
     * Gets list of all of the containers associated with this account.
     * 
     * @param limit The maximum number of container names to return
     * @param marker All of the names will come after <code>marker</code> lexicographically.
     * @return A list of containers
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public List<FilesCDNContainer> listCdnContainerInfo(int limit, String marker) throws IOException, HttpException, FilesException
    {
    	if (this.isLoggedin())
    	{
    		GetMethod method = null;
    		try {
    			method = new GetMethod(cdnManagementURL);
    			method.getParams().setSoTimeout(connectionTimeOut);
    			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
    			params.add(new NameValuePair("format", "xml"));
    			if (limit > 0) {
    				params.add(new NameValuePair("limit", String.valueOf(limit)));
    			}
    			if (marker != null) {
    				params.add(new NameValuePair("marker", marker));
    			}
    			method.setQueryString(params.toArray(new NameValuePair[params.size()]));
    		
    			client.executeMethod(method);
    			FilesResponse response = new FilesResponse(method);

    			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				method.releaseConnection();
    				if(login()) {
    					method = new GetMethod(cdnManagementURL);
    					method.getParams().setSoTimeout(connectionTimeOut);
    					method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    					method.setQueryString(params.toArray(new NameValuePair[params.size()]));
    	    			
    					client.executeMethod(method);
    					response = new FilesResponse(method);
    				}
    				else {
    					throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}

    			if (response.getStatusCode() == HttpStatus.SC_OK)
    			{
     				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     				DocumentBuilder builder = factory.newDocumentBuilder();
     				Document document = builder.parse(response.getResponseBodyAsStream());

     	    		NodeList nodes = document.getChildNodes();
     	    		Node accountNode = nodes.item(0);
     	    		if (! "account".equals(accountNode.getNodeName())) {
     	    			logger.error("Got unexpected type of XML");
     	    			return null;
     	    		}
     	    		ArrayList <FilesCDNContainer> containerList = new ArrayList<FilesCDNContainer>();
     	    		NodeList containerNodes = accountNode.getChildNodes();
     	    		for(int i=0; i < containerNodes.getLength(); ++i) {
     	    			Node containerNode = containerNodes.item(i);
     	    			if(!"container".equals(containerNode.getNodeName())) continue;
     	    			FilesCDNContainer container = new FilesCDNContainer();
     	    			NodeList objectData = containerNode.getChildNodes(); 
     	    			for(int j=0; j < objectData.getLength(); ++j) {   					
     	    				Node data = objectData.item(j);
     	    				if ("name".equals(data.getNodeName())) {
     	    					container.setName(data.getTextContent());
     	    				}
     	    				else if ("cdn_url".equals(data.getNodeName())) {
     	    					container.setCdnURL(data.getTextContent());
     	    				}
     	    				else if ("cdn_enabled".equals(data.getNodeName())) {
     	    					container.setEnabled(Boolean.parseBoolean(data.getTextContent()));
     	    				}
     	    				else if ("ttl".equals(data.getNodeName())) {
     	    					container.setTtl(Integer.parseInt(data.getTextContent()));
     	    				}
     	    				else {
     	    					//logger.warn("Unexpected container-info tag:" + data.getNodeName());
     	    				}
     	    			}	
     	    			if (container.getName() != null) {
     	    				containerList.add(container);
     	    			}
     	    		}
     	    		return containerList;
    			}	
    			else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				logger.warn("Unauthorized access");
    				throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    			}
    			else {
    				throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		catch (SAXException ex) {
    			// probably a problem parsing the XML
    			throw new FilesException("Problem parsing XML", ex);
    		}
    		catch (ParserConfigurationException ex) {
    			// probably a problem parsing the XML
    			throw new FilesException("Problem parsing XML", ex);
    		}
    		finally {
    			if (method != null) method.releaseConnection();
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Store a file on the server
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public boolean storeObjectAs (String container, File obj, String contentType, String name) throws IOException, HttpException, FilesException
    {
    	return storeObjectAs(container, obj, contentType, name, new HashMap<String,String>(), null);
    }	
    
    /**
     * Store a file on the server
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public boolean storeObjectAs (String container, File obj, String contentType, String name, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
    	return storeObjectAs(container, obj, contentType, name, new HashMap<String,String>(), callback);
    }	
    
    /**
     * Store a file on the server, including metadata
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesAuthorizationException 
      */
    public boolean storeObjectAs (String container, File obj, String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
    	return storeObjectAs (container, obj, contentType, name, metadata, null);
    }
    
    /**
     * Store a file on the server, including metadata
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @param metadata    The callback object that will be called as the data is sent
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public boolean storeObjectAs (String container, File obj, String contentType, String name, Map<String,String> metadata, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(container) && isValidObjectName(name) )
    		{
    			if (!obj.exists())
    			{
    				throw new FileNotFoundException(name + " does not exist");
    			}

    			if (obj.isDirectory())
    			{
    				throw new IOException("The alleged file was a directory");				
    			}

    			PutMethod method = null;
    			try {
    				method = new PutMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(name));
    				method.getParams().setSoTimeout(connectionTimeOut);
    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				if (useETag) {
    					method.setRequestHeader(FilesConstants.E_TAG, md5Sum (obj));
    				}
    				method.setRequestEntity( new RequestEntityWrapper(new FileRequestEntity (obj, contentType), callback));
    				for(String key : metadata.keySet()) {
    					method.setRequestHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    				}
    				client.executeMethod(method);
    				FilesResponse response = new FilesResponse(method);
    				
    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    	       			method.releaseConnection();
    	    			if(login()) {
    	    				method = new PutMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(name));
    	    				method.getParams().setSoTimeout(connectionTimeOut);
    	    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	    				if (useETag) {
    	    					method.setRequestHeader(FilesConstants.E_TAG, md5Sum (obj));
    	    				}
    	    				method.setRequestEntity( new RequestEntityWrapper(new FileRequestEntity (obj, contentType), callback));
    	    				for(String key : metadata.keySet()) {
    	    					method.setRequestHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    	    				}
    	    				client.executeMethod(method);
    	    				response = new FilesResponse(method);
    	    			}
    	    			else {
    	    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    	    			}
    				}
    				if (response.getStatusCode() == HttpStatus.SC_CREATED)
    				{
    					return true;
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED)
    				{
    					throw new FilesException("Etag missmatch", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED)
    				{
    					throw new FilesException("Length miss-match", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else 
    				{
    					throw new FilesException("Unexpected Server Response", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) method.releaseConnection();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(name)) {
    				throw new FilesInvalidNameException(name);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }


    /**
     * Copies the file to Cloud Files, keeping the original file name in Cloud Files.
     * 
     * @param container    The name of the container to place the file in
     * @param obj          The File to transfer
     * @param contentType  The file's MIME type
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public boolean storeObject (String container, File obj, String contentType) throws IOException, HttpException, FilesException
    {
    	return storeObjectAs(container, obj, contentType, obj.getName());
    }

    /**
     * Store a file on the server, including metadata
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public boolean storeObject(String container, byte obj[], String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
    	return storeObject(container, obj, contentType, name, metadata, null);
    }
    
    /**
     * Store a file on the server, including metadata
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public boolean storeObject(String container, byte obj[], String contentType, String name, Map<String,String> metadata, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
    	if (this.isLoggedin())
    	{
    		String objName	 =  name;
    		if (isValidContianerName(container) && isValidObjectName(objName))
    		{

    			PutMethod method = null;
    			try {
    				method = new PutMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    				method.getParams().setSoTimeout(connectionTimeOut);
    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				if (useETag) {
    					method.setRequestHeader(FilesConstants.E_TAG, md5Sum (obj));
    				}
    				method.setRequestEntity(new RequestEntityWrapper(new ByteArrayRequestEntity (obj, contentType), callback));
    				for(String key : metadata.keySet()) {
    					// logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
    					method.setRequestHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    				}
    				client.executeMethod(method);
    				FilesResponse response = new FilesResponse(method);

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.releaseConnection();
    					if(login()) {
    						method = new PutMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    						method.getParams().setSoTimeout(connectionTimeOut);
    						method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						if (useETag) {
    							method.setRequestHeader(FilesConstants.E_TAG, md5Sum (obj));
    						}
    						method.setRequestEntity(new RequestEntityWrapper(new ByteArrayRequestEntity (obj, contentType), callback));
    						for(String key : metadata.keySet()) {
    							method.setRequestHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    						}
    						client.executeMethod(method);
    						response = new FilesResponse(method);
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_CREATED)
    				{
    					return true;
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED)
    				{
    					throw new FilesException("Etag missmatch", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED)
    				{
    					throw new FilesException("Length miss-match", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else 
    				{
    					throw new FilesException("Unexpected Server Response", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally{
    				if (method != null) method.releaseConnection();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {       		
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }
    
    /**
     * Store a file on the server, including metadata, with the contents coming from an input stream.  This allows you to 
     * not know the entire length of your content when you start to write it.  Nor do you have to hold it entirely in memory
     * at the same time.
     * 
     * @param container   The name of the container
     * @param data        Any object that implements InputStream
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public boolean storeStreamedObject(String container, InputStream data, String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
    	if (this.isLoggedin())
    	{
			String objName	 =  name;
			if (isValidContianerName(container) && isValidObjectName(objName))
    		{
    			PutMethod method = new PutMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.setContentChunked(true);
    			method.getParams().setSoTimeout(connectionTimeOut);
    			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			method.setRequestEntity(new InputStreamRequestEntity(data, contentType));
    			for(String key : metadata.keySet()) {
    				// logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
    				method.setRequestHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    			}
    			method.removeRequestHeader("Content-Length");
    			
    			try {
    				client.executeMethod(method);
        			FilesResponse response = new FilesResponse(method);
        			
        			if (response.getStatusCode() == HttpStatus.SC_CREATED)
        			{
        				logger.debug ("Object stored : " + name);
        				return true;
        			}
        			else {
        				logger.error(response.getStatusLine());
        				throw new FilesException("Unexpected result", response.getResponseHeaders(), response.getStatusLine());
        			}
    			}
    			finally {	
    				method.releaseConnection();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {       		
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

   /**
    * 
    * 
    * @param container The name of the container
    * @param name The name of the object
    * @param entity The name of the request entity (make sure to set the Content-Type
    * @param metadata The metadata for the object
    * @param md5sum The 32 character hex encoded MD5 sum of the data
    * @return True of the save was successful
    * @throws IOException There was a socket level exception talking to CloudFiles
    * @throws HttpException There was a protocol level error talking to CloudFiles
    * @throws FilesException There was an error talking to CloudFiles.
    */
public boolean storeObjectAs(String container, String name, RequestEntity entity, Map<String,String> metadata, String md5sum) throws IOException, HttpException, FilesException
    {
    	if (this.isLoggedin())
    	{
			String objName	 =  name;
			if (isValidContianerName(container) && isValidObjectName(objName))
    		{
    			PutMethod method = new PutMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.getParams().setSoTimeout(connectionTimeOut);
    			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			method.setRequestEntity(entity);
   				if (useETag && md5sum != null) {
					method.setRequestHeader(FilesConstants.E_TAG, md5sum);
   				}
    			method.setRequestHeader("Content-Type", entity.getContentType());
    
    			for(String key : metadata.keySet()) {
    				method.setRequestHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    			}
    			
    			try {
    				client.executeMethod(method);
        			FilesResponse response = new FilesResponse(method);
        			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
        				login();
        				method = new PutMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
            			method.getParams().setSoTimeout(connectionTimeOut);
            			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            			method.setRequestEntity(entity);
            			method.setRequestHeader("Content-Type", entity.getContentType());
            			for(String key : metadata.keySet()) {
            				method.setRequestHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
            			}
            			client.executeMethod(method);
            			response = new FilesResponse(method);
        			}
        			
        			if (response.getStatusCode() == HttpStatus.SC_CREATED)
        			{
        				logger.debug ("Object stored : " + name);
        				return true;
        			}
        			else {
        				logger.debug(response.getStatusLine());
        				throw new FilesException("Unexpected result", response.getResponseHeaders(), response.getStatusLine());
        			}
    			}
    			finally {	
    				method.releaseConnection();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {       		
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Delete the given object from it's container.
     * 
     * @param container  The container name
     * @param objName    The object name
     * @return FilesConstants.OBJECT_DELETED
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException 
     */
    public void deleteObject (String container, String objName) throws IOException, HttpException, FilesException
    {
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(container) && isValidObjectName(objName))
    		{
    			DeleteMethod method = null;
    			try {
    				method = new DeleteMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    				method.getParams().setSoTimeout(connectionTimeOut);
    				method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				client.executeMethod(method);
    				FilesResponse response = new FilesResponse(method);

    				if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    				{
    					logger.debug ("Object Deleted : "+objName);
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    				{
    					throw new FilesNotFoundException("Object was not found " + objName, response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected status from server", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) method.releaseConnection();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
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
     * @throws FilesAuthorizationException The Client's Login was invalid.  
     * @throws FilesInvalidNameException The container or object name was not valid
     */
    public FilesObjectMetaData getObjectMetaData (String container, String objName) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException
    {
    	FilesObjectMetaData metaData;
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(container) && isValidObjectName(objName))
    		{
    			HeadMethod method = new HeadMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.getParams().setSoTimeout(connectionTimeOut);
    			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

    			client.executeMethod(method);

    			FilesResponse response = new FilesResponse(method);

    			if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    			{
    				logger.debug ("Object metadata retreived  : "+objName);
    				String mimeType = response.getContentType();
    				String lastModified = response.getLastModified();
    				String eTag = response.getETag();
    				String contentLength = response.getContentLength();

    				metaData = new FilesObjectMetaData(mimeType, contentLength, eTag, lastModified);

    				Header [] headers = response.getResponseHeaders();
    				HashMap<String,String> headerMap = new HashMap<String,String>();

    				for (Header h: headers)
    				{
    					if ( h.getName().startsWith(FilesConstants.X_OBJECT_META) )
    					{
    						headerMap.put(h.getName().substring(FilesConstants.X_OBJECT_META.length()), unencodeURI(h.getValue()));
    					}
    				}
    				if (headerMap.size() > 0)
    					metaData.setMetaData(headerMap);

    				return metaData;
    			}
    			else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    			{
    				logger.info ("Object " + objName + " was not found  !");
    				return null;
    			}

    			method.releaseConnection();
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	return null;
    }


    /**
     * Get the content of the given object
     * 
     * @param container  The name of the container
     * @param objName    The name of the object
     * @return The content of the object
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesAuthorizationException 
     * @throws FilesInvalidNameException 
     * @throws FilesNotFoundException 
     */
    public byte[] getObject (String container, String objName) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException
    {
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(container) && isValidObjectName(objName))
    		{
    			GetMethod method = new GetMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.getParams().setSoTimeout(connectionTimeOut);
    			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

    			client.executeMethod(method);

    			FilesResponse response = new FilesResponse(method);

    			if (response.getStatusCode() == HttpStatus.SC_OK)
    			{
    				logger.debug ("Object data retreived  : "+objName);
    				return response.getResponseBody();
    			}
    			else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    			{
    				throw new FilesNotFoundException("Container: " + container + " did not have object " + objName, response.getResponseHeaders(), response.getStatusLine());
    			}

    			method.releaseConnection();
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	return null;
    }

    /**
     * Get's the given object's content as a stream
     * 
     * @param container  The name of the container
     * @param objName    The name of the object
     * @return An input stream that will give the objects content when read from.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesAuthorizationException 
     * @throws FilesInvalidNameException 
     */
    public InputStream getObjectAsStream (String container, String objName) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException
    {
    	if (this.isLoggedin())
    	{
    		if (isValidContianerName(container) && isValidObjectName(objName))
    		{
    			if (objName.length() > FilesConstants.OBJECT_NAME_LENGTH)
    			{
    				logger.warn ("Object Name supplied was truncated to Max allowed of " + FilesConstants.OBJECT_NAME_LENGTH + " characters !");
    				objName = objName.substring(0, FilesConstants.OBJECT_NAME_LENGTH);
    				logger.warn ("Truncated Object Name is: " + objName);
    			}

    			GetMethod method = new GetMethod(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.getParams().setSoTimeout(connectionTimeOut);
    			method.setRequestHeader(FilesConstants.X_AUTH_TOKEN, authToken);

    			client.executeMethod(method);

    			FilesResponse response = new FilesResponse(method);

    			if (response.getStatusCode() == HttpStatus.SC_OK)
    			{
    				logger.info ("Object data retreived  : "+objName);
    				return response.getResponseBodyAsStream();
    			}
    			else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    			{
    				logger.info ("Object " + objName + " was not found  !");
    				return null;
    			}

    			method.releaseConnection();
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	return null;
    }

    /**
     * Utility function to write an InputStream to a file
     * 
     * @param is
     * @param f
     * @throws IOException
     */
    static void writeInputStreamToFile (InputStream is, File f) throws IOException
    {
    	BufferedOutputStream bf = new BufferedOutputStream (new FileOutputStream (f));
    	byte[] buffer = new byte [1024];
    	int read = 0;

    	while ((read = is.read(buffer)) > 0)
    	{
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
    static String inputStreamToString(InputStream stream, String encoding) throws IOException {
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
     */
    public static String md5Sum (File f) throws IOException
    {
    	MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
	    	InputStream is = new FileInputStream(f);
	    	byte[] buffer = new byte[1024];
	    	int read = 0;

	    	while( (read = is.read(buffer)) > 0)
	    	{
	    		digest.update(buffer, 0, read);
	    	}

	    	is.close ();

	    	byte[] md5sum = digest.digest();
	    	BigInteger bigInt = new BigInteger(1, md5sum);

	       	// Front load any zeros cut off by BigInteger
	    	String md5 = bigInt.toString(16);
	    	while (md5.length() != 32) {
	    		md5 = "0" + md5;
	    	}
	    	return md5;
		} catch (NoSuchAlgorithmException e) {
			logger.fatal("The JRE is misconfigured on this computer", e);
			return null;
		}
    }
    
    /**
     * Calculates the MD5 checksum of an array of data
     * 
     * @param data The data to checksum
     * @return The checksum, represented as a base 16 encoded string.
     * @throws IOException
      */
    public static String md5Sum (byte[] data) throws IOException
    {
    	try {
    		MessageDigest digest = MessageDigest.getInstance("MD5");
        	byte[] md5sum = digest.digest(data);
        	BigInteger bigInt = new BigInteger(1, md5sum);

        	// Front load any zeros cut off by BigInteger
        	String md5 = bigInt.toString(16);
        	while (md5.length() != 32) {
        		md5 = "0" + md5;
        	}
        	return md5;
    	}
    	catch (NoSuchAlgorithmException nsae) {
    		logger.fatal("Major problems with your Java configuration", nsae);
    		return null;
    	}

    }
    
    /**
     * Encode any unicode characters that will cause us problems.
     * 
     * @param str
     * @return The string encoded for a URI
     */
    public static String sanitizeForURI(String str) {
    	URLCodec codec= new URLCodec();
    	try {
    		return codec.encode(str).replaceAll("\\+", "%20").replaceAll("%2F", "/");
    	}
    	catch (EncoderException ee) {
    		logger.warn("Error trying to encode string for URI", ee);
    		return str;
    	}
    }
    
    public static String unencodeURI(String str) {
       	URLCodec codec= new URLCodec();
    	try {
    		return codec.decode(str);
    	}
    	catch (DecoderException ee) {
    		logger.warn("Error trying to encode string for URI", ee);
    		return str;
    	}
   	
    }

    /**
     * @return The connection timeout used for communicating with the server (in milliseconds)
     */
    public int getConnectionTimeOut()
    {
    	return connectionTimeOut;
    }

    /**
     * The timeout we will use for communicating with the server (in milliseconds)
     * 
     * @param connectionTimeOut The new timeout for this connection
     */
    public void setConnectionTimeOut(int connectionTimeOut)
    {
    	this.connectionTimeOut = connectionTimeOut;
    }

    /**
     * @return The storage URL on the other end of the ReST api
     */
    public String getStorageURL()
    {
    	return storageURL;
    }

    /**
     * @return Get's our storage token.
     */
    public String getStorageToken()
    {
    	return authToken;
    }

    /**
     * Has this instance of the client authenticated itself?
     * 
     * @return True if we logged in, false otherwise.
     */
    public boolean isLoggedin()
    {
    	return isLoggedin;
    }

    /**
     * The username we are logged in with.
     * 
     * @return The username
     */
    public String getUserName()
    {
    	return username;
    }

    /**
     * Set's the username for this client. Note, setting this after login has no real impact unless the <code>login()</code>
     * method is called again.
     * 
     * @param userName the username
     */
    public void setUserName(String userName)
    {
    	this.username = userName;
    }

    /**
     * The password the client will use for the login.
     * 
     * @return The password
     */
    public String getPassword()
    {
    	return password;
    }

    /**
     * Set's the password for this client. Note, setting this after login has no real impact unless the <code>login()</code>
     * method is called again.
	 *
     * @param password The new password
     */
    public void setPassword(String password)
    {
    	this.password = password;
    }

    /**
     * The URL we will use for Authentication
     * 
     * @return The URL (represented as a string)
     */
    public String getAuthenticationURL()
    {
    	return authenticationURL;
    }

    /**
     * Changes the URL of the authentication service.  Note, if one is logged in, this doesn't have an effect unless one calls login again.
     * 
     * @param authenticationURL The new authentication URL
     */
    public void setAuthenticationURL(String authenticationURL)
    {
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
	
	private boolean isValidContianerName(String name) {
		if (name == null) return false;
		int length = name.length();
		if (length == 0 || length > FilesConstants.CONTAINER_NAME_LENGTH) return false;
		if (name.indexOf('/') != -1) return false;
		//if (name.indexOf('?') != -1) return false;
		return true;
	}
	private boolean isValidObjectName(String name) {
		if (name == null) return false;
		int length = name.length();
		if (length == 0 || length > FilesConstants.OBJECT_NAME_LENGTH) return false;
		//if (name.indexOf('?') != -1) return false;
		return true;
	}

	/**
	 * @return the cdnManagementURL
	 */
	public String getCdnManagementURL() {
		return cdnManagementURL;
	}
}
